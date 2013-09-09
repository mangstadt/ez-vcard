package ezvcard.types;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.KeyTypeParameter;
import ezvcard.util.DataUri;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * A public key for encryption.
 * 
 * <p>
 * <b>Adding a key</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //URL (vCard 4.0 only; KEYs cannot have URLs in vCard 2.1 and 3.0)
 * KeyType key = new KeyType("http://www.mywebsite.com/pubkey.pgp", KeyTypeParameter.PGP);
 * vcard.addKey(key);
 * 
 * //binary data
 * byte data[] = ...
 * key = new KeyType(data, KeyTypeParameter.PGP);
 * vcard.addKey(key);
 * 
 * //plain text value
 * key = new KeyType();
 * key.setText("...", KeyTypeParameter.PGP);
 * vcard.addKey(key);
 * 
 * //if "KeyTypeParameter" does not have the pre-defined constant that you need, then create a new instance
 * //arg 1: the value of the 2.1/3.0 TYPE parameter
 * //arg 2: the value to use for the 4.0 MEDIATYPE parameter and for 4.0 data URIs
 * //arg 3: the file extension of the data type (optional)
 * KeyTypeParameter param = new KeyTypeParameter("mykey", "application/my-key", "mkey");
 * key = new KeyType("http://www.mywebsite.com/pubkey.enc", param);
 * vcard.addKey(key);
 * </pre>
 * 
 * <p>
 * <b>Getting the keys</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * 
 * int fileCount = 0;
 * for (KeyType key : vcard.getKeys()){
 *   //the key will have either a URL or a binary data
 *   //only 4.0 vCards are allowed to use URLs for keys
 *   if (key.getData() == null){
 *     System.out.println("Key URL: " + key.getUrl());
 *   } else {
 *     KeyTypeParameter type = key.getContentType();
 *     
 *     if (type == null) {
 *       //the vCard may not have any content type data associated with the key
 *       System.out.println("Saving a key file...");
 *     } else {
 *       System.out.println("Saving a \"" + type.getMediaType() + "\" file...");
 *     }
 *     
 *     String folder;
 *     if (type == KeyTypeParameter.PGP){ //it is safe to use "==" instead of "equals()"
 *       folder = "pgp-keys";
 *     } else {
 *       folder = "other-keys";
 *     }
 *     
 *     byte data[] = key.getData();
 *     String filename = "key" + fileCount;
 *     if (type != null && type.getExtension() != null){
 *     	filename += "." + type.getExtension();
 *     }
 *     OutputStream out = new FileOutputStream(new File(folder, filename));
 *     out.write(data);
 *     out.close();
 *     fileCount++;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code KEY}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class KeyType extends BinaryType<KeyTypeParameter> {
	public static final String NAME = "KEY";

	private String text;

	/**
	 * Creates an empty key property.
	 */
	public KeyType() {
		super(NAME);
	}

	/**
	 * Creates a key property.
	 * @param data the binary data
	 * @param type the type of key (e.g. PGP)
	 */
	public KeyType(byte data[], KeyTypeParameter type) {
		super(NAME, data, type);
	}

	/**
	 * Creates a key property.
	 * @param url the URL to the key (vCard 4.0 only)
	 * @param type the type of key (e.g. PGP)
	 */
	public KeyType(String url, KeyTypeParameter type) {
		super(NAME, url, type);
	}

	/**
	 * Creates a key property.
	 * @param in an input stream to the binary data (will be closed)
	 * @param type the content type (e.g. PGP)
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public KeyType(InputStream in, KeyTypeParameter type) throws IOException {
		super(NAME, in, type);
	}

	/**
	 * Creates a key property.
	 * @param file the key file
	 * @param type the content type (e.g. PGP)
	 * @throws IOException if there's a problem reading from the file
	 */
	public KeyType(File file, KeyTypeParameter type) throws IOException {
		super(NAME, file, type);
	}

	/**
	 * Sets a plain text representation of the key.
	 * @param text the key in plain text
	 * @param type the key type
	 */
	public void setText(String text, KeyTypeParameter type) {
		this.text = text;
		data = null;
		url = null;
		setContentType(type);
	}

	/**
	 * Gets the plain text representation of the key.
	 * @return the key in plain text
	 */
	public String getText() {
		return text;
	}

	@Override
	protected KeyTypeParameter buildTypeObj(String type) {
		return KeyTypeParameter.get(type, null, null);
	}

	@Override
	protected KeyTypeParameter buildMediaTypeObj(String mediaType) {
		return KeyTypeParameter.get(null, mediaType, null);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, CompatibilityMode compatibilityMode, VCard vcard) {
		if (text == null) {
			super.doMarshalSubTypes(copy, version, compatibilityMode, vcard);
			return;
		}

		copy.setValue(VCardDataType.TEXT);
		copy.setEncoding(null);

		switch (version) {
		case V2_1:
		case V3_0:
			copy.setType((contentType == null) ? null : contentType.getValue());
			copy.setMediaType(null);
			break;
		case V4_0:
			//don't null out TYPE, it could be set to "home", "work", etc
			copy.setMediaType((contentType == null) ? null : contentType.getMediaType());
			break;
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (text == null) {
			super.doMarshalText(sb, version, compatibilityMode);
			return;
		}
		sb.append(VCardStringUtils.escape(text));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (subTypes.getValue() != VCardDataType.TEXT) {
			super.doUnmarshalText(value, version, warnings, compatibilityMode);
			return;
		}
		parseText(VCardStringUtils.unescape(value), version);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (text == null) {
			super.doMarshalXml(parent, compatibilityMode);
			return;
		}
		parent.append(VCardDataType.TEXT, text);
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String text = element.first(VCardDataType.TEXT);
		if (text != null) {
			parseText(text, element.version());
			return;
		}

		String uri = element.first(VCardDataType.URI);
		if (uri != null) {
			try {
				//parse as data URI
				DataUri dataUri = new DataUri(uri);
				KeyTypeParameter contentType = buildMediaTypeObj(dataUri.getContentType());
				setData(dataUri.getData(), contentType);
			} catch (IllegalArgumentException e) {
				//parse as URL
				KeyTypeParameter contentType = parseContentType(element.version());
				setUrl(uri, contentType);
			}
			return;
		}

		throw missingXmlElements(VCardDataType.TEXT, VCardDataType.URI);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if (!"a".equals(elementName)) {
			super.doUnmarshalHtml(element, warnings);
			return;
		}

		String href = element.absUrl("href");
		if (href.length() == 0) {
			throw new CannotParseException("<a> tag does not have a \"href\" attribute.");
		}

		try {
			DataUri uri = new DataUri(href);
			KeyTypeParameter mediaType = buildMediaTypeObj(uri.getContentType());
			setData(uri.getData(), mediaType);
		} catch (IllegalArgumentException e) {
			//not a data URI
			//TODO create buildTypeObjFromExtension() method
			setUrl(href, null);
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		if (text == null) {
			return super.doMarshalJson(version);
		}
		return JCardValue.single(VCardDataType.TEXT, text);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		if (value.getDataType() == VCardDataType.TEXT) {
			parseText(valueStr, version);
			return;
		}

		try {
			//parse as data URI
			DataUri dataUri = new DataUri(valueStr);
			KeyTypeParameter contentType = buildMediaTypeObj(dataUri.getContentType());
			setData(dataUri.getData(), contentType);
		} catch (IllegalArgumentException e) {
			//parse as URL
			KeyTypeParameter contentType = parseContentType(version);
			setUrl(valueStr, contentType);
		}
	}

	@Override
	protected void cannotUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, KeyTypeParameter contentType) {
		switch (version) {
		case V2_1:
		case V3_0:
			if (value.startsWith("http")) {
				setUrl(value, contentType);
			} else {
				setText(value, contentType);
			}
			break;
		case V4_0:
			if (subTypes.getValue() == VCardDataType.URI) {
				setUrl(value, contentType);
			} else {
				setText(value, contentType);
			}
			break;
		}
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (url == null && data == null && text == null) {
			warnings.add("Property has no value attached to it.");
		}

		if (url != null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
			warnings.add("URL values are not permitted in version " + version.getVersion() + ".");
		}
	}

	private void parseText(String value, VCardVersion version) {
		setText(value, parseContentType(version));
	}
}
