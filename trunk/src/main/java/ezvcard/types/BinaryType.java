package ezvcard.types;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.MediaTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.DataUri;
import ezvcard.util.HCardElement;
import ezvcard.util.IOUtils;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;
import ezvcard.util.org.apache.commons.codec.binary.Base64;

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
 * Represents a property whose value contains binary data (for example,
 * {@link PhotoType}).
 * @author Michael Angstadt
 * 
 * @param <T> the class used for representing the content type of the resource
 */
public abstract class BinaryType<T extends MediaTypeParameter> extends VCardType implements HasAltId {
	/**
	 * The decoded data.
	 */
	protected byte[] data;

	/**
	 * The URL to the resource.
	 */
	protected String url;

	/**
	 * The content type of the resource (for example, a JPEG image).
	 */
	protected T contentType;

	/**
	 * Creates a binary property.
	 * @param name the type name (for example, "PHOTO")
	 */
	public BinaryType(String name) {
		super(name);
	}

	/**
	 * Creates a binary property.
	 * @param name the type name (e.g. "PHOTO")
	 * @param url the URL to the resource
	 * @param type the content type
	 */
	public BinaryType(String name, String url, T type) {
		this(name);
		setUrl(url, type);
	}

	/**
	 * Creates a binary property.
	 * @param name the type name (e.g. "PHOTO")
	 * @param data the binary data
	 * @param type the content type
	 */
	public BinaryType(String name, byte[] data, T type) {
		this(name);
		setData(data, type);
	}

	/**
	 * Creates a binary property.
	 * @param name the type name (e.g. "PHOTO")
	 * @param in an input stream to the binary data (will be closed)
	 * @param type the content type
	 * @throws IOException
	 */
	public BinaryType(String name, InputStream in, T type) throws IOException {
		this(name, IOUtils.toByteArray(in, true), type);
	}

	/**
	 * Creates a binary property.
	 * @param name the type name (e.g. "PHOTO")
	 * @param file the file containing the binary data
	 * @param type the content type
	 * @throws IOException
	 */
	public BinaryType(String name, File file, T type) throws IOException {
		this(name, new FileInputStream(file), type);
	}

	/**
	 * Gets the binary data of the resource.
	 * @return the binary data or null if there is none
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the binary data of the resource.
	 * @param data the binary data
	 * @param type the content type (e.g. "JPEG image")
	 */
	public void setData(byte[] data, T type) {
		this.url = null;
		this.data = data;
		setContentType(type);
	}

	/**
	 * Gets the URL to the resource
	 * @return the URL or null if there is none
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the URL to the resource.
	 * @param url the URL
	 * @param type the content type (e.g. "JPEG image")
	 */
	public void setUrl(String url, T type) {
		this.url = url;
		this.data = null;
		setContentType(type);
	}

