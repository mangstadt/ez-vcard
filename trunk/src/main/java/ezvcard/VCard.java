package ezvcard;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import ezvcard.io.text.VCardWriter;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;
import ezvcard.property.Agent;
import ezvcard.property.Anniversary;
import ezvcard.property.Birthday;
import ezvcard.property.Birthplace;
import ezvcard.property.CalendarRequestUri;
import ezvcard.property.CalendarUri;
import ezvcard.property.Categories;
import ezvcard.property.Classification;
import ezvcard.property.ClientPidMap;
import ezvcard.property.Deathdate;
import ezvcard.property.Deathplace;
import ezvcard.property.Email;
import ezvcard.property.Expertise;
import ezvcard.property.FormattedName;
import ezvcard.property.FreeBusyUrl;
import ezvcard.property.Gender;
import ezvcard.property.Geo;
import ezvcard.property.HasAltId;
import ezvcard.property.Hobby;
import ezvcard.property.Impp;
import ezvcard.property.Interest;
import ezvcard.property.Key;
import ezvcard.property.Kind;
import ezvcard.property.Label;
import ezvcard.property.Language;
import ezvcard.property.Logo;
import ezvcard.property.Mailer;
import ezvcard.property.Member;
import ezvcard.property.Nickname;
import ezvcard.property.Note;
import ezvcard.property.OrgDirectory;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.ProductId;
import ezvcard.property.Profile;
import ezvcard.property.RawProperty;
import ezvcard.property.Related;
import ezvcard.property.Revision;
import ezvcard.property.Role;
import ezvcard.property.SortString;
import ezvcard.property.Sound;
import ezvcard.property.Source;
import ezvcard.property.SourceDisplayText;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Timezone;
import ezvcard.property.Title;
import ezvcard.property.Uid;
import ezvcard.property.Url;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;
import ezvcard.util.ListMultimap;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Represents a vCard.
 * @author Michael Angstadt
 */
public class VCard implements Iterable<VCardProperty> {
	private VCardVersion version = VCardVersion.V3_0;

	private final ListMultimap<Class<? extends VCardProperty>, VCardProperty> properties = new ListMultimap<Class<? extends VCardProperty>, VCardProperty>();

	/**
	 * <p>
	 * Marshals this vCard to its text representation.
	 * </p>
	 * <p>
	 * The vCard will be marshalled to whatever version is attached to this
	 * VCard object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @return the vCard string
	 * @see Ezvcard
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public String write() {
		return Ezvcard.write(this).go();
	}

	/**
	 * <p>
	 * Marshals this vCard to its text representation.
	 * </p>
	 * <p>
	 * The vCard will be marshalled to whatever version is attached to this
	 * VCard object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem writing to the file
	 * @see Ezvcard
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public void write(File file) throws IOException {
		Ezvcard.write(this).go(file);
	}

	/**
	 * <p>
	 * Marshals this vCard to its text representation.
	 * </p>
	 * <p>
	 * The vCard will be marshalled to whatever version is attached to this
	 * VCard object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write the vCard to
	 * @see Ezvcard
	 * @throws IOException if there's a problem writing to the output stream
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public void write(OutputStream out) throws IOException {
		Ezvcard.write(this).go(out);
	}

	/**
	 * <p>
	 * Marshals this vCard to its text representation.
	 * </p>
	 * <p>
	 * The vCard will be marshalled to whatever version is attached to this
	 * VCard object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write the vCard to
	 * @throws IOException if there's a problem writing to the writer
	 * @see Ezvcard
	 * @see <a href="http://www.imc.org/pdi/vcard-21.rtf">vCard 2.1</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426">RFC 2426 (3.0)</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6350">RFC 6350 (4.0)</a>
	 */
	public void write(Writer writer) throws IOException {
		Ezvcard.write(this).go(writer);
	}

	/**
	 * <p>
	 * Marshals this vCard to its XML representation (xCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @return the vCard XML document
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public String writeXml() {
		return Ezvcard.writeXml(this).indent(2).go();
	}

	/**
	 * <p>
	 * Marshals this vCard to its XML representation (xCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the vCard
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public void writeXml(File file) throws IOException, TransformerException {
		Ezvcard.writeXml(this).indent(2).go(file);
	}

	/**
	 * <p>
	 * Marshals this vCard to its XML representation (xCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write the vCard to
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public void writeXml(OutputStream out) throws TransformerException {
		Ezvcard.writeXml(this).indent(2).go(out);
	}

	/**
	 * <p>
	 * Marshals this vCard to its XML representation (xCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write the vCard to
	 * @throws TransformerException if there's a problem writing to the writer
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
	 */
	public void writeXml(Writer writer) throws TransformerException {
		Ezvcard.writeXml(this).indent(2).go(writer);
	}

	/**
	 * <p>
	 * Marshals this vCard to a basic HTML page (hCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @return the HTML page
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public String writeHtml() {
		return Ezvcard.writeHtml(this).go();
	}

	/**
	 * <p>
	 * Marshals this vCard to a basic HTML page (hCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public void writeHtml(File file) throws IOException {
		Ezvcard.writeHtml(this).go(file);
	}

	/**
	 * <p>
	 * Marshals this vCard to a basic HTML page (hCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write to
	 * @throws IOException if there's a problem writing to the output stream
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public void writeHtml(OutputStream out) throws IOException {
		Ezvcard.writeHtml(this).go(out);
	}

	/**
	 * <p>
	 * Marshals this vCard to a basic HTML page (hCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write to
	 * @throws IOException if there's a problem writing to the writer
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public void writeHtml(Writer writer) throws IOException {
		Ezvcard.writeHtml(this).go(writer);
	}

	/**
	 * <p>
	 * Marshals this vCard to its JSON representation (jCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @return the JSON string
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public String writeJson() {
		return Ezvcard.writeJson(this).go();
	}

	/**
	 * <p>
	 * Marshals this vCard to its JSON representation (jCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem writing to the file
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public void writeJson(File file) throws IOException {
		Ezvcard.writeJson(this).go(file);
	}

	/**
	 * <p>
	 * Marshals this vCard to its JSON representation (jCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write the vCard to
	 * @see Ezvcard
	 * @throws IOException if there's a problem writing to the output stream
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public void writeJson(OutputStream out) throws IOException {
		Ezvcard.writeJson(this).go(out);
	}

	/**
	 * <p>
	 * Marshals this vCard to its JSON representation (jCard).
	 * </p>
	 * <p>
	 * Use the {@link Ezvcard} class to customize the marshalling process and to
	 * write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write the vCard to
	 * @throws IOException if there's a problem writing to the writer
	 * @see Ezvcard
	 * @see <a href="http://tools.ietf.org/html/rfc7095">RFC 7095</a>
	 */
	public void writeJson(Writer writer) throws IOException {
		Ezvcard.writeJson(this).go(writer);
	}

	/**
	 * Gets the version attached to this vCard.
	 * @return the vCard version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of this vCard. When marshalling a vCard with the
	 * {@link VCardWriter} class, use the {@link VCardWriter#setTargetVersion
	 * setTargetVersion} method to define what version the vCard should be
	 * marshalled as. {@link VCardWriter} <b>does not</b> look at the version
	 * that is set on the VCard object.
	 * @param version the vCard version
	 */
	public void setVersion(VCardVersion version) {
		this.version = version;
	}

	/**
	 * Gets the type of entity this vCard represents.
	 * <p>
	 * <b>Property name:</b> {@code KIND}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the kind
	 */
	public Kind getKind() {
		return getProperty(Kind.class);
	}

	/**
	 * Sets the type of entity this vCard represents.
	 * <p>
	 * <b>Property name:</b> {@code KIND}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param kind the kind
	 */
	public void setKind(Kind kind) {
		setProperty(Kind.class, kind);
	}

	/**
	 * Gets the gender of the person.
	 * <p>
	 * <b>Property name:</b> {@code GENDER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the gender
	 */
	public Gender getGender() {
		return getProperty(Gender.class);
	}

