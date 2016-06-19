# ez-vcard

|     |     |
| --- | --- |
| Continuous Integration: | [![](https://travis-ci.org/mangstadt/ez-vcard.svg?branch=master)](https://travis-ci.org/mangstadt/ez-vcard) |
| Code Coverage: | [![codecov.io](http://codecov.io/github/mangstadt/ez-vcard/coverage.svg?branch=master)](http://codecov.io/github/mangstadt/ez-vcard?branch=master) |
| Maven Central: | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.ez-vcard/ez-vcard/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.ez-vcard/ez-vcard) |
| Chat Room: | [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mangstadt/ez-vcard?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) |
| License: | [![FreeBSD License](https://img.shields.io/badge/License-FreeBSD-red.svg)](https://github.com/mangstadt/ez-vcard/blob/master/LICENSE) |

ez-vcard is a vCard library written in Java.  It can read and write vCards in many different formats.  The "ez" stands for "easy" because the goal is to create a library that's easy to use.

<p align="center"><strong><a href="https://github.com/mangstadt/ez-vcard/wiki/Downloads">Downloads</a> |
<a href="http://mangstadt.github.io/ez-vcard/javadocs/latest/index.html">Javadocs</a> |
<a href="#maven">Maven</a> | <a href="https://github.com/mangstadt/ez-vcard/wiki">Documentation</a></strong></p>

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
 * Low Java version requirement (1.5 or above, 1.6 for jCards).
 * Few dependencies on external libraries.  Dependencies can be selectively excluded based on the functionality that is needed (see [Dependencies](https://github.com/mangstadt/ez-vcard/wiki/Dependencies)).

# News

**May 17, 2016**

[Version 0.9.10](https://github.com/mangstadt/ez-vcard/wiki/Downloads) released.  This release adds a number of improvements and bug fixes.  Please see the [changelog](https://github.com/mangstadt/ez-vcard/wiki/Changelog) for details.

**February 6, 2016**

[Version 0.9.9](https://github.com/mangstadt/ez-vcard/wiki/Downloads) released.  This release adds a number of improvements and bug fixes.  Please see the [changelog](https://github.com/mangstadt/ez-vcard/wiki/Changelog) for details.

**November 14, 2015**

[Version 0.9.8](https://github.com/mangstadt/ez-vcard/wiki/Downloads) released.  This release fixes a few miscellaneous bugs.  Please see the [changelog](https://github.com/mangstadt/ez-vcard/wiki/Changelog) for details.

[Old News](https://github.com/mangstadt/ez-vcard/wiki/Old-News)

# Maven

```xml
<dependency>
   <groupId>com.googlecode.ez-vcard</groupId>
   <artifactId>ez-vcard</artifactId>
   <version>0.9.10</version>
</dependency>
```

# Build Instructions

ez-vcard uses [Maven](http://maven.apache.org/) as its build tool, and adheres to its convensions.

To build the project: `mvn compile`  
To run the unit tests: `mvn test`  
To build a JAR: `mvn package`

**Eclipse users:** Due to a quirk in the build process, before running the `eclipse:eclipse` goal, you must tweak some of the `<resource>` definitions in the POM file.  See the comments in the POM file for details.

# Questions / Feedback

You have some options:

 * [Google Groups discussion forum](http://groups.google.com/group/ez-vcard-discuss)
 * [Gitter chat room](https://gitter.im/mangstadt/ez-vcard)
 * [Post a question to StackOverflow](http://stackoverflow.com/questions/ask) with `vcard` as a tag.
 * Email me directly: [mike.angstadt@gmail.com](mailto:mike.angstadt@gmail.com)

Please submit bug reports and feature requests to the [issue tracker](https://github.com/mangstadt/ez-vcard/issues).  Contributors are listed in the project credits.

If you'd like to help improve the accuracy of ez-vcard's parsing engine, please submit vCard samples using [this form](https://docs.google.com/forms/d/1SQDjDtPvEWmp-2Sl-R-iaCUbvxOdg8MmlJGdRZlWBtk/viewform?usp=send_form).  All contributions are greatly appreciated.

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
[bgorven](https://github.com/bgorven) (jackson-databind integration, jackson pretty printing, [round-trip testing](https://github.com/mangstadt/ez-vcard/pull/49))  
Moritz Bechler (Geo URI bug fix)  
Kiran Kumar Bhushan (quoted-printable bug)  
[Sean Boylan](https://github.com/seanboylan) ([XXE vulnerability](https://github.com/mangstadt/ez-vcard/issues/55))  
Florian Brunner ([OSGi metadata](https://github.com/mangstadt/ez-vcard/issues/11))  
Pratyush Chandra ([ez-vcard-android](http://github.com/mangstadt/ez-vcard-android))  
Lívio Cipriano ([Issue 35](https://github.com/mangstadt/ez-vcard/issues/35))  
[cmargenau](https://github.com/cmargenau) ([XML 1.1 support](https://github.com/mangstadt/ez-vcard/issues/29))  
[DerBlade](https://github.com/DerBlade) ([missing parameter method](https://github.com/mangstadt/ez-vcard/issues/52))  
Juliane Dombrowski ([quoted-printable line folding](https://github.com/mangstadt/ez-vcard/issues/9))  
F. Gaffron ([quoted-printable charsets](https://github.com/mangstadt/ez-vcard/issues/12))  
[isindir](https://github.com/isindir) ([Javadoc fix](https://github.com/mangstadt/ez-vcard/pull/53))  
knutolav ([Issue 1](https://github.com/mangstadt/ez-vcard/issues/1), [Issue 2](https://github.com/mangstadt/ez-vcard/issues/2))  
[Nico Lehmann](https://github.com/ekorn) ([Windows 10 Contacts compatibility issue](https://github.com/mangstadt/ez-vcard/issues/56))  
David Nault ([Issue 3](https://github.com/mangstadt/ez-vcard/issues/3), [Issue 7](https://github.com/mangstadt/ez-vcard/issues/7))  
[rfc2822](https://github.com/rfc2822) ([folding line issue](https://github.com/mangstadt/ez-vcard/issues/30), [IMPP issue](https://github.com/mangstadt/ez-vcard/issues/32), [trailing semicolons issue](https://github.com/mangstadt/ez-vcard/issues/57))  
[Steven Ruppert](https://github.com/blendmaster) ([folding surrogate character pairs](https://github.com/mangstadt/ez-vcard/pull/36), [parsing tel URIs](https://github.com/mangstadt/ez-vcard/pull/38))  
Matt Siegel ([base64 property value bug](https://github.com/mangstadt/ez-vcard/issues/21), [unit test bug](https://github.com/mangstadt/ez-vcard/issues/22))  
David Spieler ([hCard template bug](https://github.com/mangstadt/ez-vcard/issues/19))  
Tom Vogel ([quoted-printable charsets](https://github.com/mangstadt/ez-vcard/issues/10))  
Eike Weyl (Wiki fix, Javadoc fix)  
沈健 (plain-text vCard formatting issue)

**Caffeine Suppliers**  
'feine  
Starbucks  
Volo Coffeehouse

_No animals were harmed in the making of this library._

Thank you to [DAVDroid](https://davdroid.bitfire.at/) for their generous donation.

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=ez%2dvcard&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
