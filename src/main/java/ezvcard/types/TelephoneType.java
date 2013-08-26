package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardDataType;
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
 * <pre>
 * VCard vcard = new VCard();
 * TelephoneType tel = new TelephoneType(&quot;(123) 555-6789&quot;);
 * tel.addType(TelephoneTypeParameter.HOME);
 * tel.setPref(2); //the second-most preferred
 * vcard.addTelephoneNumber(tel);
 * 
 * TelUri uri = TelUri.global(&quot;+1-800-555-9876&quot;);
 * uri.setExtension(&quot;111&quot;);
 * tel = new TelephoneType(uri);
 * tel.addType(TelephoneTypeParameter.WORK);
 * tel.setPref(1); //the most preferred
 * vcard.addTelephoneNumber(tel);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>TEL</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @return the "tel" URI or null if it is not set
	 */
	public TelUri getUri() {
		return uri;
	}

	/**
	 * Sets a "tel" URI representing the phone number.
	 * <p>
	 * <b>Supported versions:</b> <code>4.0</code>
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
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		if (version == VCardVersion.V4_0 && uri != null) {
			copy.setValue(ValueParameter.URI);
		} else {
			copy.setValue(null);
		}

		//replace "TYPE=pref" with "PREF=1"
		if (version == VCardVersion.V4_0) {
			if (getTypes().contains(TelephoneTypeParameter.PREF)) {
				copy.removeType(TelephoneTypeParameter.PREF.getValue());
				copy.setPref(1);
			}
		} else {
			copy.setPref(null);

			//find the TEL with the lowest PREF value in the vCard
			TelephoneType mostPreferred = null;
			for (TelephoneType tel : vcard.getTelephoneNumbers()) {
				Integer pref = tel.getPref();
				if (pref != null) {
					if (mostPreferred == null || pref < mostPreferred.getPref()) {
						mostPreferred = tel;
					}
				}
			}
			if (this == mostPreferred) {
				copy.addType(TelephoneTypeParameter.PREF.getValue());
			}
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			if (version == VCardVersion.V4_0) {
				sb.append(uri.toString());
			} else {
				warnings.add("Tel URIs are not supported by vCard version " + version + ".  The URI will be converted to a string.  Some data may be lost.");

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

		throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		if (subTypes.getValue() == ValueParameter.URI) {
			try {
				setUri(TelUri.parse(value));
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
				setText(value);
			}
		} else {
			setText(value);
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			parent.uri(uri.toString());
		} else if (text != null) {
			parent.text(text);
		} else {
			throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.uri();
		if (value != null) {
			try {
				setUri(TelUri.parse(value));
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
				setText(value);
			}
			return;
		}

		value = element.text();
		if (value != null) {
			setText(value);
			return;
		}

		throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		List<String> types = element.types();
		for (String type : types) {
			subTypes.addType(type);
		}

		String href = element.attr("href");
		if (href.length() > 0) {
			try {
				setUri(TelUri.parse(href));
				return;
			} catch (IllegalArgumentException e) {
				//not a tel URI
			}
		}
		setText(element.value());
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		if (uri != null) {
			return JCardValue.single(JCardDataType.URI, uri.toString());
		}
		if (text != null) {
			return JCardValue.single(JCardDataType.TEXT, text);
		}
		throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		if (value.getDataType() == JCardDataType.URI) {
			try {
				setUri(TelUri.parse(valueStr));
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse property value as a URI.  Assuming it's text.");
				setText(valueStr);
			}
		} else {
			setText(valueStr);
		}
	}
}