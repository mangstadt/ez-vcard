package ezvcard.property;

import ezvcard.SupportedVersions;
import ezvcard.VCardVersion;

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
 * Defines the type of entity that this vCard represents, such as an individual
 * or an organization.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Kind kind = Kind.individual();
 * vcard.setKind(kind);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Kind kind = vcard.getKind();
 * if (kind.isIndividual()) {
 *   //vCard contains information on an individual person
 * } else if (kind.isGroup()) {
 *   //vCard contains information on a group of people
 * }
 * //etc
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code KIND}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-25">RFC 6350 p.25</a>
 */
@SupportedVersions(VCardVersion.V4_0)
public class Kind extends TextProperty {
	public static final String INDIVIDUAL = "individual";
	public static final String GROUP = "group";
	public static final String ORG = "org";
	public static final String LOCATION = "location";
	public static final String APPLICATION = "application";
	public static final String DEVICE = "device";

	/**
	 * Creates a kind property. Use of this constructor is discouraged. Please
	 * use one of the static factory methods to create a new KIND property.
	 * @param kind the kind value (e.g. "group")
	 */
	public Kind(String kind) {
		super(kind);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Kind(Kind original) {
		super(original);
	}

	/**
	 * Determines if the value is set to "individual".
	 * @return true if the value is "individual" (case-insensitive), false if
	 * not
	 */
	public boolean isIndividual() {
		return INDIVIDUAL.equalsIgnoreCase(value);
	}

	/**
	 * Determines if the value is set to "group".
	 * @return true if the value is "group" (case-insensitive), false if not
	 */
	public boolean isGroup() {
		return GROUP.equalsIgnoreCase(value);
	}

	/**
	 * Determines if the value is set to "org".
	 * @return true if the value is "org" (case-insensitive), false if not
	 */
	public boolean isOrg() {
		return ORG.equalsIgnoreCase(value);
	}

	/**
	 * Determines if the value is set to "location".
	 * @return true if the value is "location" (case-insensitive), false if not
	 */
	public boolean isLocation() {
		return LOCATION.equalsIgnoreCase(value);
	}

	/**
	 * Determines if the value is set to "application".
	 * @return true if the value is "application" (case-insensitive), false if
	 * not
	 * @see <a href="http://tools.ietf.org/html/rfc6473">RFC 6473</a>
	 */
	public boolean isApplication() {
		return APPLICATION.equalsIgnoreCase(value);
	}

	/**
	 * Determines if the value is set to "device".
	 * @return true if the value is "device" (case-insensitive), false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6869">RFC 6869</a>
	 */
	public boolean isDevice() {
		return DEVICE.equalsIgnoreCase(value);
	}

	/**
	 * Creates a new KIND property whose value is set to "individual".
	 * @return the new KIND property
	 */
	public static Kind individual() {
		return new Kind(INDIVIDUAL);
	}

	/**
	 * Creates a new KIND property whose value is set to "group".
	 * @return the new KIND property
	 */
	public static Kind group() {
		return new Kind(GROUP);
	}

	/**
	 * Creates a new KIND property whose value is set to "org".
	 * @return the new KIND property
	 */
	public static Kind org() {
		return new Kind(ORG);
	}

	/**
	 * Creates a new KIND property whose value is set to "location".
	 * @return the new KIND property
	 */
	public static Kind location() {
		return new Kind(LOCATION);
	}

	/**
	 * Creates a new KIND property whose value is set to "application".
	 * @return the new KIND property
	 * @see <a href="http://tools.ietf.org/html/rfc6473">RFC 6473</a>
	 */
	public static Kind application() {
		return new Kind(APPLICATION);
	}

	/**
	 * Creates a new KIND property whose value is set to "device".
	 * @return the new KIND property
	 * @see <a href="http://tools.ietf.org/html/rfc6869">RFC 6869</a>
	 */
	public static Kind device() {
		return new Kind(DEVICE);
	}

	@Override
	public Kind copy() {
		return new Kind(this);
	}
}
