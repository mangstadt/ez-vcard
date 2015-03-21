ez-vcard is a vCard library written in Java (requires 1.5 or above).  It can read and write vCards in many different formats.  The "ez" stands for "easy" because the goal is to create a library that's easy to use.

This wiki contains documentation describing how to use ez-vcard.  Use the sidebar on the left side of the page to navigate the wiki.  You can also check out the Javadocs for additional information about ez-vcard's API.

```
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

```
VCard vcard = new VCard();

StructuredName n = new StructuredName();
n.setFamily("Doe");
n.setGiven("Jonathan");
n.addPrefix("Mr");
vcard.setStructuredName(n);

vcard.setFormattedName("John Doe");

String str = Ezvcard.write(vcard).version(VCardVersion.V4_0).go();
```

# Features #

  * Simple interface for parsing/creating vCards (see [code examples](Examples.md)).
  * Full compliance with 2.1, 3.0, and 4.0 specifications ([2.1](http://www.imc.org/pdi/vcard-21.rtf), [RFC 2426](http://tools.ietf.org/html/rfc2426), [RFC 6350](http://tools.ietf.org/html/rfc6350))
  * Full compliance with xCard specification ([RFC 6351](http://tools.ietf.org/html/rfc6351))
  * Full compliance with hCard specification (http://microformats.org/wiki/hcard)
  * Full compliance with jCard draft specification ([RFC 7095](http://tools.ietf.org/html/rfc7095))
  * Supports the `AGENT` property
  * Supports circumflex accent encoding for parameter values ([RFC 6868](http://tools.ietf.org/html/rfc6868))
  * Extensive unit test coverage
  * Supports streaming of plain-text and JSON-encoded vCards
  * Android compatibility
  * Few dependencies on external libraries.  Dependencies can be selectively excluded based on the functionality that is needed (see [Dependencies](Dependencies.md)).