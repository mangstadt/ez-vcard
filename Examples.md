

# 1 Reading #

_See [Reading vCards](ReadingVCards.md) page for more info._

## Example 1.1: Reading from a plain-text vCard ##

```
String text =
"BEGIN:vcard\r\n" +
"VERSION:3.0\r\n" +
"N:House;Gregory;;Dr;MD\r\n" +
"FN:Dr. Gregory House M.D.\r\n" +
"END:vcard\r\n";

VCard vcard = Ezvcard.parse(text).first();
```

## Example 1.2: Reading from an XML document (xCard) ##

```
String xml =
"<vcards xmlns=\"urn:ietf:params:xml:ns:vcard-4.0\">" +
  "<vcard>" +
    "<n>" +
      "<surname>House</surname>" +
      "<given>Gregory</given>" +
      "<prefix>Dr</prefix>" +
      "<suffix>MD</suffix>" +
    "</n>" +
    "<fn><text>Dr. Gregory House M.D.</text></fn>" +
  "</vcard>" +
"</vcards>";

VCard vcard = Ezvcard.parseXml(xml).first();
```

## Example 1.3: Reading from an HTML document (hCard) ##

```
String html =
"<html>" +
  "<head><link rel=\"profile\" href=\"http://microformats.org/profile/hcard\" /></head>" +
  "<body>" +
    "<div class=\"vcard\">" +
      "<h1 class=\"fn\">Dr. Gregory House M.D.</h1>" +
      "<div class=\"n\">" +
        "<span class=\"prefix\">Dr</span> " + 
        "<span class=\"given-name\">Gregory</span> " + 
        "<span class=\"family-name\">House</span> " +
        "<span class=\"suffix\">MD</span>" +
      "</div>" +
    "</div>" +
  "</body>" +
"</html>";

VCard vcard = Ezvcard.parseHtml(html).first();
```

## Example 1.4: Reading from a JSON string (jCard) ##

```
String json =
"[\"vcard\"," +
  "[" +
    "[\"version\", {}, \"text\", \"4.0\"]," +
    "[\"n\", {}, \"text\", [\"House\", \"Gregory\", \"\", \"Dr\", \"MD\"]]," +
    "[\"fn\", {}, \"text\", \"Dr. Gregory House M.D.\"]" +
  "]" +
"]";

VCard vcard = Ezvcard.parseJson(json).first();
```

## Example 1.5: Reading from a file ##

```
File file = new File("vcard.vcf");
VCard vcard = Ezvcard.parse(file).first();
```

## Example 1.6: Reading from a `Reader` ##

```
Reader reader = ...
VCard vcard = Ezvcard.parse(reader).first();
```

## Example 1.7: Reading multiple vCards from the same stream ##

```
Reader reader = ...
List<VCard> vcards = Ezvcard.parse(reader).all();
```

# 2 Writing #

_See [Writing vCards](WritingVCards.md) page for more info._

## Example 2.1: Writing a plain-text vCard ##

```
VCard vcard = new VCard();

StructuredName n = new StructuredName();
n.setFamily("House");
n.setGiven("Gregory");
n.addPrefix("Dr");
n.addSuffix("MD");
vcard.setStructuredName(n);

vcard.setFormattedName("Dr. Gregory House M.D.");

String text = Ezvcard.write(vcard).version(VCardVersion.V3_0).go();
```

## Example 2.2: Writing to an XML document (xCard) ##

```
VCard vcard = ...
String xml = Ezvcard.writeXml(vcard).go();
```

## Example 2.3: Writing to an HTML document (hCard) ##

```
VCard vcard = ...
String html = Ezvcard.writeHtml(vcard).go();
```

## Example 2.4: Writing to a JSON string (jCard) ##

```
VCard vcard = ...
String json = Ezvcard.writeJson(vcard).go();
```

## Example 2.5: Writing to a file ##

```
VCard vcard = ...
File file = new File("vcard.vcf");
Ezvcard.write(vcard).go(file);
```

## Example 2.6: Writing to a `Writer` ##

```
VCard vcard = ...
Writer writer = ...
Ezvcard.write(vcard).go(writer);
writer.close();
```

## Example 2.7: Writing multiple vCards to the same stream ##

```
Collection<VCard> vcards = ...
Writer writer = ...
Ezvcard.write(vcards).go(writer);
```

# 3 Extended (non-standard) properties #

_See [Extended Properties](ExtendedProperties.md) page for more info._

## Example 3.1: Getting an extended  property ##

```
VCard vcard = ...
List<RawProperty> managers = vcard.getExtendedProperty("X-MS-MANAGER");
for (RawProperty manager : managers){
  System.out.println(manager.getValue());
}
```

## Example 3.2: Setting an extended property ##

```
VCard vcard = ...
vcard.addExtendedProperty("X-MS-MANAGER", "Michael Scott");
```

# 4 Extended (non-standard) parameters #

## Example 4.1: Getting an extended parameter ##

```
VCard vcard = ...
Geo geo = vcard.getGeo();
List<String> vacation = geo.getParameters("X-VACATION");
```

## Example 4.2: Setting an extended parameter ##
```
Geo geo = new Geo(21.306944,-157.858333);
geo.addParameter("X-VACATION", "true");
```

# 5 Misc #

## Example 5.1: Converting a vCard from one version to another ##
```
File fromFile = new File("vcard-2.1.vcf");
File toFile = new File("vcard-4.0.vcf");

VCard vcard = Ezvcard.parse(fromFile).first();
Ezvcard.write(vcard).version(VCardVersion.V4_0).go(toFile);
```

