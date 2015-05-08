package ezvcard.property;

import java.util.EnumSet;
import java.util.Set;

import ezvcard.VCardVersion;
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
 * if (kind.isIndividual()){
 *   //vCard contains information on an individual person
 * } else if (kind.isGroup()){
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
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V4_0);
	}

	/**
	 * Determines if the value is set to "individual".
	 * @return true if the value is "individual", false if not
	 */
	public boolean isIndividual() {
		return INDIVIDUAL.equals(value);
	}

	/**
	 * Determines if the value is set to "group".
	 * @return true if the value is "group", false if not
	 */
	public boolean isGroup() {
		return GROUP.equals(value);
	}

	/**
	 * Determines if the value is set to "org".
	 * @return true if the value is "org", false if not
	 */
	public boolean isOrg() {
		return ORG.equals(value);
	}

	/**
	 * Determines if the value is set to "location".
	 * @return true if the value is "location", false if not
	 */
	public boolean isLocation() {
		return LOCATION.equals(value);
	}

	/**
	 * Determines if the value is set to "application".
	 * @return true if the value is "application", false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6473">RFC 6473</a>
	 */
	public boolean isApplication() {
		return APPLICATION.equals(value);
	}

	/**
	 * Determines if the value is set to "device".
	 * @return true if the value is "device", false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6869">RFC 6869</a>
	 */
	public boolean isDevice() {
		return DEVICE.equals(value);
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
}
