package ezvcard.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ezvcard.SupportedVersions;
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
 * Maps a globally-unique URI to a PID parameter value. The PID parameter can be
 * set on any property where multiple instances are allowed (such as
 * {@link Email} or {@link Address}, but not {@link StructuredName} because only
 * 1 instance of it is allowed). It allows an individual property instance to be
 * uniquely identifiable.
 * </p>
 * 
 * <p>
 * This property, along with the PID parameter, is used during the
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
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Address adr = new Address();
 * adr.addPid(1, 1);
 * vcard.addAddress(adr);
 * 
 * Email email = vcard.addEmail("johndoe@hotmail.com");
 * emai.addPid(1, 1);
 * email = vcard.addEmail("jdoe@company.com");
 * email.addPid(2, 2);
 * 
 * //specify the URI to use
 * ClientPidMap clientpidmap = new ClientPidMap(1, "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
 * vcard.addClientPidMap(clientpidmap);
 * 
 * //or, generate a random URI
 * clientpidmap = ClientPidMap.random(2);
 * vcard.addClientPidMap(clientpidmap);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code CLIENTPIDMAP}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350 p.47</a>
 */
@SupportedVersions(VCardVersion.V4_0)
public class ClientPidMap extends VCardProperty {
	private Integer pid;
	private String uri;

	/**
	 * Creates a client PID map property.
	 * @param pid the PID
	 * @param uri the globally unique URI
	 */
	public ClientPidMap(Integer pid, String uri) {
		this.pid = pid;
		this.uri = uri;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public ClientPidMap(ClientPidMap original) {
		super(original);
		pid = original.pid;
		uri = original.uri;
	}

	/**
	 * Generates a CLIENTPIDMAP type that contains a random UID URI.
	 * @param pid the PID
	 * @return a CLIENTPIDMAP type with a random UID URI
	 */
	public static ClientPidMap random(Integer pid) {
		String uuid = UUID.randomUUID().toString();
		return new ClientPidMap(pid, "urn:uuid:" + uuid);
	}

	/**
	 * Gets the value that is used to link the URI in this property to the
	 * property that the URI belongs to.
	 * @return the PID
	 */
	public Integer getPid() {
		return pid;
	}

	/**
	 * Gets the value that is used to link the URI in this property to the
	 * property that the URI belongs to.
	 * @param pid the PID
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
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		if (pid == null && uri == null) {
			warnings.add(new ValidationWarning(8));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("pid", pid);
		values.put("uri", uri);
		return values;
	}

	@Override
	public ClientPidMap copy() {
		return new ClientPidMap(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		ClientPidMap other = (ClientPidMap) obj;
		if (pid == null) {
			if (other.pid != null) return false;
		} else if (!pid.equals(other.pid)) return false;
		if (uri == null) {
			if (other.uri != null) return false;
		} else if (!uri.equals(other.uri)) return false;
		return true;
	}
}
