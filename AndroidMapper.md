

The Android Mapper project converts an ez-vcard `VCard` object into the appropriate Android data fields, so it can be added to an Android user's contact list.

# 1 Code Sample #

```
File vcardFile = ...
VCardReader reader = null;
try {
  reader = new VCardReader(vcardFile);
  reader.registerScribe(new AndroidCustomFieldScribe());

  ContactOperations operations = new ContactOperations(getApplicationContext());
  VCard vcard = null;
  while ((vcard = reader.readNext()) != null) {
      operations.insertContact(vcard);
  }
} finally {
  reader.close();
}
```

# 2 Download #

A download package does not exist yet.  The code can be found here:

[SVN/android-mapper/trunk/android-mapper/src/main/java/ezvcard/android](https://code.google.com/p/ez-vcard/source/browse/#svn%2Fandroid-mapper%2Ftrunk%2Fandroid-mapper%2Fsrc%2Fmain%2Fjava%2Fezvcard%2Fandroid)

# 3 Project Notes #

The entire project can be found here:

[SVN/android-mapper/trunk](https://code.google.com/p/ez-vcard/source/browse/#svn%2Fandroid-mapper%2Ftrunk)

The project contains a sample Android app, which demonstrates how to use the library.

## 3.1 Development Environment Setup Instructions ##

If you are not using an Android-enabled IDE, follow these instructions so you can get the project to a point where you can build it.

  1. Go to https://developer.android.com/sdk/index.html
  1. Click on "View All Downloads and Sizes"
  1. Download the appropriate file for your OS in the "SDK Tools Only" table.
  1. Unzip/install the file to a location of your choice.
  1. Run the "tools/android" script in the installation to open the Android SDK Manager.
  1. Install the the following packages (if some of these aren't in the list, install what you can, then restart the SDK Manager to see if they appear):
    * Tools -> Android SDK Tools (latest version)
    * Tools -> Android SDK Platform-tools (latest version)
    * Tools -> Android SDK Build-tools (version 19.0.3)
    * Android 4.4.2 (API 19) -> SDK Platform
    * Extras -> Android Support Library (latest version)
    * Extras -> Android Support Repository (latest version)
  1. Open the "local.properties" file in the root of the Android Mapper project, change the "sdk.dir" property to point to where you unzipped/installed the SDK Tools.
  1. Run `./gradlew build` to build the application.