## Example 5.2: Creating alternative representations (ALTID parameter) ##

Properties that support alternative representations have special methods in the `VCard` class that allow you to add groups of alternative representation property instances.  These methods end in "Alt" and accept a collection of property objects.  An appropriate `ALTID` parameter value is automatically generated and assigned to the properties.

```
VCard vcard = new VCard();

Note note1 = new Note("Hello world!");
note1.setLanguage("en");

Note note2 = new Note("Bonjour tout le monde!");
note2.setLanguage("fr");

Note note3 = new Note("Hallo Welt!");
note3.setLanguage("de");

vcard.addNoteAlt(note1, note2, note3);
```

## Example 5.3: Creating a vCard ##

The code below generates a complete vCard, encoding it in plain text, XML, HTML, and JSON formats.

The program also validates the vCard twice before writing it--once under version 3.0 and once under version 4.0.  When validated under version 3.0, three warnings will be returned, which point out that the KIND, GENDER, and LANG properties are not supported by vCard version 3.0.  The program also validates the vCard under version 4.0, since this is the version that is used when writing to XML and JSON.  No warnings are generated when validated under version 4.0.

```
import java.io.*;
import java.util.*;

import ezvcard.*;
import ezvcard.parameter.*;
import ezvcard.property.*;

public class JohnDoeVCard {
  public static void main(String[] args) throws Throwable {
    VCard vcard = createVCard();

    //validate vCard for version 3.0
    System.out.println("Version 3.0 validation warnings:");
    System.out.println(vcard.validate(VCardVersion.V3_0));
    System.out.println();

    //validate vCard for version 4.0 (xCard and jCard use this version)
    System.out.println("Version 4.0 validation warnings:");
    System.out.println(vcard.validate(VCardVersion.V4_0));

    //write vCard
    File file = new File("john-doe.vcf");
    System.out.println("Writing " + file.getName() + "...");
    Ezvcard.write(vcard).version(VCardVersion.V3_0).go(file);

    //write xCard
    file = new File("john-doe.xml");
    System.out.println("Writing " + file.getName() + "...");
    Ezvcard.writeXml(vcard).indent(2).go(file);

    //write hCard
    file = new File("john-doe.html");
    System.out.println("Writing " + file.getName() + "...");
    Ezvcard.writeHtml(vcard).go(file);

    //write jCard
    file = new File("john-doe.json");
    System.out.println("Writing " + file.getName() + "...");
    Ezvcard.writeJson(vcard).go(file);
  }

  private static VCard createVCard() throws IOException {
    VCard vcard = new VCard();

    vcard.setKind(Kind.individual());

    vcard.setGender(Gender.male());

    vcard.addLanguage("en-US");

    StructuredName n = new StructuredName();
    n.setFamily("Doe");
    n.setGiven("Jonathan");
    n.addPrefix("Mr");
    vcard.setStructuredName(n);

    vcard.setFormattedName("Jonathan Doe");

    vcard.setNickname("John", "Jonny");

    vcard.addTitle("Widget Engineer");

    vcard.setOrganization("Acme Co. Ltd.", "Widget Department");

    Address adr = new Address();
    adr.setStreetAddress("123 Wall St.");
    adr.setLocality("New York");
    adr.setRegion("NY");
    adr.setPostalCode("12345");
    adr.setCountry("USA");
    adr.setLabel("123 Wall St.\nNew York, NY 12345\nUSA");
    adr.addType(AddressType.WORK);
    vcard.addAddress(adr);

    adr = new Address();
    adr.setStreetAddress("123 Main St.");
    adr.setLocality("Albany");
    adr.setRegion("NY");
    adr.setPostalCode("54321");
    adr.setCountry("USA");
    adr.setLabel("123 Main St.\nAlbany, NY 54321\nUSA");
    adr.addType(AddressType.HOME);
    vcard.addAddress(adr);

    vcard.addTelephoneNumber("1-555-555-1234", TelephoneType.WORK);
    vcard.addTelephoneNumber("1-555-555-5678", TelephoneType.WORK, TelephoneType.CELL);

    vcard.addEmail("johndoe@hotmail.com", EmailType.HOME);
    vcard.addEmail("doe.john@acme.com", EmailType.WORK);

    vcard.addUrl("http://www.acme-co.com");

    vcard.setCategories("widgetphile", "biker", "vCard expert");

    vcard.setGeo(37.6, -95.67);

    java.util.TimeZone tz = java.util.TimeZone.getTimeZone("America/New_York");
    vcard.setTimezone(new Timezone(tz));

    File file = new File("portrait.jpg");
    Photo photo = new Photo(file, ImageType.JPEG);
    vcard.addPhoto(photo);

    file = new File("pronunciation.ogg");
    Sound sound = new Sound(file, SoundType.OGG);
    vcard.addSound(sound);

    vcard.setUid(Uid.random());

    vcard.setRevision(Revision.now());

    return vcard;
  }
}
```
## Example 5.4: Validating a VCard object ##

```
VCard vcard = new VCard();
vcard.setGender(Gender.male());
System.out.println(vcard.validate(VCardVersion.V3_0));

//outputs the following:
//A structured name property must be defined.
//A formatted name property must be defined.
//[Gender]: Property is not supported by version 3.0.  Supported versions are: [4.0]
```VCard vcard = new VCard();
vcard.setGender(Gender.male());
System.out.println(vcard.validate(VCardVersion.V3_0));

//outputs the following:
//A structured name property must be defined.
//A formatted name property must be defined.
//[Gender]: Property is not supported by version 3.0.  Supported versions are: [4.0]
}}}```