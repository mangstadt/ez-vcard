package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
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
 */

/**
 * Marshals {@link ClientPidMap} properties.
 * @author Michael Angstadt
 */
public class ClientPidMapScribe extends VCardPropertyScribe<ClientPidMap> {
	public ClientPidMapScribe() {
		super(ClientPidMap.class, "CLIENTPIDMAP");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(ClientPidMap property, WriteContext context) {
		//@formatter:off
		return structured(context.isIncludeTrailingSemicolons(), new Object[] {
			property.getPid(),
			property.getUri()
		});
		//@formatter:on
	}

	@Override
	protected ClientPidMap _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		SemiStructuredIterator it = semistructured(value, 2);
		String pid = it.next();
		String uri = it.next();
		if (pid == null || uri == null) {
			throw new CannotParseException(3);
		}

		return parse(pid, uri);
	}

	@Override
	protected void _writeXml(ClientPidMap property, XCardElement parent) {
		Integer pid = property.getPid();
		parent.append("sourceid", (pid == null) ? "" : pid.toString());

		parent.append(VCardDataType.URI, property.getUri());
	}

	@Override
	protected ClientPidMap _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String sourceid = element.first("sourceid");
		String uri = element.first(VCardDataType.URI);

		if (uri == null && sourceid == null) {
			throw missingXmlElements(VCardDataType.URI.getName().toLowerCase(), "sourceid");
		}
		if (uri == null) {
			throw missingXmlElements(VCardDataType.URI);
		}
		if (sourceid == null) {
			throw missingXmlElements("sourceid");
		}

		return parse(sourceid, uri);
	}

	@Override
	protected JCardValue _writeJson(ClientPidMap property) {
		return JCardValue.structured(property.getPid(), property.getUri());
	}

	@Override
	protected ClientPidMap _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		StructuredIterator it = structured(value);
		String pid = it.nextString();
		String uri = it.nextString();
		return parse(pid, uri);
	}

	private ClientPidMap parse(String pid, String uri) {
		try {
			return new ClientPidMap(Integer.parseInt(pid), uri);
		} catch (NumberFormatException e) {
			throw new CannotParseException(4);
		}
	}
}
