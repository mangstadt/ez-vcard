package ezvcard.property;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarning;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * n.setFamily("House");
 * n.setGiven("Gregory");
 * n.getPrefixes().add("Dr");
 * n.getSuffixes().add("MD");
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
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350 p.29</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
 */
public class StructuredName extends VCardProperty implements HasAltId {
	private String family;
	private String given;
	private final List<String> additional;
	private final List<String> prefixes;
	private final List<String> suffixes;

	public StructuredName() {
		additional = new ArrayList<>();
		prefixes = new ArrayList<>();
		suffixes = new ArrayList<>();
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public StructuredName(StructuredName original) {
		super(original);
		family = original.family;
		given = original.given;
		additional = new ArrayList<>(original.additional);
		prefixes = new ArrayList<>(original.prefixes);
		suffixes = new ArrayList<>(original.suffixes);
	}

	/**
	 * Gets the family name (aka "surname" or "last name").
	 * @return the family name or null if not set
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Sets the family name (aka "surname" or "last name").
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
	 * Gets the list that stores additional names the person goes by (for
	 * example, a middle name).
	 * @return the additional names (this list is mutable)
	 */
	public List<String> getAdditionalNames() {
		return additional;
	}

	/**
	 * Gets the list that stores the person's honorific prefixes.
	 * @return the prefixes (e.g. "Dr.", "Mr.") (this list is mutable)
	 */
	public List<String> getPrefixes() {
		return prefixes;
	}

	/**
	 * Gets the list that stores the person's honorary suffixes.
	 * @return the suffixes (e.g. "M.D.", "Jr.") (this list is mutable)
	 */
	public List<String> getSuffixes() {
		return suffixes;
	}

	/**
	 * <p>
	 * Gets the list that holds string(s) which define how to sort the vCard.
	 * </p>
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortString} property instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the sort string(s) (this list is mutable). For example, if the
	 * family name is "d'Aboville" and the given name is "Christine", the sort
	 * strings might be ["Aboville", "Christine"].
	 */
	public List<String> getSortAs() {
		return parameters.getSortAs();
	}

	/**
	 * <p>
	 * Defines a sortable version of the person's name.
	 * </p>
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortString} property instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param family the sortable version of the family name (for example,
	 * "Adboville" if the family name is "d'Aboville") or null to remove
	 */
	public void setSortAs(String family) {
		parameters.setSortAs(family);
	}

	/**
	 * <p>
	 * Defines a sortable version of the person's name.
	 * </p>
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortString} property instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param family the sortable version of the family name (for example,
	 * "Adboville" if the family name is "d'Aboville")
	 * @param given the sortable version of the given name
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

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("family", family);
		values.put("given", given);
		values.put("additional", additional);
		values.put("prefixes", prefixes);
		values.put("suffixes", suffixes);
		return values;
	}

	@Override
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		/*
		 * 2.1 does not allow multi-valued components.
		 */
		if (version == VCardVersion.V2_1) {
			//@formatter:off
			if (additional.size() > 1 ||
				prefixes.size() > 1 ||
				suffixes.size() > 1) {
				warnings.add(new ValidationWarning(34));
			}
			//@formatter:on
		}
	}

	@Override
	public StructuredName copy() {
		return new StructuredName(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + additional.hashCode();
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((given == null) ? 0 : given.hashCode());
		result = prime * result + prefixes.hashCode();
		result = prime * result + suffixes.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		StructuredName other = (StructuredName) obj;
		if (!additional.equals(other.additional)) return false;
		if (family == null) {
			if (other.family != null) return false;
		} else if (!family.equals(other.family)) return false;
		if (given == null) {
			if (other.given != null) return false;
		} else if (!given.equals(other.given)) return false;
		if (!prefixes.equals(other.prefixes)) return false;
		if (!suffixes.equals(other.suffixes)) return false;
		return true;
	}
}