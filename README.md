# ez-vcard

ez-vcard is a vCard library written in Java (*requires 1.5 or above*).  It can read and write vCards in many different formats.  The "ez" stands for "easy" because the goal is to create a library that's easy to use.

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

 * Simple, intuitive API (see code examples).
 * [Android compatibility library](http://github.com/mangstadt/ez-vcard-android)
 * Full compliance with 2.1, 3.0, and 4.0 specifications ([2.1](http://www.imc.org/pdi/vcard-21.rtf), [RFC 2426](http://tools.ietf.org/html/rfc2426), [RFC 6350](http://tools.ietf.org/html/rfc6350))
 * Full compliance with xCard specification ([RFC 6351](http://tools.ietf.org/html/rfc6351))
 * Full compliance with hCard specification (http://microformats.org/wiki/hcard)
 * Full compliance with jCard specification ([RFC 7095](http://tools.ietf.org/html/rfc7095))
 * Supports circumflex accent encoding for parameter values ([RFC 6868](http://tools.ietf.org/html/rfc6868))
 * Extensive unit test coverage
 * Few dependencies on external libraries.  Dependencies can be selectively excluded based on the functionality that is needed (see Dependencies).

# News

*March 21, 2015*

Due to the [impending shutdown of Google Code](http://google-opensource.blogspot.com/2015/03/farewell-to-google-code.html), ez-vcard has moved to Github!  Please bear with me as I work out the kinks. :)

*October 13, 2014*

Downloads Version 0.9.6 released.  This released fixes a few miscellaneous bugs.  Please see the changelog for details.

*August 17, 2014*

Do you use ez-vcard with Android?  The [ez-vcard-android](http://github.com/mangstadt/ez-vcard-android) project makes it easier to use ez-vcard with Android!

*July 26, 2014*

Version 0.9.5 released.  This released fixes a few miscellaneous bugs.  Please see the changelog for details.

# Maven

```xml
<dependency>
   <groupId>com.googlecode.ez-vcard</groupId>
   <artifactId>ez-vcard</artifactId>
   <version>0.9.6</version>
</dependency>
```

# Supported specifications

  * [vCard 2.1](http://www.imc.org/pdi/vcard-21.rtf)
  * [RFC 2426](http://tools.ietf.org/html/rfc2426) (vCard 3.0)
  * [RFC 6350](http://tools.ietf.org/html/rfc6350) (vCard 4.0)
  * [RFC 6351](http://tools.ietf.org/html/rfc6351) (xCard)
  * [hCard 1.0](http://microformats.org/wiki/hcard)
  * [RFC 7095](http://tools.ietf.org/html/rfc7095) (jCard)
  * [RFC 6473](http://tools.ietf.org/html/rfc6473) (KIND:application)
  * [RFC 6869](http://tools.ietf.org/html/rfc6869) (KIND:device)
  * [RFC 6474](http://tools.ietf.org/html/rfc6474) (BIRTHPLACE, DEATHPLACE, DEATHDATE)
  * [RFC 6715](http://tools.ietf.org/html/rfc6715) (Open Mobile Alliance Converged Address Book extensions)
  * [RFC 6868](http://tools.ietf.org/html/rfc6868) (circumflex accent encoding)

# Questions / Feedback

Questions and feedback can be posted to the [discussion forum](http://groups.google.com/group/ez-vcard-discuss).  You can also email me directly: mike(dot)angstadt(at)gmail(dot)com

Please submit bug reports and feature requests to the [issue tracker](https://github.com/mangstadt/ez-vcard/issues).  Contributors are listed in the project credits.

# Credits

<table border="0">
<tr><th align="left" valign="top">Lead Developer:</th><td>Michael Angstadt</td></tr>
<tr><th align="left" valign="top">Documentation:</th><td>Michael Angstadt</td></tr>
<tr><th align="left" valign="top">Contributors:</th><td>David Nault (x2)</td></tr>
<tr><th></th><td>knutolav (x2)</td></tr>
<tr><th></th><td>Kiran Kumar Bhushan</td></tr>
<tr><th></th><td>Moritz Bechler</td></tr>
<tr><th></th><td>JulianeDombrowski</td></tr>
<tr><th></th><td>Florian Brunner</td></tr>
<tr><th></th><td>Tom Vogel</td></tr>
<tr><th></th><td>F. Gaffron</td></tr>
<tr><th></th><td>Pratyush Chandra (<a href="http://github.com/mangstadt/ez-vcard-android">Android compatibility</a>)</td></tr>
<tr><th></th><td><a href="http://stackoverflow.com/users/2736496/aliteralmind">aliteralmind</a></td></tr>
<tr><th></th><td>沈健</td></tr>
<tr><th></th><td>David Spieler</td></tr>
<tr><th></th><td>Matt Siegel</td></tr>
<tr><th></th><td>Eike Weyl (wiki fix)</td></tr>
<tr><th align="left" valign="top">Architecture Ideas:</th><td>George El-Haddad (<a href="https://sourceforge.net/projects/cardme/">CardMe Project</a>)</td></tr>
<tr><th align="left" valign="top">Maven Central Reviewer:</th><td>Joel Orlina</td></tr>
<tr><th align="left" valign="top">Project Hosting:</th><td>Google Code</td></tr>
<tr><th></th><td>Github</td></tr>
<tr><th align="left" valign="top">Caffeine Suppliers:</th><td>Starbucks</td></tr>
<tr><th></th><td>Volo Coffeehouse</td></tr>
<tr><th></th><td>'feine</td></tr>
</table>

_No animals were harmed in the making of this library. ;-)_

# Donate

Please consider donating a dollar or two to help pay for my coffee! :)  Thank you.

[Donate](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=ez%2dvcard&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
