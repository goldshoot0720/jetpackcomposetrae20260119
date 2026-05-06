package com.example.jetpackcomposetrae20260119.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

class FengTubeRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun refreshFeeds(): List<FengTubeChannelFeed> = withContext(Dispatchers.IO) {
        val feeds = CHANNELS.map { channel ->
            runCatching {
                val channelId = resolveChannelId(channel.sourceUrl)
                fetchChannelFeed(channel = channel, channelId = channelId)
            }.getOrElse { error ->
                Log.e(TAG, "Failed to load FengTube channel: ${channel.sourceUrl}", error)
                FengTubeChannelFeed(
                    channel = channel,
                    channelTitle = channel.fallbackName,
                    videos = emptyList(),
                    fetchedAt = Instant.now().toString(),
                    errorMessage = error.message ?: "讀取失敗"
                )
            }
        }

        val merged = mergeWithCachedFallback(feeds)
        saveFeeds(merged)
        merged
    }

    fun getCachedFeeds(): List<FengTubeChannelFeed> {
        val raw = prefs.getString(FEEDS_KEY, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val channel = FengTubeChannel(
                        sourceUrl = item.optString("sourceUrl"),
                        fallbackName = item.optString("fallbackName")
                    )
                    val videosArray = item.optJSONArray("videos") ?: JSONArray()
                    val videos = buildList {
                        for (videoIndex in 0 until videosArray.length()) {
                            val video = videosArray.optJSONObject(videoIndex) ?: continue
                            add(
                                FengTubeVideo(
                                    title = video.optString("title"),
                                    url = video.optString("url"),
                                    channelTitle = video.optString("channelTitle"),
                                    publishedAt = video.optString("publishedAt")
                                )
                            )
                        }
                    }

                    add(
                        FengTubeChannelFeed(
                            channel = channel,
                            channelTitle = item.optString("channelTitle"),
                            videos = videos,
                            fetchedAt = item.optString("fetchedAt"),
                            errorMessage = item.optString("errorMessage").ifBlank { null }
                        )
                    )
                }
            }
        }.getOrElse {
            Log.e(TAG, "Failed to read cached FengTube feeds", it)
            emptyList()
        }
    }

    private fun mergeWithCachedFallback(feeds: List<FengTubeChannelFeed>): List<FengTubeChannelFeed> {
        val cachedByUrl = getCachedFeeds().associateBy { it.channel.sourceUrl }
        return feeds.map { feed ->
            if (feed.videos.isNotEmpty()) {
                feed
            } else {
                cachedByUrl[feed.channel.sourceUrl]?.copy(errorMessage = feed.errorMessage) ?: feed
            }
        }
    }

    private fun resolveChannelId(sourceUrl: String): String {
        val html = fetchText(sourceUrl)
        val patterns = listOf(
            Regex(""""channelId"\s*:\s*"(UC[\w-]+)""""),
            Regex(""""externalId"\s*:\s*"(UC[\w-]+)""""),
            Regex("""youtube\.com/channel/(UC[\w-]+)""")
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(html)?.groupValues?.getOrNull(1)
        } ?: error("找不到 YouTube channelId")
    }

    private fun fetchChannelFeed(
        channel: FengTubeChannel,
        channelId: String
    ): FengTubeChannelFeed {
        val xml = fetchText("https://www.youtube.com/feeds/videos.xml?channel_id=$channelId")
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))
        val entries = document.getElementsByTagName("entry")
        val channelTitle = document.getElementsByTagName("title")
            .item(0)
            ?.textContent
            ?.trim()
            ?.ifBlank { null }
            ?: channel.fallbackName

        val videos = buildList {
            for (index in 0 until entries.length) {
                val entry = entries.item(index) as? Element ?: continue
                val title = entry.textOf("title") ?: continue
                val videoId = entry.textOf("yt:videoId") ?: continue
                val published = entry.textOf("published").orEmpty()
                add(
                    FengTubeVideo(
                        title = title,
                        url = "https://www.youtube.com/watch?v=$videoId",
                        channelTitle = channelTitle,
                        publishedAt = published
                    )
                )
            }
        }.take(10)

        return FengTubeChannelFeed(
            channel = channel,
            channelTitle = channelTitle,
            videos = videos,
            fetchedAt = Instant.now().toString()
        )
    }

    private fun fetchText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return connection.run {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
            )
            setRequestProperty("Accept-Language", "zh-TW,zh;q=0.9,en;q=0.8")
            val stream = if (responseCode in 200..299) inputStream else errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            disconnect()
            if (responseCode !in 200..299) {
                error("HTTP $responseCode")
            }
            text
        }
    }

    private fun saveFeeds(feeds: List<FengTubeChannelFeed>) {
        val array = JSONArray()
        feeds.forEach { feed ->
            array.put(
                JSONObject().apply {
                    put("sourceUrl", feed.channel.sourceUrl)
                    put("fallbackName", feed.channel.fallbackName)
                    put("channelTitle", feed.channelTitle)
                    put("fetchedAt", feed.fetchedAt)
                    put("errorMessage", feed.errorMessage.orEmpty())
                    put(
                        "videos",
                        JSONArray().apply {
                            feed.videos.forEach { video ->
                                put(
                                    JSONObject().apply {
                                        put("title", video.title)
                                        put("url", video.url)
                                        put("channelTitle", video.channelTitle)
                                        put("publishedAt", video.publishedAt)
                                    }
                                )
                            }
                        }
                    )
                }
            )
        }
        prefs.edit().putString(FEEDS_KEY, array.toString()).apply()
    }

    private fun Element.textOf(tagName: String): String? {
        return getElementsByTagName(tagName)
            .item(0)
            ?.textContent
            ?.trim()
            ?.ifBlank { null }
    }

    companion object {
        private const val TAG = "FengTubeRepository"
        private const val PREFS_NAME = "feng_tube"
        private const val FEEDS_KEY = "feeds"

        val CHANNELS = listOf(
            FengTubeChannel("https://www.youtube.com/@SJdiao/videos", "SJdiao"),
            FengTubeChannel("https://www.youtube.com/@henren778", "henren778"),
            FengTubeChannel("https://www.youtube.com/@libertas1984/videos", "libertas1984"),
            FengTubeChannel("https://www.youtube.com/@sunlao/videos", "sunlao"),
            FengTubeChannel("https://www.youtube.com/@Torontobigface/videos", "Torontobigface"),
            FengTubeChannel("https://www.youtube.com/@junyulan/videos", "junyulan"),
            FengTubeChannel("https://www.youtube.com/@blackwhite_raven/videos", "blackwhite_raven"),
            FengTubeChannel("https://www.youtube.com/@quedaren/videos", "quedaren"),
            FengTubeChannel("https://www.youtube.com/@%E5%A4%B8%E5%85%8B%E8%AF%B4", "夸克说"),
            FengTubeChannel("https://www.youtube.com/@%E5%96%B5%E5%96%B5%E7%9C%8B%E4%B8%80%E7%9C%8B/videos", "喵喵看一看")
        )

        fun formatPublishedAt(isoInstant: String): String {
            return runCatching {
                val instant = Instant.parse(isoInstant)
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                formatter.format(instant.atZone(ZoneId.systemDefault()))
            }.getOrDefault("--")
        }
    }
}
