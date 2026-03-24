# v1.2.1 Release Validation

Date: 2026-03-24

Release:
- Tag: `v1.2.1`
- APK: `app/build/outputs/apk/release/app-release.apk`

Scope:
- Modernized home dashboard layout
- Refined subscription overview and reminder presentation
- Redesigned oil monitoring screen and trend visualization
- Replaced corrupted UI copy with clear Traditional Chinese labels

Build Verification:
- `assembleDebug`: passed
- `assembleRelease`: passed

Recommended Smoke Test:
- Install the `v1.2.1` APK and confirm the app launches normally
- Verify the two home tabs switch correctly
- Verify subscription data loads and renders without corrupted text
- Verify the oil monitoring screen shows latest price, chart, and recent history
- Verify manual refresh works on the oil monitoring screen
- Verify notification permission flow does not block startup
- Verify there are no obvious layout breaks on a smaller phone screen

Notes:
- GitHub Release has been published with the signed APK attached
- GitHub `main` has been aligned to the `v1.2.1` release commit
