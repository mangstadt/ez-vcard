package ezvcard.io.xml;

import javax.xml.namespace.QName;

import ezvcard.VCardVersion;

/**
 * Contains the XML element names of the standard xCard elements (except the
 * property element names).
 * @author Michael Angstadt
 */
public interface XCardQNames {
	String NAMESPACE = VCardVersion.V4_0.getXmlNamespace();
	QName VCARDS = new QName(NAMESPACE, "vcards");
	QName VCARD = new QName(NAMESPACE, "vcard");
	QName GROUP = new QName(NAMESPACE, "group");
	QName PARAMETERS = new QName(NAMESPACE, "parameters");
}
