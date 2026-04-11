package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Sand
import com.example.jetpackcomposetrae20260119.ui.theme.Slate

private data class MarriageReasonOption(
    val id: String,
    val title: String,
    val summary: String,
    val punchline: String,
    val tags: List<String>
)

private val marriageReasonOptions = listOf(
    MarriageReasonOption(
        id = "moon-elder-539",
        title = "今彩 539 根本是月老代班系統",
        summary = "鋒兄跟塗哥都把開獎畫面講得像感情認證流程，只要號碼一中，整段戀愛史就被包裝成天意安排。",
        punchline = "不是先交往再結婚，是先中獎再補上命運說明書。",
        tags = listOf("鋒兄", "塗哥", "月老代班")
    ),
    MarriageReasonOption(
        id = "feng-contract",
        title = "鋒兄說這其實是中獎合約書",
        summary = "思敏那期號碼一開出來，鋒兄立刻認定這不是普通運氣，而是一份宇宙已經蓋章的關係契約。",
        punchline = "不結婚就像拒簽頭獎文件，聽起來荒謬，但他講得超有自信。",
        tags = listOf("思敏", "宇宙契約", "超展開")
    ),
    MarriageReasonOption(
        id = "tu-called",
        title = "塗哥認定財神直接幫他點名",
        summary = "惠璇隨手挑的號碼進了中獎圈後，塗哥瞬間把這件事理解成財神與月老共同連線，公開宣布未來走向。",
        punchline = "如果天都幫你點名了，剩下的只是去婚宴現場報到。",
        tags = listOf("惠璇", "財神來電", "公開宣布")
    ),
    MarriageReasonOption(
        id = "double-jackpot",
        title = "兩對情侶共用同一套荒唐中獎理論",
        summary = "鋒兄配思敏、塗哥配惠璇，最後都把彩券畫面說成婚姻證據，整桌朋友嘴上吐槽，心裡卻越聽越完整。",
        punchline = "理論越扯，敬酒時越有人想聽第二次。",
        tags = listOf("雙線敘事", "婚宴傳說", "朋友全買單")
    ),
    MarriageReasonOption(
        id = "love-math",
        title = "戀愛不是玄學，是大聲講出來的數學",
        summary = "原本只是一次普通下注，結果一中就被講成感情公式成立，連旁邊沒下注的人都被迫一起驗算。",
        punchline = "他們叫它浪漫，旁觀者只覺得這是高強度心證推導。",
        tags = listOf("戀愛數學", "強行證明", "高強度")
    ),
    MarriageReasonOption(
        id = "lottery-vows",
        title = "背期號比背結婚誓詞還熟",
        summary = "鋒兄能把那期號碼倒背如流，每次講到關鍵轉折都像在重播人生最重要的告白畫面。",
        punchline = "誓詞可以改稿，頭獎期號卻被他直接刻進人生設定。",
        tags = listOf("人生明牌", "期號誓詞", "記憶點")
    ),
    MarriageReasonOption(
        id = "banquet-joke",
        title = "最離譜的理由反而變成婚宴主題",
        summary = "原本只是朋友間的笑話，最後卻演變成每桌都在重複的祝酒詞，越講越像這段婚姻真的有官方見證。",
        punchline = "最瞎的理由，偏偏成了整場最有品牌感的一句話。",
        tags = listOf("婚宴", "祝酒詞", "最瞎但最紅")
    ),
    MarriageReasonOption(
        id = "tomorrow-ticket",
        title = "聽完整段故事的人都想順手買一張",
        summary = "大家嘴上說這套理由太扯，實際上回家前都默默確認了明天的開獎時間，怕命運真的只差一張彩券。",
        punchline = "荒謬是真的荒謬，但誘惑也是真的很完整。",
        tags = listOf("旁觀者效應", "明天就買", "甜蜜混亂")
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LotteryComparisonScreen(
    viewModel: LotteryComparisonViewModel,
    headerContent: LazyListScope.() -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf("") }
    val selected = marriageReasonOptions.firstOrNull { it.id == selectedId }
    val tags = selected?.tags ?: listOf("今彩 539", "朋友亂講", "甜得很認真")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            headerContent()

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Porcelain
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                        Text(
                            text = "醉蝦結婚理由",
                            style = MaterialTheme.typography.labelMedium,
                            color = Copper
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "挑一條最離譜的理由，看今彩 539 怎麼被講成婚姻證物",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "內容參考 mydailycash 的婚宴笑話支線，保留那種一本正經胡說八道的語氣，整理成手機版故事卡。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Porcelain
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "理由選單",
                            style = MaterialTheme.typography.titleLarge,
                            color = Midnight
                        )
                        Text(
                            text = "鋒兄、塗哥都把今彩 539 講成月老分部。選一條最瞎的理由，看看中獎、命運和婚禮怎麼被硬湊成同一套宇宙觀。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selected?.title ?: "請選一條醉蝦結婚理由",
                                onValueChange = {},
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                label = { Text("理由選單") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                }
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                marriageReasonOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.title) },
                                        onClick = {
                                            selectedId = option.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0xFFFFFCF4)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = selected?.title ?: "還沒選理由",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = selected?.summary
                                ?: "先從選單挑一條，看鋒兄和塗哥怎麼把開獎畫面講成結婚保證書。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Midnight
                        )
                        Text(
                            text = selected?.punchline
                                ?: "中獎可以靠運氣，結婚理由靠的是一種不肯輸的想像力。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Copper,
                            fontWeight = FontWeight.SemiBold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tags.forEach { tag ->
                                Text(
                                    text = tag,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Sand)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Midnight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Porcelain
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "內容來源",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink
                        )
                        Text(
                            text = "參考專案：goldshoot0720 / mydailycash",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Midnight
                        )
                        Text(
                            text = "本頁改寫自該專案的婚宴理由選單與故事卡內容，重新整理成 Atlas Monitor 的分頁閱讀形式。",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
