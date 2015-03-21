# 0.9.6 #
_October 13, 2014_

  * Added a "readAll()" method to all reader classes.
  * Added optional dependency definitions to OSGi bundle settings.
  * Removed the constructors from `VCardWriter` that allow you to customize the line folding settings.  See the [VCardWriter](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/io/text/VCardWriter.html) Javadocs for new instructions on how to modify these settings.
  * Fixed a bug where variables in the hCard template were being removed during the build process (credit: David Spieler; see [Issue 19](https://code.google.com/p/ez-vcard/issues/detail?id=19)).
  * Fixed a bug where LABEL properties in nested 2.1 vCards weren't being assigned to their ADR properties correctly.
  * Fixed a validation bug where the CATEGORIES and SORT-STRING properties were considered to be part of the 2.1 spec (they are not).

# 0.9.5 #
_July 26, 2014_

We've reached **1,000 commits!** :D

  * Fixed a bug where not all newline sequences were being written as `<br />` in hCards (credit: aliteralmind).
  * Fixed a bug where XCardWriter was incorrectly encoding "\r" characters in Windows newline sequences (credit: aliteralmind).
  * Date property values are now encoded correctly in version 3.0 vCards (credit: 沈健).  Before, they were encoded in "basic" format (e.g. "20140726").  Now, they are encoded in "extended" format (e.g. "2014-07-26").

# 0.9.4 #
_May 31, 2014_

  * Added a streaming API for xCards (`XCardReader`, `XCardWriter`).
  * Fixed a bug where `<unknown>` property values in xCard documents weren't being parsed correctly.
  * Fixed a bug that caused a NPE to be thrown when removing the LEVEL parameter from a EXPERTISE or INTEREST property.
  * Fixed a bug that prevented ORG properties with empty values from being written to an hCard ([Issue 16](https://code.google.com/p/ez-vcard/issues/detail?id=16)).
  * Fixed an hCard bug where a vCard with an embedded vCard would treat the embedded vCard's properties as its own.
  * Renamed `HCardReader` to `HCardParser`.
  * Upgraded jsoup dependency from version 1.7.1 to 1.7.3.
  * Upgraded jackson dependency from version 2.1.3 to 2.3.3.
  * Improved the performance of plain-text vCard parsing by removing a regular expression.
  * Removed a reference to a jsoup class in "VCardPropertyScribe".  This allows users to create custom scribe classes without having to add the optional jsoup dependency to their project.

# 0.9.3 #
_April 1, 2014_

  * Fixed a bug where incorrectly encoded quoted-printable values were preventing the vCard from being parsed ([Issue 15](https://code.google.com/p/ez-vcard/issues/detail?id=15)).
  * Javadoc improvements.

# 0.9.2 #
_February 4, 2014_

  * If a property's value is encoded in quoted-printable encoding, then the property _should_ contain a CHARSET parameter that defines the character set that the property value was encoded in.  However, not all vCards may include such a parameter.  To account for this, you can now define the default character set to use when parsing quoted-printable properties that do not have a CHARSET parameter ([Issue 12](https://code.google.com/p/ez-vcard/issues/detail?id=12)).  Use the "VCardReader.setDefaultQuotedPrintableCharset()" method to define this setting.
  * Quoted-printable property values are now encoded using UTF-8 when written if no CHARSET parameter is present.  This improves interoperability, since virtually all systems support this character set.  Before, the character set of the "Writer" object would be used, which could be a non-standard character set or a character set specific to the local operating system.
  * Fixed a bug where ORG properties with multiple values weren't being written correctly in plain-text vCards ([Issue 13](https://code.google.com/p/ez-vcard/issues/detail?id=13)).
  * Fixed a bug where the "ez-vcard.properties" file wasn't being filtered during the build process.  This caused the PRODID property of vCards created by ez-vcard to show the wrong version number of ez-vcard ([Issue 14](https://code.google.com/p/ez-vcard/issues/detail?id=14)).

# 0.9.1 #
_December 24, 2013_

  * Added OSGi support (credit: Florian Brunner).
  * Moved the validation and parser warnings into a resource bundle for i18n.  Note that some messages were tweaked in the process.
  * Validation warnings now have IDs associated with them so they can be handled programmatically.
  * Added a validation check which checks whether the CHARSET parameter is a recognized character set.
  * Fixed a bug that prevented quoted-printable property values from being encoded correctly when written (credit: Tom Vogel).

# 0.9.0 #
_October 25, 2013_

This update includes significant changes to the API.  Existing code that uses eariler versions may break.

  * Added a new marshalling framework.  This moves the marshalling code out of the property classes and into classes of their own (called "scribes"), creating a cleaner separation between the data model and the marshalling code.  Property classes are no longer required to have a default constructor.
  * Removed the "Type" suffix from the property class names (for example, renamed `AddressType` to `Address`).  Property classes that are meant to be parent classes have had their "Type" suffix replaced with "Property" (for example, renamed `TextType` to `TextProperty`).
  * Removed the "Parameter" suffix from the parameter class names (for example, renamed `AddressTypeParameter` to `AddressType`).
  * Removed the property name from the `VCardType` class (i.e. removed the `getTypeName()` method).  Each property's name now resides in its "scribe" class (located in the "`ezvcard.io.scribe`" package).
  * Grouped the I/O classes into sub-packages based on their format type (for example, `VCardReader` was moved from "`ezvcard.io`" to "`ezvcard.io.text`").
  * Renamed all methods in the `VCardType` class so that they use "parameter" terminology instead of "sub type" terminology (for example, renamed `getSubTypes` to `getParameters`).
  * Renamed all methods in the `VCard` class so that they use "property" terminology instead of "type" terminology (for example, renamed `addType` to `addProperty`)
  * Renamed and/or moved the following packages/classes:
    * `ezvcard.types` --> `ezvcard.property`
    * `ezvcard.parameters` --> `ezvcard.parameter`
    * `ezvcard.VCardSubTypes` --> `ezvcard.parameter.VCardParameters`
    * `ezvcard.types.VCardType` --> `ezvcard.property.VCardProperty`
    * `ezvcard.util.HCardElement` --> `ezvcard.io.html.HCardElement`
    * `ezvcard.util.XCardElement` --> `ezvcard.io.xml.XCardElement`
    * `ezvcard.util.JsonValue` --> `ezvcard.io.json.JsonValue`
    * `ezvcard.util.JCardValue` --> `ezvcard.io.json.JCardValue`
  * The `Ezvcard` class now flushes the stream after every plain-text or JSON-encoded vCard is written.
  * Fixed a bug with the `Ezvcard` class that prevented UTF-8 encoding from being used when writing xCards and jCards.

# 0.8.5 #
_October 1, 2013_

  * Fixed a bug where "quoted-printable" encoded property values were not being folded correctly (see [Issue 9](https://code.google.com/p/ez-vcard/issues/detail?id=9)).

# 0.8.4 #
_September 19, 2013_

**Major changes:**

  * **Validation framework**: Added a validation framework that checks `VCard` objects for errors.  It can be invoked by calling `VCard.validate()`.  The writer classes no longer generate a list of warnings when a `VCard` is written.
  * **XCardDocument/XCardReader merge**: Merged the `XCardReader` class into the `XCardDocument` class.  `XCardDocument` is now used to both read and write xCards.
  * **Unparsable properties no longer ignored**: Added a `CannotParseException`, which is thrown if a property value cannot be parsed.  This will cause the property to be unmarshalled as a `RawType` instead of being completely ignored, allowing for its raw value to be retrieved.  For xCards, such properties will be unmarshalled as `XmlType` properties.
  * **Improved UTF-8 support**: UTF-8 encoding is now used essentially whenever a `File` or `InputStream/OutputStream` object is passed into one of ez-vcard's reader/writer classes.  Java `Reader/Writer` objects are configured to use their own character encoding, so they are not effected by this change.
    * If a `File` or `OutputStream` object is passed into a plain-text vCard writer, and the target version is 4.0, UTF-8 encoding will be used. All other versions will use the JVM's default character encoding.
    * If a `File` or `OutputStream` object is passed into a xCard or jCard writer, UTF-8 encoding will be used.
    * If a `File` or `InputStream` object is passed into a plain-text vCard reader, the JVM's default character encoding is still used for backwards compatibility to older vCard versions.
    * If a `File` or `InputStream` object is passed into a xCard reader, the "encoding" attribute in the header portion of the XML document will be properly taken into account.  Before, it was ignored and the document was always parsed according to the JVM's default character encoding.
    * If a `File` or `InputStream` object is passed into a jCard reader, UTF-8 decoding will be used.

Other changes:

  * Added the `VCardDataType` class, which represents a vCard data type (such as "text" and "uri"). It replaces the `ValueParameter` and `JCardDataType` classes.
  * Added the ability to append onto existing files when writing plain-text files with the `Ezvcard` class.
  * The `Ezvcard.parse(File)` methods no longer throw a `FileNotFoundException`.
  * Added the `XCardDocument.registerParameterDataType()` method for setting the data types of extended parameters.
  * The `XmlType` property now stores its value as a `Document` object instead of a `String`.
  * The following `VCardSubType` methods now throw an `IllegalStateException` when their parameter value cannot be parsed (for example, if the PREF parameter is non-numeric).  Before, they would return "null", which was misleading because "null" also means that the parameter does not exist.  Raw parameter values can still be retrieved using the `get()` method.
    * `getPref()`
    * `getGeo()`
    * `getPids()`
    * `getIndex()`
  * Added a `versionStrict` flag to the writer classes.  When enabled, properties that do not support the target vCard version will not be written to the data stream (for example, a GENDER property, which is only supported by 4.0, will not be written to a 2.1 vCard).  This is how the writers have functioned in the past.  When the flag is disabled, properties that do not support the target vCard version will be written anyway.  This setting is enabled by default.
  * A `CannotParseException` is now consistently thrown during the parsing of xCard properties when the XML element which holds the property's value cannot be found.  Before, a `SkipMeException` was sometimes thrown, but not always.
  * Changed the behavior for how properties with empty values are written.  Before, they were not written to the vCard at all, but now they are.  The new `VCard.validate()` method can be called to check for properties that have empty or inappropriate values.
  * Changes to the `TimezoneType` property:
    * Now accepts a `java.util.TimeZone` object as its value.
    * If it only contains a text value and it is being written to a 2.1 vCard, it will attempt to calculate the UTC offset by treating the text value as an Olson timezone ID.
    * When parsing an xCard, if its xCard value contains both a `<text>` and a `<uri>` element (which is not allowed), only the `<text>` element will be parsed.  Before, both were parsed.
  * User-defined type classes can now override the standard type classes.  This means that it's now possible to create alternative type classes for standard properties like ADR.
  * Made the following classes immutable:
    * `DataUri`
    * `GeoUri`
    * `TelUri`
  * Added the following methods to `VCardType`:
    * `getSubType(String)`
    * `getSubTypes(String)`
    * `setSubType(String, String)`
    * `addSubType(String, String)`
    * `removeSubType(String)`
    * `removeType(VCardType)`
  * Renamed the following methods:
    * `XCardDocument.addVCard` --> `add`
    * `HCardPage.addVCard` --> `add`
    * `JCardValue.getSingleValued` --> `asSingle`
    * `JCardValue.getMultivalued` --> `asMulti`
    * `JCardValue.getStructured` --> `asStructured`
  * Added syntax highlighting to the code samples in the Javadocs. :)

# 0.8.3 #
_August 4, 2013_

  * Updated jCard functionality to adhere to the most recent draft specification.
    * Removed the "vcardstream" array.
    * jCards can optionally be wrapped in a JSON array (useful when writing multiple jCards).
  * Fixed a bug that prevented `KEY` URLs from being parsed correctly.
  * Fixed the XML marshalling of N and ADR properties so that XML elements for every value component is included, even if the component is null or empty.
  * Fixed a bug that gave `LANG` properties the wrong data type in jCards.
  * `ORG` property values are no longer surrounded by brackets in jCard if the property only has one value.
  * Date-related property values are now written to "date" or "date-time" XML elements, instead of "date-and-or-time" elements.
  * Date-related properties with date, date-time, or partial-date values (such as `BDAY`) no longer include `VALUE` or `CALSCALE` parameters when written, except when the value is text (in which case a `VALUE=text` parameter is added).
  * Fixed the marshalling of UTC offset values for the `TZ` property for 4.0 vCards and xCards ("basic" format should be used, not "extended").
  * Refactored the way in which the `VCard` class stores its properties:
    * When parsing, _all_ property instances are now added to the `VCard` object.  Previously, for properties with a cardinality of zero or one, the only instance that was retrievable was the one that was parsed last.
    * Property order is more predictable during marshalling.  Properties are essentially written in the order that they were added to the `VCard` object.
    * Removed the `addExtendedType(VCardType)` method.  To add an extended type class instance, use `addType(VCardType)` instead.
    * Removed the `getExtendedType(Class)` method.  To get an extended type class instance, use `getType(Class)` instead.
  * Refactored the jCard reading/writing code:
    * The `JCardValue` class now holds the raw JSON data in the form of `JsonValue` objects.  This adds greater flexibility for reading/writing wonky property values.
    * Removed the `JCardDataType.UNKNOWN` enum.  A "null" value is now used to represent the "unknown" data type.
    * Pretty printing has been tweaked slightly.
  * Refactored the parsing code of plain-text vCards.  Parsing is slightly more strict: `BEGIN:VCARD` and `END:VCARD` properties are now **required**, or else the vCard will not be parsed.

# 0.8.2 #
_June 17, 2013_

  * Fixed an Android compatibility issue: Android's version of commons-codec (1.2) was overriding the version that ez-vcard uses (1.6) (see [this discussion](https://groups.google.com/forum/?fromgroups=#!topic/ez-vcard-discuss/w2TK7yetwr8)).  Selection portions of the commons-codec 1.6 source code have been added to the ez-vcard code base.  Their package names were changed in order to eliminate the conflict.
  * vCard (plain-text) and jCard (JSON) readers now include line numbers in their warnings (for example: "Line 10 (TEL property): Could not parse property value as a URI.  Assuming it's text.")
  * Each warning message is now prefixed with the name of the property that the warning belongs to (see example in previous bullet point).
  * Minor text changes to some of the warning messages.

# 0.8.1 #
_May 21, 2013_

  * Added a new `PartialDate` class for representing truncated and reduced accuracy dates.
  * The `TelephoneType` class (TEL property) now distinguishes between text and URI values.  A URI value is represented with the new `TelUri` class.
  * A `TemplateException` exception is no longer thrown when writing a vCard to an HTML page (see [Issue 7](https://code.google.com/p/ez-vcard/issues/detail?id=7), credit: dnault).
  * GENDER is no longer marshalled as a structured jCard property if it does not contain the optional text component.
  * Fixed a bug dealing with the way in which XML namespaces are handled in xCards.

# 0.8.0 #
_April 17, 2013_

  * Added support for **jCard** (JSON-encoded vCards, see: [jCard draft specification](http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-01)).
  * Added methods to the `VCard` class for adding groups of alternative representations.  These methods will automatically generate an appropriate `ALTID` parameter value.
  * Fixed a typo in the name of the `DateOrTimeType.setCalscale()` method.
  * Fixed an issue with hCards where `AGENT` URLs were not being parsed correctly.
  * `TZ` parameters in the `ADR` property are no longer parsed as URIs.
  * Improved overall unit test coverage.

# 0.7.3 #
_March 29, 2013_

  * Fixed an issue that prevented the `GEO` property from marshalling correctly under certain locales (credit: Moritz Bechler).

# 0.7.2 #
_February 24, 2013_

  * Renamed the marshal methods in the `VCardType` class to better reflect their format (e.g. renamed "unmarshalValue" to "unmarshalText").
  * Fixed a bug in how "quoted-printable" property values are parsed: values that have empty lines at the end of them are now parsed correctly (see [this discussion](https://groups.google.com/forum/?fromgroups=#!topic/ez-vcard-discuss/IZgLDVlofGE))
  * Added `KIND:device` ([RFC 6869](http://tools.ietf.org/html/rfc6869)).
  * Changed how the `SORT-AS` parameter works in the `OrganizationType` and `StructuredNameType` classes.
  * Major fixes/additions to parameter values in plain-text vCards:
    * Added support for circumflex accent encoding ([RFC 6868](http://tools.ietf.org/html/rfc6868)).
    * Certain invalid characters are now removed completely from each parameter value on marshal.  A warning is logged if the parameter gets modified in any way.  These characters are:
      * ASCII control characters
      * The following characters in 2.1 vCards: `,` `.` `:` `=` `[` `]`
    * Other invalid characters are now replaced with another character.  A warning is logged if the parameter gets modified in any way.  These characters are:
      * Double quotes in 3.0/4.0 vCards are replaced with single quotes (before, they were escaped with backslashes, which is not part of the standard).
      * Newlines in 2.1/3.0 vCards are replaced with spaces (before, they were escaped with backslashes, which is not part of the standard).  Newlines continue to be escaped with backslashes in 4.0 vCards in order to support the `LABEL` parameter.
    * Fixed how double-quoted, multi-valued parameters in 3.0/4.0 are implemented.  For multi-valued parameters, double quotes were put around all the values instead of each individual value, which is incorrect according to the ABNF.
    * Added the ability to parse multi-valued `TYPE` parameters that are enclosed entirely in double quotes (e.g. `ADR;TYPE="home,work"`).  This is done to account for an error in the 4.0 specs.

# 0.7.1 #
_January 17, 2013_

  * Fixed a critical error in the way an xCard is written.  If the vCard had a PRODID property and "XCardDocument" was configured to add a PRODID property, then it would not write the vCard.

# 0.7.0 #
_January 13, 2013_

  * It's the **Usability Update!**
    * Added a method chaining interface for reading/writing vCards (`Ezvcard` class).
    * Added convenience constructors to `KeyType`, `LogoType`, `PhotoType`, and `SoundType` classes for getting the binary data from a `File` or `InputStream` object (in addition to the original `byte` array).
    * Added convenience constructors to the reader classes so that the vCards can be read from a `String`, `File`, or `InputStream` (in addition the original `Reader`).
    * Added convenience methods to the writer classes so that the vCards can be written to a `String`, `File`, or `OutputStream` (in addition the original `Writer`).
    * Added convenience methods to the `VCard` class to make it easier to add certain basic properties.  For example, to set the `FN` property, a `String` can be passed into the `setFormattedName()` method instead of a `FormattedNameType` object.
    * Added the `Iterable` interface to the `VCard` class, which allows you to iterate over each of the properties in the vCard.
    * Added the `VCard.getAllTypes()` method to get all properties in the vCard.
  * Added support for pretty-printing xCard XML documents.
  * Made xCard parsing more flexible by allowing the xCard XML element to exist anywhere inside the XML document.
  * Fixed namespace-related bugs in the xCard parsing code.
  * Fixed how newlines are encoded in 2.1 vCards: property values that contain newlines are now "quoted-printable" encoded (see [Issue 3](https://code.google.com/p/ez-vcard/issues/detail?id=3)).
  * Deprecated the `get/setCompatibilityMode()` methods on the readers/writers.

# 0.6.0 #
_December 12, 2012_

  * **Added support for the [hCard](http://microformats.org/wiki/hcard) microformat** (HTML-encoded vCards).
  * `PRODID` is now used instead of `X-GENERATOR` for marking the vCard as having been generated by ez-vcard.  This is what `PRODID` is supposed to be used for.  Since `PRODID` is not supported in 2.1, `X-PRODID` is used in 2.1 vCards.
  * Fixes to `IMPP` property class:
    * Added 3.0 to its list of supported versions.  `IMPP` was introduced in a separate specification after the 3.0 specs were released.
    * Added ICQ and Skype support.
    * Fixed the scheme string for MSN instant messenger handles.
    * Added getters for getting the IM protocol (e.g. "aim") and handle (e.g. "johndoe@aol.com").
  * Removed 2.1 from `NICKNAME`'s list of supported versions.  This property is not defined in the 2.1 specs.
  * Fixed a typo in the name of the `CALADRURI` property.
  * When unescaping an escaped newline in a plain text vCard, the local system's newline sequence is now used instead of "\r\n".
  * `GEO` coordinate values are now rounded to 6 decimals instead of 4 when marshalling (see [Issue 2](https://code.google.com/p/ez-vcard/issues/detail?id=2)).
  * Added `RevisionType.now()` helper constructor.  It generates a `REV` property whose value is the current time.
  * Renamed `XCardMarshaller` to `XCardDocument`.

# 0.5.0 #
_November 8, 2012_

  * Marshalling classes (`VCardReader`, `VCardWriter`, `XCardReader`, `XCardMarshaller`) no longer throw a checked `VCardException`.
  * ORG-DIRECTORY type class was missing the `getSupportedVersions()` method.
  * Added the ability to specify the XML element name of extended types (xCard).
  * Improved the performance of the marshaller classes.  The same `List` object is now used for collecting the marshalling warnings of each vCard type (previously, a new `List` object was created for every vCard type).
  * Refactored the way in which AGENT is marshalled to make the marshalling of embedded vCards more generic.  An `EmbeddedVCardException` is now thrown from the `AgentType` class, which the marshaller catches and then uses to marshal/unmarshal the embedded vCard.
  * Renamed `AgentType.get/setVcard()` to `AgentType.get/setVCard()` (capitalized the "C")
  * `VCard.addExtendedType(String, String)` now builds a `RawType` instead of a `TextType`.
  * Removed the `VCardReaderBuilder` class.
  * Removed the more obscure image and sound types from `ImageTypeParameter` and `SoundTypeParameter` and added a couple new ones.
  * Removed the `Protocol` enum from the `ImppType` class and added helper methods for reading/creating handles for common IM clients (e.g. AIM and Yahoo! Messenger).

# 0.4.1 #
_October 2, 2012_

  * Tweaked POM file for inclusion in Maven Central.
  * Removed the `getType()` and `setType()` methods from the `ImppType` class.  These methods were not necessary because `ImppType` inherits from `MultiValuedTypeParameterType`.

# 0.4.0 #
_September 24, 2012_

  * Added support for [RFC 6473](http://tools.ietf.org/html/rfc6473) (KIND:application)
  * Added support for [RFC 6474](http://tools.ietf.org/html/rfc6474) (BIRTHPLACE, DEATHPLACE, DEATHDATE)
  * Added support for [RFC 6715](http://tools.ietf.org/html/rfc6715) (Open Mobile Alliance Converged Address Book extensions)
  * Added convenience methods to `VCard` class for reading/writing xCards.
  * Fixed bug in the RELATED property that prevented URI values from being read.
  * Multiple instances of the BDAY and ANNIVERSARY properties can now be added to the `VCard` object.  This is necessary because these properties support the ALTID parameter.

# 0.3.0 #
_September 8, 2012_

  * Added support for [xCard](http://tools.ietf.org/html/rfc6351) (an XML representation for vCards).
  * Added the `SkipMeException`, which can be thrown from a type class's marshal or unmarshal methods.  If thrown from a marshal method, it will prevent the property from being marshalled.  If thrown from an unmarshal method, it will prevent the type object from being added to the `VCard` object.
  * Added logic to the `EMAIL` and `TEL` properties to determine whether to use the "`TYPE=pref`" or "`PREF=1`" parameter (depending on the vCard version) when marshalling.
  * Fixed the `TYPE` parameter of the `RELATED` property so that it is (1) optional and (2) multi-valued.
  * Fixed the `GEO` property so it properly marshals/unmarshals its value for 4.0 vCards.
  * Fixed the `VCard` object so it no longer is missing getters/setters for the `GENDER` property.
  * Removed the "guava" dependency.
  * Removed the "commons-io" dependency.

# 0.2.0 #
_August 11, 2012_

  * Added support for vCard 4.0
  * Removed the space that ez-vcard added after the colon in the marshalled vCard.  This prevented some mail clients from reading the vCard (see [Issue 1](https://code.google.com/p/ez-vcard/issues/detail?id=1)).
  * Values for the BEGIN, END, and PROFILE types are now in upper-case (i.e. "BEGIN:VCARD").  The 2.1/3.0 specs appear to require it to be in upper-case, so better safe than sorry (see [Issue 1](https://code.google.com/p/ez-vcard/issues/detail?id=1)).
  * Fleshed out the Javadocs.

# 0.1.1 #
_July 28, 2012_

  * Removed SLF4J dependency.
  * Modified `VCard` class to only allow one UID type in accordance with the specs.
  * Binary vCard types such as PHOTO and LOGO now use the proper ENCODING value when marshalled ("base64" for 2.1 and "b" for 3.0).
  * Renamed `DisplayableNameType` class to `SourceDisplayTextType`.
  * Renamed `OrgType` class to `OrganizationType`.

# 0.1.0 #
_July 23, 2012_

First release