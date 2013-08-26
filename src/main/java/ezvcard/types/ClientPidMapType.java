package ezvcard.types;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
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
 * <p>
 * Maps a globally-unique URI to a PID parameter value. The PID parameter can be
 * set on any type where multiple instances are allowed (such as
 * {@link EmailType} or {@link AddressType}, but not {@link StructuredNameType}
 * because only 1 instance of it is allowed). It allows an individual type
 * instance to be uniquely identifiable.
 * </p>
 * 
 * <p>
 * The CLIENTPIDMAP type and the PID parameter are used during the
 * synchronization (merging) process of two versions of the same vCard. For
 * example, if the user has a copy of her vCard on her desktop computer and her
 * smart phone, and she makes different modifications to each copy, then the two
 * copies could be synchronized in order to merge all the changes into a single,
 * new vCard.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * AddressType adr = new AddressType();
 * adr.addPid(1, 1);
 * vcard.addAddress(adr);
 * 
 * EmailType email = new EmailType(&quot;my-email@hotmail.com&quot;);
 * emai.addPid(1, 1);
 * vcard.addEmail(email);
 * email = new EmailType(&quot;my-other-email@yahoo.com&quot;);
 * emai.addPid(2, 2);
 * vcard.addEmail(email);
 * 
 * //specify the URI to use
 * ClientPidMapType clientpidmap = new ClientPidMapType(1, &quot;urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af&quot;);
 * vcard.addClientPidMap(clientpidmap);
 * 
 * //generate a random URI
 * clientpidmap = ClientPidMapType.random(2);
 * vcard.addClientPidMap(clientpidmap);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> <code>CLIENTPIDMAP</code>
 * </p>
 * <p>
 * <b>Supported versions:</b> <code>4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class ClientPidMapType extends VCardType {
	public static final String NAME = "CLIENTPIDMAP";

	private Integer pid;
	private String uri;

	/**
	 * Creates an empty client PID map property.
	 */
	public ClientPidMapType() {
		super(NAME);
	}

	/**
	 * Creates a client PID map property.
	 * @param pid the PID
	 * @param uri the globally unique URI
	 */
	public ClientPidMapType(int pid, String uri) {
		super(NAME);
		this.pid = pid;
		this.uri = uri;
	}

	/**
	 * Generates a CLIENTPIDMAP type that contains a random UID URI.
	 * @param pid the PID
	 * @return a CLIENTPIDMAP type with a random UID URI
	 */
	public static ClientPidMapType random(int pid) {
		String uuid = UUID.randomUUID().toString();
		return new ClientPidMapType(pid, "urn:uuid:" + uuid);
	}

	/**
	 * Gets the value that is used to link the URI in this property to the
	 * property that the URI belongs to.
	 * @return the PID
	 * @see VCardSubTypes#getPids
	 */
	public Integer getPid() {
		return pid;
	}

	/**
	 * Gets the value that is used to link the URI in this property to the
	 * property that the URI belongs to.
	 * @param pid the PID
	 * @see VCardSubTypes#getPids
	 */
	public void setPid(Integer pid) {
		this.pid = pid;
	}

	/**
	 * Gets the URI.
	 * @return the URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the URI.
	 * @param uri the URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	@Override
	protected void doMarshalText(StringBuilder sb, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		checkForValue();
		sb.append(pid).append(';').append(VCardStringUtils.escape(uri));
	}

	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = value.split(";", 2);
		if (split.length < 2) {
			throw new SkipMeException("Unable to parse.  Value must contain a PID and a URI, separated by a semi-colon.");
		} else {
			pid = parsePid(split[0]);
			uri = VCardStringUtils.unescape(split[1]);
		}
	}

	@Override
	protected void doMarshalXml(XCardElement parent, List<String> warnings, CompatibilityMode compatibilityMode) {
		checkForValue();

		if (uri != null) {
			parent.append(ValueParameter.URI, uri);
		}
		if (pid != null) {
			parent.append("sourceid", pid.toString());
		}
	}

	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		uri = element.first(ValueParameter.URI);

		String value = element.first("sourceid");
		pid = (value == null) ? null : parsePid(value);
	}

	@Override
	protected JCardValue doMarshalJson(VCardVersion version, List<String> warnings) {
		checkForValue();

		return JCardValue.structured(JCardDataType.TEXT, pid + "", uri);
	}

	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		Iterator<List<String>> it = value.getStructured().iterator();

		String str = nextJsonComponent(it);
		pid = (str == null) ? null : parsePid(str);

		uri = nextJsonComponent(it);
	}

	private void checkForValue() {
		if (pid == null && uri == null) {
			throw new SkipMeException("Property has no value associated with it.");
		}
	}

	private Integer parsePid(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new SkipMeException("Unable to parse PID component: " + e.getMessage());
		}
	}

	private String nextJsonComponent(Iterator<List<String>> it) {
		if (!it.hasNext()) {
			return null;
		}

		List<String> values = it.next();
		if (values.isEmpty()) {
			return null;
		}

		String value = values.get(0);
		return (value == null || value.length() == 0) ? null : value;
	}
}
