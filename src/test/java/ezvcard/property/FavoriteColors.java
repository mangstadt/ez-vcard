package ezvcard.property;

import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;

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
 * @author Michael Angstadt
 */
public class FavoriteColors extends VCardProperty {
	private List<String> colors = new ArrayList<String>();

	public List<String> getColors() {
		return colors;
	}

	public void addColor(String color) {
		colors.add(color);
	}

	public String getLang() {
		return parameters.getLanguage();
	}

	public void setLang(String lang) {
		parameters.setLanguage(lang);
	}

	//optional
	//validates the property's data
	//invoked when "VCard.validate()" is called
	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (colors.isEmpty()) {
			warnings.add(new Warning("No colors are defined."));
		}

		if (colors.contains("periwinkle") && version == VCardVersion.V4_0) {
			warnings.add(new Warning("Periwinkle is deprecated in vCard 4.0."));
		}
	}

	public static class FavoriteColorsScribe extends VCardPropertyScribe<FavoriteColors> {
		public FavoriteColorsScribe() {
			super(FavoriteColors.class, "X-FAV-COLORS");
		}

		//required
		//defines the property's default data type
		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return null;
		}

		//optional
		//determines the data type based on the property value
		@Override
		protected VCardDataType _dataType(FavoriteColors property, VCardVersion version) {
			return VCardDataType.TEXT;
		}

		//optional
		//tweaks the property's parameters before the property is written
		@Override
		protected void _prepareParameters(FavoriteColors property, VCardParameters copy, VCardVersion version, VCard vcard) {
			if (copy.getLanguage() == null) {
				copy.setLanguage("en");
			}
		}

		//required
		//writes the property's value to a plain-text vCard
		@Override
		protected String _writeText(FavoriteColors property, WriteContext context) {
			return list(property.getColors());
		}

		//required
		//parses the property's value from a plain-text vCard
		@Override
		protected FavoriteColors _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
			FavoriteColors prop = new FavoriteColors();
			for (String color : list(value)) {
				prop.addColor(color);
			}
			return prop;
		}

		//optional
		//writes the property to an XML document (xCard)
		@Override
		protected void _writeXml(FavoriteColors property, XCardElement element) {
			for (String color : property.getColors()) {
				element.append(VCardDataType.TEXT, color);
			}
		}

		//optional
		//parses the property from an XML document (xCard)
		@Override
		protected FavoriteColors _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
			List<String> colors = element.all(VCardDataType.TEXT);
			if (colors.isEmpty()) {
				throw new CannotParseException("No <text> elements found.");
			}

			FavoriteColors property = new FavoriteColors();
			for (String color : colors) {
				property.addColor(color);
			}
			return property;
		}

		//optional
		//parses the property value from an HTML page (hCard)
		@Override
		protected FavoriteColors _parseHtml(HCardElement element, List<String> warnings) {
			FavoriteColors property = new FavoriteColors();

			String lang = element.attr("lang");
			property.setLanguage((lang.length() == 0) ? null : lang);

			property.getColors().addAll(element.allValues("color")); //gets the hCard values of all descendant elements that have a CSS class named "color"

			return property;
		}

		//optional
		//writes the property to a JSON stream (jCard)
		@Override
		protected JCardValue _writeJson(FavoriteColors property) {
			return JCardValue.multi(property.getColors());
		}

		//optional
		//parses the property value from a JSON stream (jCard)
		@Override
		protected FavoriteColors _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
			FavoriteColors property = new FavoriteColors();
			for (String color : value.asMulti()) {
				property.addColor(color);
			}
			return property;
		}
	}
}
