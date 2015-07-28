package ezvcard.io.xml;

import javax.xml.namespace.QName;

import ezvcard.VCardVersion;

/**
 * Contains the XML element names of the standard xCard elements (except the
 * property element names).
 * @author Michael Angstadt
 */
public interface XCardQNames {
	public static final String NAMESPACE = VCardVersion.V4_0.getXmlNamespace();
	public static final QName VCARDS = new QName(NAMESPACE, "vcards");
	public static final QName VCARD = new QName(NAMESPACE, "vcard");
	public static final QName GROUP = new QName(NAMESPACE, "group");
	public static final QName PARAMETERS = new QName(NAMESPACE, "parameters");
}
