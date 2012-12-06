package ezvcard.io;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
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
import ezvcard.types.CategoriesType;
import ezvcard.types.EmailType;
import ezvcard.types.ImppType;
import ezvcard.types.LabelType;
import ezvcard.types.NicknameType;
import ezvcard.types.RawType;
import ezvcard.types.SourceType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TypeList;
import ezvcard.types.UrlType;
import ezvcard.types.VCardType;
import ezvcard.util.HCardUtils;

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
	protected String pageUrl;
	protected List<String> warnings = new ArrayList<String>();
	protected Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	protected Elements vcardElements;
	protected Iterator<Element> it;
	protected List<LabelType> labels = new ArrayList<LabelType>();
	protected List<String> warningsBuffer = new ArrayList<String>();
	protected VCard curVCard;
	protected Elements embeddedVCards = new Elements();
	protected NicknameType nickname;
	protected CategoriesType categories;

	/**
	 * Reads vCards from a webpage.
	 * @param url the URL of the webpage
	 * @throws IOException if there's a problem opening the webpage
	 */
	public HCardReader(URL url) throws IOException {
		pageUrl = url.toString();
		Document document = Jsoup.connect(pageUrl).get();
		init(document, url.getRef());
	}

	/**
	 * Reads vCards from an HTML page.
	 * @param html the HTML page
	 */
	public HCardReader(String html) {
		this(html, null);
	}

	/**
	 * Reads vCards from an HTML page.
	 * @param html the HTML page
	 * @param pageUrl the URL of the page
	 */
	public HCardReader(String html, String pageUrl) {
		this.pageUrl = pageUrl;

		String anchor = null;
		if (pageUrl != null) {
			try {
				URL url = new URL(pageUrl);
				anchor = url.getRef();
			} catch (MalformedURLException e) {
				//ignore
			}
		}

		Document document = (pageUrl == null) ? Jsoup.parse(html) : Jsoup.parse(html, pageUrl);
		init(document, anchor);
	}

	/**
	 * Constructor for reading embedded vCards.
	 * @param embeddedVCard the HTML element of the embedded vCard
	 * @param pageUrl the URL of the page
	 */
	private HCardReader(Element embeddedVCard, String pageUrl) {
		this.pageUrl = pageUrl;
		vcardElements = new Elements(embeddedVCard);
		it = vcardElements.iterator();
	}

	private void init(Document document, String anchor) {
		Element searchIn = null;
		if (anchor != null) {
			searchIn = document.getElementById(anchor);
		}
		if (searchIn == null) {
			searchIn = document;
		}

		vcardElements = searchIn.getElementsByClass("vcard");
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

	/**
	 * Reads the next vCard.
	 * @return the next vCard or null if there are no more
	 */
	public VCard readNext() {
		Element vcardElement = null;
		while (it.hasNext() && vcardElement == null) {
			vcardElement = it.next();

			//if this element is a child of another "vcard" element, then ignore it because it's an embedded vcard
			if (HCardUtils.isChildOf(vcardElement, vcardElements)) {
				vcardElement = null;
			}
		}
		if (vcardElement == null) {
			return null;
		}

		warnings.clear();
		warningsBuffer.clear();
		labels.clear();
		nickname = null;
		categories = null;

		curVCard = new VCard();
		curVCard.setVersion(VCardVersion.V3_0);
		if (pageUrl != null) {
			curVCard.addSource(new SourceType(pageUrl));
		}

		//visit all descendant nodes, depth-first
		for (Element child : vcardElement.children()) {
			visit(child);
		}

		//assign labels to their addresses
		for (LabelType label : labels) {
			boolean orphaned = true;
			for (AddressType adr : curVCard.getAddresses()) {
				if (adr.getLabel() == null && adr.getTypes().equals(label.getTypes())) {
					adr.setLabel(label.getValue());
					orphaned = false;
					break;
				}
			}
			if (orphaned) {
				curVCard.addOrphanedLabel(label);
			}
		}

		return curVCard;
	}

	private void visit(Element element) {
		Set<String> classNames = element.classNames();
		for (String className : classNames) {
			if (UrlType.NAME.equalsIgnoreCase(className)) {
				String href = element.attr("href");
				if (href.length() > 0) {
					if (!classNames.contains(EmailType.NAME.toLowerCase()) && href.matches("(?i)mailto:.*")) {
						className = EmailType.NAME;
					} else if (!classNames.contains(TelephoneType.NAME.toLowerCase()) && href.matches("(?i)tel:.*")) {
						className = TelephoneType.NAME;
					} else {
						//try parsing as IMPP
						warningsBuffer.clear();
						ImppType impp = new ImppType();
						try {
							impp.unmarshalHtml(element, warningsBuffer);
							addToVCard(impp, curVCard);
							warnings.addAll(warningsBuffer);
							continue;
						} catch (SkipMeException e) {
							//URL is not an instant messenger URL
						}
					}
				}
			}

			VCardType type = createTypeObject(className);
			if (type == null) {
				//if no type class is found, then it must be an arbitrary CSS class that has nothing to do with vCard
				continue;
			}

			warningsBuffer.clear();
			try {
				type.unmarshalHtml(element, warningsBuffer);

				//add to vcard
				if (type instanceof LabelType) {
					//LABELs must be treated specially so they can be matched up with their ADRs
					labels.add((LabelType) type);
				} else if (type instanceof NicknameType) {
					//add all NICKNAMEs to the same type object
					NicknameType nn = (NicknameType) type;
					if (nickname == null) {
						nickname = nn;
						addToVCard(nickname, curVCard);
					} else {
						nickname.getValues().addAll(nn.getValues());
					}
				} else if (type instanceof CategoriesType) {
					//add all CATEGORIES to the same type object
					CategoriesType c = (CategoriesType) type;
					if (categories == null) {
						categories = c;
						addToVCard(categories, curVCard);
					} else {
						categories.getValues().addAll(c.getValues());
					}
				} else {
					addToVCard(type, curVCard);
				}
			} catch (SkipMeException e) {
				warningsBuffer.add(type.getTypeName() + " property will not be unmarshalled: " + e.getMessage());
			} catch (EmbeddedVCardException e) {
				if (HCardUtils.isChildOf(element, embeddedVCards)) {
					//prevents multiple-nested embedded elements from overwriting each other
					continue;
				}

				embeddedVCards.add(element);
				HCardReader embeddedReader = new HCardReader(element, pageUrl);
				try {
					VCard embeddedVCard = embeddedReader.readNext();
					e.injectVCard(embeddedVCard);
				} finally {
					for (String w : embeddedReader.getWarnings()) {
						warnings.add("Problem unmarshalling nested vCard value from " + type.getTypeName() + ": " + w);
					}
				}
				addToVCard(type, curVCard);
			} catch (UnsupportedOperationException e) {
				//type class does not support hCard
				warningsBuffer.add("Type class \"" + type.getClass().getName() + "\" does not support hCard unmarshalling.");
			} finally {
				warnings.addAll(warningsBuffer);
			}
		}

		for (Element child : element.children()) {
			visit(child);
		}
	}

	/**
	 * Creates the appropriate {@link VCardType} instance, given the type name.
	 * This method does not unmarshal the type, it just creates the type object.
	 * @param typeName the type name (e.g. "fn")
	 * @return the type object or null if the type name was not recognized
	 */
	private VCardType createTypeObject(String typeName) {
		typeName = typeName.toLowerCase();
		VCardType t = null;
		Class<? extends VCardType> clazz = TypeList.getTypeClassByHCardTypeName(typeName);
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
			Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(typeName);
			if (extendedTypeClass != null) {
				try {
					t = extendedTypeClass.newInstance();
				} catch (Exception e) {
					//this should never happen because the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
					throw new RuntimeException("Extended type class \"" + extendedTypeClass.getName() + "\" must have a public, no-arg constructor.");
				}
			} else if (typeName.startsWith("x-")) {
				t = new RawType(typeName); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
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
			return t.getTypeName().toLowerCase();
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}
}
