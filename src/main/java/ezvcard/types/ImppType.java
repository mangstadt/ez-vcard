package ezvcard.types;

import java.util.List;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.parameters.ImppTypeParameter;
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
 * Represents the IMPP type for storing instant messaging (IM) info (see RFC
 * 4770).
 * @author Michael Angstadt
 */
public class ImppType extends MultiValuedTypeParameterType<ImppTypeParameter> {
	public static final String NAME = "IMPP";

	/**
	 * A list of possible IM protocols, as listed in RFC 4770 p.2.
	 */
	public static enum Protocol {
		SIP, XMPP, IRC, YMSGR, MSN, AIM, IM, PRES;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	/**
	 * The IM protocol (e.g. "aim").
	 */
	private String protocol;

	/**
	 * The IM URI.
	 */
	private String uri;

	public ImppType() {
		this(null, null);
	}

	/**
	 * @param protocol the IM protocol
	 * @param uri the IM URI
	 */
	public ImppType(String protocol, String uri) {
		super(NAME);
		this.protocol = protocol;
		this.uri = uri;
	}

	/**
	 * Gets the IM protocol.
	 * @return the IM protocol (e.g. "aim")
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Sets the IM protocol.
	 * @param protocol the IM protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Sets the IM protocol using example protocols provided by the
	 * {@link ImppType.Protocol} enum.
	 * @param protocol the IM protocol
	 */
	public void setProtocol(Protocol protocol) {
		setProtocol(protocol.toString());
	}

	@Override
	protected ImppTypeParameter buildTypeObj(String type) {
		ImppTypeParameter param = ImppTypeParameter.valueOf(type);
		if (param == null) {
			param = new ImppTypeParameter(type);
		}
		return param;
	}

	/**
	 * Gets the IM URI.
	 * @return the IM URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the IM URI.
	 * @param uri the IM URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder sb = new StringBuilder();
		if (protocol != null) {
			sb.append(VCardStringUtils.escapeText(protocol));
			sb.append(':');
		}
		sb.append(VCardStringUtils.escapeText(uri));
		return sb.toString();
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		value = VCardStringUtils.unescape(value);
		String split[] = value.split(":", 2);
		if (split.length < 2) {
			warnings.add(NAME + " type is not in the correct format.  Assuming that the entire value is a URI.");
			uri = split[0];
		} else {
			protocol = split[0];
			uri = split[1];
		}
	}
}
