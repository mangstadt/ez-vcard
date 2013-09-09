package ezvcard.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.VCardStringUtils.JoinCallback;
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
 * Contains the separated components of the person's name.
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * StructuredNameType n = new StructuredNameType();
 * n.setFamily(&quot;House&quot;);
 * n.setGiven(&quot;Gregory&quot;);
 * n.addPrefix(&quot;Dr&quot;);
 * n.addSuffix(&quot;MD&quot;);
 * vcard.setStructuredName(n);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code N}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class StructuredNameType extends VCardType implements HasAltId {
	public static final String NAME = "N";

	private String family;
	private String given;
	private List<String> additional = new ArrayList<String>();
	private List<String> prefixes = new ArrayList<String>();
	private List<String> suffixes = new ArrayList<String>();

	/**
	 * Creates a structured name property.
	 */
	public StructuredNameType() {
		super(NAME);
	}

	/**
	 * Gets the family name (aka "last name").
	 * @return the family name or null if not set
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Sets the family name (aka "last name").
	 * @param family the family name or null to remove
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * Gets the given name (aka "first name").
	 * @return the given name or null if not set
	 */
	public String getGiven() {
		return given;
	}

	/**
	 * Sets the given name (aka "first name").
	 * @param given the given name or null to remove
	 */
	public void setGiven(String given) {
		this.given = given;
	}

	/**
	 * Gets any additional names the person goes by.
	 * @return the additional names or empty list if there are none
	 */
	public List<String> getAdditional() {
		return additional;
	}

	/**
	 * Adds an additional name the person goes by.
	 * @param additional the additional name to add
	 */
	public void addAdditional(String additional) {
		this.additional.add(additional);
	}

	/**
	 * Gets the prefixes.
	 * @return the prefixes (e.g. "Mr.") or empty list if there are none
	 */
	public List<String> getPrefixes() {
		return prefixes;
	}

	/**
	 * Adds a prefix.
	 * @param prefix the prefix to add (e.g. "Mr.")
	 */
	public void addPrefix(String prefix) {
		this.prefixes.add(prefix);
	}

	/**
	 * Gets the suffixes.
	 * @return the suffixes (e.g. "Jr.") or empty list if there are none
	 */
	public List<String> getSuffixes() {
		return suffixes;
	}

	/**
	 * Adds a suffix.
	 * @param suffix the suffix to add (e.g. "Jr.")
	 */
	public void addSuffix(String suffix) {
		this.suffixes.add(suffix);
	}

	/**
	 * Gets the string(s) that define how to sort the vCard.
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortStringType SORT-STRING}
	 * property instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the sort string(s) (e.g. ["Aboville", "Christine"] if the family
	 * name is "d'Aboville" and the given name is "Christine") or empty list if
	 * there are none
	 * @see VCardSubTypes#getSortAs
	 */
	public List<String> getSortAs() {
		return subTypes.getSortAs();
	}

	/**
	 * Sets the string that defines how to sort the vCard.
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortStringType SORT-STRING}
	 * property instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param family the sorttable family name (e.g. "Adboville" if the family
	 * name is "d'Aboville") or null to remove
	 */
	public void setSortAs(String family) {
		if (family == null) {
			subTypes.setSortAs();
		} else {
			subTypes.setSortAs(family);
		}
	}

	/**
	 * Sets the strings that define how to sort the vCard.
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortStringType SORT-STRING}
	 * property instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param family the sortable family name (e.g. "Adboville" if the family
	 * name is "d'Aboville")
	 * @param given the sortable given name
	 */
	public void setSortAs(String family, String given) {
		subTypes.setSortAs(family, given);
	}

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
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
	protected void doMarshalText(final StringBuilder value, VCardVersion version, CompatibilityMode compatibilityMode) {
		List<List<String>> values = new ArrayList<List<String>>();
		values.add(Arrays.asList(family));
		values.add(Arrays.asList(given));
		values.add(additional);
		values.add(prefixes);
		values.add(suffixes);

		VCardStringUtils.join(values, ";", value, new JoinCallback<List<String>>() {
			public void handle(StringBuilder sb, List<String> v) {
				VCardStringUtils.join(v, ",", value, new JoinCallback<String>() {
					public void handle(StringBuilder sb, String v) {
						if (v == null) {
							return;
						}
						sb.append(VCardStringUtils.escape(v));
					}
				});
			}
		});
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//preserve empty items and don't unescape escaped characters (e.g. "additional" might have escaped commas)
		Iterator<String> it = VCardStringUtils.splitBy(value, ';', false, false).iterator();

		family = nextTextComponent(it);
		given = nextTextComponent(it);
		additional = nextTextListComponent(it);
		prefixes = nextTextListComponent(it);
		suffixes = nextTextListComponent(it);
	}

	private String nextTextComponent(Iterator<String> it) {
		if (!it.hasNext()) {
			return null;
		}

		String value = it.next();
		return (value.length() == 0) ? null : VCardStringUtils.unescape(value);
	}

	private List<String> nextTextListComponent(Iterator<String> it) {
		if (!it.hasNext()) {
			return new ArrayList<String>(0);
		}

		String value = it.next();
		return VCardStringUtils.splitBy(value, ',', true, true);
	}

	@Override
	protected void doMarshalXml(XCardElement parent, CompatibilityMode compatibilityMode) {
		parent.append("surname", family); //the XML element still needs to be printed if value == null
		parent.append("given", given);
		parent.append("additional", additional);
		parent.append("prefix", prefixes);
		parent.append("suffix", suffixes);
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		family = sanitizeXml(element, "surname");
		given = sanitizeXml(element, "given");
		additional = element.all("additional");
		prefixes = element.all("prefix");
		suffixes = element.all("suffix");
	}

	private String sanitizeXml(XCardElement element, String name) {
		String value = element.first(name);
		return (value != null && value.length() == 0) ? null : value;
	}

	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		family = element.firstValue("family-name");
		given = element.firstValue("given-name");
		additional = element.allValues("additional-name");
		prefixes = element.allValues("honorific-prefix");
		suffixes = element.allValues("honorific-suffix");
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		return JCardValue.structured(VCardDataType.TEXT, family, given, additional, prefixes, suffixes);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		Iterator<List<String>> it = value.getStructured().iterator();

		family = nextJsonComponent(it);
		given = nextJsonComponent(it);
		additional = nextJsonListComponent(it);
		prefixes = nextJsonListComponent(it);
		suffixes = nextJsonListComponent(it);
	}

	private String nextJsonComponent(Iterator<List<String>> it) {
		List<String> values = nextJsonListComponent(it);
		return values.isEmpty() ? null : values.get(0);
	}

	private List<String> nextJsonListComponent(Iterator<List<String>> it) {
		if (!it.hasNext()) {
			return new ArrayList<String>(0);
		}

		List<String> values = it.next();
		List<String> nonEmpty = new ArrayList<String>(values.size());
		for (String valueStr : values) {
			if (valueStr.length() == 0) {
				continue;
			}
			nonEmpty.add(valueStr);
		}
		return nonEmpty;
	}
}