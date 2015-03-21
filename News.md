**October 13, 2014**

[Version 0.9.6](Downloads.md) released.  This released fixes a few miscellaneous bugs.  Please see the [changelog](Changelog.md) for details.

**August 17, 2014**

Do you use ez-vcard with Android?  The [Android Mapper](AndroidMapper.md) project makes it easier to use ez-vcard with Android!

**July 26, 2014**

[Version 0.9.5](Downloads.md) released.  This released fixes a few miscellaneous bugs.  Please see the [changelog](Changelog.md) for details.

**May 31, 2014**

[Version 0.9.4](Downloads.md) released.  This released adds xCard streaming support, and fixes a handful of bugs.  Please see the [changelog](Changelog.md) for details.

**April 1, 2014**

[Version 0.9.3](Downloads.md) released.  This release fixes a bug related to the decoding of invalid quoted-printable values.  Please see the [changelog](Changelog.md) for details.

**February 4, 2014**

[Version 0.9.2](Downloads.md) released.  This release includes a number of bug fixes.  Please see the [changelog](Changelog.md) for details.

**December 24, 2013**

**Happy Holidays** from the ez-vcard team!

[Version 0.9.1](http://code.google.com/p/ez-vcard/downloads/list) is released.  This release adds OSGi support, internationalized warnings, and bug fixes.  See the [changelog](Changelog.md) for more details.

We are looking for people to translate ez-vcard's parser and validation warnings into other languages.  If you are interested, please contact me at mike(dot)angstadt(at)gmail(dot)com, or submit an [Issue](https://code.google.com/p/ez-vcard/issues).  The message file can be found here: https://code.google.com/p/ez-vcard/source/browse/trunk/src/main/resources/ezvcard/messages.properties

**October 25, 2013**

[Version 0.9.0](http://code.google.com/p/ez-vcard/downloads/list) released.  This release adds a revamped marshalling framework and includes a significant reorganization of the API.

Existing code that uses older versions of ez-vcard will most likely break, due to the fact that many packages and classes have been renamed.  Please see the [changelog](Changelog.md) for a list of these changes.

**October 1, 2013**

[Version 0.8.5](http://code.google.com/p/ez-vcard/downloads/list) released.  This release fixes a bug dealing with "quoted-printable" property values not being folded correctly.

See the [changelog](Changelog.md) for more details.

**September 19, 2013**

[Version 0.8.4](http://code.google.com/p/ez-vcard/downloads/list) released.  This release adds a validation framework, better UTF-8 support, and a large number of other, relatively minor, changes.

See the [changelog](Changelog.md) for a complete list of changes.

**August 4, 2013**

[Version 0.8.3](http://code.google.com/p/ez-vcard/downloads/list) released.  This release adds updated jCard support and includes a host of bug fixes and other changes.

See the [changelog](Changelog.md) for a complete list of changes.

**June 17, 2013**

[Version 0.8.2](http://code.google.com/p/ez-vcard/downloads/list) released.  This release fixes an issue with Android systems where an older version of "commons-codec" would be used instead of the version that ez-vcard requires.

See the [changelog](Changelog.md) for a complete list of changes.

**May 21, 2013**

[Version 0.8.1](http://code.google.com/p/ez-vcard/downloads/list) released.  This release includes a number of miscellaneous improvements and bug fixes.

See the [changelog](Changelog.md) for a complete list of changes.

**April 17, 2013**

[Version 0.8.0](http://code.google.com/p/ez-vcard/downloads/list) released.  This release adds **support for jCard** (JSON-encoded vCards).

See the [changelog](Changelog.md) for a complete list of changes.

**March 29, 2013**

[Version 0.7.3](http://code.google.com/p/ez-vcard/downloads/list) released.  This release fixes an issue that prevented the `GEO` property from marshalling correctly under certain locales.

See the [changelog](Changelog.md) for details.

**February 24, 2013**

[Version 0.7.2](http://code.google.com/p/ez-vcard/downloads/list) released.  This release includes:

  * Support for circumflex accent encoding ([RFC 6868](http://tools.ietf.org/html/rfc6868))
  * Support for `KIND:device` ([RFC 6869](http://tools.ietf.org/html/rfc6869))
  * Bug fixes

See the [changelog](Changelog.md) for details.

**January 17, 2013**

Released [Version 0.7.1](http://code.google.com/p/ez-vcard/downloads/list) to fix a critical bug in how xCard are written.  See the [changelog](Changelog.md) for details.

**January 13, 2013**

[Version 0.7.0](http://code.google.com/p/ez-vcard/downloads/list) is the **Usability Update!**

Many changes were made to the API to make it easier to use.  Notably, a **method chaining interface** was added for parsing and writing vCards (see code samples further down the page).  Convenience methods were also added to the `VCard` class and other classes to make it easier to work with the API.  There were some bug fixes also.

See the [changelog](Changelog.md) for a complete list of the changes in this version.

**December 12, 2012**

[Version 0.6.0](http://code.google.com/p/ez-vcard/downloads/list) released.  The most notable addition in this version is support for the **[hCard](http://microformats.org/wiki/hcard) microformat** (HTML-encoded vCards).

See the [changelog](Changelog.md) for a complete list of the changes.

**November 8, 2012**

[Version 0.5.0](http://code.google.com/p/ez-vcard/downloads/list) released.  Notable changes include:

  * Marshalling classes no longer throw a checked `VCardException`.
  * The XML element name of extended types can now be specified.

See the [changelog](Changelog.md) for the other changes.

**Oct 2 2012**

ez-vcard has been released to **Maven Central**!!  Scroll down for dependency information.

**Sept 24 2012**

[Version 0.4.0](http://code.google.com/p/ez-vcard/downloads/list) released.  Notably, added support for the following specifications:

  * [RFC 6473](http://tools.ietf.org/html/rfc6473) (KIND:application)
  * [RFC 6474](http://tools.ietf.org/html/rfc6474) (BIRTHPLACE, DEATHPLACE, DEATHDATE)
  * [RFC 6715](http://tools.ietf.org/html/rfc6715) (Open Mobile Alliance Converged Address Book extensions)

See the [changelog](Changelog.md) for the rest of the changes.

**Sept 8 2012**

[Version 0.3.0](http://code.google.com/p/ez-vcard/downloads/list) released.  Notable changes in this version include:

  * Support for the **[xCard](http://tools.ietf.org/html/rfc6351)** standard (an XML representation for vCards).
  * Removal of the "guava" and "commons-io" dependencies.  ez-vcard now only requires one dependency (commons-codec).

See the [changelog](Changelog.md) for the rest of the changes.

**August 11 2012**

[Version 0.2.0](http://code.google.com/p/ez-vcard/downloads/list) released.  Notably, this version adds support for **vCard version 4.0**.  See the [changelog](Changelog.md) for more details.

**July 28 2012**

[Version 0.1.1](http://code.google.com/p/ez-vcard/downloads/list) released.  See the [changelog](Changelog.md) for details.

**July 23 2012**

[Version 0.1](http://code.google.com/p/ez-vcard/downloads/list) released.  It supports vCard versions 2.1 and 3.0.