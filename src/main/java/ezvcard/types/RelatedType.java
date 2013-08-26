package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.RelatedTypeParameter;
import ezvcard.parameters.ValueParameter;
import ezvcard.util.JCardDataType;
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
 * Someone that the person is related to. It can contain either a URI or a plain
 * text value.
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * RelatedType related = new RelatedType();
 * related.addType(RelatedTypeParameter.FRIEND);
 * related.setUri(&quot;urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af&quot;);
 * vcard.addRelated(related);
 * 
 * related = new RelatedType();
 * related.addType(RelatedTypeParameter.CO_WORKER);
 * related.addType(RelatedTypeParameter.FRIEND);
 * related.setUri(&quot;http://joesmoe.name/vcard.vcf&quot;);
 * vcard.addRelated(related);
 * 
 * related = new RelatedType();
 * related.addType(RelatedTypeParameter.SPOUSE);
 * related.setText(&quot;Edna Smith&quot;);
 * vcard.addRelated(related);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>RELATED</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class RelatedType extends MultiValuedTypeParameterType<RelatedTypeParameter> implements HasAltId {
	public static final String NAME = "RELATED";

	private String uri;
	private String text;

	/**
	 * Creates a related property.
	 */
	public RelatedType() {
		super(NAME);
	}

	/**
	 * Gets the URI value.
	 * @return the URI value or null if no URI value is set
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the URI to an email address.
	 * @param email the email address
	 */
	public void setUriEmail(String email) {
		setUri("mailto:" + email);
	}

	/**
	 * Sets the URI to an instant messaging handle.
	 * @param protocol the IM protocol (e.g. "aim")
	 * @param handle the handle
	 */
	public void setUriIM(String protocol, String handle) {
		setUri(protocol + ":" + handle);
	}

	/**
	 * Sets the URI to a telephone number.
	 * @param telephone the telephone number
	 */
	public void setUriTelephone(String telephone) {
		setUri("tel:" + telephone);
	}

	/**
	 * Sets the URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
		text = null;
	}

	/**
	 * Gets the text value.
	 * @return the text value or null if no text value is set
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the value to free-form text instead of a URI.
	 * @param text the text
	 */
	public void setText(String text) {
		this.text = text;
		uri = null;
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
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected RelatedTypeParameter buildTypeObj(String type) {
		return RelatedTypeParameter.get(type);
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode, VCard vcard) {
		if (uri != null) {
			copy.setValue(ValueParameter.URI);
		} else if (text != null) {
			copy.setValue(ValueParameter.TEXT);
		}
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			sb.append(uri);
		} else if (text != null) {
			sb.append(VCardStringUtils.escape(text));
		} else {
			throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		if (subTypes.getValue() == ValueParameter.TEXT) {
			setText(value);
		} else {
			setUri(value);
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (uri != null) {
			parent.append(ValueParameter.URI, uri);
		} else if (text != null) {
			parent.append(ValueParameter.TEXT, text);
		} else {
			throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(ValueParameter.URI);
		if (value != null) {
			setUri(value);
		} else {
			value = element.first(ValueParameter.TEXT);
			if (value != null) {
				setText(value);
			} else {
				throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
			}
		}
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		if (uri != null) {
			return JCardValue.single(JCardDataType.URI, uri);
		} else if (text != null) {
			return JCardValue.single(JCardDataType.TEXT, text);
		} else {
			throw new SkipMeException("Property has neither a URI nor a text value associated with it.");
		}
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.getSingleValued();
		if (value.getDataType() == JCardDataType.TEXT) {
			setText(valueStr);
		} else {
			setUri(valueStr);
		}
	}
}
