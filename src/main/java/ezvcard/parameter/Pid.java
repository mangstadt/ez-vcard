package ezvcard.parameter;

import ezvcard.property.ClientPidMap;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 * Represents a PID parameter value. A PID uniquely identifies a property. They
 * are used when two different versions of the same vCard have to be merged
 * together (called "synchronizing").
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#section-5.5">RFC 6350
 * Section 5.5</a>
 */
public class Pid {
	private final Integer localId, clientPidMapReference;

	/**
	 * Creates a new PID.
	 * @param localId the local ID (cannot be null, must be positive)
	 * @param clientPidMapReference an integer that references the property's
	 * globally unique ID (optional, must be positive). It must match the first
	 * value in an existing {@link ClientPidMap} property
	 * @throws NullPointerException if local ID is null
	 */
	public Pid(Integer localId, Integer clientPidMapReference) {
		if (localId == null) {
			throw new NullPointerException("Local ID must not be null.");
		}
		this.localId = localId;
		this.clientPidMapReference = clientPidMapReference;
	}

	/**
	 * Creates a new PID.
	 * @param localId the local ID (cannot be null, must be positive)
	 * @throws NullPointerException if local ID is null
	 */
	public Pid(Integer localId) {
		this(localId, null);
	}

	/**
	 * Gets the local ID
	 * @return the local ID
	 */
	public Integer getLocalId() {
		return localId;
	}

	/**
	 * Gets the reference to the property's globally unique ID (stored in a
	 * {@link ClientPidMap} property).
	 * @return the reference ID or null if not set
	 */
	public Integer getClientPidMapReference() {
		return clientPidMapReference;
	}

	/**
	 * Parses a string into a {@link Pid} object.
	 * @param value the string value (e.g. "1.2")
	 * @return the {@link Pid} object
	 * @throws NumberFormatException if there's a problem parsing the string
	 */
	public static Pid valueOf(String value) {
		int dot = value.indexOf('.');
		String localIdStr, clientPidMapReferenceStr;
		if (dot < 0) {
			localIdStr = value;
			clientPidMapReferenceStr = null;
		} else {
			localIdStr = value.substring(0, dot);
			clientPidMapReferenceStr = (dot == value.length() - 1) ? null : value.substring(dot + 1);
		}

		Integer localId = Integer.valueOf(localIdStr);
		Integer clientPidMapReference = (clientPidMapReferenceStr == null) ? null : Integer.valueOf(clientPidMapReferenceStr);
		return new Pid(localId, clientPidMapReference);
	}

	@Override
	public String toString() {
		return (clientPidMapReference == null) ? Integer.toString(localId) : localId + "." + clientPidMapReference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientPidMapReference == null) ? 0 : clientPidMapReference.hashCode());
		result = prime * result + localId.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Pid other = (Pid) obj;
		if (clientPidMapReference == null) {
			if (other.clientPidMapReference != null) return false;
		} else if (!clientPidMapReference.equals(other.clientPidMapReference)) return false;
		if (!localId.equals(other.localId)) return false;
		return true;
	}
}
