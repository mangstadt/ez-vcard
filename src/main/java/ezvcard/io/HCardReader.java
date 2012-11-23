package ezvcard.io;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.types.AddressType;
import ezvcard.types.EmailType;
import ezvcard.types.ImppType;
import ezvcard.types.LabelType;
import ezvcard.types.RawType;
import ezvcard.types.SourceType;
import ezvcard.types.TypeList;
import ezvcard.types.UrlType;
import ezvcard.types.VCardType;

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
 * Reads vCards encoded in HTML (hCard format).
 * @author Michael Angstadt
 * @see <a
 * href="http://microformats.org/wiki/hcard">http://microformats.org/wiki/hcard</a>
 */
public class HCardReader {
	protected String baseUrl;
	protected List<String> warnings = new ArrayList<String>();
	protected Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	protected Elements vcardElements;
	protected Iterator<Element> it;

	public static void main(String args[]) {
		Document doc = Jsoup.parse("<html><body><DiV cLaSs=vcard><a class=\"fn url\" href=\"test.html\"><span class=\"value\">John&nbsp;Doe</span><br>Secret Agent</a><div class=\"agent vcard\"></div></div></body></html>", "http://foo.com/#test");
		Elements elements = doc.getElementsByClass("vcard");
		System.out.println(elements);
	}

	public HCardReader(URL url) throws IOException {
		this.baseUrl = url.toString();

		Document doc;
		InputStream in = null;
		try {
			in = url.openStream();
			doc = Jsoup.parse(in, "UTF-8", url.toString());
		} finally {
			if (in != null) {
				in.close();
			}
		}

		vcardElements = doc.getElementsByClass("vcard");
		it = vcardElements.iterator();
	}

	public HCardReader(String html) {
		Document doc = Jsoup.parse(html);
		vcardElements = doc.getElementsByClass("vcard");
		it = vcardElements.iterator();
	}

	public HCardReader(String html, String baseUrl) {
		this.baseUrl = baseUrl;

		Document doc = Jsoup.parse(html, baseUrl);
		vcardElements = doc.getElementsByClass("vcard");
		it = vcardElements.iterator();
	}

	/**
	 * Registers an extended type class.
	 * @param clazz the extended type class to register (MUST have a public,
	 * no-arg constructor)
	 */
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.put(getTypeNameFromTypeClass(clazz), clazz);
	}

	/**
	 * Removes an extended type class that was previously registered.
	 * @param clazz the extended type class to remove (MUST have a public,
	 * no-arg constructor)
	 */
	public void unregisterExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.remove(getTypeNameFromTypeClass(clazz));
	}

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	public VCard readNext() {
		Element vcardElement = null;
		while (it.hasNext() && vcardElement == null) {
			vcardElement = it.next();
			for (Element parent : vcardElement.parents()) {
				if (vcardElements.contains(parent)) {
					//if this element is a child of another "vcard" element, then ignore it because it's an embedded vcard
					vcardElement = null;
					break;
				}
			}
		}
		if (vcardElement == null) {
			return null;
		}

		warnings.clear();

		List<LabelType> labels = new ArrayList<LabelType>();
		List<String> warningsBuffer = new ArrayList<String>();

		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V3_0);
		if (baseUrl != null) {
			vcard.addSource(new SourceType(baseUrl));
		}

		Elements properties = vcardElement.children();
		for (Element property : properties) {
			Set<String> classNames = property.classNames();
			for (String className : classNames) {
				//check for a URL that points to an instant messenger address or email address
				if (UrlType.NAME.equalsIgnoreCase(className)) {
					String href = property.attr("href");
					if (href.length() > 0) {
						if (href.matches("(?i)(aim:|ymsgr:|msnim:|xmpp:|skype:|https?://(www\\.)?icq\\.com).*")) {
							className = ImppType.NAME;
						} else if (!classNames.contains(EmailType.NAME.toLowerCase()) && href.matches("(?i)mailto:.*")) {
							className = EmailType.NAME;
						}
					}
				}

				VCardType type = createTypeObject(className);
				if (type == null) {
					//no type class found, it must be an arbitrary CSS class that has nothing to do with vCard
					continue;
				}

				warningsBuffer.clear();
				try {
					type.unmarshalHtml(property, warningsBuffer);

					//add to vcard
					if (type instanceof LabelType) {
						//LABELs must be treated specially so they can be matched up with their ADRs
						labels.add((LabelType) type);
					} else {
						addToVCard(type, vcard);
					}
				} catch (SkipMeException e) {
					warningsBuffer.add(type.getTypeName() + " property will not be unmarshalled: " + e.getMessage());
				} catch (EmbeddedVCardException e) {
					//TODO implement this
					addToVCard(type, vcard);
				} catch (UnsupportedOperationException e) {
					//type class does not support hCard
					warningsBuffer.add("Type class \"" + type.getClass().getName() + "\" does not support hCard unmarshalling.");
				} finally {
					warnings.addAll(warningsBuffer);
				}
			}
		}

		//assign labels to their addresses
		for (LabelType label : labels) {
			boolean orphaned = true;
			for (AddressType adr : vcard.getAddresses()) {
				if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())) {
					adr.setLabel(label.getValue());
					orphaned = false;
					break;
				}
			}
			if (orphaned) {
				vcard.addOrphanedLabel(label);
			}
		}

		return vcard;
	}

	/**
	 * Creates the appropriate {@link VCardType} instance, given the type name.
	 * This method does not unmarshal the type, it just creates the type object.
	 * @param name the type name (e.g. "FN")
	 * @return the Type that was created
	 */
	private VCardType createTypeObject(String name) {
		name = name.toLowerCase();
		VCardType t = null;
		Class<? extends VCardType> clazz = TypeList.getTypeClass(name);
		if (clazz != null) {
			try {
				//create a new instance of the class
				t = clazz.newInstance();
			} catch (Exception e) {
				//it is the responsibility of the EZ-vCard developer to ensure that this exception is never thrown
				//all type classes defined in the EZ-vCard library MUST have public, no-arg constructors
				throw new RuntimeException(e);
			}
		} else {
			Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(name);
			if (extendedTypeClass != null) {
				try {
					t = extendedTypeClass.newInstance();
				} catch (Exception e) {
					//this should never happen because the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
					throw new RuntimeException("Extended type class \"" + extendedTypeClass.getName() + "\" must have a public, no-arg constructor.");
				}
			} else if (name.startsWith("x-")) {
				t = new RawType(name); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
			}
		}
		return t;
	}

	/**
	 * Adds a type object to the vCard.
	 * @param t the type object
	 * @param vcard the vCard
	 */
	private void addToVCard(VCardType t, VCard vcard) {
		Method method = TypeList.getAddMethod(t.getClass());
		if (method != null) {
			try {
				method.invoke(vcard, t);
			} catch (Exception e) {
				//this should NEVER be thrown because the method MUST be public
				throw new RuntimeException(e);
			}
		} else {
			vcard.addExtendedType(t);
		}
	}

	/**
	 * Gets the type name from a type class.
	 * @param clazz the type class
	 * @return the type name
	 */
	private String getTypeNameFromTypeClass(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			return t.getTypeName().toUpperCase();
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}
}
