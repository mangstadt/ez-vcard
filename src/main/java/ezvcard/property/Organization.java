package ezvcard.property;

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
 * Defines a list of organizations the person belongs to. The list is ordered.
 * It begins with the broadest organization and ends with the most specific.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Organization org = new Organization();
 * org.addValue(&quot;Google&quot;);
 * org.addValue(&quot;GMail Team&quot;);
 * org.addValue(&quot;Spam Detection Team&quot;);
 * vcard.setOrganization(org);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code ORG}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Organization extends TextListProperty implements HasAltId {
	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	/**
	 * Gets the TYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return parameters.getType();
	}

	/**
	 * Sets the TYPE parameter.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param type the TYPE value (this should be either "work" or "home") or
	 * null to remove
	 */
	public void setType(String type) {
		parameters.setType(type);
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
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
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
	 * @return the sort string(s) or empty list if there are none
	 * @see VCardParameters#getSortAs
	 */
	public List<String> getSortAs() {
		return parameters.getSortAs();
	}

	/**
	 * Sets the string(s) that define how to sort the vCard. These strings
	 * correspond to the values that are in this property.
	 * <p>
	 * 2.1 and 3.0 vCards should use the {@link SortString SORT-STRING} property
	 * instead.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param names the names or empty parameter list to remove
	 * @see VCardParameters#setSortAs
	 */
	public void setSortAs(String... names) {
		parameters.setSortAs(names);
	}
}
