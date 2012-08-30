package ezvcard.types;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.MediaTypeParameter;
import ezvcard.parameters.ValueParameter;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * Represents a Type that contains binary data (for example, the "PHOTO" type).
 * @author Michael Angstadt
 * 
 * @param <T> the class used for representing the content type of the resource
 */
public abstract class BinaryType<T extends MediaTypeParameter> extends VCardType {
	/**
	 * Regex for parsing a 4.0 data URI.
	 */
	private static final Pattern DATA_URI = Pattern.compile("^data:(.*?);base64,(.*)", Pattern.CASE_INSENSITIVE);

	/**
	 * The decoded data.
	 */
	protected byte[] data;

	/**
	 * The URL to the resource.
	 */
	protected String url;

	/**
	 * The content type of the resource (e.g. "JPEG image").
	 */
	protected T contentType;

	/**
	 * @param name the type name (e.g. "PHOTO")
	 */
	public BinaryType(String name) {
		super(name);
	}

	/**
	 * @param name the type name (e.g. "PHOTO")
	 * @param url the URL to the resource
	 * @param type the content type
	 */
	public BinaryType(String name, String url, T type) {
		this(name);
		setUrl(url, type);
	}

	/**
	 * @param name the type name (e.g. "PHOTO")
	 * @param data the binary data
	 * @param type the content type
	 */
	public BinaryType(String name, byte[] data, T type) {
		this(name);
		setData(data, type);
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
	 * vCard versions: 4.0
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
	 * vCard versions: 4.0
	 * </p>
	 * @param type the TYPE value (should be either "work" or "home") or null to
	 * remove
	 */
	public void setType(String type) {
		subTypes.setType(type);
	}

	/**
	 * Gets all PID parameter values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the PID values or empty set if there are none
	 * @see VCardSubTypes#getPids
	 */
	public Set<Integer[]> getPids() {
		return subTypes.getPids();
	}

	/**
	 * Adds a PID value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param localId the local ID
	 * @param clientPidMapRef the ID used to reference the property's globally
	 * unique identifier in the CLIENTPIDMAP property.
	 * @see VCardSubTypes#addPid(int, int)
	 */
	public void addPid(int localId, int clientPidMapRef) {
		subTypes.addPid(localId, clientPidMapRef);
	}

	/**
	 * Removes all PID values.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @see VCardSubTypes#removePids
	 */
	public void removePids() {
		subTypes.removePids();
	}

	/**
	 * Gets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the preference value or null if it doesn't exist
	 * @see VCardSubTypes#getPref
	 */
	public Integer getPref() {
		return subTypes.getPref();
	}

	/**
	 * Sets the preference value.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param pref the preference value or null to remove
	 * @see VCardSubTypes#setPref
	 */
	public void setPref(Integer pref) {
		subTypes.setPref(pref);
	}

	/**
	 * Gets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the ALTID or null if it doesn't exist
	 * @see VCardSubTypes#getAltId
	 */
	public String getAltId() {
		return subTypes.getAltId();
	}

	/**
	 * Sets the ALTID.
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param altId the ALTID or null to remove
	 * @see VCardSubTypes#setAltId
	 */
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
			ValueParameter vp = null;
			switch (version) {
			case V2_1:
				vp = ValueParameter.URL;
				break;
			case V3_0:
			case V4_0:
				vp = ValueParameter.URI;
				break;
			}
			copy.setValue(vp);

			copy.setEncoding(null);

			if (version == VCardVersion.V4_0) {
				//don't null out TYPE, it could be set to "home" or "work"
				copy.setMediaType(contentType.getMediaType());
			} else {
				copy.setType(contentType.getValue());
				copy.setMediaType(null);
			}
		}
		if (data != null) {
			copy.setMediaType(null);
			if (version == VCardVersion.V2_1) {
				copy.setEncoding(EncodingParameter.BASE64);
				copy.setValue(null);
				copy.setType(contentType.getValue());
			} else if (version == VCardVersion.V3_0) {
				copy.setEncoding(EncodingParameter.B);
				copy.setValue(null);
				copy.setType(contentType.getValue());
			} else {
				copy.setEncoding(null);
				copy.setValue(ValueParameter.URI);
				//don't null out TYPE, it could be set to "home" or "work"
			}
		}
	}

	@Override
	protected void doMarshalValue(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		if (url != null) {
			sb.append(url);
		} else if (data != null) {
			String base64 = new String(Base64.encodeBase64(data));
			if (version == VCardVersion.V4_0) {
				String mediaType = (contentType == null || contentType.getMediaType() == null) ? "application/octet-stream" : contentType.getMediaType();
				sb.append("data:" + mediaType + ";base64," + base64);
			} else {
				sb.append(base64);
			}
		} else {
			throw new SkipMeException("Property has neither a URL nor binary data attached to it.");
		}
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//check for a 4.0 data URI
		Matcher m = DATA_URI.matcher(value);
		if (m.find()) {
			String mediaType = m.group(1);
			String base64 = m.group(2);

			T contentType = buildMediaTypeObj(mediaType);
			setData(Base64.decodeBase64(base64), contentType);
			return;
		}

		//get content type from 4.0 MEDIATYPE parameter
		T contentType = null;
		String mediaType = subTypes.getMediaType();
		if (mediaType != null) {
			contentType = buildMediaTypeObj(mediaType);
		} else {
			//get content type from 2.1/3.0 TYPE parameter
			String type = subTypes.getType();
			if (type != null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
				contentType = buildTypeObj(getType());
			}
		}

		//check for a URL
		ValueParameter valueSubType = subTypes.getValue();
		if (valueSubType == ValueParameter.URL || valueSubType == ValueParameter.URI) {
			setUrl(value, contentType);
			return;
		}

		//check for 2.1/3.0 base64 data
		EncodingParameter encodingSubType = subTypes.getEncoding();
		if (encodingSubType != null) {
			if (encodingSubType != EncodingParameter.B && encodingSubType != EncodingParameter.BASE64) {
				warnings.add("Unrecognized " + EncodingParameter.NAME + " parameter value \"" + encodingSubType + "\" in " + getTypeName() + " property.  Attempting to decode as base64.");
			}
			setData(Base64.decodeBase64(value), contentType);
			return;
		}

		//see if the value is a URL incase they didn't set the VALUE parameter
		if (value.matches("(?i)http.*")) {
			setUrl(value, contentType);
			return;
		}

		cannotUnmarshalValue(value, version, warnings, compatibilityMode, contentType);
	}

	@Override
	protected void doUnmarshalValue(Element element, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		String value = parseChildElement(element, "uri", warnings);
		if (value != null) {
			doUnmarshalValue(value, version, warnings, compatibilityMode);
		}
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
		warnings.add("No " + ValueParameter.NAME + " or " + EncodingParameter.NAME + " parameter given.  Attempting to decode as base64.");
		setData(Base64.decodeBase64(value), contentType);
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
}
