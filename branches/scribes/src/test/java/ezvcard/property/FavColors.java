package ezvcard.property;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardSubTypes;
import ezvcard.util.XmlUtils;

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
 */

/**
 * @author Michael Angstadt
 */
public class FavColors extends VCardProperty {
	private List<String> favColors = new ArrayList<String>();

	public List<String> getFavColors() {
		return favColors;
	}

	public void addFavColor(String color) {
		favColors.add(color);
	}

	public String getLang() {
		return subTypes.getLanguage();
	}

	public void setLang(String lang) {
		subTypes.setLanguage(lang);
	}

	//optional
	//validates the property's data
	//invoked when "VCard.validate()" is called
	@Override
	protected void _validate(List<String> warnings, VCardVersion version, VCard vcard) {
		if (favColors.isEmpty()) {
			warnings.add("No colors are defined.");
		}

		if (favColors.contains("periwinkle") && version == VCardVersion.V4_0) {
			warnings.add("Periwinkle is deprecated in vCard 4.0.");
		}
	}

	public static class FavColorsScribe extends VCardPropertyScribe<FavColors> {
		public FavColorsScribe() {
			super(FavColors.class, "X-FAV-COLORS", new QName("http://fav-colors.net", "fav-colors"));
		}

		//required
		//defines the property's default vCard data type
		@Override
		protected VCardDataType _defaultDataType(VCardVersion version) {
			return VCardDataType.TEXT;
		}

		//optional
		//determines the data type based on the property value
		@Override
		protected VCardDataType _dataType(FavColors property, VCardVersion version) {
			return _defaultDataType(version);
		}

		//optional
		//modifies the property's parameters before the property is written
		@Override
		protected void _prepareParameters(FavColors property, VCardSubTypes copy, VCardVersion version, VCard vcard) {
			if (copy.getLanguage() == null) {
				copy.setLanguage("en");
			}
		}

		//required
		//writes the property value to a plain-text vCard
		@Override
		protected String _writeText(FavColors property, VCardVersion version) {
			StringBuilder sb = new StringBuilder();
			if (!property.getFavColors().isEmpty()) {
				for (String color : property.getFavColors()) {
					sb.append(escape(color)).append(',');
				}
				sb.deleteCharAt(sb.length() - 1); //remove last comma
			}
			return sb.toString();
		}

		//required
		//parses the property's value from a plain-text vCard
		@Override
		protected FavColors _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
			FavColors prop = new FavColors();
			for (String color : list(value)) {
				prop.addFavColor(color);
			}
			return prop;
		}

		//optional
		//writes the property to an XML document
		@Override
		protected void _writeXml(FavColors property, XCardElement element) {
			Element theElement = element.element();
			for (String color : property.getFavColors()) {
				Element colorElement = theElement.getOwnerDocument().createElementNS(qname.getNamespaceURI(), "color");
				colorElement.setTextContent(color);
				theElement.appendChild(colorElement);
			}
		}

		//optional
		//parses the property from an XML document
		@Override
		protected FavColors _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
			NodeList nl = element.element().getElementsByTagNameNS(qname.getNamespaceURI(), "color");
			List<Element> colorElements = XmlUtils.toElementList(nl);
			if (colorElements.isEmpty()) {
				throw new CannotParseException("No <color> elements found.");
			}

			FavColors property = new FavColors();
			for (Element colorElement : colorElements) {
				property.addFavColor(colorElement.getTextContent());
			}
			return property;
		}

		//optional
		//parses the property value from an HTML page
		@Override
		protected FavColors _parseHtml(HCardElement element, List<String> warnings) {
			FavColors property = new FavColors();

			String lang = element.attr("lang");
			property.setLang((lang.length() == 0) ? null : lang);

			property.getFavColors().addAll(element.allValues("color")); //gets the hCard values of all descendant elements that have a CSS class named "color"

			return property;
		}

		//optional
		//writes the property to a JSON stream
		@Override
		protected JCardValue _writeJson(FavColors property) {
			return JCardValue.multi(VCardDataType.TEXT, property.getFavColors());
		}

		//optional
		//parses the property value from a JSON stream
		@Override
		protected FavColors _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
			FavColors property = new FavColors();
			property.getFavColors().addAll(value.asMulti());
			return property;
		}
	}
}
