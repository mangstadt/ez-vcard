package ezvcard.util;

import ezvcard.VCardVersion;

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
 * Basic class for building a vCard string.
 * @author Michael Angstadt
 */
public class VCardBuilder {
	private final StringBuilder sb = new StringBuilder();
	private int count = 0;
	private VCardVersion defaultVersion;

	public VCardBuilder(VCardVersion defaultVersion) {
		this.defaultVersion = defaultVersion;
		begin();
	}

	public VCardBuilder begin() {
		return begin(defaultVersion);
	}

	public VCardBuilder begin(VCardVersion version) {
		if (count > 0) {
			prop("END").value("VCARD");
		}
		prop("BEGIN").value("VCARD");
		prop("VERSION").value(version.getVersion());
		count++;
		return this;
	}

	public VCardBuilder prop(String name) {
		prop(null, name);
		return this;
	}

	public VCardBuilder prop(String group, String name) {
		if (group != null) {
			sb.append(group).append('.');
		}
		sb.append(name);
		return this;
	}

	public VCardBuilder param(String namelessValue) {
		param(namelessValue, null);
		return this;
	}

	public VCardBuilder param(String name, String value) {
		sb.append(';').append(name);
		if (value != null) {
			sb.append('=').append(value);
		}
		return this;
	}

	public VCardBuilder value(String value) {
		value = VCardStringUtils.escapeNewlines(value);
		sb.append(':').append(value).append("\r\n");
		return this;
	}

	public int size() {
		return count;
	}

	@Override
	public String toString() {
		return sb.toString() + "END:VCARD";
	}
}
