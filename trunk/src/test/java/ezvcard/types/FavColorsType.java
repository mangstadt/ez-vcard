package ezvcard.types;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.io.SkipMeException;
import ezvcard.util.HCardElement;
import ezvcard.util.JCardValue;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.XCardElement;
import ezvcard.util.XmlUtils;

public class FavColorsType extends VCardType {
	private static final QName qname = new QName("http://fav-colors.net", "fav-colors");
	private List<String> favColors = new ArrayList<String>();

	//default constructor required
	public FavColorsType() {
		super("X-FAV-COLORS");
	}

	public List<String> getFavColors() {
		return favColors;
	}

	public void addFavColor(String color) {
		favColors.add(color);
	}

	@Override
	public String getLanguage() {
		return subTypes.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	//modifies the property's parameters before the property is written
	@Override
	protected void doMarshalSubTypes(VCardSubTypes copy, VCardVersion version, CompatibilityMode compatibilityMode, VCard vcard) {
		if (version == VCardVersion.V2_1) {
			copy.setLanguage(null); //remove the "LANGUAGE" parameter
		}
	}

	//writes the property value to a plain-text vCard
	@Override
	protected void doMarshalText(StringBuilder value, VCardVersion version, CompatibilityMode compatibilityMode) {
		if (!favColors.isEmpty()) {
			for (String color : sanitizeColors(compatibilityMode)) {
				value.append(VCardStringUtils.escape(color)).append(',');
			}
			value.deleteCharAt(value.length() - 1); //remove last comma
		}
	}

	//parses the property's value from a plain-text vCard
	@Override
	protected void doUnmarshalText(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		favColors = VCardStringUtils.splitBy(value, ',', true, true);
		if (favColors.contains("periwinkle") && version == VCardVersion.V4_0) {
			warnings.add("Periwinkle is deprecated in vCard 4.0.");
		}
	}

	//the XML namespace and element name to use when reading/writing from/to an XML document
	@Override
	public QName getQName() {
		return qname;
	}

	//writes the property to an XML document
	@Override
	protected void doMarshalXml(XCardElement element, CompatibilityMode compatibilityMode) {
		Element theElement = element.element();
		for (String color : sanitizeColors(compatibilityMode)) {
			Element colorElement = theElement.getOwnerDocument().createElementNS(qname.getNamespaceURI(), "color");
			colorElement.setTextContent(color);
			theElement.appendChild(colorElement);
		}
	}

	//parses the property from an XML document
	@Override
	protected void doUnmarshalXml(XCardElement element, List<String> warnings, CompatibilityMode compatibilityMode) {
		favColors.clear();
		NodeList nl = element.element().getElementsByTagNameNS(qname.getNamespaceURI(), "color");
		List<Element> colorElements = XmlUtils.toElementList(nl);
		for (Element colorElement : colorElements) {
			favColors.add(colorElement.getTextContent());
		}
		if (favColors.contains("periwinkle")) {
			warnings.add("Periwinkle is deprecated in vCard 4.0.");
		}
	}

	//parses the property value from an HTML page
	@Override
	protected void doUnmarshalHtml(HCardElement element, List<String> warnings) {
		String lang = element.attr("lang");
		setLanguage((lang.length() == 0) ? null : lang);

		favColors.clear();
		favColors.addAll(element.allValues("color")); //gets the hCard values of all descendant elements that have a CSS class named "color"
	}

	//writes the property to a JSON stream
	@Override
	protected JCardValue doMarshalJson(VCardVersion version) {
		List<String> santizied = sanitizeColors(CompatibilityMode.RFC);
		return JCardValue.multi(VCardDataType.TEXT, santizied);
	}

	//parses the property value from a JSON stream
	@Override
	protected void doUnmarshalJson(JCardValue value, VCardVersion version, List<String> warnings) {
		favColors.clear();
		for (String valueStr : value.asMulti()) {
			favColors.add(valueStr);
		}
	}

	private List<String> sanitizeColors(CompatibilityMode compatibilityMode) {
		List<String> colors = new ArrayList<String>(favColors.size());
		for (String color : favColors) {
			if (compatibilityMode == CompatibilityMode.MS_OUTLOOK && "blue".equals(color)) {
				//Microsoft uses "azure" instead of "blue"
				color = "azure";
			} else if ("apple".equals(color)) {
				//this exception will prevent the property from being written to the vCard
				throw new SkipMeException("This property will not be marshalled because \"apple\" is not a color.");
			}
			colors.add(color);
		}
		return colors;
	}
}
