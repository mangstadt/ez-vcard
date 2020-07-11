package ezvcard;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import ezvcard.io.html.HCardPage;
import ezvcard.io.json.JCardWriter;
import ezvcard.io.text.VCardWriter;
import ezvcard.io.xml.XCardWriter;
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
import ezvcard.util.StringUtils;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
	private VCardVersion version;
	private final ListMultimap<Class<? extends VCardProperty>, VCardProperty> properties = new ListMultimap<Class<? extends VCardProperty>, VCardProperty>();

	/**
	 * Creates a new vCard set to version 3.0.
	 */
	public VCard() {
		this(VCardVersion.V3_0);
	}

	/**
	 * Creates a new vCard.
	 * @param version the version to assign to the vCard
	 */
	public VCard(VCardVersion version) {
		this.version = version;
	}

	/**
	 * Creates a deep copy of the given vCard.
	 * @param original the vCard to copy
	 */
	public VCard(VCard original) {
		version = original.version;
		for (VCardProperty property : original.getProperties()) {
			addProperty(property.copy());
		}
	}

	/**
	 * <p>
	 * Marshals this vCard to its text representation.
	 * </p>
	 * <p>
	 * The vCard will be marshalled to whatever vCard version is assigned to
	 * this object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link VCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @return the vCard string
	 * @see VCardWriter
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
	 * The vCard will be marshalled to whatever vCard version is assigned to
	 * this object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link VCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem writing to the file
	 * @see VCardWriter
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
	 * The vCard will be marshalled to whatever vCard version is assigned to
	 * this object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link VCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write the vCard to
	 * @see VCardWriter
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
	 * The vCard will be marshalled to whatever vCard version is assigned to
	 * this object (see {@link #setVersion(VCardVersion)}). If no version is
	 * set, then it will be marshalled to 3.0.
	 * </p>
	 * <p>
	 * Use the {@link VCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write the vCard to
	 * @throws IOException if there's a problem writing to the writer
	 * @see VCardWriter
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
	 * Use the {@link XCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @return the vCard XML document
	 * @see XCardWriter
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
	 * Use the {@link XCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the vCard
	 * @see XCardWriter
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
	 * Use the {@link XCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write the vCard to
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 * @see XCardWriter
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
	 * Use the {@link XCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write the vCard to
	 * @throws TransformerException if there's a problem writing to the writer
	 * @see XCardWriter
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
	 * Use the {@link HCardPage} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @return the HTML page
	 * @see HCardPage
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
	 * Use the {@link HCardPage} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write to
	 * @throws IOException if there's a problem writing to the file
	 * @see HCardPage
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
	 * Use the {@link HCardPage} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write to
	 * @throws IOException if there's a problem writing to the output stream
	 * @see HCardPage
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
	 * Use the {@link HCardPage} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write to
	 * @throws IOException if there's a problem writing to the writer
	 * @see HCardPage
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
	 * Use the {@link JCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @return the JSON string
	 * @see JCardWriter
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
	 * Use the {@link JCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param file the file to write the vCard to
	 * @throws IOException if there's a problem writing to the file
	 * @see JCardWriter
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
	 * Use the {@link JCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param out the output stream to write the vCard to
	 * @see JCardWriter
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
	 * Use the {@link JCardWriter} class for more control over the marshalling
	 * process and to write multiple vCards to the same stream.
	 * </p>
	 * @param writer the writer to write the vCard to
	 * @throws IOException if there's a problem writing to the writer
	 * @see JCardWriter
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
	 * <p>
	 * Sets the version of this vCard.
	 * </p>
	 * <p>
	 * When marshalling a vCard with the {@link VCardWriter} class, use the
	 * {@link VCardWriter#setTargetVersion setTargetVersion} method to define
	 * what version the vCard should be marshalled as. {@link VCardWriter}
	 * <b>does not</b> look at the version that is set on the VCard object.
	 * </p>
	 * @param version the vCard version
	 */
	public void setVersion(VCardVersion version) {
		this.version = version;
	}

	/**
	 * <p>
	 * Gets the type of entity this vCard represents.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KIND}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the kind property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-25">RFC 6350
	 * p.25</a>
	 */
	public Kind getKind() {
		return getProperty(Kind.class);
	}

	/**
	 * <p>
	 * Sets the type of entity this vCard represents.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KIND}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param kind the kind property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-25">RFC 6350
	 * p.25</a>
	 */
	public void setKind(Kind kind) {
		setProperty(Kind.class, kind);
	}

	/**
	 * <p>
	 * Gets the gender of the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GENDER}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the gender property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-32">RFC 6350
	 * p.32</a>
	 */
	public Gender getGender() {
		return getProperty(Gender.class);
	}

	/**
	 * <p>
	 * Sets the gender of the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GENDER}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param gender the gender property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-32">RFC 6350
	 * p.32</a>
	 */
	public void setGender(Gender gender) {
		setProperty(Gender.class, gender);
	}

	/**
	 * <p>
	 * Gets the members of the group that this vCard represents.
	 * </p>
	 * <p>
	 * Note: If a vCard has any {@link Member} properties, then it must also
	 * have a {@link Kind} property whose value is set to "group".
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the members properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-41">RFC 6350
	 * p.41</a>
	 */
	public List<Member> getMembers() {
		return getProperties(Member.class);
	}

	/**
	 * <p>
	 * Adds a member to the group that this vCard represents.
	 * </p>
	 * <p>
	 * Note: If a vCard has any {@link Member} properties, then it must also
	 * have a {@link Kind} property whose value is set to "group".
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param member the member property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-41">RFC 6350
	 * p.41</a>
	 */
	public void addMember(Member member) {
		addProperty(member);
	}

	/**
	 * <p>
	 * Adds a member to the group that this vCard represents.
	 * </p>
	 * <p>
	 * Note: If a vCard has any {@link Member} properties, then it must also
	 * have a {@link Kind} property whose value is set to "group".
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MEMBER}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-41">RFC 6350
	 * p.41</a>
	 */
	public void addMemberAlt(Member... altRepresentations) {
		addPropertyAlt(Member.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the profile property. This property simply identifies the vCard as a
	 * vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PROFILE}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the profile property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public Profile getProfile() {
		return getProperty(Profile.class);
	}

	/**
	 * <p>
	 * Sets the profile property. This property simply identifies the vCard as a
	 * vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PROFILE}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 * @param profile the profile property or null to remove
	 */
	public void setProfile(Profile profile) {
		setProperty(Profile.class, profile);
	}

	/**
	 * <p>
	 * Gets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CLASS}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the classification property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426
	 * p.26</a>
	 */
	public Classification getClassification() {
		return getProperty(Classification.class);
	}

	/**
	 * <p>
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CLASS}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param classification the classification property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426
	 * p.26</a>
	 */
	public void setClassification(Classification classification) {
		setProperty(Classification.class, classification);
	}

	/**
	 * <p>
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}<br>
	 * <b>Property name:</b> {@code CLASS}
	 * </p>
	 * @param classification the classification (e.g. "PUBLIC", "PRIVATE",
	 * "CONFIDENTIAL") or null to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426
	 * p.26</a>
	 */
	public Classification setClassification(String classification) {
		Classification type = (classification == null) ? null : new Classification(classification);
		setClassification(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the URIs that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOURCE} <b>Supported versions:</b>
	 * {@code 3.0, 4.0}
	 * </p>
	 * @return the source properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-24">RFC 6350
	 * p.24</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public List<Source> getSources() {
		return getProperties(Source.class);
	}

	/**
	 * <p>
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOURCE} <b>Supported versions:</b>
	 * {@code 3.0, 4.0}
	 * </p>
	 * @param source the source property
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-24">RFC 6350
	 * p.24</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public void addSource(Source source) {
		addProperty(source);
	}

	/**
	 * <p>
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param source the source URI (e.g. "http://example.com/vcard.vcf")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-24">RFC 6350
	 * p.24</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public Source addSource(String source) {
		Source type = new Source(source);
		addSource(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-24">RFC 6350
	 * p.24</a>
	 */
	public void addSourceAlt(Source... altRepresentations) {
		addPropertyAlt(Source.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets a textual representation of the {@link Source} property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NAME}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public SourceDisplayText getSourceDisplayText() {
		return getProperty(SourceDisplayText.class);
	}

	/**
	 * <p>
	 * Sets a textual representation of the {@link Source} property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NAME}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sourceDisplayText the property null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public void setSourceDisplayText(SourceDisplayText sourceDisplayText) {
		setProperty(SourceDisplayText.class, sourceDisplayText);
	}

	/**
	 * <p>
	 * Sets a textual representation of the {@link Source} property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NAME}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sourceDisplayText a textual representation of the vCard source or
	 * null to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-5">RFC 2426 p.5</a>
	 */
	public SourceDisplayText setSourceDisplayText(String sourceDisplayText) {
		SourceDisplayText type = (sourceDisplayText == null) ? null : new SourceDisplayText(sourceDisplayText);
		setSourceDisplayText(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the {@link FormattedName} property.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see
	 * {@link #addFormattedNameAlt(FormattedName...) addFormattedNameAlt}) or if
	 * properties with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the formatted name properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public List<FormattedName> getFormattedNames() {
		return getProperties(FormattedName.class);
	}

	/**
	 * <p>
	 * Gets the person's full name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the first formatted name property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public FormattedName getFormattedName() {
		return getProperty(FormattedName.class);
	}

	/**
	 * <p>
	 * Sets the person's full name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public void setFormattedNameAlt(FormattedName... altRepresentations) {
		setPropertyAlt(FormattedName.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a version of the person's full name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public void addFormattedNameAlt(FormattedName... altRepresentations) {
		addPropertyAlt(FormattedName.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's full name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param formattedName the formatted name property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public void setFormattedName(FormattedName formattedName) {
		setProperty(FormattedName.class, formattedName);
	}

	/**
	 * <p>
	 * Adds a version of the person's full name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param formattedName the formatted name property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public void addFormattedName(FormattedName formattedName) {
		addProperty(formattedName);
	}

	/**
	 * <p>
	 * Sets the person's full name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param formattedName the formatted name (e.g. "John Doe") or null to
	 * remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-28">RFC 6350
	 * p.28</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-8">RFC 2426 p.8</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public FormattedName setFormattedName(String formattedName) {
		FormattedName type = (formattedName == null) ? null : new FormattedName(formattedName);
		setFormattedName(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all structured name properties.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see
	 * {@link #setStructuredNameAlt(StructuredName...) setStructuredNameAlt}) or
	 * if properties with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @return the structured name properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public List<StructuredName> getStructuredNames() {
		return getProperties(StructuredName.class);
	}

	/**
	 * <p>
	 * Gets the individual components of the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the first structured name property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public StructuredName getStructuredName() {
		return getProperty(StructuredName.class);
	}

	/**
	 * <p>
	 * Sets the individual components of the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code N}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.9</a>
	 */
	public void setStructuredNameAlt(StructuredName... altRepresentations) {
		setPropertyAlt(StructuredName.class, altRepresentations);
	}

	/**
	 * Sets the individual components of the person's name.
	 * <p>
	 * <b>Property name:</b> {@code N}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param structuredName the structured name property or null to remove
	 */
	public void setStructuredName(StructuredName structuredName) {
		setProperty(StructuredName.class, structuredName);
	}

	/**
	 * <p>
	 * Gets all instances of the {@link Nickname} property.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see {@link #setNicknameAlt(Nickname...)
	 * setNicknameAlt}) or if properties with different TYPE parameters are
	 * defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the nickname properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public List<Nickname> getNicknames() {
		return getProperties(Nickname.class);
	}

	/**
	 * <p>
	 * Gets the person's nicknames.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the nickname property (may contain multiple values) or null if
	 * not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public Nickname getNickname() {
		return getProperty(Nickname.class);
	}

	/**
	 * <p>
	 * Sets the person's nicknames.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public void setNicknameAlt(Nickname... altRepresentations) {
		setPropertyAlt(Nickname.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a collection of nicknames for the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public void addNicknameAlt(Nickname... altRepresentations) {
		addPropertyAlt(Nickname.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's nicknames.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param nickname the nickname property (may contain multiple values) or
	 * null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public void setNickname(Nickname nickname) {
		setProperty(Nickname.class, nickname);
	}

	/**
	 * <p>
	 * Adds a set of nicknames for the person.
	 * </p>
	 * <p>
	 * Note that only version 4.0 vCards support multiple instances of this
	 * property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param nickname the nickname property (may contain multiple values)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public void addNickname(Nickname nickname) {
		addProperty(nickname);
	}

	/**
	 * <p>
	 * Sets the person's nicknames.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param nicknames the nicknames (e.g. "John", "Jon")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-29">RFC 6350
	 * p.29</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-9">RFC 2426 p.9</a>
	 */
	public Nickname setNickname(String... nicknames) {
		Nickname property = null;
		if (nicknames != null && nicknames.length > 0 && nicknames[0] != null) {
			property = new Nickname();
			property.getValues().addAll(Arrays.asList(nicknames));
		}
		setNickname(property);
		return property;
	}

	/**
	 * <p>
	 * Gets the string that should be used to sort the vCard. This typically set
	 * to the person's family name (last name).
	 * </p>
	 * <p>
	 * For 4.0 vCards, this information is stored in the
	 * {@link StructuredName#getSortAs} and/or {@link Organization#getSortAs}
	 * methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @return the sort string property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426
	 * p.22</a>
	 */
	public SortString getSortString() {
		return getProperty(SortString.class);
	}

	/**
	 * <p>
	 * Sets the string that should be used to sort the vCard. This typically set
	 * to the person's family name (last name).
	 * </p>
	 * <p>
	 * For 4.0 vCards, this information is stored in the
	 * {@link StructuredName#getSortAs} and/or {@link Organization#getSortAs}
	 * methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sortString the sort string property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426
	 * p.22</a>
	 */
	public void setSortString(SortString sortString) {
		setProperty(SortString.class, sortString);
	}

	/**
	 * <p>
	 * Sets the string that should be used to sort the vCard. This typically set
	 * to the person's family name (last name).
	 * </p>
	 * <p>
	 * For 4.0 vCards, this information is stored in the
	 * {@link StructuredName#getSortAs} and/or {@link Organization#getSortAs}
	 * methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}<br>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sortString the sort string (e.g. "Armour" if the person's last
	 * name is "d'Armour") or null to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426
	 * p.22</a>
	 */
	public SortString setSortString(String sortString) {
		SortString type = (sortString == null) ? null : new SortString(sortString);
		setSortString(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the titles associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TITLE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the title properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-17">RFC 2426
	 * p.17</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public List<Title> getTitles() {
		return getProperties(Title.class);
	}

	/**
	 * <p>
	 * Adds a title associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TITLE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param title the title property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-17">RFC 2426
	 * p.17</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public void addTitle(Title title) {
		addProperty(title);
	}

	/**
	 * <p>
	 * Adds a title associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TITLE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param title the title to add (e.g. "V.P. Research and Development")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-17">RFC 2426
	 * p.17</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public Title addTitle(String title) {
		Title type = new Title(title);
		addTitle(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a title associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TITLE}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-17">RFC 2426
	 * p.17</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public void addTitleAlt(Title... altRepresentations) {
		addPropertyAlt(Title.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the roles associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ROLE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the role properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public List<Role> getRoles() {
		return getProperties(Role.class);
	}

	/**
	 * <p>
	 * Adds a role associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ROLE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param role the role property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public void addRole(Role role) {
		addProperty(role);
	}

	/**
	 * <p>
	 * Adds a role associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ROLE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param role the role to add (e.g. "Executive")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public Role addRole(String role) {
		Role type = new Role(role);
		addRole(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a role associated with the person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ROLE}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-39">RFC 6350
	 * p.39</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public void addRoleAlt(Role... altRepresentations) {
		addPropertyAlt(Role.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the photos attached to the vCard, such as the person's portrait.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PHOTO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the photo properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-10">RFC 2426
	 * p.10</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.10</a>
	 */
	public List<Photo> getPhotos() {
		return getProperties(Photo.class);
	}

	/**
	 * <p>
	 * Adds a photo to the vCard, such as the person's portrait.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PHOTO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param photo the photo property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-10">RFC 2426
	 * p.10</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.10</a>
	 */
	public void addPhoto(Photo photo) {
		addProperty(photo);
	}

	/**
	 * <p>
	 * Adds a photo to the vCard, such as the person's portrait.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PHOTO}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-10">RFC 2426
	 * p.10</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.10</a>
	 */
	public void addPhotoAlt(Photo... altRepresentations) {
		addPropertyAlt(Photo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the logos attached to the vCard, such a company logo.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LOGO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the logo properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public List<Logo> getLogos() {
		return getProperties(Logo.class);
	}

	/**
	 * <p>
	 * Adds a logo to the vCard, such as a company logo.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LOGO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param logo the logo property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public void addLogo(Logo logo) {
		addProperty(logo);
	}

	/**
	 * <p>
	 * Adds a logo to the vCard, such as a company logo.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LOGO}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-18">RFC 2426
	 * p.18</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.17</a>
	 */
	public void addLogoAlt(Logo... altRepresentations) {
		addPropertyAlt(Logo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the sounds attached to the vCard, such as a pronunciation of the
	 * person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOUND}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the sound properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350
	 * p.45</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-23">RFC 2426
	 * p.23</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public List<Sound> getSounds() {
		return getProperties(Sound.class);
	}

	/**
	 * <p>
	 * Adds a sound to the vCard, such as a pronunciation of the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOUND}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param sound the sound property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350
	 * p.45</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-23">RFC 2426
	 * p.23</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public void addSound(Sound sound) {
		addProperty(sound);
	}

	/**
	 * <p>
	 * Adds a sound to the vCard, such as a pronunciation of the person's name.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SOUND}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350
	 * p.45</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-23">RFC 2426
	 * p.23</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.20</a>
	 */
	public void addSoundAlt(Sound... altRepresentations) {
		addPropertyAlt(Sound.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all {@link Birthplace} property instances.
	 * </p>
	 * <p>
	 * There may be multiple instances if alternative representations are
	 * defined (see {@link #setBirthplaceAlt(Birthplace...) setBirthplaceAlt}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the birthplace properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-2">RFC 6474 p.2</a>
	 */
	public List<Birthplace> getBirthplaces() {
		return getProperties(Birthplace.class);
	}

	/**
	 * <p>
	 * Gets the person's birthplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the first birthplace property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-2">RFC 6474 p.2</a>
	 */
	public Birthplace getBirthplace() {
		return getProperty(Birthplace.class);
	}

	/**
	 * <p>
	 * Sets the person's birthplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-2">RFC 6474 p.2</a>
	 */
	public void setBirthplaceAlt(Birthplace... altRepresentations) {
		setPropertyAlt(Birthplace.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthplace.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BIRTHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param birthplace the birthplace property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-2">RFC 6474 p.2</a>
	 */
	public void setBirthplace(Birthplace birthplace) {
		setProperty(Birthplace.class, birthplace);
	}

	/**
	 * <p>
	 * Gets all {@link Deathplace} property instances.
	 * </p>
	 * <p>
	 * There may be multiple instances if alternative representations are
	 * defined (see {@link #setDeathplaceAlt(Deathplace...) setDeathplaceAlt}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the deathplace properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-3">RFC 6474 p.3</a>
	 */
	public List<Deathplace> getDeathplaces() {
		return getProperties(Deathplace.class);
	}

	/**
	 * <p>
	 * Gets the person's place of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the first deathplace property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-3">RFC 6474 p.3</a>
	 */
	public Deathplace getDeathplace() {
		return getProperty(Deathplace.class);
	}

	/**
	 * <p>
	 * Sets the person's place of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-3">RFC 6474 p.3</a>
	 */
	public void setDeathplaceAlt(Deathplace... altRepresentations) {
		setPropertyAlt(Deathplace.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's place of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHPLACE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param deathplace the deathplace property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-3">RFC 6474 p.3</a>
	 */
	public void setDeathplace(Deathplace deathplace) {
		setProperty(Deathplace.class, deathplace);
	}

	/**
	 * <p>
	 * Gets all {@link Deathdate} property instances.
	 * </p>
	 * <p>
	 * There may be multiple instances if alternative representations are
	 * defined (see {@link #setDeathdateAlt(Deathdate...) setDeathdateAlt}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the death date properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-4">RFC 6474 p.4</a>
	 */
	public List<Deathdate> getDeathdates() {
		return getProperties(Deathdate.class);
	}

	/**
	 * <p>
	 * Gets the person's time of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the first death date property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-4">RFC 6474 p.4</a>
	 */
	public Deathdate getDeathdate() {
		return getProperty(Deathdate.class);
	}

	/**
	 * <p>
	 * Sets the person's time of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-4">RFC 6474 p.4</a>
	 */
	public void setDeathdateAlt(Deathdate... altRepresentations) {
		setPropertyAlt(Deathdate.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's time of death.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code DEATHDATE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param deathdate the death date property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 * @see <a href="http://tools.ietf.org/html/rfc6474#page-4">RFC 6474 p.4</a>
	 */
	public void setDeathdate(Deathdate deathdate) {
		setProperty(Deathdate.class, deathdate);
	}

	/**
	 * <p>
	 * Gets all {@link Birthday} property instances.
	 * </p>
	 * <p>
	 * There may be multiple instances if alternative representations are
	 * defined (see {@link #setBirthdayAlt(Birthday...) setBirthdayAlt}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @return the birthday properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public List<Birthday> getBirthdays() {
		return getProperties(Birthday.class);
	}

	/**
	 * <p>
	 * Gets the person's birthday.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the first birthday property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public Birthday getBirthday() {
		return getProperty(Birthday.class);
	}

	/**
	 * <p>
	 * Sets the person's birthday.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public void setBirthdayAlt(Birthday... altRepresentations) {
		setPropertyAlt(Birthday.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthday.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code BDAY}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param birthday the birthday or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-30">RFC 6350
	 * p.30</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public void setBirthday(Birthday birthday) {
		setProperty(Birthday.class, birthday);
	}

	/**
	 * <p>
	 * Gets all {@link Anniversary} property instances.
	 * </p>
	 * <p>
	 * There may be multiple instances if alternative representations are
	 * defined (see {@link #setAnniversaryAlt(Anniversary...) setAnniversaryAlt}
	 * ).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the anniversary properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-31">RFC 6350
	 * p.31</a>
	 */
	public List<Anniversary> getAnniversaries() {
		return getProperties(Anniversary.class);
	}

	/**
	 * <p>
	 * Gets the person's anniversary.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the first anniversary property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-31">RFC 6350
	 * p.31</a>
	 */
	public Anniversary getAnniversary() {
		return getProperty(Anniversary.class);
	}

	/**
	 * <p>
	 * Sets the person's anniversary.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-31">RFC 6350
	 * p.31</a>
	 */
	public void setAnniversaryAlt(Anniversary... altRepresentations) {
		setPropertyAlt(Anniversary.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's anniversary.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param anniversary the anniversary property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-31">RFC 6350
	 * p.31</a>
	 */
	public void setAnniversary(Anniversary anniversary) {
		setProperty(Anniversary.class, anniversary);
	}

	/**
	 * Gets the time that the vCard was last modified.
	 * <p>
	 * <b>Property name:</b> {@code REV}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the revision property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350
	 * p.45</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426
	 * p.22</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public Revision getRevision() {
		return getProperty(Revision.class);
	}

	/**
	 * <p>
	 * Sets the time that the vCard was last modified.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code REV}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param revision the revision property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350
	 * p.45</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426
	 * p.22</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void setRevision(Revision revision) {
		setProperty(Revision.class, revision);
	}

	/**
	 * <p>
	 * Sets the time that the vCard was last modified.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code REV}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param rev the last modified time or null to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-45">RFC 6350
	 * p.45</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-22">RFC 2426
	 * p.22</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public Revision setRevision(Date rev) {
		Revision type = (rev == null) ? null : new Revision(rev);
		setRevision(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the product ID, which identifies the software that created the
	 * vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PRODID}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the product ID property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 */
	public ProductId getProductId() {
		return getProperty(ProductId.class);
	}

	/**
	 * <p>
	 * Sets the product ID, which identifies the software that created the
	 * vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PRODID}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param productId the product ID property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 */
	public void setProductId(ProductId productId) {
		setProperty(ProductId.class, productId);
	}

	/**
	 * <p>
	 * Sets the product ID, which identifies the software that created the
	 * vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code PRODID}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param productId the product ID (e.g. "ez-vcard 1.0") or null to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 */
	public ProductId setProductId(String productId) {
		ProductId type = (productId == null) ? null : new ProductId(productId);
		setProductId(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the mailing addresses.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ADR}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the mailing address properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-32">RFC 6350
	 * p.32</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public List<Address> getAddresses() {
		return getProperties(Address.class);
	}

	/**
	 * <p>
	 * Adds a mailing address.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ADR}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param address the mailing address property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-32">RFC 6350
	 * p.32</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public void addAddress(Address address) {
		addProperty(address);
	}

	/**
	 * <p>
	 * Adds a mailing address.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ADR}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-32">RFC 6350
	 * p.32</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-11">RFC 2426
	 * p.11</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.11</a>
	 */
	public void addAddressAlt(Address... altRepresentations) {
		addPropertyAlt(Address.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all mailing labels that could not be assigned to an {@link Address}
	 * property. Use {@link Address#getLabel} to get a label that has been
	 * assigned to an address.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LABEL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the orphaned label properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-13">RFC 2426
	 * p.13</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.12</a>
	 */
	public List<Label> getOrphanedLabels() {
		return getProperties(Label.class);
	}

	/**
	 * <p>
	 * Adds a mailing label which is not associated with an {@link Address}
	 * property.
	 * </p>
	 * <p>
	 * Use of this method is strongly discouraged. To add a mailing label to an
	 * address, use the {@link Address#setLabel} method.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LABEL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param label the orphaned label property to add
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-13">RFC 2426
	 * p.13</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.12</a>
	 */
	public void addOrphanedLabel(Label label) {
		addProperty(label);
	}

	/**
	 * <p>
	 * Gets the email addresses.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the email address properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public List<Email> getEmails() {
		return getProperties(Email.class);
	}

	/**
	 * <p>
	 * Adds an email address.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param email the email address property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public void addEmail(Email email) {
		addProperty(email);
	}

	/**
	 * <p>
	 * Adds an email address.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param email the email address to add (e.g. "johndoe@aol.com")
	 * @param types the type(s) to assign to the email
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public Email addEmail(String email, EmailType... types) {
		Email property = new Email(email);
		property.getTypes().addAll(Arrays.asList(types));
		addEmail(property);
		return property;
	}

	/**
	 * <p>
	 * Adds an email address.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public void addEmailAlt(Email... altRepresentations) {
		addPropertyAlt(Email.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the telephone numbers.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TEL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the telephone number properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-34">RFC 6350
	 * p.34</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-14">RFC 2426
	 * p.14</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.13</a>
	 */
	public List<Telephone> getTelephoneNumbers() {
		return getProperties(Telephone.class);
	}

	/**
	 * <p>
	 * Adds a telephone number.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TEL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param telephoneNumber the telephone number property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-34">RFC 6350
	 * p.34</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-14">RFC 2426
	 * p.14</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.13</a>
	 */
	public void addTelephoneNumber(Telephone telephoneNumber) {
		addProperty(telephoneNumber);
	}

	/**
	 * <p>
	 * Adds a telephone number.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TEL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param telephoneNumber the telephone number to add (e.g.
	 * "+1 555-555-5555")
	 * @param types the type(s) to assign to the telephone number (e.g. "cell",
	 * "work", etc)
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-34">RFC 6350
	 * p.34</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-14">RFC 2426
	 * p.14</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.13</a>
	 */
	public Telephone addTelephoneNumber(String telephoneNumber, TelephoneType... types) {
		Telephone property = new Telephone(telephoneNumber);
		property.getTypes().addAll(Arrays.asList(types));
		addTelephoneNumber(property);
		return property;
	}

	/**
	 * <p>
	 * Adds a telephone number.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TEL}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-34">RFC 6350
	 * p.34</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-14">RFC 2426
	 * p.14</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.13</a>
	 */
	public void addTelephoneNumberAlt(Telephone... altRepresentations) {
		addPropertyAlt(Telephone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the email client that the person uses.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MAILER}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the mailer property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public Mailer getMailer() {
		return getProperty(Mailer.class);
	}

	/**
	 * <p>
	 * Sets the email client that the person uses.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MAILER}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param mailer the mailer property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public void setMailer(Mailer mailer) {
		setProperty(Mailer.class, mailer);
	}

	/**
	 * <p>
	 * Sets the email client that the person uses.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code MAILER}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param mailer the email client (e.g. "Thunderbird") or null to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-15">RFC 2426
	 * p.15</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.15</a>
	 */
	public Mailer setMailer(String mailer) {
		Mailer type = (mailer == null) ? null : new Mailer(mailer);
		setMailer(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the URLs. URLs can point to websites such as a personal homepage or
	 * business website.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code URL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the URL properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350
	 * p.47</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-25">RFC 2426
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
	 */
	public List<Url> getUrls() {
		return getProperties(Url.class);
	}

	/**
	 * <p>
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code URL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param url the URL property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350
	 * p.47</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-25">RFC 2426
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
	 */
	public void addUrl(Url url) {
		addProperty(url);
	}

	/**
	 * <p>
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code URL}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param url the URL to add (e.g. "http://example.com")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350
	 * p.47</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-25">RFC 2426
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
	 */
	public Url addUrl(String url) {
		Url type = new Url(url);
		addUrl(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code URL}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350
	 * p.47</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-25">RFC 2426
	 * p.25</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
	 */
	public void addUrlAlt(Url... altRepresentations) {
		addPropertyAlt(Url.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all instances of the {@link Timezone} property.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see {@link #setTimezoneAlt(Timezone...)
	 * setTimezoneAlt}) or if properties with different TYPE parameters are
	 * defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the timezone properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public List<Timezone> getTimezones() {
		return getProperties(Timezone.class);
	}

	/**
	 * <p>
	 * Gets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the first timezone property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public Timezone getTimezone() {
		return getProperty(Timezone.class);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void setTimezoneAlt(Timezone... altRepresentations) {
		setPropertyAlt(Timezone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void addTimezoneAlt(Timezone... altRepresentations) {
		addPropertyAlt(Timezone.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param timezone the timezone property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void setTimezone(Timezone timezone) {
		setProperty(Timezone.class, timezone);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code TZ}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param timezone the timezone property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-22">RFC 6350
	 * p.22</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void addTimezone(Timezone timezone) {
		addProperty(timezone);
	}

	/**
	 * <p>
	 * Gets all instances of the {@link Geo} property.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see {@link #setGeoAlt(Geo...) setGeoAlt}) or
	 * if properties with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the geo properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public List<Geo> getGeos() {
		return getProperties(Geo.class);
	}

	/**
	 * <p>
	 * Gets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the first geo property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public Geo getGeo() {
		return getProperty(Geo.class);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void setGeoAlt(Geo... altRepresentations) {
		setPropertyAlt(Geo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void addGeoAlt(Geo... altRepresentations) {
		addPropertyAlt(Geo.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param geo the geo property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
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
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param geo the geo property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public void addGeo(Geo geo) {
		addProperty(geo);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-38">RFC 6350
	 * p.38</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-16">RFC 2426
	 * p.16</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.16</a>
	 */
	public Geo setGeo(double latitude, double longitude) {
		Geo type = new Geo(latitude, longitude);
		setGeo(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the {@link Organization} property.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see
	 * {@link #setOrganizationAlt(Organization...) setOrganizationAlt}) or if
	 * properties with different TYPE parameters are defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the organization properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public List<Organization> getOrganizations() {
		return getProperties(Organization.class);
	}

	/**
	 * <p>
	 * Gets the hierarchy of department(s) to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the first organization property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public Organization getOrganization() {
		return getProperty(Organization.class);
	}

	/**
	 * <p>
	 * Sets the hierarchy of department(s) to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void setOrganizationAlt(Organization... altRepresentations) {
		setPropertyAlt(Organization.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a hierarchy of department(s) to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void addOrganizationAlt(Organization... altRepresentations) {
		addPropertyAlt(Organization.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param organization the organization property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
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
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param organization the organization property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void addOrganization(Organization organization) {
		addProperty(organization);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param departments the ordered list of department(s), starting with the
	 * broadest and ending with the most specific (e.g. "Google", "Gmail Team",
	 * "Spam Detection Squad") or an empty arguments list to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-40">RFC 6350
	 * p.40</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public Organization setOrganization(String... departments) {
		Organization type = null;
		if (departments.length > 0) {
			type = new Organization();
			type.getValues().addAll(Arrays.asList(departments));
		}
		setOrganization(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the {@link Categories} property.
	 * </p>
	 * <p>
	 * Version 4.0 vCards may have multiple instances if alternative
	 * representations are defined (see {@link #setCategoriesAlt(Categories...)
	 * setCategoriesAlt}) or if properties with different TYPE parameters are
	 * defined.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @return the categories properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public List<Categories> getCategoriesList() {
		return getProperties(Categories.class);
	}

	/**
	 * <p>
	 * Gets the list of "keywords" or "tags" that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the first categories property (can contain multiple values) or
	 * null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public Categories getCategories() {
		return getProperty(Categories.class);
	}

	/**
	 * <p>
	 * Sets the list of "keywords" or "tags" that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public void setCategoriesAlt(Categories... altRepresentations) {
		setPropertyAlt(Categories.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a list of "keywords" or "tags" that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public void addCategoriesAlt(Categories... altRepresentations) {
		addPropertyAlt(Categories.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the list of "keywords" or "tags" that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param categories the categories property (can contain multiple values)
	 * or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public void setCategories(Categories categories) {
		setProperty(Categories.class, categories);
	}

	/**
	 * <p>
	 * Adds a list of "keywords" or "tags" that can be used to describe the
	 * person. Note that only version 4.0 vCards support multiple instances of
	 * this property.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports multiple instances</i>
	 * </p>
	 * @param categories the categories property to add (can contain multiple
	 * values)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public void addCategories(Categories categories) {
		addProperty(categories);
	}

	/**
	 * <p>
	 * Sets the list of "keywords" or "tags" that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param categories the categories (e.g. "swimmer", "biker", "knitter") or
	 * an empty arguments list to remove
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-43">RFC 6350
	 * p.43</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-20">RFC 2426
	 * p.20</a>
	 */
	public Categories setCategories(String... categories) {
		Categories type = null;
		if (categories.length > 0) {
			type = new Categories();
			type.getValues().addAll(Arrays.asList(categories));
		}
		setCategories(type);
		return type;
	}

	/**
	 * <p>
	 * Gets information about the person's agent.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code AGENT}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the agent property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-19">RFC 2426
	 * p.19</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.18</a>
	 */
	public Agent getAgent() {
		return getProperty(Agent.class);
	}

	/**
	 * <p>
	 * Sets information about the person's agent.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code AGENT}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param agent the agent property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-19">RFC 2426
	 * p.19</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.18</a>
	 */
	public void setAgent(Agent agent) {
		setProperty(Agent.class, agent);
	}

	/**
	 * <p>
	 * Gets the notes. Notes contain free-form, miscellaneous text.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NOTE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the note properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public List<Note> getNotes() {
		return getProperties(Note.class);
	}

	/**
	 * <p>
	 * Adds a note. Notes contain free-form, miscellaneous text.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NOTE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param note the note property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void addNote(Note note) {
		addProperty(note);
	}

	/**
	 * <p>
	 * Adds a note. Notes contain free-form, miscellaneous text.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NOTE}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param note the note to add
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public Note addNote(String note) {
		Note type = new Note(note);
		addNote(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a note. Notes contain free-form, miscellaneous text.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NOTE}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-44">RFC 6350
	 * p.44</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-21">RFC 2426
	 * p.21</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.19</a>
	 */
	public void addNoteAlt(Note... altRepresentations) {
		addPropertyAlt(Note.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the unique identifier of the vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code UID}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the unique identifier property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-46">RFC 6350
	 * p.46</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-24">RFC 2426
	 * p.24</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
	 */
	public Uid getUid() {
		return getProperty(Uid.class);
	}

	/**
	 * <p>
	 * Sets the unique identifier of the vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code UID}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param uid the unique identifier property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-46">RFC 6350
	 * p.46</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-24">RFC 2426
	 * p.24</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
	 */
	public void setUid(Uid uid) {
		setProperty(Uid.class, uid);
	}

	/**
	 * <p>
	 * Gets the public encryption keys.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KEY}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the key properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-48">RFC 6350
	 * p.48</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426
	 * p.26</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.22</a>
	 */
	public List<Key> getKeys() {
		return getProperties(Key.class);
	}

	/**
	 * <p>
	 * Adds a public encryption key.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KEY}<br>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param key the key property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-48">RFC 6350
	 * p.48</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426
	 * p.26</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.22</a>
	 */
	public void addKey(Key key) {
		addProperty(key);
	}

	/**
	 * <p>
	 * Adds a public encryption key.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code KEY}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-48">RFC 6350
	 * p.48</a>
	 * @see <a href="http://tools.ietf.org/html/rfc2426#page-26">RFC 2426
	 * p.26</a>
	 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.22</a>
	 */
	public void addKeyAlt(Key... altRepresentations) {
		addPropertyAlt(Key.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the instant messaging handles.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code IMPP}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @return the IMPP properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc4770">RFC 4770</a>
	 */
	public List<Impp> getImpps() {
		return getProperties(Impp.class);
	}

	/**
	 * <p>
	 * Adds an instant messaging handle.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code IMPP}<br>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param impp the IMPP property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc4770">RFC 4770</a>
	 */
	public void addImpp(Impp impp) {
		addProperty(impp);
	}

	/**
	 * <p>
	 * Adds an instant messaging handle.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code IMPP}<br>
	 * <b>Supported versions:</b> {@code 4.0*}<br>
	 * <i>* Only 4.0 supports alternative representations</i>
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-36">RFC 6350
	 * p.36</a>
	 * @see <a href="http://tools.ietf.org/html/rfc4770">RFC 4770</a>
	 */
	public void addImppAlt(Impp... altRepresentations) {
		addPropertyAlt(Impp.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets a list of people that the person is related to.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code RELATED}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the relation properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-42">RFC 6350
	 * p.42</a>
	 */
	public List<Related> getRelations() {
		return getProperties(Related.class);
	}

	/**
	 * <p>
	 * Adds someone that the person is related to.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code RELATED}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param related the relation property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-42">RFC 6350
	 * p.42</a>
	 */
	public void addRelated(Related related) {
		addProperty(related);
	}

	/**
	 * <p>
	 * Adds someone that the person is related to.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code RELATED}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-42">RFC 6350
	 * p.42</a>
	 */
	public void addRelatedAlt(Related... altRepresentations) {
		addPropertyAlt(Related.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the languages that the person speaks.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LANG}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the language properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-37">RFC 6350
	 * p.37</a>
	 */
	public List<Language> getLanguages() {
		return getProperties(Language.class);
	}

	/**
	 * <p>
	 * Adds a language that the person speaks.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LANG}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param language the language property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-37">RFC 6350
	 * p.37</a>
	 */
	public void addLanguage(Language language) {
		addProperty(language);
	}

	/**
	 * <p>
	 * Adds a language that the person speaks.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LANG}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param language the language to add (e.g. "en-us")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-37">RFC 6350
	 * p.37</a>
	 */
	public Language addLanguage(String language) {
		Language type = new Language(language);
		addLanguage(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a language that the person speaks.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code LANG}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-37">RFC 6350
	 * p.37</a>
	 */
	public void addLanguageAlt(Language... altRepresentations) {
		addPropertyAlt(Language.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the URIs that can be used to schedule a meeting with the person on
	 * his or her calendar.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the calendar request URI properties (any changes made this list
	 * will affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-50">RFC 6350
	 * p.50</a>
	 */
	public List<CalendarRequestUri> getCalendarRequestUris() {
		return getProperties(CalendarRequestUri.class);
	}

	/**
	 * <p>
	 * Adds a URI that can be used to schedule a meeting with the person on his
	 * or her calendar.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param calendarRequestUri the calendar request URI property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-50">RFC 6350
	 * p.50</a>
	 */
	public void addCalendarRequestUri(CalendarRequestUri calendarRequestUri) {
		addProperty(calendarRequestUri);
	}

	/**
	 * <p>
	 * Adds a URI that can be used to schedule a meeting with the person on his
	 * or her calendar.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-50">RFC 6350
	 * p.50</a>
	 */
	public void addCalendarRequestUriAlt(CalendarRequestUri... altRepresentations) {
		addPropertyAlt(CalendarRequestUri.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the URIs that point to the person's calendar.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALURI}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the calendar URI properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-50">RFC 6350
	 * p.50</a>
	 */
	public List<CalendarUri> getCalendarUris() {
		return getProperties(CalendarUri.class);
	}

	/**
	 * <p>
	 * Adds a URI that points to the person's calendar.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALURI}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param calendarUri the calendar URI property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-50">RFC 6350
	 * p.50</a>
	 */
	public void addCalendarUri(CalendarUri calendarUri) {
		addProperty(calendarUri);
	}

	/**
	 * <p>
	 * Adds a URI that points to the person's calendar.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALURI}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-50">RFC 6350
	 * p.50</a>
	 */
	public void addCalendarUriAlt(CalendarUri... altRepresentations) {
		addPropertyAlt(CalendarUri.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the URLs that can be used to determine when the person is free or
	 * busy.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FBURL}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the free-busy URL properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-49">RFC 6350
	 * p.49</a>
	 */
	public List<FreeBusyUrl> getFbUrls() {
		return getProperties(FreeBusyUrl.class);
	}

	/**
	 * <p>
	 * Adds a URL that can be used to determine when the person is free or busy.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FBURL}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param fbUrl the free-busy URL property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-49">RFC 6350
	 * p.49</a>
	 */
	public void addFbUrl(FreeBusyUrl fbUrl) {
		addProperty(fbUrl);
	}

	/**
	 * <p>
	 * Adds a URL that can be used to determine when the person is free or busy.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FBURL}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-49">RFC 6350
	 * p.49</a>
	 */
	public void addFbUrlAlt(FreeBusyUrl... altRepresentations) {
		addPropertyAlt(FreeBusyUrl.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the properties that are used to assign globally-unique identifiers
	 * to individual property instances. They are used for merging together
	 * different versions of the same vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CLIENTPIDMAP}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the client PID map properties (any changes made this list will
	 * affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350
	 * p.47</a>
	 */
	public List<ClientPidMap> getClientPidMaps() {
		return getProperties(ClientPidMap.class);
	}

	/**
	 * <p>
	 * Adds a property that is used to assign a globally-unique identifier to an
	 * individual property instance. They are used for merging together
	 * different versions of the same vCard.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CLIENTPIDMAP}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param clientPidMap the client PID map property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-47">RFC 6350
	 * p.47</a>
	 */
	public void addClientPidMap(ClientPidMap clientPidMap) {
		addProperty(clientPidMap);
	}

	/**
	 * <p>
	 * Gets any XML data that is attached to the vCard.
	 * </p>
	 * <p>
	 * These properties may be present if the vCard was encoded in XML ("xCard"
	 * format) and the XML document contained non-standard elements. In this
	 * case, the properties would contain all of the non-standard XML elements.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code XML}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the XML properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-27">RFC 6350
	 * p.27</a>
	 */
	public List<Xml> getXmls() {
		return getProperties(Xml.class);
	}

	/**
	 * <p>
	 * Adds XML data to the vCard.
	 * </p>
	 * <p>
	 * These properties may be present if the vCard was encoded in XML ("xCard"
	 * format) and the XML document contained non-standard elements. In this
	 * case, the properties would contain all of the non-standard XML elements.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code XML}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param xml the XML property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-27">RFC 6350
	 * p.27</a>
	 */
	public void addXml(Xml xml) {
		addProperty(xml);
	}

	/**
	 * <p>
	 * Adds XML data to the vCard.
	 * </p>
	 * <p>
	 * These properties may be present if the vCard was encoded in XML ("xCard"
	 * format) and the XML document contained non-standard elements. In this
	 * case, the properties would contain all of the non-standard XML elements.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code XML}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6350#page-27">RFC 6350
	 * p.27</a>
	 */
	public void addXmlAlt(Xml... altRepresentations) {
		addPropertyAlt(Xml.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the professional subject areas that the person is knowledgeable in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the expertise properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-3">RFC 6715 p.3</a>
	 */
	public List<Expertise> getExpertise() {
		return getProperties(Expertise.class);
	}

	/**
	 * <p>
	 * Adds a professional subject area that the person is knowledgeable in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param expertise the expertise property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-3">RFC 6715 p.3</a>
	 */
	public void addExpertise(Expertise expertise) {
		addProperty(expertise);
	}

	/**
	 * <p>
	 * Adds a professional subject area that the person is knowledgeable in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param expertise the professional subject area to add (e.g.
	 * "programming")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-3">RFC 6715 p.3</a>
	 */
	public Expertise addExpertise(String expertise) {
		Expertise type = new Expertise(expertise);
		addExpertise(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a professional subject area that the person is knowledgeable in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-3">RFC 6715 p.3</a>
	 */
	public void addExpertiseAlt(Expertise... altRepresentations) {
		addPropertyAlt(Expertise.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the hobbies that the person actively engages in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the hobby properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-4">RFC 6715 p.4</a>
	 */
	public List<Hobby> getHobbies() {
		return getProperties(Hobby.class);
	}

	/**
	 * <p>
	 * Adds a hobby that the person actively engages in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param hobby the hobby property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-4">RFC 6715 p.4</a>
	 */
	public void addHobby(Hobby hobby) {
		addProperty(hobby);
	}

	/**
	 * <p>
	 * Adds a hobby that the person actively engages in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param hobby the hobby to add (e.g. "photography")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-4">RFC 6715 p.4</a>
	 */
	public Hobby addHobby(String hobby) {
		Hobby type = new Hobby(hobby);
		addHobby(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a hobby that the person actively engages in.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code HOBBY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-4">RFC 6715 p.4</a>
	 */
	public void addHobbyAlt(Hobby... altRepresentations) {
		addPropertyAlt(Hobby.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the person's interests.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the interest properties (any changes made this list will affect
	 * the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-5">RFC 6715 p.5</a>
	 */
	public List<Interest> getInterests() {
		return getProperties(Interest.class);
	}

	/**
	 * <p>
	 * Adds an interest.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param interest the interest property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-5">RFC 6715 p.5</a>
	 */
	public void addInterest(Interest interest) {
		addProperty(interest);
	}

	/**
	 * <p>
	 * Adds an interest.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param interest the interest to add (e.g. "football")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-5">RFC 6715 p.5</a>
	 */
	public Interest addInterest(String interest) {
		Interest type = new Interest(interest);
		addInterest(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an interest.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-5">RFC 6715 p.5</a>
	 */
	public void addInterestAlt(Interest... altRepresentations) {
		addPropertyAlt(Interest.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets the organization directories.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the organization directory properties (any changes made this list
	 * will affect the {@link VCard} object and vice versa)
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-6">RFC 6715 p.6</a>
	 */
	public List<OrgDirectory> getOrgDirectories() {
		return getProperties(OrgDirectory.class);
	}

	/**
	 * <p>
	 * Adds an organization directory.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param orgDirectory the organization directory property to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-6">RFC 6715 p.6</a>
	 */
	public void addOrgDirectory(OrgDirectory orgDirectory) {
		addProperty(orgDirectory);
	}

	/**
	 * <p>
	 * Adds an organization directory.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param orgDirectory the organization directory to add (e.g.
	 * "http://company.com/staff")
	 * @return the property object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-6">RFC 6715 p.6</a>
	 */
	public OrgDirectory addOrgDirectory(String orgDirectory) {
		OrgDirectory type = new OrgDirectory(orgDirectory);
		addOrgDirectory(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an organization directory.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}<br>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the same
	 * value. These properties contain the same information, but in different
	 * forms (such as different languages). See {@link VCardParameters#getAltId
	 * this description of the ALTID parameter} for more information. Note that
	 * this method automatically assigns an appropriate ALTID parameter to each
	 * property.
	 * @see <a href="http://tools.ietf.org/html/rfc6715#page-6">RFC 6715 p.6</a>
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
	 * @param <T> the property class
	 * @return the property or null if not found
	 */
	public <T extends VCardProperty> T getProperty(Class<T> clazz) {
		return clazz.cast(properties.first(clazz));
	}

	/**
	 * Gets all properties of a given class.
	 * @param clazz the property class
	 * @param <T> the property class
	 * @return the properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
	 */
	public <T extends VCardProperty> List<T> getProperties(Class<T> clazz) {
		return new VCardPropertyList<T>(clazz);
	}

	/**
	 * Gets all properties of a given class, grouping the alternative
	 * representations of each property together (see:
	 * {@link VCardParameters#getAltId description of ALTID})
	 * @param clazz the property class
	 * @param <T> the property class
	 * @return the properties (this list is immutable)
	 */
	public <T extends VCardProperty & HasAltId> List<List<T>> getPropertiesAlt(Class<T> clazz) {
		List<T> propertiesWithoutAltIds = new ArrayList<T>();
		ListMultimap<String, T> propertiesWithAltIds = new ListMultimap<String, T>();
		for (T property : getProperties(clazz)) {
			String altId = property.getAltId();
			if (altId == null) {
				propertiesWithoutAltIds.add(property);
			} else {
				propertiesWithAltIds.put(altId, property);
			}
		}

		int size = propertiesWithoutAltIds.size() + propertiesWithAltIds.size();
		List<List<T>> listToReturn = new ArrayList<List<T>>(size);
		for (Map.Entry<String, List<T>> entry : propertiesWithAltIds) {
			listToReturn.add(Collections.unmodifiableList(entry.getValue()));
		}

		//put properties without ALTIDs at the end
		for (T property : propertiesWithoutAltIds) {
			List<T> list = new ArrayList<T>(1);
			list.add(property);
			listToReturn.add(Collections.unmodifiableList(list));
		}

		return Collections.unmodifiableList(listToReturn);
	}

	/**
	 * Gets all the properties in this vCard.
	 * @return the properties (this list is immutable)
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
	 * Replaces all existing properties of the given property instance's class
	 * with the given property instance.
	 * @param property the property
	 * @return the properties that were replaced (this list is immutable)
	 */
	public List<VCardProperty> setProperty(VCardProperty property) {
		return properties.replace(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given class with a single
	 * property instance. If the property instance is null, then all instances
	 * of that property will be removed.
	 * @param clazz the property class (e.g. "Note.class")
	 * @param property the property or null to remove
	 * @param <T> the property class
	 * @return the properties that were replaced (this list is immutable)
	 */
	public <T extends VCardProperty> List<T> setProperty(Class<T> clazz, T property) {
		List<VCardProperty> replaced = properties.replace(clazz, property);
		return castList(replaced, clazz);
	}

	/**
	 * Removes a property instance from the vCard.
	 * @param property the property to remove
	 * @return true if it was removed, false if it wasn't found
	 */
	public boolean removeProperty(VCardProperty property) {
		return properties.remove(property.getClass(), property);
	}

	/**
	 * Removes all properties of a given class.
	 * @param clazz the class of the properties to remove (e.g. "Note.class")
	 * @param <T> the property class
	 * @return the properties that were removed (this list is immutable)
	 */
	public <T extends VCardProperty> List<T> removeProperties(Class<T> clazz) {
		List<VCardProperty> removed = properties.removeAll(clazz);
		return castList(removed, clazz);
	}

	/**
	 * Gets the first extended property with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the property or null if none were found
	 */
	public RawProperty getExtendedProperty(String name) {
		for (RawProperty raw : getExtendedProperties()) {
			if (raw.getPropertyName().equalsIgnoreCase(name)) {
				return raw;
			}
		}
		return null;
	}

	/**
	 * Gets all extended properties with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the properties (this list is immutable)
	 */
	public List<RawProperty> getExtendedProperties(String name) {
		List<RawProperty> properties = new ArrayList<RawProperty>();

		for (RawProperty raw : getExtendedProperties()) {
			if (raw.getPropertyName().equalsIgnoreCase(name)) {
				properties.add(raw);
			}
		}

		return Collections.unmodifiableList(properties);
	}

	/**
	 * Gets all extended properties.
	 * @return the properties (any changes made this list will affect the
	 * {@link VCard} object and vice versa)
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
	 * Adds an extended property.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @param dataType the property value's data type
	 * @return the property object that was created
	 */
	public RawProperty addExtendedProperty(String name, String value, VCardDataType dataType) {
		RawProperty raw = new RawProperty(name, value, dataType);
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
		return addExtendedProperty(name, value);
	}

	/**
	 * Replaces all existing extended properties with the given name with a
	 * single property instance.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @param dataType the property value's data type
	 * @return the property object that was created
	 */
	public RawProperty setExtendedProperty(String name, String value, VCardDataType dataType) {
		removeExtendedProperty(name);
		return addExtendedProperty(name, value, dataType);
	}

	/**
	 * Removes all extended properties that have the given name.
	 * @param name the component name (e.g. "X-ALT-DESC")
	 * @return the properties that were removed (this list is immutable)
	 */
	public List<RawProperty> removeExtendedProperty(String name) {
		List<RawProperty> all = getExtendedProperties();
		List<RawProperty> toRemove = new ArrayList<RawProperty>();
		for (RawProperty property : all) {
			if (property.getPropertyName().equalsIgnoreCase(name)) {
				toRemove.add(property);
			}
		}

		all.removeAll(toRemove);
		return Collections.unmodifiableList(toRemove);
	}

	/**
	 * Adds a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 * @param <T> the property class
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
	 * @param <T> the property class
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
	 * @param <T> the property class
	 * @return the properties that were replaced (this list is immutable)
	 */
	public <T extends VCardProperty & HasAltId> List<T> setPropertyAlt(Class<T> propertyClass, T... altRepresentations) {
		return setPropertyAlt(propertyClass, Arrays.asList(altRepresentations));
	}

	/**
	 * Sets a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardParameters#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 * @param <T> the property class
	 * @return the properties that were replaced (this list is immutable)
	 */
	public <T extends VCardProperty & HasAltId> List<T> setPropertyAlt(Class<T> propertyClass, Collection<T> altRepresentations) {
		List<T> removed = removeProperties(propertyClass);
		addPropertyAlt(propertyClass, altRepresentations);
		return removed;
	}

	/**
	 * Casts all objects in the given list to the given class, adding the casted
	 * objects to a new list.
	 * @param list the list to cast
	 * @param castTo the class to cast to
	 * @param <T> the class to cast to
	 * @return the new list (this list is immutable)
	 */
	private static <T> List<T> castList(List<?> list, Class<T> castTo) {
		List<T> casted = new ArrayList<T>(list.size());
		for (Object object : list) {
			casted.add(castTo.cast(object));
		}
		return Collections.unmodifiableList(casted);
	}

	/**
	 * Checks this vCard for data consistency problems or deviations from the
	 * spec. These problems will not prevent the vCard from being written to a
	 * data stream, but may prevent it from being parsed correctly by the
	 * consuming application. These problems can largely be avoided by reading
	 * the Javadocs of the property classes, or by being familiar with the vCard
	 * standard.
	 * @param version the version to check the vCard against (use
	 * {@link VCardVersion#V4_0} for xCard and jCard)
	 * @return the validation warnings
	 */
	public ValidationWarnings validate(VCardVersion version) {
		ValidationWarnings warnings = new ValidationWarnings();

		//validate overall vCard object
		if (getStructuredName() == null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
			warnings.add(null, new ValidationWarning(0));
		}
		if (getFormattedName() == null && (version == VCardVersion.V3_0 || version == VCardVersion.V4_0)) {
			warnings.add(null, new ValidationWarning(1));
		}

		//validate properties
		for (VCardProperty property : this) {
			List<ValidationWarning> propWarnings = property.validate(version, this);
			if (!propWarnings.isEmpty()) {
				warnings.add(property, propWarnings);
			}
		}

		return warnings;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("version=").append(version);
		for (VCardProperty property : properties.values()) {
			sb.append(StringUtils.NEWLINE).append(property);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + ((version == null) ? 0 : version.hashCode());

		int propertiesHash = 1;
		for (VCardProperty property : properties.values()) {
			propertiesHash += property.hashCode();
		}
		result = prime * result + propertiesHash;

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VCard other = (VCard) obj;
		if (version != other.version) return false;
		if (properties.size() != other.properties.size()) return false;

		for (Map.Entry<Class<? extends VCardProperty>, List<VCardProperty>> entry : properties) {
			Class<? extends VCardProperty> key = entry.getKey();
			List<VCardProperty> value = entry.getValue();
			List<VCardProperty> otherValue = other.properties.get(key);

			if (value.size() != otherValue.size()) {
				return false;
			}

			List<VCardProperty> otherValueCopy = new ArrayList<VCardProperty>(otherValue);
			for (VCardProperty property : value) {
				if (!otherValueCopy.remove(property)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Generates a unique ALTID parameter value.
	 * @param properties the collection of properties under which the ALTID must
	 * be unique
	 * @param <T> the property class
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
		while (altIds.contains(Integer.toString(altId))) {
			altId++;
		}
		return Integer.toString(altId);
	}

	/**
	 * <p>
	 * A list that automatically casts {@link VCardProperty} instances stored in
	 * this {@link VCard} to a given property class.
	 * </p>
	 * <p>
	 * This list is backed by the {@link VCard} object. Any changes made to the
	 * list will affect the {@link VCard} object and vice versa.
	 * </p>
	 * @param <T> the property class
	 */
	private class VCardPropertyList<T extends VCardProperty> extends AbstractList<T> {
		protected final Class<T> propertyClass;
		protected final List<VCardProperty> properties;

		/**
		 * @param propertyClass the property class
		 */
		public VCardPropertyList(Class<T> propertyClass) {
			this.propertyClass = propertyClass;
			properties = VCard.this.properties.get(propertyClass);
		}

		@Override
		public void add(int index, T value) {
			properties.add(index, value);
		}

		@Override
		public T remove(int index) {
			VCardProperty removed = properties.remove(index);
			return cast(removed);
		}

		@Override
		public T get(int index) {
			VCardProperty property = properties.get(index);
			return cast(property);
		}

		@Override
		public T set(int index, T value) {
			VCardProperty replaced = properties.set(index, value);
			return cast(replaced);
		}

		@Override
		public int size() {
			return properties.size();
		}

		private T cast(VCardProperty value) {
			return propertyClass.cast(value);
		}
	}
}
