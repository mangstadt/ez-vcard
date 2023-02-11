open module ezvcard {
  requires vinnie;
  requires com.fasterxml.jackson.core;
  requires static com.fasterxml.jackson.databind;
  requires static java.xml;
  requires static org.jsoup;
  requires static freemarker;
  exports ezvcard;
  exports ezvcard.io;
  exports ezvcard.io.chain;
  exports ezvcard.io.html;
  exports ezvcard.io.json;
  exports ezvcard.io.scribe;
  exports ezvcard.io.text;
  exports ezvcard.io.xml;
  exports ezvcard.parameter;
  exports ezvcard.property;
  exports ezvcard.util;
  exports ezvcard.util.org.apache.commons.codec;
  exports ezvcard.util.org.apache.commons.codec.binary;
}