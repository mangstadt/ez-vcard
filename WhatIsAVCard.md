vCards can be described as "electronic business cards".  They are typically used for encoding contact information about an individual, such as their name, address, and phone number.   Most email clients and address book applications provide at least basic support for vCards.  This makes vCard an ideal choice for transferring contacts between applications or for sharing contacts with others.  The latest vCard specification is defined in [RFC6350](http://tools.ietf.org/html/rfc6350).

vCards can also be used to represent entities other than individuals, such as groups and organizations.  However, they are most commonly used to represent individuals.

The vCard data model consists of a collection of **properties**.  Each property has (1) a **name**, (2) a list of **parameters** (key/value attributes), (3) a **value**, and (4) an optional **group** name.

The text below is an example of a simple vCard.  Each line in the text is a _property_.  The property _name_ is located at the beginning of the line.  If the property belongs to a _group_, then the line begins with the group name, followed by a dot, followed by the property name.  The _parameters_ are located in between the property name and the colon character.  The _value_ is located after the colon character.  The vCard below contains the person's name (in both formatted and structured formats) as well as the person's home email address.

```
BEGIN:VCARD
VERSION:4.0
N:Doe;John;;Mr;
FN:Mr. John Doe
group1.EMAIL;TYPE=home:jdoe@example.com
END:VCARD
```

Multiple vCards can exist inside of the same vCard data stream.  The text below contains two vCards.

```
BEGIN:VCARD
VERSION:4.0
FN:Mr. John Doe
END:VCARD
BEGIN:VCARD
VERSION:4.0
FN:Mrs. Jane Doe
END:VCARD
```

vCard files end in a ".vcf" extension.

vCards can also be encoded in XML ("[xCard](http://tools.ietf.org/html/rfc6351)") and JSON ("[jCard](http://tools.ietf.org/html/rfc7095)") formats.

**XML**
```
<vcards xmlns="urn:ietf:params:xml:ns:vcard-4.0">
  <vcard>
    <n>
      <surname>Doe</surname>
      <given>John</given>
      <prefix>Mr</prefix>
    </n>
    <fn>
      <text>Mr. John Doe</text>
    </fn>
    <group name="group1">
      <email>
        <parameters>
          <type><text>home</text></type>
        </parameters>
        <text>jdoe@example.com</text>
      </email>
    </group>
  </vcard>
</vcards>
```

**JSON**
```
["vcard",
  [
    ["version", {}, "text", "4.0"],
    ["n", {}, "text", ["Doe", "John", "", "Mr", ""]],
    ["fn", {}, "text", "Mr. John Doe"],
    ["email", {"group":"group1", "type":"home"}, "text", "jdoe@example.com"]
  ]
]
```

They can be embedded inside of HTML pages as well ("[hCard](http://microformats.org/wiki/hcard)").

**HTML**
```
<html>
  <head>
    <link rel="profile" href="http://microformats.org/profile/hcard" />
  </head>
  <body>
    <div class="vcard">
      <h1 class="fn n">
        <span class="prefix">Mr</span>. 
        <span class="given-name">John</span> 
        <span class="family-name">Doe</span>
      </h1>
      
      Email:
      <a class="email" href="mailto:jdoe@example.com">
      	<span class="value">jdoe@example.com</span>
      	(<span class="type">home</span>)
      </a>
    </div>
  </body>
</html>
```