	/**
	 * Gets the content type of the resource.
	 * @return the content type (e.g. "JPEG image")
	 */
	public T getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the resource.
	 * @param contentType the content type (e.g. "JPEG image")
	 */
	public void setContentType(T contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets the vCard 4.0 TYPE parameter. This should NOT be used to get the
	 * TYPE parameter for 2.1/3.0 vCards. Use {@link #getContentType} instead.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return subTypes.getType();
	}

	/**
	 * Sets the vCard 4.0 TYPE parameter. This should NOT be used to set the
	 * TYPE parameter for 2.1/3.0 vCards. Use {@link #setContentType} instead.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param type the TYPE value (should be either "work" or "home") or null to
	 * remove
	 */
	public void setType(String type) {
		subTypes.setType(type);
	}

	@Override
	public List<Integer[]> getPids() {
		return super.getPids();
	}

	@Override
	public void addPid(int localId, int clientPidMapRef) {
		super.addPid(localId, clientPidMapRef);
	}

	@Override
	public void removePids() {
		super.removePids();
	}

	@Override
	public Integer getPref() {
		return super.getPref();
	}

	@Override
	public void setPref(Integer pref) {
		super.setPref(pref);
	}

	//@Override
	public String getAltId() {
		return subTypes.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		subTypes.setAltId(altId);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		MediaTypeParameter contentType = this.contentType;
		if (contentType == null) {
			contentType = new MediaTypeParameter(null, null, null);
		}

		if (url != null) {
			copy.setEncoding(null);

			switch (version) {
			case V2_1:
				copy.setValue(ValueParameter.URL);
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
				break;
			case V3_0:
				copy.setValue(ValueParameter.URI);
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
				break;
			case V4_0:
				copy.setMediaType(contentType.getMediaType());
				break;
			}
		} else if (data != null) {
			copy.setMediaType(null);

			switch (version) {
			case V2_1:
				copy.setEncoding(EncodingParameter.BASE64);
				copy.setValue(null);
				copy.setType(contentType.getValue());
				break;
			case V3_0:
				copy.setEncoding(EncodingParameter.B);
				copy.setValue(null);
				copy.setType(contentType.getValue());
				break;
			case V4_0:
				copy.setEncoding(null);
				copy.setValue(ValueParameter.URI);
				//don't null out TYPE, it could be set to "home", "work", etc
				break;
			}
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		sb.append(write(version));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		parse(value, version, warnings, compatibilityMode);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		parent.append(ValueParameter.URI, write(parent.version()));
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(ValueParameter.URI);
		if (value == null) {
			throw new SkipMeException("No value found.");
		}
		parse(value, element.version(), warnings, compatibilityMode);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String elementName = element.tagName();
		if ("object".equals(elementName)) {
			String data = element.absUrl("data");
			if (data.length() > 0) {
				try {
					DataUri uri = new DataUri(data);
					T mediaType = buildMediaTypeObj(uri.getContentType());
					setData(uri.getData(), mediaType);
				} catch (IllegalArgumentException e) {
					T mediaType = null;
					String type = element.attr("type");
					if (type.length() > 0) {
						mediaType = buildMediaTypeObj(type);
					}
					setUrl(data, mediaType);
				}
			} else {
				throw new SkipMeException("<object> tag does not have a \"data\" attribute.");
			}
		} else {
			throw new SkipMeException("Cannot parse HTML tag \"" + elementName + "\".");
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		return JCardValue.single(ValueParameter.URI, write(version));
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		parse(valueStr, version, warnings, CompatibilityMode.RFC);
	}

	/**
	 * Called if the unmarshalling code cannot determine how to unmarshal the
	 * value.
	 * @param value the value
	 * @param version the version of the vCard
	 * @param warnings the warnings
	 * @param compatibilityMode the compatibility mode
	 * @param contentType the content type of the resource of null if unknown
	 */
	protected void cannotUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, T contentType) {
		switch (version) {
		case V2_1:
		case V3_0:
			if (value.startsWith("http")) {
				setUrl(value, contentType);
			} else {
				setData(Base64.decodeBase64(value), contentType);
			}
			break;
		case V4_0:
			setUrl(value, contentType);
			break;
		}
	}

	/**
	 * Builds a {@link MediaTypeParameter} object based on the information in
	 * the MEDIATYPE parameter or data URI of 4.0 vCards.
	 * @param mediaType the media type string (e.g. "image/jpeg")
	 * @return the parameter object
	 */
	protected abstract T buildMediaTypeObj(String mediaType);

	/**
	 * Builds a {@link MediaTypeParameter} object based on the value of the TYPE
	 * parameter in 2.1/3.0 vCards.
	 * @param type the TYPE value
	 * @return the parameter object
	 */
	protected abstract T buildTypeObj(String type);

	protected T parseContentType(VCardVersion version) {
		switch (version) {
		case V2_1:
		case V3_0:
			//get the TYPE parameter
			String type = subTypes.getType();
			if (type != null) {
				return buildTypeObj(type);
			}
			break;
		case V4_0:
			//get the MEDIATYPE parameter
			String mediaType = subTypes.getMediaType();
			if (mediaType != null) {
				return buildMediaTypeObj(mediaType);
			}
			break;
		}
		return null;
	}

	private void parse(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (version == null) {
			version = VCardVersion.V2_1;
		}

		T contentType = parseContentType(version);

		switch (version) {
		case V2_1:
		case V3_0:
			//parse as URL
			ValueParameter valueSubType = subTypes.getValue();
			if (valueSubType == ValueParameter.URL || valueSubType == ValueParameter.URI) {
				setUrl(value, contentType);
				return;
			}

			//parse as binary
			EncodingParameter encodingSubType = subTypes.getEncoding();
			if (encodingSubType == EncodingParameter.BASE64 || encodingSubType == EncodingParameter.B) {
				setData(Base64.decodeBase64(value), contentType);
				return;
			}

			break;
		case V4_0:
			try {
				//parse as data URI
				DataUri uri = new DataUri(value);
				contentType = buildMediaTypeObj(uri.getContentType());
				setData(uri.getData(), contentType);
				return;
			} catch (IllegalArgumentException e) {
				//not a data URI
			}
			break;
		}

		cannotUnmarshalValue(value, version, warnings, compatibilityMode, contentType);
	}

	private String write(VCardVersion version) {
		if (url != null) {
			return url;
		} else if (data != null) {
			if (version == VCardVersion.V4_0) {
				String mediaType = (contentType == null || contentType.getMediaType() == null) ? "application/octet-stream" : contentType.getMediaType();
				DataUri uri = new DataUri(mediaType, data);
				return uri.toString();
			} else {
				return new String(Base64.encodeBase64(data));
			}
		} else {
			throw new SkipMeException("Property has neither a URL nor binary data attached to it.");
		}
	}
}
