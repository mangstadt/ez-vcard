package ezvcard.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
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
 * <p>
 * Contains the separated components of the person's name.
 * </p>
 * 
 * <p>
 * Multiple instances of this type can be added ONLY if each instance has an
 * ALTID parameter and the value of the ALTID parameter is the same across all
 * instances. However, this is a border-case; under most circumstances, you will
 * only need to add one instance.
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
 * <b>Property name:</b> <code>N</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
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
	 * <b>Supported versions:</b> <code>4.0</code>
	 * </p>
	 * @param family the sortable family name (e.g. "Adboville" if the family
	 * name is "d'Aboville")
	 * @param given the sortable given name
	 */
	public void setSortAs(String family, String given) {
		subTypes.setSortAs(family, given);
	}

	/**
	 * Gets the language the name is written in.
	 * @return the language or null if not set
	 * @see VCardSubTypes#getLanguage
	 */
	public String getLanguage() {
		return subTypes.getLanguage();
	}

	/**
	 * Sets the language the name is written in.
	 * @param language the language or null to remove
	 * @see VCardSubTypes#setLanguage
	 */
	public void setLanguage(String language) {
		subTypes.setLanguage(language);
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
	protected void doMarshalText(StringBuilder value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (family != null) {
			value.append(VCardStringUtils.escape(family));
		}

		value.append(';');
		if (given != null) {
			value.append(VCardStringUtils.escape(given));
		}

		value.append(';');
		if (!additional.isEmpty()) {
			for (String s : additional) {
				value.append(VCardStringUtils.escape(s)).append(',');
			}
			value.deleteCharAt(value.length() - 1);
		}

		value.append(';');
		if (!prefixes.isEmpty()) {
			for (String s : prefixes) {
				value.append(VCardStringUtils.escape(s)).append(',');
			}
			value.deleteCharAt(value.length() - 1);
		}

		value.append(';');
		if (!suffixes.isEmpty()) {
			for (String s : suffixes) {
				value.append(VCardStringUtils.escape(s)).append(',');
			}
			value.deleteCharAt(value.length() - 1);
		}
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//preserve empty items and don't unescape escaped characters(e.g. "additional" might have escaped commas)
		String split[] = VCardStringUtils.splitBy(value, ';', false, false);

		int i = 0;

		family = (split.length > i && split[i].length() > 0) ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		given = (split.length > i && split[i].length() > 0) ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		if (split.length > i && split[i].length() > 0) {
			additional = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		} else {
			additional = new ArrayList<String>();
		}
		i++;

		if (split.length > i && split[i].length() > 0) {
			prefixes = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		} else {
			prefixes = new ArrayList<String>();
		}
		i++;

		if (split.length > i && split[i].length() > 0) {
			suffixes = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		} else {
			suffixes = new ArrayList<String>();
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		if (family != null) {
			parent.append("surname", family);
		}
		if (given != null) {
			parent.append("given", given);
		}
		parent.append("additional", additional);
		parent.append("prefix", prefixes);
		parent.append("suffix", suffixes);
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		family = element.get("surname");
		given = element.get("given");
		additional = element.getAll("additional");
		prefixes = element.getAll("prefix");
		suffixes = element.getAll("suffix");
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
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		JCardValue value = JCardValue.text();
		value.addValues(family, given, additional, prefixes, suffixes);
		value.setStructured(true);
		return value;
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		int i = 0;
		List<List<String>> values = value.getValuesAsStrings();

		if (values.size() > i) {
			String valueStr = values.get(i).get(0);
			family = (valueStr.length() > 0) ? valueStr : null;
		} else {
			family = null;
		}
		i++;

		if (values.size() > i) {
			String valueStr = values.get(i).get(0);
			given = (valueStr.length() > 0) ? valueStr : null;
		} else {
			given = null;
		}
		i++;

		additional.clear();
		if (values.size() > i) {
			for (String valueStr : values.get(i)) {
				if (valueStr.length() > 0) {
					additional.add(valueStr);
				}
			}
		}
		i++;

		prefixes.clear();
		if (values.size() > i) {
			for (String valueStr : values.get(i)) {
				if (valueStr.length() > 0) {
					prefixes.add(valueStr);
				}
			}
		}
		i++;

		suffixes.clear();
		if (values.size() > i) {
			for (String valueStr : values.get(i)) {
				if (valueStr.length() > 0) {
					suffixes.add(valueStr);
				}
			}
		}
	}
}