	/**
	 * Sets the gender of the person.
	 * <p>
	 * <b>Property name:</b> {@code GENDER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param gender the gender
	 */
	public void setGender(Gender gender) {
		setProperty(Gender.class, gender);
	}

	/**
	 * Gets the members of the group. Only valid if the KIND property is set to
	 * "group".
	 * 
	 * <p>
	 * 
	 * <pre class="brush:java">
	 * VCard vcard = ...
	 * Kind kind = vcard.getKind();
	 * if (kind != null && kind.isGroup()){
	 *   for (Member member : vcard.getMembers(){
	 *     ...
	 *   }
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the members
	 */
	public List<Member> getMembers() {
		return getProperties(Member.class);
	}

	/**
	 * Adds a member to the group. Only valid if the KIND property is set to
	 * "group".
	 * 
	 * <p>
	 * 
	 * <pre class="brush:java">
	 * VCard vcard = new VCard();
	 * vcard.setKind(Kind.group());
	 * vcard.addMember(...);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param member the member to add
	 */
	public void addMember(Member member) {
		addProperty(member);
	}

	/**
	 * <p>
	 * Adds a member property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addMemberAlt(Collection<Member> altRepresentations) {
		addPropertyAlt(Member.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a member property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addMemberAlt(Member... altRepresentations) {
		addPropertyAlt(Member.class, altRepresentations);
	}

	/**
	 * Gets the PROFILE property.
	 * <p>
	 * <b>Property name:</b> {@code PROFILE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the property
	 */
	public Profile getProfile() {
		return getProperty(Profile.class);
	}

	/**
	 * Sets the PROFILE property.
	 * <p>
	 * <b>Property name:</b> {@code PROFILE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param profile the property
	 */
	public void setProfile(Profile profile) {
		setProperty(Profile.class, profile);
	}

	/**
	 * Gets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * <p>
	 * <b>Property name:</b> {@code CLASS}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the classification
	 */
	public Classification getClassification() {
		return getProperty(Classification.class);
	}

	/**
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * <p>
	 * <b>Property name:</b> {@code CLASS}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param classification the classification
	 */
	public void setClassification(Classification classification) {
		setProperty(Classification.class, classification);
	}

	/**
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard. This is a convenience method for
	 * {@link #setClassification(Classification)}.
	 * <p>
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CLASS}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param classification the classification (e.g. "PUBLIC", "PRIVATE",
	 * "CONFIDENTIAL") or null to remove
	 * @return the property object that was created
	 */
	public Classification setClassification(String classification) {
		Classification type = null;
		if (classification != null) {
			type = new Classification(classification);
		}
		setClassification(type);
		return type;
	}

	/**
	 * Gets the URIs that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the sources
	 */
	public List<Source> getSources() {
		return getProperties(Source.class);
	}

	/**
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param source the source
	 */
	public void addSource(Source source) {
		addProperty(source);
	}

