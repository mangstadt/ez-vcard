Dev setup instructions:

1. Go to "https://developer.android.com/sdk/index.html"
2. Click on "View All Downloads and Sizes"
3. Download the appropriate file for your OS in the "SDK Tools Only" table.
4. Unzip it to a location of your choice.
5. Run the "tools/android" script to open the Android SDK Manager.
6. Install the the following:
  * Tools/Android SDK Tools
  * Tools/Android SDK Platform-tools
  * Tools/Android SDK Build-tools (v 19.0.3)
  * Android 4.4.2 (API 19)/SDK Platform
  * Extras/Android Support Library
  * Extras/Android Support Repository
7. If you don't see some of these, install what you can, then restart the SDK Manager and try again.
8. In "local.properties", change the "sdk.dir" to point to where you unzipped the SDK Tools. 
9. Run "./gradlew build"