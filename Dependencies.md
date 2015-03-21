ez-vcard uses the following dependencies.  They can be excluded, depending on the kind of vCard formats your application requires.

| **Name** | **Group ID** | **Artifact ID** | **Version** | **Required?** | **Description** |
|:---------|:-------------|:----------------|:------------|:--------------|:----------------|
| [jsoup](http://jsoup.org) | org.jsoup | jsoup | 1.7.3 | for hCard parsing | HTML-parsing library used for parsing hCards. |
| [FreeMarker](http://freemarker.org) | org.freemarker | freemarker | 2.3.19 | for hCard writing | Templating library used for creating HTML pages that contain hCards. |
| [Jackson](http://jackson.codehaus.org) | com.fasterxml.jackson.core | jackson-core | 2.3.3 | for jCard reading/writing | JSON processor for jCards. |
| [Apache Commons Codec](http://commons.apache.org/codec/) | commons-codec | commons-codec | 1.6 | _embedded_ | Selected portions of this library's source code have been inserted directly into the ez-vcard code base in order to resolve an Android compatibility issue (see [this discussion](https://groups.google.com/forum/?fromgroups=#!topic/ez-vcard-discuss/w2TK7yetwr8)). |

Maven-enabled projects can exclude dependencies like so:

```
<dependency>
  <groupId>com.googlecode.ez-vcard</groupId>
  <artifactId>ez-vcard</artifactId>
  <version>...</version>
  <exclusions>

    <!-- hCard functionality not needed -->
    <exclusion>
      <groupId>org.jsoup</groupId>
      <artifactId>jsoup</artifactId>
    </exclusion>
    <exclusion>
      <groupId>org.freemarker</groupId>
      <artifactId>freemarker</artifactId>
    </exclusion>

    <!-- jCard functionality not needed -->
    <exclusion>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
    </exclusion>

  </exclusions>
</dependency>
```