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

import ezvcard.ValidationWarnings.WarningsGroup;
import ezvcard.io.text.VCardWriter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.AgentType;
import ezvcard.types.AnniversaryType;
import ezvcard.types.BirthdayType;
import ezvcard.types.BirthplaceType;
import ezvcard.types.CalendarRequestUriType;
import ezvcard.types.CalendarUriType;
import ezvcard.types.CategoriesType;
import ezvcard.types.ClassificationType;
import ezvcard.types.ClientPidMapType;
import ezvcard.types.DeathdateType;
import ezvcard.types.DeathplaceType;
import ezvcard.types.EmailType;
import ezvcard.types.ExpertiseType;
import ezvcard.types.FbUrlType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GenderType;
import ezvcard.types.GeoType;
import ezvcard.types.HasAltId;
import ezvcard.types.HobbyType;
import ezvcard.types.ImppType;
import ezvcard.types.InterestType;
import ezvcard.types.KeyType;
import ezvcard.types.KindType;
import ezvcard.types.LabelType;
import ezvcard.types.LanguageType;
import ezvcard.types.LogoType;
import ezvcard.types.MailerType;
import ezvcard.types.MemberType;
import ezvcard.types.NicknameType;
import ezvcard.types.NoteType;
import ezvcard.types.OrgDirectoryType;
import ezvcard.types.OrganizationType;
import ezvcard.types.PhotoType;
import ezvcard.types.ProdIdType;
import ezvcard.types.ProfileType;
import ezvcard.types.RawType;
import ezvcard.types.RelatedType;
import ezvcard.types.RevisionType;
import ezvcard.types.RoleType;
import ezvcard.types.SortStringType;
import ezvcard.types.SoundType;
import ezvcard.types.SourceDisplayTextType;
import ezvcard.types.SourceType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
import ezvcard.types.TitleType;
import ezvcard.types.UidType;
import ezvcard.types.UrlType;
import ezvcard.types.VCardType;
import ezvcard.types.XmlType;
import ezvcard.util.ListMultimap;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Represents a vCard.
 * @author Michael Angstadt
 */
public class VCard implements Iterable<VCardType> {
	private VCardVersion version = VCardVersion.V3_0;

