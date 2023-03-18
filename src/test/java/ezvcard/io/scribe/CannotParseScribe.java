package ezvcard.io.scribe;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.ParseContext;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.CannotParseProperty;

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
 * A scribe that always throws a {@link CannotParseException}.
 * @author Michael Angstadt
 */
public class CannotParseScribe extends VCardPropertyScribe<CannotParseProperty> {
	public CannotParseScribe() {
		super(CannotParseProperty.class, "CANNOTPARSE");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return null;
	}

	@Override
	protected String _writeText(CannotParseProperty property, WriteContext context) {
		return "value";
	}

	@Override
	protected CannotParseProperty _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		throw new CannotParseException();
	}

	@Override
	protected CannotParseProperty _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
		throw new CannotParseException();
	}

	@Override
	protected CannotParseProperty _parseHtml(HCardElement element, ParseContext context) {
		throw new CannotParseException();
	}

	@Override
	protected CannotParseProperty _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		throw new CannotParseException();
	}
}
