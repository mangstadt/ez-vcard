/**
 * A library that reads and writes vCards, supporting all versions of the vCard standard (2.1, 3.0,
 * and 4.0) as well as xCard (XML-encoded vCards), hCard (HTML-encoded vCards), and jCard
 * (JSON-encoded vCards).
 */
module com.googlecode.ezvcard {
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
	
	opens ezvcard.io.html to freemarker;
	opens ezvcard.io.scribe to freemarker;
	opens ezvcard.parameter to freemarker;
	opens ezvcard.property to freemarker;
	opens ezvcard.util to freemarker;
	
	opens ezvcard.io.json to com.fasterxml.jackson.databind;
	
	requires vinnie;
	requires com.fasterxml.jackson.databind;
	requires org.jsoup;
	requires java.xml;
	requires freemarker;
}