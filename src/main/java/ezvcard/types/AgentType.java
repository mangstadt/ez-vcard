package ezvcard.types;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.VCardReader;
import ezvcard.io.VCardWriter;
import ezvcard.parameters.ValueParameter;
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
 * Represents an AGENT type.
 * @author Michael Angstadt
 */
public class AgentType extends VCardType {
	public static final String NAME = "AGENT";

	private String url;
	private VCard vcard;

	public AgentType() {
		super(NAME);
	}

	public AgentType(String url) {
		super(NAME);
		setUrl(url);
	}

	public AgentType(VCard vcard) {
		super(NAME);
		setVcard(vcard);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public VCard getVcard() {
		return vcard;
	}

	public void setVcard(VCard vcard) {
		this.vcard = vcard;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		if (url != null) {
			subTypes.setValue(ValueParameter.URI);
			return url;
		} else {
			subTypes.setValue(null);

			StringWriter sw = new StringWriter();
			VCardWriter writer = new VCardWriter(sw, version, null, "\n");
			writer.setCompatibilityMode(compatibilityMode);
			try {
				writer.write(vcard);
			} catch (IOException e) {
				//writing to string
			}
			String str = sw.toString();

			for (String w : writer.getWarnings()) {
				warnings.add("AGENT marshal warning: " + w);
			}

			str = VCardStringUtils.escapeText(str);
			return str;
		}
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) throws VCardException {
		value = VCardStringUtils.unescape(value);

		ValueParameter valueSubType = subTypes.getValue();
		if (valueSubType == ValueParameter.URI || valueSubType == ValueParameter.CONTENT_ID || value.startsWith("http")) {
			url = value;
		} else {
			VCardReader reader = new VCardReader(new StringReader(value));
			reader.setCompatibilityMode(compatibilityMode);
			try {
				vcard = reader.readNext();
			} catch (IOException e) {
				//reading from a string
			}
			for (String w : reader.getWarnings()) {
				warnings.add("AGENT unmarshal warning: " + w);
			}
		}
	}

}
