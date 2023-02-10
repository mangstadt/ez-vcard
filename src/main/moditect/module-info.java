open module ezvcard {
  requires vinnie;
  requires java.xml;
  requires freemarker;
  requires org.jsoup;
  requires com.fasterxml.jackson.core;
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