package ezvcard.types;

import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
 * Defines the location of the person's birth.
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //URI (geo)
 * BirthplaceType birthplace = new BirthplaceType();
 * birthplace.setUri(&quot;geo:39.970806,-75.174809&quot;);
 * vcard.setBirthplace(birthplace);
 * 
 * //URI (website)
 * birthplace = new BirthplaceType();
 * birthplace.setUri(&quot;http://www.chop.edu&quot;);
 * vcard.setBirthplace(birthplace);
 * 
 * //text
 * birthplace = new BirthplaceType();
 * birthplace.setText(&quot;The Children's Hospital of Philadelphia&quot;);
 * vcard.setBirthplace(birthplace);
 * 
 * //text
 * birthplace = new BirthplaceType();
 * birthplace.setText(&quot;Philadelphia, PA&quot;);
 * vcard.setBirthplace(birthplace);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code BIRTHPLACE}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
 */
public class BirthplaceType extends VCardType implements HasAltId {
	public static final String NAME = "BIRTHPLACE";

	private String uri;
	private String text;

	/**
	 * Creates a birthplace property.
	 */
	public BirthplaceType() {
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
	 * Sets the value to a URI.
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
	 * Sets the value to free-form text.
	 * @param text the text
	 */
	public void setText(String text) {
		this.text = text;
		uri = null;
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
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, CompatibilityMode compatibilityMode, VCard vcard) {
		copy.setValue((uri == null) ? null : VCardDataType.URI);
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (text != null) {
			sb.append(VCardStringUtils.escape(text));
			return;
		}
		if (uri != null) {
			sb.append(uri);
			return;
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);

		VCardDataType dataType = subTypes.getValue();
		if (dataType == VCardDataType.URI) {
			setUri(value);
		} else {
			setText(value);
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		if (text != null) {
			parent.append(VCardDataType.TEXT, text);
			return;
		}
		if (uri != null) {
			parent.append(VCardDataType.URI, uri);
			return;
		}
		parent.append(VCardDataType.TEXT, "");
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		String value = element.first(VCardDataType.TEXT);
		if (value != null) {
			setText(value);
			return;
		}

		value = element.first(VCardDataType.URI);
		if (value != null) {
			setUri(value);
			return;
		}

		throw missingXmlElements(VCardDataType.TEXT, VCardDataType.URI);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		if (text != null) {
			return JCardValue.single(VCardDataType.TEXT, text);
		}
		if (uri != null) {
			return JCardValue.single(VCardDataType.URI, uri);
		}
		return JCardValue.single(VCardDataType.TEXT, "");
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		String valueStr = value.asSingle();
		VCardDataType dataType = value.getDataType();
		if (dataType == VCardDataType.URI) {
			setUri(valueStr);
		} else {
			setText(valueStr);
		}
	}

	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (uri == null && text == null) {
			warnings.add("Property has neither a URI nor a text value associated with it.");
		}
	}
}
