package ezvcard.types;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
 * @param <T> the class used for wrapping the value of the TYPE parameter
 */
public abstract class BinaryType<T extends MediaTypeParameter> extends SingleValuedTypeParameterType<T> {
	/**
	 * Regex for parsing a data URI.
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
	 * @param name the type name
	 */
	public BinaryType(String name) {
		super(name);
	}

	/**
	 * @param name the type name
	 * @param url the URL to the resource
	 * @param type the content type
	 */
	public BinaryType(String name, String url, T type) {
		this(name);
		setUrl(url, type);
	}

	/**
	 * @param name the type name
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
	 */
	protected void setData(byte[] data) {
		this.url = null;
		this.data = data;
	}

	/**
	 * Sets the binary data of the resource.
	 * @param data the binary data
	 * @param type the content type
	 */
	public void setData(byte[] data, T type) {
		setData(data);
		setType(type);
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
	 */
	protected void setUrl(String url) {
		this.url = url;
		this.data = null;
	}

	/**
	 * Sets the URL to the resource.
	 * @param url the URL
	 * @param type the content type
	 */
	public void setUrl(String url, T type) {
		setUrl(url);
		setType(type);
	}
	
	//TODO in 2.1/3.0, TYPE is used to store the content type, but in 4.0, it's used to store "work" or "home"

	@Override
	public void setType(T type) {
		super.setType(type);
		subTypes.setMediaType(type.getMediaType());
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
	protected VCardSubTypes doMarshalSubTypes(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		VCardSubTypes copy = new VCardSubTypes(subTypes);

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
				copy.setType(null);
			} else {
				copy.setMediaType(null);
			}
		}
		if (data != null) {
			copy.setMediaType(null);
			if (version == VCardVersion.V2_1) {
				copy.setEncoding(EncodingParameter.BASE64);
				copy.setValue(null);
				copy.setMediaType(null);
			} else if (version == VCardVersion.V3_0) {
				copy.setEncoding(EncodingParameter.B);
				copy.setValue(null);
				copy.setMediaType(null);
			} else {
				copy.setEncoding(null);
				copy.setValue(ValueParameter.URI);
				copy.setType(null);
				copy.setMediaType(null);
			}
		}

		return copy;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (url != null) {
			return url;
		}
		if (data != null) {
			String base64 = new String(Base64.encodeBase64(data));
			if (version == VCardVersion.V4_0) {
				MediaTypeParameter p = getType();
				String mediaType = (p == null || p.getMediaType() == null) ? "application/octet-stream" : p.getMediaType();
				return "data:" + mediaType + ";base64," + base64;
			} else {
				return base64;
			}
		}
		return null;
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//check for MEDIATYPE parameter
		//4.0 URLs should have this
		String mediaType = subTypes.getMediaType();
		if (mediaType != null) {
			setType(buildMediaTypeObj(mediaType));
		} else {
			//check for TYPE parameter and set the MEDIATYPE parameter so they're synced
			T type = getType();
			if (type != null) {
				subTypes.setMediaType(type.getMediaType());
			}
		}

		//check for a data URI (v4.0)
		Matcher m = DATA_URI.matcher(value);
		if (m.find()) {
			mediaType = m.group(1);
			String base64 = m.group(2);

			T param = buildMediaTypeObj(mediaType);
			setData(Base64.decodeBase64(base64), param);
			return;
		}

		//check for a URL
		ValueParameter valueSubType = subTypes.getValue();
		if (valueSubType == ValueParameter.URL || valueSubType == ValueParameter.URI) {
			setUrl(value);
			return;
		}

		//check for base64 data
		EncodingParameter encodingSubType = subTypes.getEncoding();
		if (encodingSubType != null) {
			if (encodingSubType != EncodingParameter.B && encodingSubType != EncodingParameter.BASE64) {
				warnings.add("Unrecognized ENCODING type \"" + encodingSubType + "\".  Attempting to decode as base64.");
			}
			setData(Base64.decodeBase64(value));
			return;
		}

		//check the value for a URL
		if (value.matches("(?i)http.*")) {
			setUrl(value);
			return;
		}

		cannotUnmarshalValue(value, version, warnings, compatibilityMode);
	}

	/**
	 * Called if the unmarshalling code cannot determine how to unmarshal the
	 * value.
	 * @param value the value
	 * @param version the version of the vCard
	 * @param warnings the warnings
	 * @param compatibilityMode the compatibility mode
	 */
	protected void cannotUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		warnings.add("No VALUE or ENCODING type given.  Attempting to decode as base64.");
		setData(Base64.decodeBase64(value));
	}

	/**
	 * Gets a {@link MediaTypeParameter} object based on the media type string
	 * in the data URI of 4.0 vCards.
	 * @param mediaType the media type string (e.g. "image/jpeg")
	 * @return the parameter object
	 */
	protected abstract T buildMediaTypeObj(String mediaType);
}
