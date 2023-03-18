package ezvcard.io;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.VCardProperty;

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
 * @author Michael Angstadt
 */
public class MyFormattedNameProperty extends VCardProperty {
	public String value;

	public MyFormattedNameProperty(String value) {
		this.value = value;
	}

	public static class MyFormattedNameScribe extends VCardPropertyScribe<MyFormattedNameProperty> {
		public MyFormattedNameScribe() {
			super(MyFormattedNameProperty.class, "FN");
		}

		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return VCardDataType.get("name");
		}

		@Override
		protected String _writeText(MyFormattedNameProperty property, WriteContext context) {
			return property.value.toUpperCase();
		}

		@Override
		protected MyFormattedNameProperty _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			return new MyFormattedNameProperty(value.toUpperCase());
		}

		@Override
		protected void _writeXml(MyFormattedNameProperty property, XCardElement parent) {
			parent.append("name", property.value);
		}

		@Override
		protected MyFormattedNameProperty _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
			return new MyFormattedNameProperty(element.first("name").toUpperCase());
		}

		@Override
		protected MyFormattedNameProperty _parseHtml(HCardElement element, ParseContext context) {
			return new MyFormattedNameProperty(element.value().toUpperCase());
		}

		@Override
		protected JCardValue _writeJson(MyFormattedNameProperty property) {
			return JCardValue.single(property.value.toUpperCase());
		}

		@Override
		protected MyFormattedNameProperty _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
			return new MyFormattedNameProperty(value.asSingle().toUpperCase());
		}
	}
}
