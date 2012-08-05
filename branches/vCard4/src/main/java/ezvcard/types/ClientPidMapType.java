package ezvcard.types;

import java.util.List;

import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.VCardStringUtils;

/*
 Copyright (c) 2012, Michael Angstadt
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
 * Represents the CLIENTPIDMAP type.
 * @author Michael Angstadt
 */
public class ClientPidMapType extends VCardType {
	public static final String NAME = "CLIENTPIDMAP";

	private int pid;
	private String uri;

	public ClientPidMapType() {
		super(NAME);
	}

	/**
	 * @param pid the PID
	 * @param uri the globally unique URI
	 */
	public ClientPidMapType(int pid, String uri) {
		super(NAME);
		this.pid = pid;
		this.uri = uri;
	}

	/**
	 * Gets the value that is used to link the URI in this property to the
	 * property that the URI belongs to.
	 * @return the PID
	 * @see VCardSubTypes#getPids
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * Gets the value that is used to link the URI in this property to the
	 * property that the URI belongs to.
	 * @param pid the PID
	 * @see VCardSubTypes#getPids
	 */
	public void setPid(int pid) {
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
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		return pid + ";" + VCardStringUtils.escapeText(uri);
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		String split[] = value.split(";", 2);
		if (split.length < 2) {
			warnings.add("Incorrect format of " + NAME + " type value: \"" + value + "\"");
		} else {
			pid = Integer.parseInt(split[0]);
			uri = VCardStringUtils.unescape(split[1]);
		}
	}
}
