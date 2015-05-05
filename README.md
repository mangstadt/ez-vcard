# ez-vcard

ez-vcard is a vCard library written in Java.  It can read and write vCards in many different formats.  The "ez" stands for "easy" because the goal is to create a library that's easy to use.

<p align="center"><strong><a href="https://github.com/mangstadt/ez-vcard/wiki/Downloads">Downloads</a> |
<a href="http://mangstadt.github.io/ez-vcard/javadocs/latest/index.html">Javadocs</a> |
<a href="#maven">Maven</a></strong></p>

```java
String str =
"BEGIN:VCARD\r\n" +
"VERSION:4.0\r\n" +
"N:Doe;Jonathan;;Mr;\r\n" +
"FN:John Doe\r\n" +
"END:VCARD\r\n";

VCard vcard = Ezvcard.parse(str).first();
String fullName = vcard.getFormattedName().getValue();
String lastName = vcard.getStructuredName().getFamily();
```

```java
VCard vcard = new VCard();

StructuredName n = new StructuredName();
n.setFamily("Doe");
n.setGiven("Jonathan");
n.addPrefix("Mr");
vcard.setStructuredName(n);

vcard.setFormattedName("John Doe");

String str = Ezvcard.write(vcard).version(VCardVersion.V4_0).go();
```

# Features

 * Simple, intuitive API (see [Examples](https://github.com/mangstadt/ez-vcard/wiki/Examples)).
 * Android compatibility (see [ez-vcard-android](https://github.com/mangstadt/ez-vcard-android) project).
 * Full compliance with 2.1, 3.0, and 4.0 specifications (see [Supported Specifications](https://github.com/mangstadt/ez-vcard/wiki/Supported-Specifications)).
 * Supports XML, HTML, and JSON encoded vCards (see [Supported Specifications](https://github.com/mangstadt/ez-vcard/wiki/Supported-Specifications)).
 * Extensive unit test coverage.
 * Low Java version requirement (1.5 or above).
 * Few dependencies on external libraries.  Dependencies can be selectively excluded based on the functionality that is needed (see [Dependencies](https://github.com/mangstadt/ez-vcard/wiki/Dependencies)).

# News

*March 21, 2015*

Due to the [impending shutdown of Google Code](http://google-opensource.blogspot.com/2015/03/farewell-to-google-code.html), ez-vcard has moved to Github!  Please bear with me as I work out the kinks. :)

*October 13, 2014*

Downloads Version 0.9.6 released.  This released fixes a few miscellaneous bugs.  Please see the changelog for details.

*August 17, 2014*

Do you use ez-vcard with Android?  The [ez-vcard-android](http://github.com/mangstadt/ez-vcard-android) project makes it easier to use ez-vcard with Android!

*July 26, 2014*

Version 0.9.5 released.  This released fixes a few miscellaneous bugs.  Please see the changelog for details.

[Old News](https://github.com/mangstadt/ez-vcard/wiki/Old-News)

# Maven

```xml
<dependency>
   <groupId>com.googlecode.ez-vcard</groupId>
   <artifactId>ez-vcard</artifactId>
   <version>0.9.6</version>
</dependency>
```

# Questions / Feedback

Questions and feedback can be posted to the [discussion forum](http://groups.google.com/group/ez-vcard-discuss).  You can also email me directly: mike(dot)angstadt(at)gmail(dot)com

Please submit bug reports and feature requests to the [issue tracker](https://github.com/mangstadt/ez-vcard/issues).  Contributors are listed in the project credits.

# Credits

**Lead Developer**  
Michael Angstadt

**Documentation**  
Michael Angstadt

**Architecture Ideas**  
George El-Haddad ([CardMe Project](https://sourceforge.net/projects/cardme/))

**Maven Central Reviewer**  
Joel Orlina

**Project Hosting**  
[Github](https://github.com)  
[Google Code](https://code.google.com)

**Contributors**  
amarnathr ([hCard template bug](https://github.com/mangstadt/ez-vcard/issues/16))  
Moritz Bechler (Geo URI bug fix)  
Kiran Kumar Bhushan (quoted-printable bug)  
Florian Brunner ([OSGi metadata](https://github.com/mangstadt/ez-vcard/issues/11))  
Pratyush Chandra ([ez-vcard-android](http://github.com/mangstadt/ez-vcard-android))  
Juliane Dombrowski ([quoted-printable line folding](https://github.com/mangstadt/ez-vcard/issues/9))  
F. Gaffron ([quoted-printable charsets](https://github.com/mangstadt/ez-vcard/issues/12))  
knutolav ([Issue 1](https://github.com/mangstadt/ez-vcard/issues/1), [Issue 2](https://github.com/mangstadt/ez-vcard/issues/2))  
David Nault ([Issue 3](https://github.com/mangstadt/ez-vcard/issues/3), [Issue 7](https://github.com/mangstadt/ez-vcard/issues/7))  
Matt Siegel ([base64 property value bug](https://github.com/mangstadt/ez-vcard/issues/21), [unit test bug](https://github.com/mangstadt/ez-vcard/issues/22))  
David Spieler ([hCard template bug](https://github.com/mangstadt/ez-vcard/issues/19))  
Tom Vogel ([quoted-printable charsets](https://github.com/mangstadt/ez-vcard/issues/10))  
Eike Weyl (Wiki fix)  
沈健 (plain-text vCard formatting issue)

**Caffeine Suppliers**  
'feine  
Starbucks  
Volo Coffeehouse

_No animals were harmed in the making of this library._

# Donate

Show you thanks by donating to this project! Thank you! :D

![https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=ez%2dvcard&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)
