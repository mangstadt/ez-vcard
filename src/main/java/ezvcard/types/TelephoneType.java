package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.TelUri;
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
 * A telephone number.
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * TelephoneType tel = new TelephoneType(&quot;(123) 555-6789&quot;);
 * tel.addType(TelephoneTypeParameter.HOME);
 * tel.setPref(2); //the second-most preferred
 * vcard.addTelephoneNumber(tel);
 * 
 * TelUri uri = new TelUri.Builder(&quot;+1-800-555-9876&quot;).extension(&quot;111&quot;).build();
 * tel = new TelephoneType(uri);
 * tel.addType(TelephoneTypeParameter.WORK);
 * tel.setPref(1); //the most preferred
 * vcard.addTelephoneNumber(tel);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code TEL}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class TelephoneType extends MultiValuedTypeParameterType<TelephoneTypeParameter> implements HasAltId {
	public static final String NAME = "TEL";

	private String text;
	private TelUri uri;

	/**
	 * Creates an empty telephone property.
	 */
	public TelephoneType() {
		super(NAME);
	}

	/**
	 * Creates a telephone property.
	 * @param text the telephone number (e.g. "(123) 555-6789")
	 */
	public TelephoneType(String text) {
		this();
		setText(text);
	}

	/**
	 * Creates a telephone property.
	 * @param uri a "tel" URI representing the telephone number (vCard 4.0 only)
	 */
	public TelephoneType(TelUri uri) {
		this();
		setUri(uri);
	}

	/**
	 * Gets the telephone number as a text value.
	 * @return the telephone number or null if the text value is not set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the telephone number as a text value.
	 * @param text the telephone number
	 */
	public void setText(String text) {
		this.text = text;
		uri = null;
	}

	/**
	 * Gets a "tel" URI representing the phone number.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the "tel" URI or null if it is not set
	 */
	public TelUri getUri() {
		return uri;
	}

	/**
	 * Sets a "tel" URI representing the phone number.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param uri the "tel" URI
	 */
	public void setUri(TelUri uri) {
		text = null;
		this.uri = uri;
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
	protected TelephoneTypeParameter buildTypeObj(String type) {
		return TelephoneTypeParameter.get(type);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, CompatibilityMode compatibilityMode, VCard vcard) {
		switch (version) {
		case V2_1:
		case V3_0:
			copy.setValue(null);
			copy.setPref(null);

			//find the TEL with the lowest PREF value in the vCard
			TelephoneType mostPreferred = null;
			for (TelephoneType tel : vcard.getTelephoneNumbers()) {
				Integer pref = tel.getPref();
				if (pref == null) {
					continue;
				}

				if (mostPreferred == null || pref < mostPreferred.getPref()) {
					mostPreferred = tel;
				}
			}
			if (this == mostPreferred) {
				copy.addType(TelephoneTypeParameter.PREF.getValue());
			}

			break;
		case V4_0:
			VCardDataType dataType = (uri == null) ? null : VCardDataType.URI;
			copy.setValue(dataType);

			//replace "TYPE=pref" with "PREF=1"
			if (getTypes().contains(TelephoneTypeParameter.PREF)) {
				copy.removeType(TelephoneTypeParameter.PREF.getValue());
				copy.setPref(1);
			}

			break;
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			if (version == VCardVersion.V4_0) {
				sb.append(uri.toString());
			} else {
				sb.append(VCardStringUtils.escape(uri.getNumber()));

				String ext = uri.getExtension();
				if (ext != null) {
					sb.append(" x").append(ext);
				}
			}
			return;
		}

		if (text != null) {
			sb.append(VCardStringUtils.escape(text));
			return;
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);

		if (subTypes.getValue() == VCardDataType.URI) {
			try {
				setUri(TelUri.parse(value));
				return;
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
			}
		}

		setText(value);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}
		if (uri != null) {
			parent.append(VCardDataType.URI, uri.toString());
			return;
		}
		parent.append(VCardDataType.TEXT, "");
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(VCardDataType.URI);
		if (value != null) {
			try {
				setUri(TelUri.parse(value));
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
				setText(value);
			}
			return;
		}

		value = element.first(VCardDataType.TEXT);
		if (value != null) {
			setText(value);
			return;
		}

		throw missingXmlElements(VCardDataType.URI, VCardDataType.TEXT);
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		List<String> types = element.types();
		for (String type : types) {
			subTypes.addType(type);
		}

		String href = element.attr("href");
		try {
			setUri(TelUri.parse(href));
		} catch (IllegalArgumentException e) {
			//not a tel URI
			setText(element.value());
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		if (text != null) {
			return JCardValue.single(VCardDataType.TEXT, text);
		}
		if (uri != null) {
			return JCardValue.single(VCardDataType.URI, uri.toString());
		}
		return JCardValue.single(VCardDataType.TEXT, "");
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		if (value.getDataType() == VCardDataType.URI) {
			try {
				setUri(TelUri.parse(valueStr));
				return;
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
			}
		}

		setText(valueStr);
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (uri == null && text == null) {
			warnings.add("Property has neither a URI nor a text value associated with it.");
		}

		if (uri != null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
			warnings.add("\"tel\" URIs are not supported by vCard version " + version.getVersion() + ".  The URI will be converted to a string.  Some data may be lost.");
		}

		for (TelephoneTypeParameter type : getTypes()) {
			if (type == TelephoneTypeParameter.PREF) {
				//ignore because it is converted to a PREF parameter for 4.0 vCards
				continue;
			}

			if (!type.isSupported(version)) {
				warnings.add("Type value \"" + type.getValue() + "\" is not supported in version " + version.getVersion() + ".");
			}
		}
	}
}