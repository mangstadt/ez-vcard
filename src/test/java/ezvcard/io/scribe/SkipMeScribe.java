package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.SkipMeException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.SkipMeProperty;

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
 * A scribe that always throws a {@link SkipMeException}.
 * @author Michael Angstadt
 */
public class SkipMeScribe extends VCardPropertyScribe<SkipMeProperty> {
	public SkipMeScribe() {
		super(SkipMeProperty.class, "SKIPME");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return null;
	}

	@Override
	protected String _writeText(SkipMeProperty property, WriteContext context) {
		throw new SkipMeException();
	}

	@Override
	protected SkipMeProperty _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		throw new SkipMeException();
	}

	@Override
	protected void _writeXml(SkipMeProperty property, XCardElement parent) {
		throw new SkipMeException();
	}

	@Override
	protected SkipMeProperty _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		throw new SkipMeException();
	}

	@Override
	protected SkipMeProperty _parseHtml(HCardElement element, List<String> warnings) {
		throw new SkipMeException();
	}

	@Override
	protected JCardValue _writeJson(SkipMeProperty property) {
		throw new SkipMeException();
	}

	@Override
	protected SkipMeProperty _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		throw new SkipMeException();
	}
}
