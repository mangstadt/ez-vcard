package ezvcard.property;

import java.util.ArrayList;
import java.util.List;

import ezvcard.parameter.VCardParameters;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Defines the individual components of the person's name.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * StructuredName n = new StructuredName();
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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StructuredName extends VCardProperty implements HasAltId {
	private String family;
	private String given;
	private List<String> additional = new ArrayList<String>();
	private List<String> prefixes = new ArrayList<String>();
	private List<String> suffixes = new ArrayList<String>();

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
	 * 2.1 and 3.0 vCards should use the {@link SortString SORT-STRING} property
	 * instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the sort string(s) (e.g. ["Aboville", "Christine"] if the family
	 * name is "d'Aboville" and the given name is "Christine") or empty list if
	 * there are none
	 * @see VCardParameters#getSortAs
	 */
	public List<String> getSortAs() {
		return parameters.getSortAs();
	}

	/**
	 * Sets the string that defines how to sort the vCard.
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortString SORT-STRING} property
	 * instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param family the sorttable family name (e.g. "Adboville" if the family
	 * name is "d'Aboville") or null to remove
	 */
	public void setSortAs(String family) {
		if (family == null) {
			parameters.setSortAs();
		} else {
			parameters.setSortAs(family);
		}
	}

	/**
	 * Sets the strings that define how to sort the vCard.
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortString SORT-STRING} property
	 * instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param family the sortable family name (e.g. "Adboville" if the family
	 * name is "d'Aboville")
	 * @param given the sortable given name
	 */
	public void setSortAs(String family, String given) {
		parameters.setSortAs(family, given);
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
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
	}
}