	private final ListMultimap<Class<? extends VCardType>, VCardType> properties = new ListMultimap<Class<? extends VCardType>, VCardType>();

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
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
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
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
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
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
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
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcard-03">jCard
	 * draft specification</a>
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
	public KindType getKind() {
		return getType(KindType.class);
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
	public void setKind(KindType kind) {
		setType(KindType.class, kind);
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
	public GenderType getGender() {
		return getType(GenderType.class);
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
	public void setGender(GenderType gender) {
		setType(GenderType.class, gender);
	}

	/**
	 * Gets the members of the group. Only valid if the KIND property is set to
	 * "group".
	 * 
	 * <p>
	 * 
	 * <pre class="brush:java">
	 * VCard vcard = ...
	 * KindType kind = vcard.getKind();
	 * if (kind != null && kind.isGroup()){
	 *   for (MemberType member : vcard.getMembers(){
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
	public List<MemberType> getMembers() {
		return getTypes(MemberType.class);
	}

	/**
	 * Adds a member to the group. Only valid if the KIND property is set to
	 * "group".
	 * 
	 * <p>
	 * 
	 * <pre class="brush:java">
	 * VCard vcard = new VCard();
	 * vcard.setKind(KindType.group());
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
	public void addMember(MemberType member) {
		addType(member);
	}

	/**
	 * <p>
	 * Adds a member property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addMemberAlt(Collection<MemberType> altRepresentations) {
		addTypeAlt(MemberType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a member property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addMemberAlt(MemberType... altRepresentations) {
		addTypeAlt(MemberType.class, altRepresentations);
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
	public ProfileType getProfile() {
		return getType(ProfileType.class);
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
	public void setProfile(ProfileType profile) {
		setType(ProfileType.class, profile);
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
	public ClassificationType getClassification() {
		return getType(ClassificationType.class);
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
	public void setClassification(ClassificationType classification) {
		setType(ClassificationType.class, classification);
	}

	/**
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard. This is a convenience method for
	 * {@link #setClassification(ClassificationType)}.
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
	 * @return the type object that was created
	 */
	public ClassificationType setClassification(String classification) {
		ClassificationType type = null;
		if (classification != null) {
			type = new ClassificationType(classification);
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
	public List<SourceType> getSources() {
		return getTypes(SourceType.class);
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
	public void addSource(SourceType source) {
		addType(source);
	}

	/**
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard. This is a convenience method for
	 * {@link #addSource(SourceType)} .
	 * <p>
	 * <b>Property name:</b> {@code SOURCE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param source the source URI (e.g. "http://example.com/vcard.vcf")
	 * @return the type object that was created
	 */
	public SourceType addSource(String source) {
		SourceType type = new SourceType(source);
		addSource(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a source property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addSourceAlt(Collection<SourceType> altRepresentations) {
		addTypeAlt(SourceType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a source property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addSourceAlt(SourceType... altRepresentations) {
		addTypeAlt(SourceType.class, altRepresentations);
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
	public SourceDisplayTextType getSourceDisplayText() {
		return getType(SourceDisplayTextType.class);
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
	public void setSourceDisplayText(SourceDisplayTextType sourceDisplayText) {
		setType(SourceDisplayTextType.class, sourceDisplayText);
	}

	/**
	 * Sets a textual representation of the SOURCE property. This is a
	 * convenience method for
	 * {@link #setSourceDisplayText(SourceDisplayTextType)}.
	 * <p>
	 * <b>Property name:</b> {@code NAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0}
	 * </p>
	 * @param sourceDisplayText a textual representation of the vCard source or
	 * null to remove
	 * @return the type object that was created
	 */
	public SourceDisplayTextType setSourceDisplayText(String sourceDisplayText) {
		SourceDisplayTextType type = null;
		if (sourceDisplayText != null) {
			type = new SourceDisplayTextType(sourceDisplayText);
		}
		setSourceDisplayText(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the formatted name property. Version 4.0 vCards may
	 * have multiple instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}) or if properties
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
	public List<FormattedNameType> getFormattedNames() {
		return getTypes(FormattedNameType.class);
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
	public FormattedNameType getFormattedName() {
		return getType(FormattedNameType.class);
	}

	/**
	 * <p>
	 * Sets the formatted name property as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
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
	public void setFormattedNameAlt(Collection<FormattedNameType> altRepresentations) {
		setTypeAlt(FormattedNameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the formatted name property as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
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
	public void setFormattedNameAlt(FormattedNameType... altRepresentations) {
		setTypeAlt(FormattedNameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a formatted name property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addFormattedNameAlt(Collection<FormattedNameType> altRepresentations) {
		addTypeAlt(FormattedNameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a formatted name property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addFormattedNameAlt(FormattedNameType... altRepresentations) {
		addTypeAlt(FormattedNameType.class, altRepresentations);
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
	public void setFormattedName(FormattedNameType formattedName) {
		setType(FormattedNameType.class, formattedName);
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
	public void addFormattedName(FormattedNameType formattedName) {
		addType(formattedName);
	}

	/**
	 * <p>
	 * Sets the text value used for displaying the person's name. This is a
	 * convenience method for {@link #setFormattedName(FormattedNameType)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code FN}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param formattedName the formatted name (e.g. "John Doe") or null to
	 * remove
	 * @return the type object that was created
	 */
	public FormattedNameType setFormattedName(String formattedName) {
		FormattedNameType type = null;
		if (formattedName != null) {
			type = new FormattedNameType(formattedName);
		}
		setFormattedName(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all structured name properties. Version 4.0 vCards may have multiple
	 * instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
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
	public List<StructuredNameType> getStructuredNames() {
		return getTypes(StructuredNameType.class);
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
	public StructuredNameType getStructuredName() {
		return getType(StructuredNameType.class);
	}

	/**
	 * <p>
	 * Sets the structured name property as a group of alternative
	 * representations (see {@link VCardSubTypes#getAltId} for more details). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
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
	public void setStructuredNameAlt(Collection<StructuredNameType> altRepresentations) {
		setTypeAlt(StructuredNameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the structured name property as a group of alternative
	 * representations (see {@link VCardSubTypes#getAltId} for more details). An
	 * appropriate ALTID parameter value is automatically generated and assigned
	 * to the properties.
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
	public void setStructuredNameAlt(StructuredNameType... altRepresentations) {
		setTypeAlt(StructuredNameType.class, altRepresentations);
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
	public void setStructuredName(StructuredNameType structuredName) {
		setType(StructuredNameType.class, structuredName);
	}

	/**
	 * <p>
	 * Gets all instances of the nickname property. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}) or if properties
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
	public List<NicknameType> getNicknames() {
		return getTypes(NicknameType.class);
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
	public NicknameType getNickname() {
		return getType(NicknameType.class);
	}

	/**
	 * <p>
	 * Sets the nickname property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setNicknameAlt(Collection<NicknameType> altRepresentations) {
		setTypeAlt(NicknameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the nickname property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setNicknameAlt(NicknameType... altRepresentations) {
		setTypeAlt(NicknameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a nickname property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addNicknameAlt(Collection<NicknameType> altRepresentations) {
		addTypeAlt(NicknameType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a nickname property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addNicknameAlt(NicknameType... altRepresentations) {
		addTypeAlt(NicknameType.class, altRepresentations);
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
	public void setNickname(NicknameType nickname) {
		setType(NicknameType.class, nickname);
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
	public void addNickname(NicknameType nickname) {
		addType(nickname);
	}

	/**
	 * <p>
	 * Sets the person's nicknames. This is a convenience method for
	 * {@link #setNickname(NicknameType)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code NICKNAME}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param nicknames the nickname(s) (e.g. "Jonny") or null to remove
	 * @return the type object that was created
	 */
	public NicknameType setNickname(String... nicknames) {
		NicknameType type = null;
		if (nicknames != null) {
			type = new NicknameType();
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
	 * For 4.0 vCards, use the {@link StructuredNameType#getSortAs} and/or
	 * {@link OrganizationType#getSortAs} methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the sort string
	 */
	public SortStringType getSortString() {
		return getType(SortStringType.class);
	}

	/**
	 * <p>
	 * Sets the string that should be used to sort the vCard.
	 * </p>
	 * <p>
	 * For 4.0 vCards, use the {@link StructuredNameType#setSortAs} and/or
	 * {@link OrganizationType#setSortAs} methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param sortString the sort string
	 */
	public void setSortString(SortStringType sortString) {
		setType(SortStringType.class, sortString);
	}

	/**
	 * <p>
	 * Sets the string that should be used to sort the vCard. This is a
	 * convenience method for {@link #setSortString(SortStringType)}.
	 * </p>
	 * <p>
	 * For 4.0 vCards, use the {@link StructuredNameType#setSortAs} and/or
	 * {@link OrganizationType#setSortAs} methods.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code SORT-STRING}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param sortString the sort string (e.g. "Armour" if the person's last
	 * name is "d'Armour") or null to remove
	 * @return the type object that was created
	 */
	public SortStringType setSortString(String sortString) {
		SortStringType type = null;
		if (sortString != null) {
			type = new SortStringType(sortString);
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
	public List<TitleType> getTitles() {
		return getTypes(TitleType.class);
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
	public void addTitle(TitleType title) {
		addType(title);
	}

	/**
	 * Adds a title associated with the person. This is a convenience method for
	 * {@link #addTitle(TitleType)}.
	 * <p>
	 * <b>Property name:</b> {@code TITLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param title the title (e.g. "V.P. Research and Development")
	 * @return the type object that was created
	 */
	public TitleType addTitle(String title) {
		TitleType type = new TitleType(title);
		addTitle(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a title property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addTitleAlt(Collection<TitleType> altRepresentations) {
		addTypeAlt(TitleType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a title property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addTitleAlt(TitleType... altRepresentations) {
		addTypeAlt(TitleType.class, altRepresentations);
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
	public List<RoleType> getRoles() {
		return getTypes(RoleType.class);
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
	public void addRole(RoleType role) {
		addType(role);
	}

	/**
	 * Adds a role associated with the person. This is a convenience method for
	 * {@link #addRole(RoleType)}.
	 * <p>
	 * <b>Property name:</b> {@code ROLE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param role the role (e.g. "Executive")
	 * @return the type object that was created
	 */
	public RoleType addRole(String role) {
		RoleType type = new RoleType(role);
		addRole(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a role property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addRoleAlt(Collection<RoleType> altRepresentations) {
		addTypeAlt(RoleType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a role property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addRoleAlt(RoleType... altRepresentations) {
		addTypeAlt(RoleType.class, altRepresentations);
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
	public List<PhotoType> getPhotos() {
		return getTypes(PhotoType.class);
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
	public void addPhoto(PhotoType photo) {
		addType(photo);
	}

	/**
	 * <p>
	 * Adds a photo property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addPhotoAlt(Collection<PhotoType> altRepresentations) {
		addTypeAlt(PhotoType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a photo property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addPhotoAlt(PhotoType... altRepresentations) {
		addTypeAlt(PhotoType.class, altRepresentations);
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
	public List<LogoType> getLogos() {
		return getTypes(LogoType.class);
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
	public void addLogo(LogoType logo) {
		addType(logo);
	}

	/**
	 * <p>
	 * Adds a logo property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addLogoAlt(Collection<LogoType> altRepresentations) {
		addTypeAlt(LogoType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a logo property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addLogoAlt(LogoType... altRepresentations) {
		addTypeAlt(LogoType.class, altRepresentations);
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
	public List<SoundType> getSounds() {
		return getTypes(SoundType.class);
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
	public void addSound(SoundType sound) {
		addType(sound);
	}

	/**
	 * <p>
	 * Adds a sound property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addSoundAlt(Collection<SoundType> altRepresentations) {
		addTypeAlt(SoundType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a sound property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addSoundAlt(SoundType... altRepresentations) {
		addTypeAlt(SoundType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all birthplace property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
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
	public List<BirthplaceType> getBirthplaces() {
		return getTypes(BirthplaceType.class);
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
	public BirthplaceType getBirthplace() {
		return getType(BirthplaceType.class);
	}

	/**
	 * <p>
	 * Sets the person's birthplace as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}. An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
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
	public void setBirthplaceAlt(Collection<BirthplaceType> altRepresentations) {
		setTypeAlt(BirthplaceType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthplace as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}. An appropriate
	 * ALTID parameter value is automatically generated and assigned to the
	 * properties.
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
	public void setBirthplaceAlt(BirthplaceType... altRepresentations) {
		setTypeAlt(BirthplaceType.class, altRepresentations);
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
	public void setBirthplace(BirthplaceType birthplace) {
		setType(BirthplaceType.class, birthplace);
	}

	/**
	 * <p>
	 * Gets all deathplace property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
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
	public List<DeathplaceType> getDeathplaces() {
		return getTypes(DeathplaceType.class);
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
	public DeathplaceType getDeathplace() {
		return getType(DeathplaceType.class);
	}

	/**
	 * <p>
	 * Sets the person's deathplace as a group of alternative representations
	 * (see {@link VCardSubTypes#getAltId} for more details). An appropriate
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
	public void setDeathplaceAlt(Collection<DeathplaceType> altRepresentations) {
		setTypeAlt(DeathplaceType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's deathplace as a group of alternative representations
	 * (see {@link VCardSubTypes#getAltId} for more details). An appropriate
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
	public void setDeathplaceAlt(DeathplaceType... altRepresentations) {
		setTypeAlt(DeathplaceType.class, altRepresentations);
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
	public void setDeathplace(DeathplaceType deathplace) {
		setType(DeathplaceType.class, deathplace);
	}

	/**
	 * <p>
	 * Gets all death date property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
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
	public List<DeathdateType> getDeathdates() {
		return getTypes(DeathdateType.class);
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
	public DeathdateType getDeathdate() {
		return getType(DeathdateType.class);
	}

	/**
	 * <p>
	 * Sets the deathdate property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setDeathdateAlt(Collection<DeathdateType> altRepresentations) {
		setTypeAlt(DeathdateType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the deathdate property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setDeathdateAlt(DeathdateType... altRepresentations) {
		setTypeAlt(DeathdateType.class, altRepresentations);
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
	public void setDeathdate(DeathdateType deathdate) {
		setType(DeathdateType.class, deathdate);
	}

	/**
	 * <p>
	 * Gets all birthday property instances. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
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
	public List<BirthdayType> getBirthdays() {
		return getTypes(BirthdayType.class);
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
	public BirthdayType getBirthday() {
		return getType(BirthdayType.class);
	}

	/**
	 * <p>
	 * Sets the person's birthday as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setBirthdayAlt(Collection<BirthdayType> altRepresentations) {
		setTypeAlt(BirthdayType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's birthday as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setBirthdayAlt(BirthdayType... altRepresentations) {
		setTypeAlt(BirthdayType.class, altRepresentations);
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
	public void setBirthday(BirthdayType birthday) {
		setType(BirthdayType.class, birthday);
	}

	/**
	 * <p>
	 * Gets all anniversary property instances. There may be multiple instances
	 * if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code ANNIVERSARY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the anniversary properties
	 */
	public List<AnniversaryType> getAnniversaries() {
		return getTypes(AnniversaryType.class);
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
	public AnniversaryType getAnniversary() {
		return getType(AnniversaryType.class);
	}

	/**
	 * <p>
	 * Sets the person's anniversary as a group of alternative representations
	 * (see {@link VCardSubTypes#getAltId} for more details). An appropriate
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
	public void setAnniversaryAlt(Collection<AnniversaryType> altRepresentations) {
		setTypeAlt(AnniversaryType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the person's anniversary as a group of alternative representations
	 * (see {@link VCardSubTypes#getAltId} for more details). An appropriate
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
	public void setAnniversaryAlt(AnniversaryType... altRepresentations) {
		setTypeAlt(AnniversaryType.class, altRepresentations);
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
	public void setAnniversary(AnniversaryType anniversary) {
		setType(AnniversaryType.class, anniversary);
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
	public RevisionType getRevision() {
		return getType(RevisionType.class);
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
	public void setRevision(RevisionType rev) {
		setType(RevisionType.class, rev);
	}

	/**
	 * Sets the time that the vCard was last modified. This is a convenience
	 * method for {@link #setRevision(RevisionType)}.
	 * <p>
	 * <b>Property name:</b> {@code REV}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param rev the last modified time or null to remove
	 * @return the type object that was created
	 */
	public RevisionType setRevision(Date rev) {
		RevisionType type = null;
		if (rev != null) {
			type = new RevisionType(rev);
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
	public ProdIdType getProdId() {
		return getType(ProdIdType.class);
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
	 * @param prodId the product ID
	 */
	public void setProdId(ProdIdType prodId) {
		setType(ProdIdType.class, prodId);
	}

	/**
	 * Sets the product ID, which identifies the software that created the
	 * vCard. This is a convenience method for {@link #setProdId(ProdIdType)}.
	 * <p>
	 * <b>Property name:</b> {@code PRODID}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 3.0, 4.0}
	 * </p>
	 * @param prodId the product ID (e.g. "ez-vcard 1.0") or null to remove
	 * @return the type object that was created
	 */
	public ProdIdType setProdId(String prodId) {
		ProdIdType type = null;
		if (prodId != null) {
			type = new ProdIdType(prodId);
		}
		setProdId(type);
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
	public List<AddressType> getAddresses() {
		return getTypes(AddressType.class);
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
	public void addAddress(AddressType address) {
		addType(address);
	}

	/**
	 * <p>
	 * Adds an address property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addAddressAlt(Collection<AddressType> altRepresentations) {
		addTypeAlt(AddressType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an address property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addAddressAlt(AddressType... altRepresentations) {
		addTypeAlt(AddressType.class, altRepresentations);
	}

	/**
	 * Gets all mailing labels that could not be assigned to an address. Use
	 * {@link AddressType#getLabel} to get a label that has been assigned to an
	 * address.
	 * <p>
	 * <b>Property name:</b> {@code LABEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @return the orphaned labels
	 */
	public List<LabelType> getOrphanedLabels() {
		return getTypes(LabelType.class);
	}

	/**
	 * Adds a mailing label which is not associated with any address. Use of
	 * this method is discouraged. To add a mailing label to an address, use the
	 * {@link AddressType#setLabel} method.
	 * <p>
	 * <b>Property name:</b> {@code LABEL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param label the orphaned label to add
	 */
	public void addOrphanedLabel(LabelType label) {
		addType(label);
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
	public List<EmailType> getEmails() {
		return getTypes(EmailType.class);
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
	public void addEmail(EmailType email) {
		addType(email);
	}

	/**
	 * Adds an email address. This is a convenience method for
	 * {@link #addEmail(EmailType)}.
	 * <p>
	 * <b>Property name:</b> {@code EMAIL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param email the email address to add (e.g. "johndoe@aol.com")
	 * @param types the type(s) to assign to the email
	 * @return the type object that was created
	 */
	public EmailType addEmail(String email, EmailTypeParameter... types) {
		EmailType type = new EmailType(email);
		for (EmailTypeParameter t : types) {
			type.addType(t);
		}
		addEmail(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an email property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addEmailAlt(Collection<EmailType> altRepresentations) {
		addTypeAlt(EmailType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an email property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addEmailAlt(EmailType... altRepresentations) {
		addTypeAlt(EmailType.class, altRepresentations);
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
	public List<TelephoneType> getTelephoneNumbers() {
		return getTypes(TelephoneType.class);
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
	public void addTelephoneNumber(TelephoneType telephoneNumber) {
		addType(telephoneNumber);
	}

	/**
	 * Adds a telephone number. This is a convenience method for
	 * {@link #addTelephoneNumber(TelephoneType)}.
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
	 * @return the type object that was created
	 */
	public TelephoneType addTelephoneNumber(String telephoneNumber, TelephoneTypeParameter... types) {
		TelephoneType type = new TelephoneType(telephoneNumber);
		for (TelephoneTypeParameter t : types) {
			type.addType(t);
		}
		addTelephoneNumber(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a telephone property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addTelephoneNumberAlt(Collection<TelephoneType> altRepresentations) {
		addTypeAlt(TelephoneType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a telephone property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addTelephoneNumberAlt(TelephoneType... altRepresentations) {
		addTypeAlt(TelephoneType.class, altRepresentations);
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
	public MailerType getMailer() {
		return getType(MailerType.class);
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
	public void setMailer(MailerType mailer) {
		setType(MailerType.class, mailer);
	}

	/**
	 * Sets the email client that the person uses. This is a convenience method
	 * for {@link #setMailer(MailerType)}.
	 * <p>
	 * <b>Property name:</b> {@code MAILER}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0}
	 * </p>
	 * @param mailer the email client (e.g. "Thunderbird") or null to remove
	 * @return the type object that was created
	 */
	public MailerType setMailer(String mailer) {
		MailerType type = null;
		if (mailer != null) {
			type = new MailerType(mailer);
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
	public List<UrlType> getUrls() {
		return getTypes(UrlType.class);
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
	public void addUrl(UrlType url) {
		addType(url);
	}

	/**
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website. This is a convenience method for
	 * {@link #addUrl(UrlType)}.
	 * <p>
	 * <b>Property name:</b> {@code URL}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param url the URL to add (e.g. "http://example.com")
	 * @return the type object that was created
	 */
	public UrlType addUrl(String url) {
		UrlType type = new UrlType(url);
		addUrl(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a URL property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addUrlAlt(Collection<UrlType> altRepresentations) {
		addTypeAlt(UrlType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a URL property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addUrlAlt(UrlType... altRepresentations) {
		addTypeAlt(UrlType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Gets all instances of the timezone property. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}) or if properties
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
	public List<TimezoneType> getTimezones() {
		return getTypes(TimezoneType.class);
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
	public TimezoneType getTimezone() {
		return getType(TimezoneType.class);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
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
	public void setTimezoneAlt(Collection<TimezoneType> altRepresentations) {
		setTypeAlt(TimezoneType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
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
	public void setTimezoneAlt(TimezoneType... altRepresentations) {
		setTypeAlt(TimezoneType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
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
	public void addTimezoneAlt(Collection<TimezoneType> altRepresentations) {
		addTypeAlt(TimezoneType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a timezone the person lives/works in as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
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
	public void addTimezoneAlt(TimezoneType... altRepresentations) {
		addTypeAlt(TimezoneType.class, altRepresentations);
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
	public void setTimezone(TimezoneType timezone) {
		setType(TimezoneType.class, timezone);
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
	public void addTimezone(TimezoneType timezone) {
		addType(timezone);
	}

	/**
	 * <p>
	 * Gets all instances of the geo property. Version 4.0 vCards may have
	 * multiple instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}) or if properties
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
	public List<GeoType> getGeos() {
		return getTypes(GeoType.class);
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
	public GeoType getGeo() {
		return getType(GeoType.class);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works as a group
	 * of alternative representations (see: {@link VCardSubTypes#getAltId
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
	public void setGeoAlt(Collection<GeoType> altRepresentations) {
		setTypeAlt(GeoType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a geographical position of where the person lives/works as a group
	 * of alternative representations (see: {@link VCardSubTypes#getAltId
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
	public void addGeoAlt(Collection<GeoType> altRepresentations) {
		addTypeAlt(GeoType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a geographical position of where the person lives/works as a group
	 * of alternative representations (see: {@link VCardSubTypes#getAltId
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
	public void addGeoAlt(GeoType... altRepresentations) {
		addTypeAlt(GeoType.class, altRepresentations);
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
	public void setGeo(GeoType geo) {
		setType(GeoType.class, geo);
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
	public void addGeo(GeoType geo) {
		addType(geo);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works. This is a
	 * convenience method for {@link #setGeo(GeoType)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code GEO}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @return the type object that was created
	 */
	public GeoType setGeo(double latitude, double longitude) {
		GeoType type = new GeoType(latitude, longitude);
		setGeo(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all instances of the organization property. Version 4.0 vCards may
	 * have multiple instances if alternative representations are defined (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}) or if properties
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
	public List<OrganizationType> getOrganizations() {
		return getTypes(OrganizationType.class);
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
	public OrganizationType getOrganization() {
		return getType(OrganizationType.class);
	}

	/**
	 * <p>
	 * Sets the organization property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setOrganizationAlt(Collection<OrganizationType> altRepresentations) {
		setTypeAlt(OrganizationType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the organization property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setOrganizationAlt(OrganizationType... altRepresentations) {
		setTypeAlt(OrganizationType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an organization property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addOrganizationAlt(Collection<OrganizationType> altRepresentations) {
		addTypeAlt(OrganizationType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an organization property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addOrganizationAlt(OrganizationType... altRepresentations) {
		addTypeAlt(OrganizationType.class, altRepresentations);
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
	public void setOrganization(OrganizationType organization) {
		setType(OrganizationType.class, organization);
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
	public void addOrganization(OrganizationType organization) {
		addType(organization);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs. This is a
	 * convenience method for {@link #setOrganization(OrganizationType)}.
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
	 * @return the type object that was created
	 */
	public OrganizationType setOrganization(String... departments) {
		OrganizationType type = null;
		if (departments != null) {
			type = new OrganizationType();
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
	 * {@link VCardSubTypes#getAltId description of ALTID}) or if properties
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
	public List<CategoriesType> getCategoriesList() {
		return getTypes(CategoriesType.class);
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
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @return the categories
	 */
	public CategoriesType getCategories() {
		return getType(CategoriesType.class);
	}

	/**
	 * <p>
	 * Sets the categories property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setCategoriesAlt(Collection<CategoriesType> altRepresentations) {
		setTypeAlt(CategoriesType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Sets the categories property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void setCategoriesAlt(CategoriesType... altRepresentations) {
		setTypeAlt(CategoriesType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a categories property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addCategoriesAlt(Collection<CategoriesType> altRepresentations) {
		addTypeAlt(CategoriesType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a categories property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addCategoriesAlt(CategoriesType... altRepresentations) {
		addTypeAlt(CategoriesType.class, altRepresentations);
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
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param categories the categories or null to remove (note: multiple
	 * categories may be added to this object)
	 */
	public void setCategories(CategoriesType categories) {
		setType(CategoriesType.class, categories);
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
	public void addCategories(CategoriesType categories) {
		addType(categories);
	}

	/**
	 * <p>
	 * Sets the list of keywords (aka "tags") that can be used to describe the
	 * person. This is a convenience method for
	 * {@link #setCategories(CategoriesType)}.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CATEGORIES}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param categories the category or categories (e.g. "swimmer", "biker",
	 * "knitter")
	 * @return the type object that was created
	 */
	public CategoriesType setCategories(String... categories) {
		CategoriesType type = null;
		if (categories != null) {
			type = new CategoriesType();
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
	public AgentType getAgent() {
		return getType(AgentType.class);
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
	public void setAgent(AgentType agent) {
		setType(AgentType.class, agent);
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
	public List<NoteType> getNotes() {
		return getTypes(NoteType.class);
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
	public void addNote(NoteType note) {
		addType(note);
	}

	/**
	 * Adds a note. Notes contain free-form, miscellaneous text. This is a
	 * convenience method for {@link #addNote(NoteType)}.
	 * <p>
	 * <b>Property name:</b> {@code NOTE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
	 * </p>
	 * @param note the note to add
	 * @return the type object that was created
	 */
	public NoteType addNote(String note) {
		NoteType type = new NoteType(note);
		addNote(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a note property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addNoteAlt(Collection<NoteType> altRepresentations) {
		addTypeAlt(NoteType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a note property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addNoteAlt(NoteType... altRepresentations) {
		addTypeAlt(NoteType.class, altRepresentations);
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
	public UidType getUid() {
		return getType(UidType.class);
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
	public void setUid(UidType uid) {
		setType(UidType.class, uid);
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
	public List<KeyType> getKeys() {
		return getTypes(KeyType.class);
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
	public void addKey(KeyType key) {
		addType(key);
	}

	/**
	 * <p>
	 * Adds a key property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addKeyAlt(Collection<KeyType> altRepresentations) {
		addTypeAlt(KeyType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a key property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addKeyAlt(KeyType... altRepresentations) {
		addTypeAlt(KeyType.class, altRepresentations);
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
	public List<ImppType> getImpps() {
		return getTypes(ImppType.class);
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
	public void addImpp(ImppType impp) {
		addType(impp);
	}

	/**
	 * <p>
	 * Adds an impp property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addImppAlt(Collection<ImppType> altRepresentations) {
		addTypeAlt(ImppType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an impp property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addImppAlt(ImppType... altRepresentations) {
		addTypeAlt(ImppType.class, altRepresentations);
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
	public List<RelatedType> getRelations() {
		return getTypes(RelatedType.class);
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
	public void addRelated(RelatedType related) {
		addType(related);
	}

	/**
	 * <p>
	 * Adds a related property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addRelatedAlt(Collection<RelatedType> altRepresentations) {
		addTypeAlt(RelatedType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a related property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addRelatedAlt(RelatedType... altRepresentations) {
		addTypeAlt(RelatedType.class, altRepresentations);
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
	public List<LanguageType> getLanguages() {
		return getTypes(LanguageType.class);
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
	public void addLanguage(LanguageType language) {
		addType(language);
	}

	/**
	 * Adds a language that the person speaks. This is a convenience method for
	 * {@link #addLanguage(LanguageType)}.
	 * <p>
	 * <b>Property name:</b> {@code LANG}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param language the language to add (e.g. "en-us")
	 * @return the type object that was created
	 */
	public LanguageType addLanguage(String language) {
		LanguageType type = new LanguageType(language);
		addLanguage(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a language property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addLanguageAlt(Collection<LanguageType> altRepresentations) {
		addTypeAlt(LanguageType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a language property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addLanguageAlt(LanguageType... altRepresentations) {
		addTypeAlt(LanguageType.class, altRepresentations);
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
	public List<CalendarRequestUriType> getCalendarRequestUris() {
		return getTypes(CalendarRequestUriType.class);
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
	public void addCalendarRequestUri(CalendarRequestUriType calendarRequestUri) {
		addType(calendarRequestUri);
	}

	/**
	 * <p>
	 * Adds a calendar request URI property as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCalendarRequestUriAlt(Collection<CalendarRequestUriType> altRepresentations) {
		addTypeAlt(CalendarRequestUriType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a calendar request URI property as a group of alternative
	 * representations (see: {@link VCardSubTypes#getAltId description of ALTID}
	 * ). An appropriate ALTID parameter value is automatically generated and
	 * assigned to the properties.
	 * </p>
	 * <p>
	 * <b>Property name:</b> {@code CALADRURI}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param altRepresentations the alternative representations of the property
	 */
	public void addCalendarRequestUriAlt(CalendarRequestUriType... altRepresentations) {
		addTypeAlt(CalendarRequestUriType.class, altRepresentations);
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
	public List<CalendarUriType> getCalendarUris() {
		return getTypes(CalendarUriType.class);
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
	public void addCalendarUri(CalendarUriType calendarUri) {
		addType(calendarUri);
	}

	/**
	 * <p>
	 * Adds a calendar URI property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addCalendarUriAlt(Collection<CalendarUriType> altRepresentations) {
		addTypeAlt(CalendarUriType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a calendar URI property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addCalendarUriAlt(CalendarUriType... altRepresentations) {
		addTypeAlt(CalendarUriType.class, altRepresentations);
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
	public List<FbUrlType> getFbUrls() {
		return getTypes(FbUrlType.class);
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
	public void addFbUrl(FbUrlType fbUrl) {
		addType(fbUrl);
	}

	/**
	 * <p>
	 * Adds an fburl property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addFbUrlAlt(Collection<FbUrlType> altRepresentations) {
		addTypeAlt(FbUrlType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an fburl property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addFbUrlAlt(FbUrlType... altRepresentations) {
		addTypeAlt(FbUrlType.class, altRepresentations);
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
	public List<ClientPidMapType> getClientPidMaps() {
		return getTypes(ClientPidMapType.class);
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
	public void addClientPidMap(ClientPidMapType clientPidMap) {
		addType(clientPidMap);
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
	public List<XmlType> getXmls() {
		return getTypes(XmlType.class);
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
	public void addXml(XmlType xml) {
		addType(xml);
	}

	/**
	 * <p>
	 * Adds an XML property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addXmlAlt(Collection<XmlType> altRepresentations) {
		addTypeAlt(XmlType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an XML property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addXmlAlt(XmlType... altRepresentations) {
		addTypeAlt(XmlType.class, altRepresentations);
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
	public List<ExpertiseType> getExpertise() {
		return getTypes(ExpertiseType.class);
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
	public void addExpertise(ExpertiseType expertise) {
		addType(expertise);
	}

	/**
	 * Adds a professional subject area of which the the person is
	 * knowledgeable. This is a convenience method for
	 * {@link #addExpertise(ExpertiseType)}.
	 * <p>
	 * <b>Property name:</b> {@code EXPERTISE}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param expertise the professional skill to add (e.g. "programming")
	 * @return the type object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public ExpertiseType addExpertise(String expertise) {
		ExpertiseType type = new ExpertiseType(expertise);
		addExpertise(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an expertise property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addExpertiseAlt(Collection<ExpertiseType> altRepresentations) {
		addTypeAlt(ExpertiseType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an expertise property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addExpertiseAlt(ExpertiseType... altRepresentations) {
		addTypeAlt(ExpertiseType.class, altRepresentations);
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
	public List<HobbyType> getHobbies() {
		return getTypes(HobbyType.class);
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
	public void addHobby(HobbyType hobby) {
		addType(hobby);
	}

	/**
	 * Adds a hobby that the person actively engages in. This is a convenience
	 * method for {@link #addHobby(HobbyType)}.
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
	public HobbyType addHobby(String hobby) {
		HobbyType type = new HobbyType(hobby);
		addHobby(type);
		return type;
	}

	/**
	 * <p>
	 * Adds a hobby property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addHobbyAlt(Collection<HobbyType> altRepresentations) {
		addTypeAlt(HobbyType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds a hobby property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addHobbyAlt(HobbyType... altRepresentations) {
		addTypeAlt(HobbyType.class, altRepresentations);
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
	public List<InterestType> getInterests() {
		return getTypes(InterestType.class);
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
	public void addInterest(InterestType interest) {
		addType(interest);
	}

	/**
	 * Adds an interest. This is a convenience method for
	 * {@link #addInterest(InterestType)}.
	 * <p>
	 * <b>Property name:</b> {@code INTEREST}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param interest the interest to add (e.g. "football")
	 * @return the type object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public InterestType addInterest(String interest) {
		InterestType type = new InterestType(interest);
		addInterest(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an interest property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addInterestAlt(Collection<InterestType> altRepresentations) {
		addTypeAlt(InterestType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an interest property as a group of alternative representations (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}). An appropriate
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
	public void addInterestAlt(InterestType... altRepresentations) {
		addTypeAlt(InterestType.class, altRepresentations);
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
	public List<OrgDirectoryType> getOrgDirectories() {
		return getTypes(OrgDirectoryType.class);
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
	public void addOrgDirectory(OrgDirectoryType orgDirectory) {
		addType(orgDirectory);
	}

	/**
	 * Adds an organization directory. This is a convenience method for
	 * {@link #addOrgDirectory(OrgDirectoryType)}.
	 * <p>
	 * <b>Property name:</b> {@code ORG-DIRECTORY}
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param orgDirectory the organization directory to add (e.g.
	 * "http://company.com/staff")
	 * @return the type object that was created
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public OrgDirectoryType addOrgDirectory(String orgDirectory) {
		OrgDirectoryType type = new OrgDirectoryType(orgDirectory);
		addOrgDirectory(type);
		return type;
	}

	/**
	 * <p>
	 * Adds an org directory property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addOrgDirectoryAlt(Collection<OrgDirectoryType> altRepresentations) {
		addTypeAlt(OrgDirectoryType.class, altRepresentations);
	}

	/**
	 * <p>
	 * Adds an org directory property as a group of alternative representations
	 * (see: {@link VCardSubTypes#getAltId description of ALTID}). An
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
	public void addOrgDirectoryAlt(OrgDirectoryType... altRepresentations) {
		addTypeAlt(OrgDirectoryType.class, altRepresentations);
	}

	/**
	 * Iterates through each of the vCard's properties in no particular order.
	 * Does not include the "BEGIN", "END", or "VERSION" properties.
	 * @return the iterator
	 */
	public Iterator<VCardType> iterator() {
		return properties.values().iterator();
	}

	/**
	 * Gets the first property of a given class.
	 * @param clazz the property class
	 * @return the property or null if not found
	 */
	public <T extends VCardType> T getType(Class<T> clazz) {
		return clazz.cast(properties.first(clazz));
	}

	/**
	 * Gets all properties of a given class.
	 * @param clazz the property class
	 * @return the properties
	 */
	public <T extends VCardType> List<T> getTypes(Class<T> clazz) {
		List<VCardType> props = properties.get(clazz);

		//cast to the requested class
		List<T> ret = new ArrayList<T>(props.size());
		for (VCardType property : props) {
			ret.add(clazz.cast(property));
		}
		return ret;
	}

	/**
	 * Gets all properties of a given class, grouping the alternative
	 * representations of each property together (see:
	 * {@link VCardSubTypes#getAltId description of ALTID})
	 * @param clazz the property class
	 * @return the properties
	 */
	public <T extends VCardType & HasAltId> List<List<T>> getTypesAlt(Class<T> clazz) {
		List<T> nullAltId = new ArrayList<T>();
		ListMultimap<String, T> map = new ListMultimap<String, T>();
		for (T property : getTypes(clazz)) {
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
	 * Gets all the properties.
	 * @return the properties
	 */
	public Collection<VCardType> getAllTypes() {
		return properties.values();
	}

	/**
	 * Adds a property.
	 * @param property the property to add
	 */
	public void addType(VCardType property) {
		properties.put(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given class with a single
	 * property instance. If the property instance is null, then all instances
	 * of that property will be removed.
	 * @param clazz the property class (e.g. "Note.class")
	 * @param property the property or null to remove
	 */
	public <T extends VCardType> void setType(Class<T> clazz, T property) {
		properties.replace(clazz, property);
	}

	/**
	 * Removes a property instance from the vCard.
	 * @param property the property to remove
	 */
	public void removeType(VCardType property) {
		properties.remove(property.getClass(), property);
	}

	/**
	 * Removes all properties of a given class.
	 * @param clazz the class of the properties to remove (e.g. "Note.class")
	 */
	public void removeTypes(Class<? extends VCardType> clazz) {
		properties.removeAll(clazz);
	}

	/**
	 * Gets the first extended property with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the property or null if none were found
	 */
	public RawType getExtendedType(String name) {
		for (RawType raw : getTypes(RawType.class)) {
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
	public List<RawType> getExtendedTypes(String name) {
		List<RawType> props = new ArrayList<RawType>();

		for (RawType raw : getTypes(RawType.class)) {
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
	public List<RawType> getExtendedTypes() {
		return getTypes(RawType.class);
	}

	/**
	 * Adds an extended property.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawType addExtendedType(String name, String value) {
		RawType raw = new RawType(name, value);
		addType(raw);
		return raw;
	}

	/**
	 * Replaces all existing extended properties with the given name with a
	 * single property instance.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawType setExtendedType(String name, String value) {
		removeExtendedType(name);
		RawType raw = new RawType(name, value);
		addType(raw);
		return raw;
	}

	/**
	 * Removes all extended properties that have the given name.
	 * @param name the component name (e.g. "X-ALT-DESC")
	 */
	public void removeExtendedType(String name) {
		List<RawType> xproperties = getExtendedTypes(name);
		for (RawType xproperty : xproperties) {
			properties.remove(xproperty.getClass(), xproperty);
		}
	}

	/**
	 * Adds a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardType & HasAltId> void addTypeAlt(Class<T> propertyClass, T... altRepresentations) {
		addTypeAlt(propertyClass, Arrays.asList(altRepresentations));
	}

	/**
	 * Adds a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardType & HasAltId> void addTypeAlt(Class<T> propertyClass, Collection<T> altRepresentations) {
		String altId = generateAltId(getTypes(propertyClass));
		for (T property : altRepresentations) {
			property.setAltId(altId);
			addType(property);
		}
	}

	/**
	 * Sets a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardType & HasAltId> void setTypeAlt(Class<T> propertyClass, T... altRepresentations) {
		removeTypes(propertyClass);
		addTypeAlt(propertyClass, altRepresentations);
	}

	/**
	 * Sets a property in the form of a collection of alternative
	 * representations. This method will generate a unique ALTID parameter value
	 * and assign it to each of the property instances (see:
	 * {@link VCardSubTypes#getAltId description of ALTID}).
	 * @param propertyClass the property class
	 * @param altRepresentations the alternative representations of the property
	 * to add
	 */
	public <T extends VCardType & HasAltId> void setTypeAlt(Class<T> propertyClass, Collection<T> altRepresentations) {
		removeTypes(propertyClass);
		addTypeAlt(propertyClass, altRepresentations);
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
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();

		//validate overall vCard object
		List<String> vcardWarnings = new ArrayList<String>();
		if (getStructuredName() == null && (version == VCardVersion.V2_1 || version == VCardVersion.V3_0)) {
			vcardWarnings.add("A structured name property must be defined.");
		}
		if (getFormattedName() == null && (version == VCardVersion.V3_0 || version == VCardVersion.V4_0)) {
			vcardWarnings.add("A formatted name property must be defined.");
		}
		if (!vcardWarnings.isEmpty()) {
			groups.add(new WarningsGroup(null, vcardWarnings));
		}

		//validate properties
		for (VCardType property : this) {
			List<String> warnings = property.validate(version, this);
			if (!warnings.isEmpty()) {
				groups.add(new WarningsGroup(property, warnings));
			}
		}

		return new ValidationWarnings(groups, version);
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