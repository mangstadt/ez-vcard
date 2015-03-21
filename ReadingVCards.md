

# 1 `Ezvcard` class #

For most parsing operations, the [Ezvcard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/Ezvcard.html) class can be used.  This class contains static methods that make use of method chaining, providing an elegant and user-friendly way to parse a vCard.

For example, the code below parses the first vCard from a file.

```
File file = new File("john-doe.vcf");
VCard vcard = Ezvcard.parse(file).first();
```

## 1.1 Entry methods ##

Different parsing methods can be called, depending on the format of the vCard.  Each method is overloaded to support a variety of inputs, including `String`, `File`, `InputStream`, and `Reader`.

| `Ezvcard.parse(...)` | Parses vCards encoded in the traditional, plain text format. |
|:---------------------|:-------------------------------------------------------------|
| `Ezvcard.parseXml(...)` | Parses vCards from an XML document (xCard). |
| `Ezvcard.parseHtml(...)` | Parses vCards from an HTML page (hCard). |
| `Ezvcard.parseJson(...)` | Parses vCards from a JSON stream (jCard). |

## 1.2 Chaining methods ##

The following chaining methods can be called to customize the parsing operation.  The methods which are available depend on the type of vCard being parsed.

| **Method**                        | **Text** | **XML** | **HTML** | **JSON** | **Description** |
|:----------------------------------|:---------|:--------|:---------|:---------|:----------------|
| `caretDecoding(boolean)`        | x      |       |        |        | Sets whether parameter values should be decoded using circumflex accent encoding (enabled by default).  See the [javadocs](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/text/VCardReader.html#setCaretDecodingEnabled(boolean)) for a description of this encoding mechanism. |
| `pageUrl(String)`               |        |       | x     |         | Sets the URL of the webpage being parsed.  This is used to resolve relative links within the HTML page and to set the SOURCE property on the vCard.  Calling this method has no effect if a `URL` object was passed into the `Ezvcard.parseHtml()` method. |
| `register(VCardPropertyScribe)` | x      | x     | x     | x       | Registers a property scribe.  See [ExtendedProperties](ExtendedProperties.md) for more information on scribes. |
| `warnings(List<List<String>>)`  | x      | x     | x     | x       | Allows the user to retrieve the warnings that were generated during the parsing operation.  Warnings are generated if the vCard deviates from the standard in some way and/or ez-vcard has to make a guess as to how to parse the vCard. The warnings will be added to the `List` object that was passed into this method. This `List` object is a "list of lists" because the data stream may contain more than one vCard and each parsed vCard generates its own list of warnings. |

## 1.3 Termination methods ##

The chaining operation terminates when one of the following methods is called.  These methods are what execute the actual parsing operation and returns the parsed [VCard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/VCard.html) object(s).

| **Method**  | **Description** |
|:------------|:----------------|
| `all()`   | Parses all of the vCards from the stream, returning a `List<VCard>`. |
| `first()` | Parses only the first vCard from the stream, returning a single [VCard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/VCard.html) object (or `null` if the stream did not contain any vCards).  Most vCard files only contain a single vCard, so this method acts as a convenient alternative to `all()` in that the programmer does not have to retrieve the first element from a list. |

# 2 Reader classes #

For additional control over the parsing operation, the following classes can be used.  These classes are what what [Ezvcard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/Ezvcard.html) calls under the hood.

| **Class** | **Description** | **Streaming?** |
|:----------|:----------------|:---------------|
| [VCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/text/VCardReader.html) | Parses vCards encoded in the traditional, plain text format. | yes |
| [XCardDocument](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/xml/XCardDocument.html) | Parses vCards from an XML document (xCard). | no |
| [XCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/xml/XCardReader.html) | Parses vCards from an XML document (xCard). | yes |
| [HCardParser](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/html/HCardParser.html) | Parses vCards from an HTML page (hCard). | no |
| [JCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/json/JCardReader.html) | Parses vCards from a JSON stream (jCard). | yes |

## 2.1 `VCardReader` ##

[VCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/text/VCardReader.html) is used to read vCards that are encoded in the traditional, plain text format.  Use the `readNext()` method to read the next vCard from the input stream.

The example below reads the vCards from a file.

```
File file = new File("vcards.vcf");
VCardReader reader = new VCardReader(file);
VCard vcard = null;
while ((vcard = reader.readNext()) != null){
  ...
}
reader.close();
```

## 2.2 `XCardDocument` ##

[XCardDocument](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/xml/XCardReader.html) is used to read vCards from an XML document (xCard standard).  Use the `parseFirst()` or `parseAll()` methods to read the vCards from the XML document.

The example below reads the vCards from an XML file.

```
File file = new File("vcards.xml");
XCardDocument document = new XCardDocument(file);
List<VCard> vcards = document.parseAll();
```

## 2.3 `XCardReader` ##

[XCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/xml/XCardReader.html) is used to read vCards from an XML document (xCard standard) in a streaming fashion.  Use the `readNext()` method to read the next vCard from the input stream.

The example below reads the vCards from a file.

```
File file = new File("vcards.xml");
XCardReader reader = new XCardReader(file);
VCard vcard = null;
while ((vcard = reader.readNext()) != null){
  ...
}
reader.close();
```

## 2.4 `HCardParser` ##

[HCardParser](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/html/HCardParser.html) is used to read vCards from an HTML document (hCard standard).  Use the `parseFirst()` or `parseAll()` methods to read the vCards from the HTML document.

The example below reads the vCards that are embedded inside a website.

```
URL url = new URL("http://microformats.org/wiki/hcard");
HCardParser parser = new HCardParser(url);
List<VCard> vcards = parser.parseAll();
```

If your application does not require hCard parsing functionality, you can exclude the "jsoup" dependency from your build.

```
<dependency>
  <groupId>com.googlecode.ez-vcard</groupId>
  <artifactId>ez-vcard</artifactId>
  <version>...</version>
  <exclusions>
    <!-- hCard parsing not needed -->
    <exclusion>
      <groupId>org.jsoup</groupId>
      <artifactId>jsoup</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

## 2.5 `JCardReader` ##

[JCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/json/JCardReader.html) is used to read vCards that are encoded in JSON (jCard standard).  Use the `readNext()` method to read the next vCard from the input stream.

The example below reads the vCards from a file.

```
File file = new File("vcards.json");
JCardReader reader = new JCardReader(file);
VCard vcard = null;
while ((vcard = reader.readNext()) != null){
  ...
}
reader.close();
```

If your application does not require jCard parsing functionality, you can exclude the "jackson-core" dependency from your build.

```
<dependency>
  <groupId>com.googlecode.ez-vcard</groupId>
  <artifactId>ez-vcard</artifactId>
  <version>...</version>
  <exclusions>
    <!-- jCard parsing not needed -->
    <exclusion>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

# 3 Differences between `Ezvcard` and reader classes #

Most of the functionality in the reader classes can be accessed from the [Ezvcard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/Ezvcard.html) class.  However, there is one exception.

## 3.1 Streaming ##

The [VCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/text/VCardReader.html), [XCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/text/XCardReader.html), and [JCardReader](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/json/JCardReader.html) classes are streaming--they parse the vCard data as it is read off the wire.  This makes them useful for parsing large data streams.  [Ezvcard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/Ezvcard.html), by contrast, consumes more memory because it stores all of the parsed [VCard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/VCard.html) objects in memory at once.

The code sample below shows how the streaming API works.  Each [VCard](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/VCard.html) object is read one at a time, allowing them to be garbage collected once they have been processed and the next has been read.

```
Reader reader = ...
VCardReader vcardReader = new VCardReader(reader);
VCard vcard = null;
for ((vcard = vcardReader.readNext()) != null) {
  //do something with the VCard before reading the next one
}
vcardReader.close();
```

[XCardDocument](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/xml/XCardDocument.html) and [HCardParser](https://ez-vcard.googlecode.com/svn/javadocs/latest/ezvcard/io/html/HCardParser.html) are **not** streaming.  These classes read the entire XML/HTML document into memory before parsing out the vCard data.