	/**
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard. This is a convenience method for
	 * {@link #addSource(Source)} .
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param source the source URI (e.g. "http://example.com/vcard.vcf")
	 * @return the property object that was created
	 */
	public Source addSource(String source) {
		Source type = new Source(source);
		addSource(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a source property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addSourceAlt(Collection<Source> altRepresentations) {
		addPropertyAlt(Source.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a source property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addSourceAlt(Source... altRepresentations) {
		addPropertyAlt(Source.class, altRepresentations);
	}

	/**
	 * Gets a textual representation of the SOURCE property.
	 * <p>
	 * <b>Property name:</b> {@code NAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return a textual representation of the vCard source
	 */
	public SourceDisplayText getSourceDisplayText() {
		return getProperty(SourceDisplayText.class);
	}

	/**
	 * Sets a textual representation of the SOURCE property.
	 * <p>
	 * <b>Property name:</b> {@code NAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sourceDisplayText a textual representation of the vCard source
	 */
	public void setSourceDisplayText(SourceDisplayText sourceDisplayText) {
		setProperty(SourceDisplayText.class, sourceDisplayText);
	}

	/**
	 * Sets a textual representation of the SOURCE property. This is a
	 * convenience method for {@link #setSourceDisplayText(SourceDisplayText)}.
	 * <p>
	 * <b>Property name:</b> {@code NAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sourceDisplayText a textual representation of the vCard source or
	 * null to remove
	 * @return the property object that was created
	 */
	public SourceDisplayText setSourceDisplayText(String sourceDisplayText) {
		SourceDisplayText type = null;
		if (sourceDisplayText != null) {
			type = new SourceDisplayText(sourceDisplayText);
		}
		setSourceDisplayText(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the formatted name property. Version 4.0 vCards may
	 * have multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}) or if properties
	 * with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the formatted name properties
	 */
	public List<FormattedName> getFormattedNames() {
		return getProperties(FormattedName.class);
	}

	/**
	 * <p>
	 * Gets the text value used for displaying the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the formatted name property or null if one doesn't exist
	 */
	public FormattedName getFormattedName() {
		return getProperty(FormattedName.class);
	}

	/**
	 * <p>
	 * Sets the formatted name property as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setFormattedNameAlt(Collection<FormattedName> altRepresentations) {
		setPropertyAlt(FormattedName.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the formatted name property as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setFormattedNameAlt(FormattedName... altRepresentations) {
		setPropertyAlt(FormattedName.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a formatted name property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addFormattedNameAlt(Collection<FormattedName> altRepresentations) {
		addPropertyAlt(FormattedName.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a formatted name property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addFormattedNameAlt(FormattedName... altRepresentations) {
		addPropertyAlt(FormattedName.class, altRepresentations);
	}

	/*
	 * Not going to add this method because if they are defining alternative
	 * representations, then they'll probably want to set parameters on each one
	 * (like "LANGUAGE").
	 * 
	 * public void addFormattedName(String... altRepresentations) { }
	 */

	/**
	 * <p>
	 * Sets the text value used for displaying the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param formattedName the formatted name property or null to remove
	 */
	public void setFormattedName(FormattedName formattedName) {
		setProperty(FormattedName.class, formattedName);
	}

	/**
	 * <p>
	 * Adds a text value used for displaying the person's name. Note that only
	 * version 4.0 vCards support multiple instances of this property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param formattedName the formatted name property
	 */
	public void addFormattedName(FormattedName formattedName) {
		addProperty(formattedName);
	}

	/**
	 * <p>
	 * Sets the text value used for displaying the person's name. This is a
	 * convenience method for {@link #setFormattedName(FormattedName)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param formattedName the formatted name (e.g. "John Doe") or null to
	 * remove
	 * @return the property object that was created
	 */
	public FormattedName setFormattedName(String formattedName) {
		FormattedName type = null;
		if (formattedName != null) {
			type = new FormattedName(formattedName);
		}
		setFormattedName(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all structured name properties. Version 4.0 vCards may have multiple
	 * instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}
	 * </p>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * @return the structured name property objects
	 */
	public List<StructuredName> getStructuredNames() {
		return getProperties(StructuredName.class);
	}

	/**
	 * <p>
	 * Gets the individual components of the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the components of the person's name
	 */
	public StructuredName getStructuredName() {
		return getProperty(StructuredName.class);
	}

	/**
	 * <p>
	 * Sets the structured name property as a group of alternative
	 * representations (see {@link VCardParameters#getAltId} for more details).
	 * An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setStructuredNameAlt(Collection<StructuredName> altRepresentations) {
		setPropertyAlt(StructuredName.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the structured name property as a group of alternative
	 * representations (see {@link VCardParameters#getAltId} for more details).
	 * An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setStructuredNameAlt(StructuredName... altRepresentations) {
		setPropertyAlt(StructuredName.class, altRepresentations);
	}

	/**
	 * Sets the individual components of the person's name.
	 * <p>
	 * <b>Property name:</b> {@code N}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param structuredName the components of the person's name or null to
	 * remove
	 */
	public void setStructuredName(StructuredName structuredName) {
		setProperty(StructuredName.class, structuredName);
	}

	/**
	 * <p>
	 * Gets all instances of the nickname property. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}) or if properties
	 * with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the nickname properties
	 */
	public List<Nickname> getNicknames() {
		return getProperties(Nickname.class);
	}

	/**
	 * <p>
	 * Gets the person's nicknames.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the person's nicknames
	 */
	public Nickname getNickname() {
		return getProperty(Nickname.class);
	}

	/**
	 * <p>
	 * Sets the nickname property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setNicknameAlt(Collection<Nickname> altRepresentations) {
		setPropertyAlt(Nickname.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the nickname property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setNicknameAlt(Nickname... altRepresentations) {
		setPropertyAlt(Nickname.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a nickname property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addNicknameAlt(Collection<Nickname> altRepresentations) {
		addPropertyAlt(Nickname.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a nickname property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addNicknameAlt(Nickname... altRepresentations) {
		addPropertyAlt(Nickname.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's nickname(s).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param nickname the nickname property or null to remove (note that
	 * multiple nicknames may be added this object)
	 */
	public void setNickname(Nickname nickname) {
		setProperty(Nickname.class, nickname);
	}

	/**
	 * <p>
	 * Adds a set of nicknames. Note that only version 4.0 vCards support
	 * multiple instances of this property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param nickname the nickname property (note that multiple nicknames may
	 * be added this object)
	 */
	public void addNickname(Nickname nickname) {
		addProperty(nickname);
	}

	/**
	 * <p>
	 * Sets the person's nicknames. This is a convenience method for
	 * {@link #setNickname(Nickname)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param nicknames the nickname(s) (e.g. "Jonny") or null to remove
	 * @return the property object that was created
	 */
	public Nickname setNickname(String... nicknames) {
		Nickname type = null;
		if (nicknames != null) {
			type = new Nickname();
			for (String nickname : nicknames) {
				type.addValue(nickname);
			}
		}
		setNickname(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the string that should be used to sort the vCard.
	 * </p>
	 * <p>
	 * For 4.0 vCards, use the {@link StructuredName#getSortAs} and/or
	 * {@link Organization#getSortAs} methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the sort string
	 */
	public SortString getSortString() {
		return getProperty(SortString.class);
	}

	/**
	 * <p>
	 * Sets the string that should be used to sort the vCard.
	 * </p>
	 * <p>
	 * For 4.0 vCards, use the {@link StructuredName#setSortAs} and/or
	 * {@link Organization#setSortAs} methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sortString the sort string
	 */
	public void setSortString(SortString sortString) {
		setProperty(SortString.class, sortString);
	}

	/**
	 * <p>
	 * Sets the string that should be used to sort the vCard. This is a
	 * convenience method for {@link #setSortString(SortString)}.
	 * </p>
	 * <p>
	 * For 4.0 vCards, use the {@link StructuredName#setSortAs} and/or
	 * {@link Organization#setSortAs} methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sortString the sort string (e.g. "Armour" if the person's last
	 * name is "d'Armour") or null to remove
	 * @return the property object that was created
	 */
	public SortString setSortString(String sortString) {
		SortString type = null;
		if (sortString != null) {
			type = new SortString(sortString);
		}
		setSortString(type);
		return type;
	}

	/**
	 * Gets the titles associated with the person.
	 * <p>
	 * <b>Property name:</b> {@code TITLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the titles
	 */
	public List<Title> getTitles() {
		return getProperties(Title.class);
	}

	/**
	 * Adds a title associated with the person.
	 * <p>
	 * <b>Property name:</b> {@code TITLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param title the title
	 */
	public void addTitle(Title title) {
		addProperty(title);
	}

	/**
	 * Adds a title associated with the person. This is a convenience method for
	 * {@link #addTitle(Title)}.
	 * <p>
	 * <b>Property name:</b> {@code TITLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param title the title (e.g. "V.P. Research and Development")
	 * @return the property object that was created
	 */
	public Title addTitle(String title) {
		Title type = new Title(title);
		addTitle(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a title property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TITLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addTitleAlt(Collection<Title> altRepresentations) {
		addPropertyAlt(Title.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a title property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TITLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addTitleAlt(Title... altRepresentations) {
		addPropertyAlt(Title.class, altRepresentations);
	}

	/**
	 * Gets the roles associated with the person.
	 * <p>
	 * <b>Property name:</b> {@code ROLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return getProperties(Role.class);
	}

	/**
	 * Adds a role associated with the person.
	 * <p>
	 * <b>Property name:</b> {@code ROLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param role the role
	 */
	public void addRole(Role role) {
		addProperty(role);
	}

	/**
	 * Adds a role associated with the person. This is a convenience method for
	 * {@link #addRole(Role)}.
	 * <p>
	 * <b>Property name:</b> {@code ROLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param role the role (e.g. "Executive")
	 * @return the property object that was created
	 */
	public Role addRole(String role) {
		Role type = new Role(role);
		addRole(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a role property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ROLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addRoleAlt(Collection<Role> altRepresentations) {
		addPropertyAlt(Role.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a role property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ROLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addRoleAlt(Role... altRepresentations) {
		addPropertyAlt(Role.class, altRepresentations);
	}

	/**
	 * Gets the photos attached to the vCard, such as a picture of the person's
	 * face.
	 * <p>
	 * <b>Property name:</b> {@code PHOTO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the photos
	 */
	public List<Photo> getPhotos() {
		return getProperties(Photo.class);
	}

	/**
	 * Adds a photo to the vCard, such as a picture of the person's face.
	 * <p>
	 * <b>Property name:</b> {@code PHOTO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param photo the photo to add
	 */
	public void addPhoto(Photo photo) {
		addProperty(photo);
	}

	/**
	 * <p>
	 * Adds a photo property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addPhotoAlt(Collection<Photo> altRepresentations) {
		addPropertyAlt(Photo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a photo property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addPhotoAlt(Photo... altRepresentations) {
		addPropertyAlt(Photo.class, altRepresentations);
	}

	/**
	 * Gets the logos attached to the vCard, such a company logo.
	 * <p>
	 * <b>Property name:</b> {@code LOGO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the logos
	 */
	public List<Logo> getLogos() {
		return getProperties(Logo.class);
	}

	/**
	 * Adds a logo to the vCard, such as a company logo.
	 * <p>
	 * <b>Property name:</b> {@code LOGO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param logo the logo to add
	 */
	public void addLogo(Logo logo) {
		addProperty(logo);
	}

	/**
	 * <p>
	 * Adds a logo property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LOGO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addLogoAlt(Collection<Logo> altRepresentations) {
		addPropertyAlt(Logo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a logo property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LOGO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addLogoAlt(Logo... altRepresentations) {
		addPropertyAlt(Logo.class, altRepresentations);
	}

	/**
	 * Gets the sounds attached to the vCard, such as a pronunciation of the
	 * person's name.
	 * <p>
	 * <b>Property name:</b> {@code SOUND}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the sounds
	 */
	public List<Sound> getSounds() {
		return getProperties(Sound.class);
	}

	/**
	 * Adds a sound to the vCard, such as a pronunciation of the person's name.
	 * <p>
	 * <b>Property name:</b> {@code SOUND}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param sound the sound to add
	 */
	public void addSound(Sound sound) {
		addProperty(sound);
	}

	/**
	 * <p>
	 * Adds a sound property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addSoundAlt(Collection<Sound> altRepresentations) {
		addPropertyAlt(Sound.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a sound property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addSoundAlt(Sound... altRepresentations) {
		addPropertyAlt(Sound.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all birthplace property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the birthplace properties
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public List<Birthplace> getBirthplaces() {
		return getProperties(Birthplace.class);
	}

	/**
	 * <p>
	 * Gets the person's birthplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the birthplace or null if one doesn't exist
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public Birthplace getBirthplace() {
		return getProperty(Birthplace.class);
	}

	/**
	 * <p>
	 * Sets the person's birthplace as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}. An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setBirthplaceAlt(Collection<Birthplace> altRepresentations) {
		setPropertyAlt(Birthplace.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthplace as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}. An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setBirthplaceAlt(Birthplace... altRepresentations) {
		setPropertyAlt(Birthplace.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param birthplace the birthplace or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setBirthplace(Birthplace birthplace) {
		setProperty(Birthplace.class, birthplace);
	}

	/**
	 * <p>
	 * Gets all deathplace property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the deathplace properties
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public List<Deathplace> getDeathplaces() {
		return getProperties(Deathplace.class);
	}

	/**
	 * <p>
	 * Gets the person's deathplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the deathplace or null if one doesn't exist
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public Deathplace getDeathplace() {
		return getProperty(Deathplace.class);
	}

	/**
	 * <p>
	 * Sets the person's deathplace as a group of alternative representations
	 * (see {@link VCardParameters#getAltId} for more details). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setDeathplaceAlt(Collection<Deathplace> altRepresentations) {
		setPropertyAlt(Deathplace.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's deathplace as a group of alternative representations
	 * (see {@link VCardParameters#getAltId} for more details). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setDeathplaceAlt(Deathplace... altRepresentations) {
		setPropertyAlt(Deathplace.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's deathplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param deathplace the deathplace or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setDeathplace(Deathplace deathplace) {
		setProperty(Deathplace.class, deathplace);
	}

	/**
	 * <p>
	 * Gets all death date property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the death date properties
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public List<Deathdate> getDeathdates() {
		return getProperties(Deathdate.class);
	}

	/**
	 * <p>
	 * Gets the person's time of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the time of death or null if one doesn't exist
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public Deathdate getDeathdate() {
		return getProperty(Deathdate.class);
	}

	/**
	 * <p>
	 * Sets the deathdate property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setDeathdateAlt(Collection<Deathdate> altRepresentations) {
		setPropertyAlt(Deathdate.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the deathdate property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setDeathdateAlt(Deathdate... altRepresentations) {
		setPropertyAlt(Deathdate.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's time of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param deathdate the time of death or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setDeathdate(Deathdate deathdate) {
		setProperty(Deathdate.class, deathdate);
	}

	/**
	 * <p>
	 * Gets all birthday property instances. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @return the birthday properties
	 */
	public List<Birthday> getBirthdays() {
		return getProperties(Birthday.class);
	}

	/**
	 * <p>
	 * Gets the person's birthday.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the birthday
	 */
	public Birthday getBirthday() {
		return getProperty(Birthday.class);
	}

	/**
	 * <p>
	 * Sets the person's birthday as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setBirthdayAlt(Collection<Birthday> altRepresentations) {
		setPropertyAlt(Birthday.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthday as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setBirthdayAlt(Birthday... altRepresentations) {
		setPropertyAlt(Birthday.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthday.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param birthday the birthday or null to remove
	 */
	public void setBirthday(Birthday birthday) {
		setProperty(Birthday.class, birthday);
	}

	/**
	 * <p>
	 * Gets all anniversary property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the anniversary properties
	 */
	public List<Anniversary> getAnniversaries() {
		return getProperties(Anniversary.class);
	}

	/**
	 * <p>
	 * Gets the person's anniversary.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the anniversary
	 */
	public Anniversary getAnniversary() {
		return getProperty(Anniversary.class);
	}

	/**
	 * <p>
	 * Sets the person's anniversary as a group of alternative representations
	 * (see {@link VCardParameters#getAltId} for more details). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setAnniversaryAlt(Collection<Anniversary> altRepresentations) {
		setPropertyAlt(Anniversary.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's anniversary as a group of alternative representations
	 * (see {@link VCardParameters#getAltId} for more details). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setAnniversaryAlt(Anniversary... altRepresentations) {
		setPropertyAlt(Anniversary.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's anniversary.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param anniversary the anniversary or null to remove
	 */
	public void setAnniversary(Anniversary anniversary) {
		setProperty(Anniversary.class, anniversary);
	}

	/**
	 * Gets the time that the vCard was last modified.
	 * <p>
	 * <b>Property name:</b> {@code REV}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the last modified time
	 */
	public Revision getRevision() {
		return getProperty(Revision.class);
	}

	/**
	 * Sets the time that the vCard was last modified.
	 * <p>
	 * <b>Property name:</b> {@code REV}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param rev the last modified time
	 */
	public void setRevision(Revision rev) {
		setProperty(Revision.class, rev);
	}

	/**
	 * Sets the time that the vCard was last modified. This is a convenience
	 * method for {@link #setRevision(Revision)}.
	 * <p>
	 * <b>Property name:</b> {@code REV}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param rev the last modified time or null to remove
	 * @return the property object that was created
	 */
	public Revision setRevision(Date rev) {
		Revision type = null;
		if (rev != null) {
			type = new Revision(rev);
		}
		setRevision(type);
		return type;
	}

	/**
	 * Gets the product ID, which identifies the software that created the
	 * vCard.
	 * <p>
	 * <b>Property name:</b> {@code PRODID}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the product ID
	 */
	public ProductId getProductId() {
		return getProperty(ProductId.class);
	}

	/**
	 * Sets the product ID, which identifies the software that created the
	 * vCard.
	 * <p>
	 * <b>Property name:</b> {@code PRODID}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param productId the product ID
	 */
	public void setProductId(ProductId productId) {
		setProperty(ProductId.class, productId);
	}

	/**
	 * Sets the product ID, which identifies the software that created the
	 * vCard. This is a convenience method for {@link #setProductId(ProductId)}.
	 * <p>
	 * <b>Property name:</b> {@code PRODID}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param productId the product ID (e.g. "ez-vcard 1.0") or null to remove
	 * @return the property object that was created
	 */
	public ProductId setProductId(String productId) {
		ProductId type = null;
		if (productId != null) {
			type = new ProductId(productId);
		}
		setProductId(type);
		return type;
	}

	/**
	 * Gets the mailing addresses.
	 * <p>
	 * <b>Property name:</b> {@code ADR}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the mailing addresses
	 */
	public List<Address> getAddresses() {
		return getProperties(Address.class);
	}

	/**
	 * Adds a mailing address.
	 * <p>
	 * <b>Property name:</b> {@code ADR}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param address the mailing address to add
	 */
	public void addAddress(Address address) {
		addProperty(address);
	}

	/**
	 * <p>
	 * Adds an address property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ADR}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addAddressAlt(Collection<Address> altRepresentations) {
		addPropertyAlt(Address.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an address property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ADR}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addAddressAlt(Address... altRepresentations) {
		addPropertyAlt(Address.class, altRepresentations);
	}

	/**
	 * Gets all mailing labels that could not be assigned to an address. Use
	 * {@link Address#getLabel} to get a label that has been assigned to an
	 * address.
	 * <p>
	 * <b>Property name:</b> {@code LABEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the orphaned labels
	 */
	public List<Label> getOrphanedLabels() {
		return getProperties(Label.class);
	}

	/**
	 * Adds a mailing label which is not associated with any address. Use of
	 * this method is discouraged. To add a mailing label to an address, use the
	 * {@link Address#setLabel} method.
	 * <p>
	 * <b>Property name:</b> {@code LABEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param label the orphaned label to add
	 */
	public void addOrphanedLabel(Label label) {
		addProperty(label);
	}

	/**
	 * Gets the email addresses.
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the email addresses
	 */
	public List<Email> getEmails() {
		return getProperties(Email.class);
	}

	/**
	 * Adds an email address.
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param email the email address to add
	 */
	public void addEmail(Email email) {
		addProperty(email);
	}

	/**
	 * Adds an email address. This is a convenience method for
	 * {@link #addEmail(Email)}.
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param email the email address to add (e.g. "johndoe@aol.com")
	 * @param types the type(s) to assign to the email
	 * @return the property object that was created
	 */
	public Email addEmail(String email, EmailType... types) {
		Email type = new Email(email);
		for (EmailType t : types) {
			type.addType(t);
		}
		addEmail(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an email property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addEmailAlt(Collection<Email> altRepresentations) {
		addPropertyAlt(Email.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an email property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addEmailAlt(Email... altRepresentations) {
		addPropertyAlt(Email.class, altRepresentations);
	}

	/**
	 * Gets the telephone numbers.
	 * <p>
	 * <b>Property name:</b> {@code TEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the telephone numbers
	 */
	public List<Telephone> getTelephoneNumbers() {
		return getProperties(Telephone.class);
	}

	/**
	 * Adds a telephone number.
	 * <p>
	 * <b>Property name:</b> {@code TEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param telephoneNumber the telephone number to add
	 */
	public void addTelephoneNumber(Telephone telephoneNumber) {
		addProperty(telephoneNumber);
	}

	/**
	 * Adds a telephone number. This is a convenience method for
	 * {@link #addTelephoneNumber(Telephone)}.
	 * <p>
	 * <b>Property name:</b> {@code TEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param telephoneNumber the telephone number to add (e.g.
	 * "+1 555-555-5555")
	 * @param types the type(s) to assign to the telephone number (e.g. "cell",
	 * "work", etc)
	 * @return the property object that was created
	 */
	public Telephone addTelephoneNumber(String telephoneNumber, TelephoneType... types) {
		Telephone type = new Telephone(telephoneNumber);
		for (TelephoneType t : types) {
			type.addType(t);
		}
		addTelephoneNumber(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a telephone property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addTelephoneNumberAlt(Collection<Telephone> altRepresentations) {
		addPropertyAlt(Telephone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a telephone property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addTelephoneNumberAlt(Telephone... altRepresentations) {
		addPropertyAlt(Telephone.class, altRepresentations);
	}

	/**
	 * Gets the email client that the person uses.
	 * <p>
	 * <b>Property name:</b> {@code MAILER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the email client
	 */
	public Mailer getMailer() {
		return getProperty(Mailer.class);
	}

	/**
	 * Sets the email client that the person uses.
	 * <p>
	 * <b>Property name:</b> {@code MAILER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param mailer the email client
	 */
	public void setMailer(Mailer mailer) {
		setProperty(Mailer.class, mailer);
	}

	/**
	 * Sets the email client that the person uses. This is a convenience method
	 * for {@link #setMailer(Mailer)}.
	 * <p>
	 * <b>Property name:</b> {@code MAILER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param mailer the email client (e.g. "Thunderbird") or null to remove
	 * @return the property object that was created
	 */
	public Mailer setMailer(String mailer) {
		Mailer type = null;
		if (mailer != null) {
			type = new Mailer(mailer);
		}
		setMailer(type);
		return type;
	}

	/**
	 * Gets the URLs. URLs can point to websites such as a personal homepage or
	 * business website.
	 * <p>
	 * <b>Property name:</b> {@code URL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the URLs
	 */
	public List<Url> getUrls() {
		return getProperties(Url.class);
	}

	/**
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website.
	 * <p>
	 * <b>Property name:</b> {@code URL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param url the URL to add
	 */
	public void addUrl(Url url) {
		addProperty(url);
	}

	/**
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website. This is a convenience method for {@link #addUrl(Url)}.
	 * <p>
	 * <b>Property name:</b> {@code URL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param url the URL to add (e.g. "http://example.com")
	 * @return the property object that was created
	 */
	public Url addUrl(String url) {
		Url type = new Url(url);
		addUrl(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a URL property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code URL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addUrlAlt(Collection<Url> altRepresentations) {
		addPropertyAlt(Url.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a URL property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code URL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addUrlAlt(Url... altRepresentations) {
		addPropertyAlt(Url.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all instances of the timezone property. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}) or if properties
	 * with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the timezones
	 */
	public List<Timezone> getTimezones() {
		return getProperties(Timezone.class);
	}

	/**
	 * <p>
	 * Gets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the timezone
	 */
	public Timezone getTimezone() {
		return getProperty(Timezone.class);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setTimezoneAlt(Collection<Timezone> altRepresentations) {
		setPropertyAlt(Timezone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setTimezoneAlt(Timezone... altRepresentations) {
		setPropertyAlt(Timezone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addTimezoneAlt(Collection<Timezone> altRepresentations) {
		addPropertyAlt(Timezone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addTimezoneAlt(Timezone... altRepresentations) {
		addPropertyAlt(Timezone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param timezone the timezone or null to remove
	 */
	public void setTimezone(Timezone timezone) {
		setProperty(Timezone.class, timezone);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param timezone the timezone or null to remove
	 */
	public void addTimezone(Timezone timezone) {
		addProperty(timezone);
	}

	/**
	 * <p>
	 * Gets all instances of the geo property. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}) or if properties
	 * with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the geo properties
	 */
	public List<Geo> getGeos() {
		return getProperties(Geo.class);
	}

	/**
	 * <p>
	 * Gets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the geographical position or null if one doesn't exist
	 */
	public Geo getGeo() {
		return getProperty(Geo.class);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works as a group
	 * of alternative representations (see: {@link VCardParameters#getAltId
	 * description of ALTID}). An appropriate ALTID parameter value is
	 * automatically generated and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setGeoAlt(Collection<Geo> altRepresentations) {
		setPropertyAlt(Geo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a geographical position of where the person lives/works as a group
	 * of alternative representations (see: {@link VCardParameters#getAltId
	 * description of ALTID}). An appropriate ALTID parameter value is
	 * automatically generated and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addGeoAlt(Collection<Geo> altRepresentations) {
		addPropertyAlt(Geo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a geographical position of where the person lives/works as a group
	 * of alternative representations (see: {@link VCardParameters#getAltId
	 * description of ALTID}). An appropriate ALTID parameter value is
	 * automatically generated and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addGeoAlt(Geo... altRepresentations) {
		addPropertyAlt(Geo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param geo the geographical position or null to remove
	 */
	public void setGeo(Geo geo) {
		setProperty(Geo.class, geo);
	}

	/**
	 * <p>
	 * Adds a geographical position of where the person lives/works. Note that
	 * only version 4.0 vCards support multiple instances of this property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param geo the geographical position
	 */
	public void addGeo(Geo geo) {
		addProperty(geo);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works. This is a
	 * convenience method for {@link #setGeo(Geo)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @return the property object that was created
	 */
	public Geo setGeo(double latitude, double longitude) {
		Geo type = new Geo(latitude, longitude);
		setGeo(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the organization property. Version 4.0 vCards may
	 * have multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}) or if properties
	 * with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the organization properties
	 */
	public List<Organization> getOrganizations() {
		return getProperties(Organization.class);
	}

	/**
	 * <p>
	 * Gets the hierarchy of department(s) to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the department(s)
	 */
	public Organization getOrganization() {
		return getProperty(Organization.class);
	}

	/**
	 * <p>
	 * Sets the organization property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setOrganizationAlt(Collection<Organization> altRepresentations) {
		setPropertyAlt(Organization.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the organization property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setOrganizationAlt(Organization... altRepresentations) {
		setPropertyAlt(Organization.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an organization property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addOrganizationAlt(Collection<Organization> altRepresentations) {
		addPropertyAlt(Organization.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an organization property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addOrganizationAlt(Organization... altRepresentations) {
		addPropertyAlt(Organization.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param organization the organization property or null to remove
	 */
	public void setOrganization(Organization organization) {
		setProperty(Organization.class, organization);
	}

	/**
	 * <p>
	 * Adds a hierarchy of departments to which the person belongs. Note that
	 * only version 4.0 vCards support multiple instances of this property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param organization the organization property
	 */
	public void addOrganization(Organization organization) {
		addProperty(organization);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs. This is a
	 * convenience method for {@link #setOrganization(Organization)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param departments the ordered list of department(s), starting with the
	 * broadest and ending with the most specific (e.g. "Google", "GMail Team",
	 * "Spam Detection Squad") or null to remove
	 * @return the property object that was created
	 */
	public Organization setOrganization(String... departments) {
		Organization type = null;
		if (departments != null) {
			type = new Organization();
			for (String department : departments) {
				type.addValue(department);
			}
		}
		setOrganization(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the categories property. Version 4.0 vCards may
	 * have multiple instances if alternative representations are defined (see:
	 * {@link VCardParameters#getAltId description of ALTID}) or if properties
	 * with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the categories properties
	 */
	public List<Categories> getCategoriesList() {
		return getProperties(Categories.class);
	}

	/**
	 * <p>
	 * Gets the list of keywords (aka "tags") that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the categories
	 */
	public Categories getCategories() {
		return getProperty(Categories.class);
	}

	/**
	 * <p>
	 * Sets the categories property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setCategoriesAlt(Collection<Categories> altRepresentations) {
		setPropertyAlt(Categories.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the categories property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void setCategoriesAlt(Categories... altRepresentations) {
		setPropertyAlt(Categories.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a categories property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCategoriesAlt(Collection<Categories> altRepresentations) {
		addPropertyAlt(Categories.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a categories property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCategoriesAlt(Categories... altRepresentations) {
		addPropertyAlt(Categories.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the list of keywords (aka "tags") that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param categories the categories or null to remove (note: multiple
	 * categories may be added to this object)
	 */
	public void setCategories(Categories categories) {
		setProperty(Categories.class, categories);
	}

	/**
	 * <p>
	 * Adds a list of keywords (aka "tags") that can be used to describe the
	 * person. Note that only version 4.0 vCards support multiple instances of
	 * this property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param categories the categories (note: multiple categories may be added
	 * to this object)
	 */
	public void addCategories(Categories categories) {
		addProperty(categories);
	}

	/**
	 * <p>
	 * Sets the list of keywords (aka "tags") that can be used to describe the
	 * person. This is a convenience method for
	 * {@link #setCategories(Categories)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param categories the category or categories (e.g. "swimmer", "biker",
	 * "knitter")
	 * @return the property object that was created
	 */
	public Categories setCategories(String... categories) {
		Categories type = null;
		if (categories != null) {
			type = new Categories();
			for (String category : categories) {
				type.addValue(category);
			}
		}
		setCategories(type);
		return type;
	}

	/**
	 * Gets information about the person's agent.
	 * <p>
	 * <b>Property name:</b> {@code AGENT}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the agent information
	 */
	public Agent getAgent() {
		return getProperty(Agent.class);
	}

	/**
	 * Sets information about the person's agent.
	 * <p>
	 * <b>Property name:</b> {@code AGENT}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param agent the agent information
	 */
	public void setAgent(Agent agent) {
		setProperty(Agent.class, agent);
	}

	/**
	 * Gets the notes. Notes contain free-form, miscellaneous text.
	 * <p>
	 * <b>Property name:</b> {@code NOTE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the notes
	 */
	public List<Note> getNotes() {
		return getProperties(Note.class);
	}

	/**
	 * Adds a note. Notes contain free-form, miscellaneous text.
	 * <p>
	 * <b>Property name:</b> {@code NOTE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param note the note to add
	 */
	public void addNote(Note note) {
		addProperty(note);
	}

	/**
	 * Adds a note. Notes contain free-form, miscellaneous text. This is a
	 * convenience method for {@link #addNote(Note)}.
	 * <p>
	 * <b>Property name:</b> {@code NOTE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param note the note to add
	 * @return the property object that was created
	 */
	public Note addNote(String note) {
		Note type = new Note(note);
		addNote(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a note property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NOTE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addNoteAlt(Collection<Note> altRepresentations) {
		addPropertyAlt(Note.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a note property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NOTE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addNoteAlt(Note... altRepresentations) {
		addPropertyAlt(Note.class, altRepresentations);
	}

	/**
	 * Gets the unique identifier of the vCard.
	 * <p>
	 * <b>Property name:</b> {@code UID}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the unique identifier
	 */
	public Uid getUid() {
		return getProperty(Uid.class);
	}

	/**
	 * Sets the unique identifier of the vCard.
	 * <p>
	 * <b>Property name:</b> {@code UID}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param uid the unique identifier
	 */
	public void setUid(Uid uid) {
		setProperty(Uid.class, uid);
	}

	/**
	 * Gets the public encryption keys.
	 * <p>
	 * <b>Property name:</b> {@code KEY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the keys
	 */
	public List<Key> getKeys() {
		return getProperties(Key.class);
	}

	/**
	 * Adds a public encryption key.
	 * <p>
	 * <b>Property name:</b> {@code KEY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param key the key to add
	 */
	public void addKey(Key key) {
		addProperty(key);
	}

	/**
	 * <p>
	 * Adds a key property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KEY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addKeyAlt(Collection<Key> altRepresentations) {
		addPropertyAlt(Key.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a key property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KEY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addKeyAlt(Key... altRepresentations) {
		addPropertyAlt(Key.class, altRepresentations);
	}

	/**
	 * Gets the instant messaging handles.
	 * <p>
	 * <b>Property name:</b> {@code IMPP}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the instant messaging handles
	 */
	public List<Impp> getImpps() {
		return getProperties(Impp.class);
	}

	/**
	 * Adds an instant messaging handle.
	 * <p>
	 * <b>Property name:</b> {@code IMPP}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param impp the instant messaging handle to add
	 */
	public void addImpp(Impp impp) {
		addProperty(impp);
	}

	/**
	 * <p>
	 * Adds an impp property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code IMPP}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addImppAlt(Collection<Impp> altRepresentations) {
		addPropertyAlt(Impp.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an impp property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code IMPP}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addImppAlt(Impp... altRepresentations) {
		addPropertyAlt(Impp.class, altRepresentations);
	}

	/**
	 * Gets a list of people that the person is related to.
	 * <p>
	 * <b>Property name:</b> {@code RELATED}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the person's relations
	 */
	public List<Related> getRelations() {
		return getProperties(Related.class);
	}

	/**
	 * Adds someone that the person is related to.
	 * <p>
	 * <b>Property name:</b> {@code RELATED}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param related the relation to add
	 */
	public void addRelated(Related related) {
		addProperty(related);
	}

	/**
	 * <p>
	 * Adds a related property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code RELATED}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addRelatedAlt(Collection<Related> altRepresentations) {
		addPropertyAlt(Related.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a related property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code RELATED}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addRelatedAlt(Related... altRepresentations) {
		addPropertyAlt(Related.class, altRepresentations);
	}

	/**
	 * Gets the languages that the person speaks.
	 * <p>
	 * <b>Property name:</b> {@code LANG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the languages
	 */
	public List<Language> getLanguages() {
		return getProperties(Language.class);
	}

	/**
	 * Adds a language that the person speaks.
	 * <p>
	 * <b>Property name:</b> {@code LANG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param language the language to add
	 */
	public void addLanguage(Language language) {
		addProperty(language);
	}

	/**
	 * Adds a language that the person speaks. This is a convenience method for
	 * {@link #addLanguage(Language)}.
	 * <p>
	 * <b>Property name:</b> {@code LANG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param language the language to add (e.g. "en-us")
	 * @return the property object that was created
	 */
	public Language addLanguage(String language) {
		Language type = new Language(language);
		addLanguage(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a language property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LANG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addLanguageAlt(Collection<Language> altRepresentations) {
		addPropertyAlt(Language.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a language property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LANG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addLanguageAlt(Language... altRepresentations) {
		addPropertyAlt(Language.class, altRepresentations);
	}

	/**
	 * Gets the URIs that can be used to schedule a meeting with the person on
	 * his or her calendar.
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the calendar request URIs
	 */
	public List<CalendarRequestUri> getCalendarRequestUris() {
		return getProperties(CalendarRequestUri.class);
	}

	/**
	 * Adds a URI that can be used to schedule a meeting with the person on his
	 * or her calendar.
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param calendarRequestUri the calendar request URI to add
	 */
	public void addCalendarRequestUri(CalendarRequestUri calendarRequestUri) {
		addProperty(calendarRequestUri);
	}

	/**
	 * <p>
	 * Adds a calendar request URI property as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCalendarRequestUriAlt(Collection<CalendarRequestUri> altRepresentations) {
		addPropertyAlt(CalendarRequestUri.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a calendar request URI property as a group of alternative
	 * representations (see: {@link VCardParameters#getAltId description of
	 * ALTID} ). An appropriate ALTID parameter value is automatically generated
	 * and assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCalendarRequestUriAlt(CalendarRequestUri... altRepresentations) {
		addPropertyAlt(CalendarRequestUri.class, altRepresentations);
	}

	/**
	 * Gets the URIs that point to the person's calendar.
	 * <p>
	 * <b>Property name:</b> {@code CALURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the calendar URIs
	 */
	public List<CalendarUri> getCalendarUris() {
		return getProperties(CalendarUri.class);
	}

	/**
	 * Adds a URI that points to the person's calendar.
	 * <p>
	 * <b>Property name:</b> {@code CALURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param calendarUri the calendar URI to add
	 */
	public void addCalendarUri(CalendarUri calendarUri) {
		addProperty(calendarUri);
	}

	/**
	 * <p>
	 * Adds a calendar URI property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCalendarUriAlt(Collection<CalendarUri> altRepresentations) {
		addPropertyAlt(CalendarUri.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a calendar URI property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCalendarUriAlt(CalendarUri... altRepresentations) {
		addPropertyAlt(CalendarUri.class, altRepresentations);
	}

	/**
	 * Gets the URLs that can be used to determine when the person is free
	 * and/or busy.
	 * <p>
	 * <b>Property name:</b> {@code FBURL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the free-busy URLs
	 */
	public List<FreeBusyUrl> getFbUrls() {
		return getProperties(FreeBusyUrl.class);
	}

	/**
	 * Adds a URL that can be used to determine when the person is free and/or
	 * busy.
	 * <p>
	 * <b>Property name:</b> {@code FBURL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param fbUrl the free-busy URL to add
	 */
	public void addFbUrl(FreeBusyUrl fbUrl) {
		addProperty(fbUrl);
	}

	/**
	 * <p>
	 * Adds an fburl property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FBURL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addFbUrlAlt(Collection<FreeBusyUrl> altRepresentations) {
		addPropertyAlt(FreeBusyUrl.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an fburl property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FBURL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addFbUrlAlt(FreeBusyUrl... altRepresentations) {
		addPropertyAlt(FreeBusyUrl.class, altRepresentations);
	}

	/**
	 * Gets the properties that are used to assign globally-unique identifiers
	 * to individual property instances. CLIENTPIDMAPs are used for merging
	 * together different versions of the same vCard.
	 * <p>
	 * <b>Property name:</b> {@code CLIENTPIDMAP}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the client PID maps
	 */
	public List<ClientPidMap> getClientPidMaps() {
		return getProperties(ClientPidMap.class);
	}

	/**
	 * Adds a property that is used to assign a globally-unique identifier to an
	 * individual property instance. CLIENTPIDMAPs are used for merging together
	 * different versions of the same vCard.
	 * <p>
	 * <b>Property name:</b> {@code CLIENTPIDMAP}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param clientPidMap the client PID map to add
	 */
	public void addClientPidMap(ClientPidMap clientPidMap) {
		addProperty(clientPidMap);
	}

	/**
	 * Gets any XML data that is attached to the vCard. XML properties may be
	 * present if the vCard was encoded in XML and the XML document contained
	 * non-standard elements. The XML vCard properties in this case would
	 * contain all of the non-standard XML elements.
	 * <p>
	 * <b>Property name:</b> {@code XML}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the XML data
	 */
	public List<Xml> getXmls() {
		return getProperties(Xml.class);
	}

	/**
	 * Adds XML data to the vCard. XML properties may be present if the vCard
	 * was encoded in XML and the XML document contained non-standard elements.
	 * The XML vCard properties in this case would contain all of the
	 * non-standard XML elements.
	 * <p>
	 * <b>Property name:</b> {@code XML}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param xml the XML data to add
	 */
	public void addXml(Xml xml) {
		addProperty(xml);
	}

	/**
	 * <p>
	 * Adds an XML property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code XML}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addXmlAlt(Collection<Xml> altRepresentations) {
		addPropertyAlt(Xml.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an XML property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code XML}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addXmlAlt(Xml... altRepresentations) {
		addPropertyAlt(Xml.class, altRepresentations);
	}

	/**
	 * Gets the professional subject areas of which the the person is
	 * knowledgeable.
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the professional skills
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<Expertise> getExpertise() {
		return getProperties(Expertise.class);
	}

	/**
	 * Adds a professional subject area of which the the person is
	 * knowledgeable.
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param expertise the professional skill to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addExpertise(Expertise expertise) {
		addProperty(expertise);
	}

	/**
	 * Adds a professional subject area of which the the person is
	 * knowledgeable. This is a convenience method for
	 * {@link #addExpertise(Expertise)}.
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param expertise the professional skill to add (e.g. "programming")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public Expertise addExpertise(String expertise) {
		Expertise type = new Expertise(expertise);
		addExpertise(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an expertise property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addExpertiseAlt(Collection<Expertise> altRepresentations) {
		addPropertyAlt(Expertise.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an expertise property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addExpertiseAlt(Expertise... altRepresentations) {
		addPropertyAlt(Expertise.class, altRepresentations);
	}

	/**
	 * Gets the hobbies that the person actively engages in.
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the hobbies
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<Hobby> getHobbies() {
		return getProperties(Hobby.class);
	}

	/**
	 * Adds a hobby that the person actively engages in.
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param hobby the hobby to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addHobby(Hobby hobby) {
		addProperty(hobby);
	}

	/**
	 * Adds a hobby that the person actively engages in. This is a convenience
	 * method for {@link #addHobby(Hobby)}.
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param hobby the hobby to add (e.g. "photography")
	 * @return the type objec that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public Hobby addHobby(String hobby) {
		Hobby type = new Hobby(hobby);
		addHobby(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a hobby property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addHobbyAlt(Collection<Hobby> altRepresentations) {
		addPropertyAlt(Hobby.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a hobby property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addHobbyAlt(Hobby... altRepresentations) {
		addPropertyAlt(Hobby.class, altRepresentations);
	}

	/**
	 * Gets the person's interests.
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the interests
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<Interest> getInterests() {
		return getProperties(Interest.class);
	}

	/**
	 * Adds an interest.
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param interest the interest to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addInterest(Interest interest) {
		addProperty(interest);
	}

	/**
	 * Adds an interest. This is a convenience method for
	 * {@link #addInterest(Interest)}.
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param interest the interest to add (e.g. "football")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public Interest addInterest(String interest) {
		Interest type = new Interest(interest);
		addInterest(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an interest property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addInterestAlt(Collection<Interest> altRepresentations) {
		addPropertyAlt(Interest.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an interest property as a group of alternative representations (see:
	 * {@link VCardParameters#getAltId description of ALTID}). An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the property
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addInterestAlt(Interest... altRepresentations) {
		addPropertyAlt(Interest.class, altRepresentations);
	}

	/**
	 * Gets the organization directories.
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the organization directories
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<OrgDirectory> getOrgDirectories() {
		return getProperties(OrgDirectory.class);
	}

	/**
	 * Adds an organization directory.
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param orgDirectory the organization directory to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addOrgDirectory(OrgDirectory orgDirectory) {
		addProperty(orgDirectory);
	}

	/**
	 * Adds an organization directory. This is a convenience method for
	 * {@link #addOrgDirectory(OrgDirectory)}.
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param orgDirectory the organization directory to add (e.g.
	 * "http://company.com/staff")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public OrgDirectory addOrgDirectory(String orgDirectory) {
		OrgDirectory type = new OrgDirectory(orgDirectory);
		addOrgDirectory(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an org directory property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addOrgDirectoryAlt(Collection<OrgDirectory> altRepresentations) {
		addPropertyAlt(OrgDirectory.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an org directory property as a group of alternative representations
	 * (see: {@link VCardParameters#getAltId description of ALTID}). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addOrgDirectoryAlt(OrgDirectory... altRepresentations) {
		addPropertyAlt(OrgDirectory.class, altRepresentations);
	}

	/**
	 * Iterates through each of the vCard's properties in no particular order.
	 * Does not include the "BEGIN", "END", or "VERSION" properties.
	 * @return the iterator
	 */
	public Iterator<VCardProperty> iterator() {
		return properties.values().iterator();
	}

	/**
	 * Gets the first property of a given class.
	 * @param clazz the property class
	 * @return the property or null if not found
	 */
	public <T extends VCardProperty> T getProperty(Class<T> clazz) {
		return clazz.cast(properties.first(clazz));
	}

	/**
	 * Gets all properties of a given class.
	 * @param clazz the property class
	 * @return the properties
	 */
	public <T extends VCardProperty> List<T> getProperties(Class<T> clazz) {
		List<VCardProperty> props = properties.get(clazz);

		//cast to the requested class
		List<T> ret = new ArrayList<T>(props.size());
		for (VCardProperty property : props) {
			ret.add(clazz.cast(property));
		}
		return ret;
	}

	/**
	 * Gets all properties of a given class, grouping the alternative
	 * representations of each property together (see:
	 * {@link VCardParameters#getAltId description of ALTID})
	 * @param clazz the property class
	 * @return the properties
	 */
	public <T extends VCardProperty & HasAltId> List<List<T>> getPropertiesAlt(Class<T> clazz) {
		List<T> nullAltId = new ArrayList<T>();
		ListMultimap<String, T> map = new ListMultimap<String, T>();
		for (T property : getProperties(clazz)) {
			String altId = property.getAltId();
			if (altId == null) {
				nullAltId.add(property);
			} else {
				map.put(altId, property);
			}
		}

		List<List<T>> list = new ArrayList<List<T>>();
		for (Map.Entry<String, List<T>> entry : map) {
			list.add(entry.getValue());
		}

		//put properties without ALTIDs at the end
		for (T property : nullAltId) {
			List<T> l = new ArrayList<T>(1);
			l.add(property);
			list.add(l);
		}

		return list;
	}

	/**
	 * Gets all the properties in this vCard.
	 * @return the properties
	 */
	public Collection<VCardProperty> getProperties() {
		return properties.values();
	}

	/**
	 * Adds a property.
	 * @param property the property to add
	 */
	public void addProperty(VCardProperty property) {
		properties.put(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given class with a single
	 * property instance. If the property instance is null, then all instances
	 * of that property will be removed.
	 * @param clazz the property class (e.g. "Note.class")
	 * @param property the property or null to remove
	 */
	public <T extends VCardProperty> void setProperty(Class<T> clazz, T property) {
		properties.replace(clazz, property);
	}

	/**
	 * Removes a property instance from the vCard.
	 * @param property the property to remove
	 */
	public void removeProperty(VCardProperty property) {
		properties.remove(property.getClass(), property);
	}

	/**
	 * Removes all properties of a given class.
	 * @param clazz the class of the properties to remove (e.g. "Note.class")
	 */
	public void removeProperties(Class<? extends VCardProperty> clazz) {
		properties.removeAll(clazz);
	}

	/**
	 * Gets the first extended property with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the property or null if none were found
	 */
	public RawProperty getExtendedProperty(String name) {
		for (RawProperty raw : getProperties(RawProperty.class)) {
			if (raw.getPropertyName().equalsIgnoreCase(name)) {
				return raw;
			}
		}
		return null;
	}

	/**
	 * Gets all extended properties with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the properties
	 */
	public List<RawProperty> getExtendedProperties(String name) {
		List<RawProperty> props = new ArrayList<RawProperty>();

		for (RawProperty raw : getProperties(RawProperty.class)) {
			if (raw.getPropertyName().equalsIgnoreCase(name)) {
				props.add(raw);
			}
		}

		return props;
	}

	/**
	 * Gets all extended properties.
	 * @return the properties
	 */
	public List<RawProperty> getExtendedProperties() {
		return getProperties(RawProperty.class);
	}

	/**
	 * Adds an extended property.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty addExtendedProperty(String name, String value) {
		RawProperty raw = new RawProperty(name, value);
		addProperty(raw);
		return raw;
	}

	/**
	 * Replaces all existing extended properties with the given name with a
	 * single property instance.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty setExtendedProperty(String name, String value) {
		removeExtendedProperty(name);
		RawProperty raw = new RawProperty(name, value);
		addProperty(raw);
		return raw;
	}

	/**
	 * Removes all extended properties that have the given name.
	 * @param name the component name (e.g. "X-ALT-DESC")
	 */
	public void removeExtendedProperty(String name) {
		List<RawProperty> xproperties = getExtendedProperties(name);
		for (RawProperty xproperty : xproperties) {
			properties.remove(xproperty.getClass(), xproperty);
		}
	}

	/**
	 * Adds a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardProperty & HasAltId> void addPropertyAlt(Class<T> propertyClass, T... altRepresentations) {
		addPropertyAlt(propertyClass, Arrays.asList(altRepresentations));
	}

	/**
	 * Adds a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardProperty & HasAltId> void addPropertyAlt(Class<T> propertyClass, Collection<T> altRepresentations) {
		String altId = generateAltId(getProperties(propertyClass));
		for (T property : altRepresentations) {
			property.setAltId(altId);
			addProperty(property);
		}
	}

	/**
	 * Sets a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardProperty & HasAltId> void setPropertyAlt(Class<T> propertyClass, T... altRepresentations) {
		setPropertyAlt(propertyClass, Arrays.asList(altRepresentations));
	}

	/**
	 * Sets a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardProperty & HasAltId> void setPropertyAlt(Class<T> propertyClass, Collection<T> altRepresentations) {
		removeProperties(propertyClass);
		addPropertyAlt(propertyClass, altRepresentations);
	}

	/**
	 * Checks this vCard for data consistency problems or deviations from the
	 * spec. These problems will not prevent the vCard from being written to a
	 * data stream, but may prevent it from being parsed correctly by the
	 * consuming application. These problems can largely be avoided by reading
	 * the Javadocs of the property classes, or by being familiar with the vCard
	 * standard.
	 * @param version the version to check the vCard against (use 4.0 for xCard
	 * and jCard)
	 * @return the validation warnings
	 */
	public ValidationWarnings validate(VCardVersion version) {
		ValidationWarnings warnings = new ValidationWarnings();

		//validate overall vCard object
		if (getStructuredName() == null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
			warnings.add(null, new Warning(0));
		}
		if (getFormattedName() == null && (version == VCardVersion.V3_0 || version == VCardVersion.V4_0)) {
			warnings.add(null, new Warning(1));
		}

		//validate properties
		for (VCardProperty property : this) {
			List<Warning> propWarnings = property.validate(version, this);
			if (!propWarnings.isEmpty()) {
				warnings.add(property, propWarnings);
			}
		}

		return warnings;
	}

	/**
	 * Generates a unique ALTID parameter value.
	 * @param properties the collection of properties under which the ALTID must
	 * be unique
	 * @return a unique ALTID
	 */
	static <T extends HasAltId> String generateAltId(Collection<T> properties) {
		Set<String> altIds = new HashSet<String>();
		for (T property : properties) {
			String altId = property.getAltId();
			if (altId != null) {
				altIds.add(altId);
			}
		}

		int altId = 1;
		while (altIds.contains(altId + "")) {
			altId++;
		}
		return altId + "";
	}
}