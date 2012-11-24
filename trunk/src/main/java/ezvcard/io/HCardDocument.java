package ezvcard.io;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import ezvcard.EZVCard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.LabelType;
import ezvcard.types.MemberType;
import ezvcard.types.TextType;
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
 * Writes vCards to an HTML page (hCard format).
 * @author Michael Angstadt
 * @see <a
 * href="http://microformats.org/wiki/hcard">http://microformats.org/wiki/hcard</a>
 * @see <a
 * href="http://microformats.org/wiki/hcard-parsing">http://microformats.org/wiki/hcard-parsing</a>
 */
public class HCardDocument {
	protected boolean addGenerator = true;
	protected final List<String> warnings = new ArrayList<String>();
	protected final Document document;
	protected final Element root;
	protected VCardVersion targetVersion = VCardVersion.V3_0;
	protected final boolean embeddedWriter;

	public HCardDocument() {
		document = Document.createShell("");
		Element head = document.head();
		head.appendElement("title").text("Sample hCard ");
		Element link = head.appendElement("link");
		link.attr("rel", "profile");
		link.attr("href", "http://microformats.org/profile/hcard");
		root = document.body();
		embeddedWriter = false;
	}

	/**
	 * Constructor for embedded vCards.
	 * @param embedded the HTML element to add the vCard to
	 */
	private HCardDocument(Element embedded) {
		document = null;
		root = embedded;
		embeddedWriter = true;
	}

	/**
	 * Gets whether or not a "X-GENERATOR" extended type will be added to each
	 * vCard. The type includes the version number and URL of this library.
	 * @return true if it will be added, false if not (defaults to true)
	 */
	public boolean isAddGenerator() {
		return addGenerator;
	}

	/**
	 * Sets whether or not to add a "X-GENERATOR" extended type to the vCard.
	 * The type includes the version number and URL of this library.
	 * @param addGenerator true to add this extended type, false not to
	 * (defaults to true)
	 */
	public void setAddGenerator(boolean addGenerator) {
		this.addGenerator = addGenerator;
	}

	/**
	 * Gets the warnings from the last vCard that was marshalled. This list is
	 * reset every time a new vCard is written.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	/**
	 * Adds a vCard to the HTML page.
	 * @param vcard the vCard to add
	 */
	public void addVCard(final VCard vcard) {
		warnings.clear();

		if (vcard.getStructuredName() == null) {
			warnings.add("vCard version " + targetVersion + " requires that a structured name be defined.");
		}

		if (vcard.getFormattedName() == null) {
			warnings.add("vCard version " + targetVersion + " requires that a formatted name be defined.");
		}

		List<VCardType> typesToAdd = new ArrayList<VCardType>();

		//use reflection to get all VCardType fields in the VCard class
		//the order that the Types are in doesn't matter (except for BEGIN, END, and VERSION)
		for (Field f : vcard.getClass().getDeclaredFields()) {
			try {
				f.setAccessible(true);
				Object value = f.get(vcard);
				if (value instanceof VCardType) {
					VCardType type = (VCardType) value;
					validateAndAddToList(type, vcard, typesToAdd);
				} else if (value instanceof Collection) {
					Collection<?> collection = (Collection<?>) value;
					for (Object obj : collection) {
						if (obj instanceof VCardType) {
							VCardType type = (VCardType) obj;
							validateAndAddToList(type, vcard, typesToAdd);
						}
					}
				}
			} catch (IllegalArgumentException e) {
				//shouldn't be thrown because we're passing the correct object into Field.get()
			} catch (IllegalAccessException e) {
				//shouldn't be thrown because we're calling Field.setAccessible(true)
			}
		}

		//add extended types
		for (List<VCardType> list : vcard.getExtendedTypes().values()) {
			for (VCardType extendedType : list) {
				validateAndAddToList(extendedType, vcard, typesToAdd);
			}
		}

		//add an extended type saying it was generated by EZ vCard
		if (addGenerator) {
			typesToAdd.add(new TextType("X-GENERATOR", "ez-vcard " + EZVCard.VERSION));
		}

		Element vcardRoot = embeddedWriter ? root : root.appendElement("div");
		vcardRoot.addClass("vcard");

		List<String> warningsBuffer = new ArrayList<String>();
		for (VCardType type : typesToAdd) {
			//marshal the value
			warningsBuffer.clear();
			Element element = new Element(Tag.valueOf("div"), "");
			try {
				type.marshalHtml(element, warningsBuffer);
				vcardRoot.appendChild(element);
			} catch (SkipMeException e) {
				warningsBuffer.add(type.getTypeName() + " property will not be marshalled: " + e.getMessage());
				continue;
			} catch (EmbeddedVCardException e) {
				HCardDocument embeddedWriter = new HCardDocument(element);
				try {
					embeddedWriter.addVCard(e.getVCard());
				} finally {
					for (String w : embeddedWriter.getWarnings()) {
						warnings.add(type.getTypeName() + " marshal warning: " + w);
					}
				}
				vcardRoot.appendChild(element);
			} finally {
				warnings.addAll(warningsBuffer);
			}
		}
	}

	/**
	 * Gets the HTML document.
	 * @return the HTML document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Writes the HTML document to a string.
	 * @return the HTML document
	 */
	public String write() {
		return document.outerHtml();
	}

	/**
	 * Writes the HTML document to an output stream
	 * @param writer the output stream
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void write(Writer writer) throws IOException {
		writer.write(write());
	}

	/**
	 * Validates a type instance and adds it to a list if it is determined that
	 * the type should be marshalled.
	 * @param type the type instance
	 * @param vcard the vCard that is being marshalled
	 * @param typesToAdd the list of types to marshal
	 */
	private void validateAndAddToList(VCardType type, VCard vcard, List<VCardType> typesToAdd) {
		if (type == null) {
			return;
		}

		//determine if this type is supported by the target version
		boolean supported = false;
		for (VCardVersion v : type.getSupportedVersions()) {
			if (v == targetVersion) {
				supported = true;
				break;
			}
		}
		if (!supported) {
			warnings.add(type.getTypeName() + " is not supported by vCard version " + targetVersion + " and will not be added to the vCard.  Supported versions are: " + Arrays.toString(type.getSupportedVersions()));
			return;
		}

		//check for correct KIND value if there are MEMBER types
		if (type instanceof MemberType && (vcard.getKind() == null || !vcard.getKind().isGroup())) {
			warnings.add("KIND must be set to \"group\" in order to add MEMBER properties to the vCard.");
			return;
		}

		typesToAdd.add(type);

		//add LABEL types for each ADR type if the vCard version is not 4.0
		if (type instanceof AddressType && targetVersion != VCardVersion.V4_0) {
			AddressType adr = (AddressType) type;
			String labelStr = adr.getLabel();
			if (labelStr != null) {
				LabelType label = new LabelType(labelStr);
				for (AddressTypeParameter t : adr.getTypes()) {
					label.addType(t);
				}
				typesToAdd.add(label);
			}
		}
	}
}
