package ezvcard;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import ezvcard.io.VCardWriter;
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
import freemarker.template.TemplateException;

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
 * Represents the data in a vCard.
 * @author Michael Angstadt
 */
public class VCard implements Iterable<VCardType> {
	private VCardVersion version = VCardVersion.V3_0;
	private KindType kind;
	private GenderType gender;
	private List<MemberType> members = new ArrayList<MemberType>();
	private ProfileType profile;
	private ClassificationType classification;
	private List<SourceType> sources = new ArrayList<SourceType>();
	private SourceDisplayTextType sourceDisplayText;
	private List<FormattedNameType> formattedNames = new ArrayList<FormattedNameType>();
	private List<StructuredNameType> structuredNames = new ArrayList<StructuredNameType>();
	private List<NicknameType> nicknames = new ArrayList<NicknameType>();
	private SortStringType sortString;
	private List<TitleType> titles = new ArrayList<TitleType>();
	private List<RoleType> roles = new ArrayList<RoleType>();
	private List<PhotoType> photos = new ArrayList<PhotoType>();
	private List<LogoType> logos = new ArrayList<LogoType>();
	private List<SoundType> sounds = new ArrayList<SoundType>();
	private List<BirthplaceType> birthplaces = new ArrayList<BirthplaceType>();
	private List<DeathplaceType> deathplaces = new ArrayList<DeathplaceType>();
	private List<DeathdateType> deathdates = new ArrayList<DeathdateType>();
	private List<BirthdayType> birthdays = new ArrayList<BirthdayType>();
	private List<AnniversaryType> anniversaries = new ArrayList<AnniversaryType>();
	private RevisionType rev;
	private ProdIdType prodId;
	private List<AddressType> addresses = new ArrayList<AddressType>();
	private List<LabelType> labels = new ArrayList<LabelType>();
	private List<EmailType> emails = new ArrayList<EmailType>();
	private List<TelephoneType> telephoneNumbers = new ArrayList<TelephoneType>();
	private MailerType mailer;
	private List<UrlType> urls = new ArrayList<UrlType>();
	private List<TimezoneType> timezones = new ArrayList<TimezoneType>();
	private List<GeoType> geos = new ArrayList<GeoType>();
	private List<OrganizationType> organizations = new ArrayList<OrganizationType>();
	private List<CategoriesType> categories = new ArrayList<CategoriesType>();
	private AgentType agent;
	private List<NoteType> notes = new ArrayList<NoteType>();
	private UidType uid;
	private List<KeyType> keys = new ArrayList<KeyType>();
	private List<ImppType> impps = new ArrayList<ImppType>();
	private List<RelatedType> relations = new ArrayList<RelatedType>();
	private List<LanguageType> languages = new ArrayList<LanguageType>();
	private List<CalendarRequestUriType> calendarRequestUris = new ArrayList<CalendarRequestUriType>();
	private List<CalendarUriType> calendarUris = new ArrayList<CalendarUriType>();
	private List<FbUrlType> fbUrls = new ArrayList<FbUrlType>();
	private List<ClientPidMapType> clientPidMaps = new ArrayList<ClientPidMapType>();
	private List<XmlType> xmls = new ArrayList<XmlType>();
	private List<ExpertiseType> expertises = new ArrayList<ExpertiseType>();
	private List<HobbyType> hobbies = new ArrayList<HobbyType>();
	private List<InterestType> interests = new ArrayList<InterestType>();
	private List<OrgDirectoryType> orgDirectories = new ArrayList<OrgDirectoryType>();
	private ListMultimap<String, VCardType> extendedTypes = new ListMultimap<String, VCardType>();

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
	public String writeHtml() throws TemplateException {
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
	 * @throws TemplateException if there's a problem with the freemarker
	 * template
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public void writeHtml(File file) throws IOException, TemplateException {
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
	 * @throws TemplateException if there's a problem with the freemarker
	 * template
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public void writeHtml(OutputStream out) throws IOException, TemplateException {
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
	 * @throws TemplateException if there's a problem with the freemarker
	 * template
	 * @see Ezvcard
	 * @see <a href="http://microformats.org/wiki/hcard">hCard 1.0</a>
	 */
	public void writeHtml(Writer writer) throws IOException, TemplateException {
		Ezvcard.writeHtml(this).go(writer);
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
	 * vCard property name: KIND
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the kind
	 */
	public KindType getKind() {
		return kind;
	}

	/**
	 * Sets the type of entity this vCard represents.
	 * <p>
	 * vCard property name: KIND
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param kind the kind
	 */
	public void setKind(KindType kind) {
		this.kind = kind;
	}

	/**
	 * Gets the gender of the person.
	 * <p>
	 * vCard property name: GENDER
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the gender
	 */
	public GenderType getGender() {
		return gender;
	}

	/**
	 * Sets the gender of the person.
	 * <p>
	 * vCard property name: GENDER
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param gender the gender
	 */
	public void setGender(GenderType gender) {
		this.gender = gender;
	}

	/**
	 * Gets the members of the group. Only valid if the KIND property is set to
	 * "group".
	 * 
	 * <p>
	 * 
	 * <pre>
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
	 * vCard property name: MEMBER
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the members
	 */
	public List<MemberType> getMembers() {
		return members;
	}

	/**
	 * Adds a member to the group. Only valid if the KIND property is set to
	 * "group".
	 * 
	 * <p>
	 * 
	 * <pre>
	 * VCard vcard = new VCard();
	 * vcard.setKind(KindType.group());
	 * vcard.addMember(...);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * vCard property name: MEMBER
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param member the member to add
	 */
	public void addMember(MemberType member) {
		members.add(member);
	}

	/**
	 * Gets the PROFILE property.
	 * <p>
	 * vCard property name: PROFILE
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @return the property
	 */
	public ProfileType getProfile() {
		return profile;
	}

	/**
	 * Sets the PROFILE property.
	 * <p>
	 * vCard property name: PROFILE
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @param profile the property
	 */
	public void setProfile(ProfileType profile) {
		this.profile = profile;
	}

	/**
	 * Gets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * <p>
	 * vCard property name: CLASS
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @return the classification
	 */
	public ClassificationType getClassification() {
		return classification;
	}

	/**
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard.
	 * <p>
	 * vCard property name: CLASS
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @param classification the classification
	 */
	public void setClassification(ClassificationType classification) {
		this.classification = classification;
	}

	/**
	 * Sets the classification of the vCard, which describes the sensitivity of
	 * the information in the vCard. This is a convenience method for
	 * {@link #setClassification(ClassificationType)}.
	 * <p>
	 * </p>
	 * <p>
	 * vCard property name: CLASS
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @param classification the classification (e.g. "PUBLIC", "PRIVATE",
	 * "CONFIDENTIAL")
	 * @return the type object that was created
	 */
	public ClassificationType setClassification(String classification) {
		ClassificationType type = new ClassificationType(classification);
		setClassification(type);
		return type;
	}

	/**
	 * Gets the URIs that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * <p>
	 * vCard property name: SOURCE
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @return the sources
	 */
	public List<SourceType> getSources() {
		return sources;
	}

	/**
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard.
	 * <p>
	 * vCard property name: SOURCE
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param source the source
	 */
	public void addSource(SourceType source) {
		sources.add(source);
	}

	/**
	 * Adds a URI that can be used to retrieve the most up-to-date version of
	 * the person's vCard. This is a convenience method for
	 * {@link #addSource(SourceType)} .
	 * <p>
	 * vCard property name: SOURCE
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
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
	 * Gets a textual representation of the SOURCE property.
	 * <p>
	 * vCard property name: NAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @return a textual representation of the vCard source
	 */
	public SourceDisplayTextType getSourceDisplayText() {
		return sourceDisplayText;
	}

	/**
	 * Sets a textual representation of the SOURCE property.
	 * <p>
	 * vCard property name: NAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @param sourceDisplayText a textual representation of the vCard source
	 */
	public void setSourceDisplayText(SourceDisplayTextType sourceDisplayText) {
		this.sourceDisplayText = sourceDisplayText;
	}

	/**
	 * Sets a textual representation of the SOURCE property. This is a
	 * convenience method for
	 * {@link #setSourceDisplayText(SourceDisplayTextType)}.
	 * <p>
	 * vCard property name: NAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0
	 * </p>
	 * @param sourceDisplayText a textual representation of the vCard source
	 * @return the type object that was created
	 */
	public SourceDisplayTextType setSourceDisplayText(String sourceDisplayText) {
		SourceDisplayTextType type = new SourceDisplayTextType(sourceDisplayText);
		setSourceDisplayText(type);
		return type;
	}

	/**
	 * <p>
	 * Gets the text values for displaying the person's name.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getFormattedName}.
	 * </p>
	 * <p>
	 * vCard property name: FN
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the formatted name property
	 * @see FormattedNameType#getAltId
	 */
	public List<FormattedNameType> getFormattedNames() {
		return formattedNames;
	}

	/**
	 * <p>
	 * Gets the text value used for displaying the person's name.
	 * </p>
	 * <p>
	 * Use {@link #getFormattedNames} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: FN
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the formatted name property
	 */
	public FormattedNameType getFormattedName() {
		return formattedNames.isEmpty() ? null : formattedNames.get(0);
	}

	/**
	 * <p>
	 * Adds a text value used for displaying the person's name.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setFormattedName(FormattedNameType)}.
	 * </p>
	 * <p>
	 * vCard property name: FN
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param formattedName the formatted name property
	 * @see FormattedNameType#setAltId
	 */
	public void addFormattedName(FormattedNameType formattedName) {
		formattedNames.add(formattedName);
	}

	/**
	 * <p>
	 * Sets the text value used for displaying the person's name.
	 * </p>
	 * <p>
	 * Use {@link #addFormattedName} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: FN
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param formattedName the formatted name property
	 */
	public void setFormattedName(FormattedNameType formattedName) {
		formattedNames.clear();
		addFormattedName(formattedName);
	}

	/**
	 * <p>
	 * Sets the text value used for displaying the person's name. This is a
	 * convenience method for {@link #setFormattedName(FormattedNameType)}.
	 * </p>
	 * <p>
	 * Use {@link #addFormattedName} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: FN
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param formattedName the formatted name (e.g. "John Doe")
	 * @return the type object that was created
	 */
	public FormattedNameType setFormattedName(String formattedName) {
		FormattedNameType type = new FormattedNameType(formattedName);
		setFormattedName(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all structured name properties.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getStructuredName}.
	 * </p>
	 * <p>
	 * vCard property name: N
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the N properties
	 * @see StructuredNameType#getAltId
	 */
	public List<StructuredNameType> getStructuredNames() {
		return structuredNames;
	}

	/**
	 * <p>
	 * Gets the individual components of the person's name.
	 * </p>
	 * <p>
	 * Use {@link #getStructuredNames} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: N
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the components of the person's name
	 */
	public StructuredNameType getStructuredName() {
		return structuredNames.isEmpty() ? null : structuredNames.get(0);
	}

	/**
	 * <p>
	 * Adds a property that contains the individual components of the person's
	 * name.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setStructuredName}.
	 * </p>
	 * <p>
	 * vCard property name: N
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param structuredName the components of the person's name
	 * @see StructuredNameType#setAltId
	 */
	public void addStructuredName(StructuredNameType structuredName) {
		structuredNames.add(structuredName);
	}

	/**
	 * Sets the individual components of the person's name.
	 * <p>
	 * vCard property name: N
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param structuredName the components of the person's name
	 */
	public void setStructuredName(StructuredNameType structuredName) {
		structuredNames.clear();
		addStructuredName(structuredName);
	}

	/**
	 * <p>
	 * Gets all instances of the NICKNAME property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getNickname}.
	 * </p>
	 * <p>
	 * vCard property name: NICKNAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @return the person's nicknames
	 * @see NicknameType#getAltId
	 */
	public List<NicknameType> getNicknames() {
		return nicknames;
	}

	/**
	 * <p>
	 * Gets the person's nicknames.
	 * </p>
	 * <p>
	 * Use {@link #getNicknames} to get the alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: NICKNAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @return the person's nicknames
	 */
	public NicknameType getNickname() {
		return nicknames.isEmpty() ? null : nicknames.get(0);
	}

	/**
	 * <p>
	 * Adds an instance of the NICKNAME property to the vCard.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setNickname(NicknameType)}.
	 * </p>
	 * <p>
	 * vCard property name: NICKNAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param nickname the NICKNAME property to add
	 * @see NicknameType#setAltId
	 */
	public void addNickname(NicknameType nickname) {
		nicknames.add(nickname);
	}

	/**
	 * <p>
	 * Sets one or more nicknames.
	 * </p>
	 * <p>
	 * Use {@link #addNickname} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: NICKNAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param nickname the nicknames (multiple nicknames can be added to this
	 * object)
	 */
	public void setNickname(NicknameType nickname) {
		nicknames.clear();
		addNickname(nickname);
	}

	/**
	 * <p>
	 * Sets one or more nicknames. This is a convenience method for
	 * {@link #setNickname(NicknameType)}.
	 * </p>
	 * <p>
	 * Use {@link #addNickname} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: NICKNAME
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param nicknames the nickname(s)
	 * @return the type object that was created
	 */
	public NicknameType setNickname(String... nicknames) {
		NicknameType type = new NicknameType();
		for (String nickname : nicknames) {
			type.addValue(nickname);
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
	 * vCard property name: SORT-STRING
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @return the sort string
	 */
	public SortStringType getSortString() {
		return sortString;
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
	 * vCard property name: SORT-STRING
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param sortString the sort string
	 */
	public void setSortString(SortStringType sortString) {
		this.sortString = sortString;
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
	 * vCard property name: SORT-STRING
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param sortString the sort string (e.g. "Armour" if the person's last
	 * name is "d'Armour")
	 * @return the type object that was created
	 */
	public SortStringType setSortString(String sortString) {
		SortStringType type = new SortStringType(sortString);
		setSortString(type);
		return type;
	}

	/**
	 * Gets the titles associated with the person.
	 * <p>
	 * vCard property name: TITLE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the titles
	 */
	public List<TitleType> getTitles() {
		return titles;
	}

	/**
	 * Adds a title associated with the person.
	 * <p>
	 * vCard property name: TITLE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param title the title
	 */
	public void addTitle(TitleType title) {
		titles.add(title);
	}

	/**
	 * Adds a title associated with the person. This is a convenience method for
	 * {@link #addTitle(TitleType)}.
	 * <p>
	 * vCard property name: TITLE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Gets the roles associated with the person.
	 * <p>
	 * vCard property name: ROLE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the roles
	 */
	public List<RoleType> getRoles() {
		return roles;
	}

	/**
	 * Adds a role associated with the person.
	 * <p>
	 * vCard property name: ROLE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param role the role
	 */
	public void addRole(RoleType role) {
		roles.add(role);
	}

	/**
	 * Adds a role associated with the person. This is a convenience method for
	 * {@link #addRole(RoleType)}.
	 * <p>
	 * vCard property name: ROLE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Gets the photos attached to the vCard, such as a picture of the person's
	 * face.
	 * <p>
	 * vCard property name: PHOTO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the photos
	 */
	public List<PhotoType> getPhotos() {
		return photos;
	}

	/**
	 * Adds a photo to the vCard, such as a picture of the person's face.
	 * <p>
	 * vCard property name: PHOTO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param photo the photo to add
	 */
	public void addPhoto(PhotoType photo) {
		photos.add(photo);
	}

	/**
	 * Gets the logos attached to the vCard, such a company logo.
	 * <p>
	 * vCard property name: LOGO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the logos
	 */
	public List<LogoType> getLogos() {
		return logos;
	}

	/**
	 * Adds a logo to the vCard, such as a company logo.
	 * <p>
	 * vCard property name: LOGO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param logo the logo to add
	 */
	public void addLogo(LogoType logo) {
		logos.add(logo);
	}

	/**
	 * Gets the sounds attached to the vCard, such as a pronunciation of the
	 * person's name.
	 * <p>
	 * vCard property name: SOUND
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the sounds
	 */
	public List<SoundType> getSounds() {
		return sounds;
	}

	/**
	 * Adds a sound to the vCard, such as a pronunciation of the person's name.
	 * <p>
	 * vCard property name: SOUND
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param sound the sound to add
	 */
	public void addSound(SoundType sound) {
		sounds.add(sound);
	}

	/**
	 * <p>
	 * Gets all birthplace property instances.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getBirthplace}.
	 * </p>
	 * <p>
	 * vCard property name: BIRTHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the birthplace properties
	 * @see BirthplaceType#getAltId
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public List<BirthplaceType> getBirthplaces() {
		return birthplaces;
	}

	/**
	 * <p>
	 * Adds a birthplace property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setBirthplace}.
	 * </p>
	 * <p>
	 * vCard property name: BIRTHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param birthplace the birthplace to add
	 * @see BirthplaceType#setAltId
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void addBirthplace(BirthplaceType birthplace) {
		birthplaces.add(birthplace);
	}

	/**
	 * <p>
	 * Gets the person's birthplace.
	 * </p>
	 * <p>
	 * Use {@link #getBirthplaces} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: BIRTHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the birthplace
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public BirthplaceType getBirthplace() {
		return birthplaces.isEmpty() ? null : birthplaces.get(0);
	}

	/**
	 * <p>
	 * Sets the person's birthplace.
	 * </p>
	 * <p>
	 * Use {@link #addBirthplace} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: BIRTHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param birthplace the birthplace
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setBirthplace(BirthplaceType birthplace) {
		birthplaces.clear();
		addBirthplace(birthplace);
	}

	/**
	 * <p>
	 * Gets all deathplace property instances.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getDeathplace}.
	 * </p>
	 * <p>
	 * vCard property name: DEATHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the deathplace properties
	 * @see DeathplaceType#getAltId
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public List<DeathplaceType> getDeathplaces() {
		return deathplaces;
	}

	/**
	 * <p>
	 * Adds a deathplace property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setDeathplace}.
	 * </p>
	 * <p>
	 * vCard property name: DEATHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param deathplace the deathplace to add
	 * @see DeathplaceType#setAltId
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void addDeathplace(DeathplaceType deathplace) {
		deathplaces.add(deathplace);
	}

	/**
	 * <p>
	 * Gets the person's deathplace.
	 * </p>
	 * <p>
	 * Use {@link #getDeathplaces} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: DEATHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the deathplace
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public DeathplaceType getDeathplace() {
		return deathplaces.isEmpty() ? null : deathplaces.get(0);
	}

	/**
	 * <p>
	 * Sets the person's deathplace.
	 * </p>
	 * <p>
	 * Use {@link #addDeathplace} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: DEATHPLACE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param deathplace the deathplace
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setDeathplace(DeathplaceType deathplace) {
		deathplaces.clear();
		addDeathplace(deathplace);
	}

	/**
	 * <p>
	 * Gets all death date property instances.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getDeathdate}.
	 * </p>
	 * <p>
	 * vCard property name: DEATHDATE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the death date properties
	 * @see DeathdateType#getAltId
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public List<DeathdateType> getDeathdates() {
		return deathdates;
	}

	/**
	 * <p>
	 * Adds a death date property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setDeathdate}.
	 * </p>
	 * <p>
	 * vCard property name: DEATHDATE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param deathdate the death date to add
	 * @see DeathdateType#setAltId
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void addDeathdate(DeathdateType deathdate) {
		deathdates.add(deathdate);
	}

	/**
	 * <p>
	 * Gets the person's time of death.
	 * </p>
	 * <p>
	 * Use {@link #getDeathdates} to get the alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: DEATHDATE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the time of death
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public DeathdateType getDeathdate() {
		return deathdates.isEmpty() ? null : deathdates.get(0);
	}

	/**
	 * <p>
	 * Sets the person's time of death.
	 * </p>
	 * <p>
	 * Use {@link #addDeathdate} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: DEATHDATE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param deathdate the time of death
	 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
	 */
	public void setDeathdate(DeathdateType deathdate) {
		deathdates.clear();
		addDeathdate(deathdate);
	}

	/**
	 * <p>
	 * Gets all birthday property instances.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getBirthday}.
	 * </p>
	 * <p>
	 * vCard property name: BDAY
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the birthday properties
	 * @see BirthdayType#getAltId
	 */
	public List<BirthdayType> getBirthdays() {
		return birthdays;
	}

	/**
	 * <p>
	 * Adds a birthday property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setBirthday}.
	 * </p>
	 * <p>
	 * vCard property name: BDAY
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param birthday the birthday to add
	 * @see BirthdayType#setAltId
	 */
	public void addBirthday(BirthdayType birthday) {
		birthdays.add(birthday);
	}

	/**
	 * <p>
	 * Gets the person's birthday.
	 * </p>
	 * <p>
	 * Use {@link #getBirthdays} to get the alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: BDAY
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the birthday
	 */
	public BirthdayType getBirthday() {
		return birthdays.isEmpty() ? null : birthdays.get(0);
	}

	/**
	 * <p>
	 * Sets the person's birthday.
	 * </p>
	 * <p>
	 * Use {@link #addBirthday} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: BDAY
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param birthday the birthday
	 */
	public void setBirthday(BirthdayType birthday) {
		birthdays.clear();
		addBirthday(birthday);
	}

	/**
	 * <p>
	 * Gets all anniversary property instances.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getAnniversary}.
	 * </p>
	 * <p>
	 * vCard property name: ANNIVERSARY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the death date properties
	 * @see AnniversaryType#getAltId
	 */
	public List<AnniversaryType> getAnniversaries() {
		return anniversaries;
	}

	/**
	 * <p>
	 * Adds an anniversary property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setAnniversary}.
	 * </p>
	 * <p>
	 * vCard property name: ANNIVERSARY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param anniversary the anniversary to add
	 * @see AnniversaryType#setAltId
	 */
	public void addAnniversary(AnniversaryType anniversary) {
		anniversaries.add(anniversary);
	}

	/**
	 * <p>
	 * Gets the person's anniversary.
	 * </p>
	 * <p>
	 * Use {@link #getAnniversaries} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: ANNIVERSARY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the anniversary
	 */
	public AnniversaryType getAnniversary() {
		return anniversaries.isEmpty() ? null : anniversaries.get(0);
	}

	/**
	 * <p>
	 * Sets the person's anniversary.
	 * </p>
	 * <p>
	 * Use {@link #addAnniversary} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: ANNIVERSARY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param anniversary the anniversary
	 */
	public void setAnniversary(AnniversaryType anniversary) {
		anniversaries.clear();
		addAnniversary(anniversary);
	}

	/**
	 * Gets the time that the vCard was last modified.
	 * <p>
	 * vCard property name: REV
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the last modified time
	 */
	public RevisionType getRevision() {
		return rev;
	}

	/**
	 * Sets the time that the vCard was last modified.
	 * <p>
	 * vCard property name: REV
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param rev the last modified time
	 */
	public void setRevision(RevisionType rev) {
		this.rev = rev;
	}

	/**
	 * Sets the time that the vCard was last modified. This is a convenience
	 * method for {@link #setRevision(RevisionType)}.
	 * <p>
	 * vCard property name: REV
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param rev the last modified time
	 * @return the type object that was created
	 */
	public RevisionType setRevision(Date rev) {
		RevisionType type = new RevisionType(rev);
		setRevision(type);
		return type;
	}

	/**
	 * Gets the product ID, which identifies the software that created the
	 * vCard.
	 * <p>
	 * vCard property name: PRODID
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @return the product ID
	 */
	public ProdIdType getProdId() {
		return prodId;
	}

	/**
	 * Sets the product ID, which identifies the software that created the
	 * vCard.
	 * <p>
	 * vCard property name: PRODID
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param prodId the product ID
	 */
	public void setProdId(ProdIdType prodId) {
		this.prodId = prodId;
	}

	/**
	 * Sets the product ID, which identifies the software that created the
	 * vCard. This is a convenience method for {@link #setProdId(ProdIdType)}.
	 * <p>
	 * vCard property name: PRODID
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param prodId the product ID (e.g. "ez-vcard 1.0")
	 * @return the type object that was created
	 */
	public ProdIdType setProdId(String prodId) {
		ProdIdType type = new ProdIdType(prodId);
		setProdId(type);
		return type;
	}

	/**
	 * Gets the mailing addresses.
	 * <p>
	 * vCard property name: ADR
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the mailing addresses
	 */
	public List<AddressType> getAddresses() {
		return addresses;
	}

	/**
	 * Adds a mailing address.
	 * <p>
	 * vCard property name: ADR
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param address the mailing address to add
	 */
	public void addAddress(AddressType address) {
		addresses.add(address);
	}

	/**
	 * Gets all mailing labels that could not be assigned to an address. Use
	 * {@link AddressType#getLabel} to get a label that has been assigned to an
	 * address.
	 * <p>
	 * vCard property name: LABEL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @return the orphaned labels
	 */
	public List<LabelType> getOrphanedLabels() {
		return labels;
	}

	/**
	 * Adds a mailing label which is not associated with any address. Use of
	 * this method is discouraged. To add a mailing label to an address, use the
	 * {@link AddressType#setLabel} method.
	 * <p>
	 * vCard property name: LABEL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param label the orphaned label to add
	 */
	public void addOrphanedLabel(LabelType label) {
		labels.add(label);
	}

	/**
	 * Gets the email addresses.
	 * <p>
	 * vCard property name: EMAIL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the email addresses
	 */
	public List<EmailType> getEmails() {
		return emails;
	}

	/**
	 * Adds an email address.
	 * <p>
	 * vCard property name: EMAIL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param email the email address to add
	 */
	public void addEmail(EmailType email) {
		emails.add(email);
	}

	/**
	 * Adds an email address. This is a convenience method for
	 * {@link #addEmail(EmailType)}.
	 * <p>
	 * vCard property name: EMAIL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Gets the telephone numbers.
	 * <p>
	 * vCard property name: TEL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the telephone numbers
	 */
	public List<TelephoneType> getTelephoneNumbers() {
		return telephoneNumbers;
	}

	/**
	 * Adds a telephone number.
	 * <p>
	 * vCard property name: TEL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param telephoneNumber the telephone number to add
	 */
	public void addTelephoneNumber(TelephoneType telephoneNumber) {
		telephoneNumbers.add(telephoneNumber);
	}

	/**
	 * Adds a telephone number. This is a convenience method for
	 * {@link #addTelephoneNumber(TelephoneType)}.
	 * <p>
	 * vCard property name: TEL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Gets the email client that the person uses.
	 * <p>
	 * vCard property name: MAILER
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @return the email client
	 */
	public MailerType getMailer() {
		return mailer;
	}

	/**
	 * Sets the email client that the person uses.
	 * <p>
	 * vCard property name: MAILER
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param mailer the email client
	 */
	public void setMailer(MailerType mailer) {
		this.mailer = mailer;
	}

	/**
	 * Sets the email client that the person uses. This is a convenience method
	 * for {@link #setMailer(MailerType)}.
	 * <p>
	 * vCard property name: MAILER
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param mailer the email client (e.g. "Thunderbird")
	 * @return the type object that was created
	 */
	public MailerType setMailer(String mailer) {
		MailerType type = new MailerType(mailer);
		setMailer(type);
		return type;
	}

	/**
	 * Gets the URLs. URLs can point to websites such as a personal homepage or
	 * business website.
	 * <p>
	 * vCard property name: URL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the URLs
	 */
	public List<UrlType> getUrls() {
		return urls;
	}

	/**
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website.
	 * <p>
	 * vCard property name: URL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param url the URL to add
	 */
	public void addUrl(UrlType url) {
		urls.add(url);
	}

	/**
	 * Adds a URL. URLs can point to websites such as a personal homepage or
	 * business website. This is a convenience method for
	 * {@link #addUrl(UrlType)}.
	 * <p>
	 * vCard property name: URL
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Gets all instances of the timezone property.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getTimezone}.
	 * </p>
	 * <p>
	 * vCard property name: TZ
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the timezones
	 * @see TimezoneType#getAltId
	 */
	public List<TimezoneType> getTimezones() {
		return timezones;
	}

	/**
	 * <p>
	 * Gets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * Use {@link #getTimezones} to get the alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: TZ
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the timezone
	 */
	public TimezoneType getTimezone() {
		return timezones.isEmpty() ? null : timezones.get(0);
	}

	/**
	 * <p>
	 * Sets the timezone the person lives/works in.
	 * </p>
	 * <p>
	 * Use {@link #addTimezone} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: TZ
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param timezone the timezone
	 */
	public void setTimezone(TimezoneType timezone) {
		timezones.clear();
		addTimezone(timezone);
	}

	/**
	 * <p>
	 * Adds a timezone to the vCard.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setTimezone}.
	 * </p>
	 * <p>
	 * vCard property name: TZ
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param timezone the timezone to add
	 * @see TimezoneType#setAltId
	 */
	public void addTimezone(TimezoneType timezone) {
		timezones.add(timezone);
	}

	/**
	 * <p>
	 * Gets all geographical position properties.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getGeo}.
	 * </p>
	 * <p>
	 * vCard property name: GEO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the geographical positions
	 * @see GeoType#getAltId
	 */
	public List<GeoType> getGeos() {
		return geos;
	}

	/**
	 * <p>
	 * Gets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * Use {@link #getGeos} to get the alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: GEO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the geographical position
	 */
	public GeoType getGeo() {
		return geos.isEmpty() ? null : geos.get(0);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works.
	 * </p>
	 * <p>
	 * Use {@link #addGeo} to add alternative representations of this property
	 * (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: GEO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param geo the geographical position
	 */
	public void setGeo(GeoType geo) {
		geos.clear();
		addGeo(geo);
	}

	/**
	 * <p>
	 * Sets the geographical position of where the person lives/works. This is a
	 * convenience method for {@link #setGeo(GeoType)}.
	 * </p>
	 * <p>
	 * Use {@link #addGeo} to add alternative representations of this property
	 * (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: GEO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Adds a geographical position to the vCard.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setGeo(GeoType)}.
	 * </p>
	 * <p>
	 * vCard property name: GEO
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param geo the geographical position to add
	 * @see GeoType#setAltId
	 */
	public void addGeo(GeoType geo) {
		geos.add(geo);
	}

	/**
	 * <p>
	 * Gets the hierarchy of department(s) to which the person belongs.
	 * </p>
	 * <p>
	 * Use {@link #getOrganizations} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: ORG
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the department(s)
	 */
	public OrganizationType getOrganization() {
		return organizations.isEmpty() ? null : organizations.get(0);
	}

	/**
	 * <p>
	 * Gets all organization properties.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getOrganization}.
	 * </p>
	 * <p>
	 * vCard property name: ORG
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the organization properties
	 * @see OrganizationType#getAltId
	 */
	public List<OrganizationType> getOrganizations() {
		return organizations;
	}

	/**
	 * <p>
	 * Adds an organization property to the vCard.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setOrganization(OrganizationType)}.
	 * </p>
	 * <p>
	 * vCard property name: ORG
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param organization the organization type object to add
	 * @see OrganizationType#setAltId
	 */
	public void addOrganization(OrganizationType organization) {
		organizations.add(organization);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs.
	 * </p>
	 * <p>
	 * Use {@link #addOrganization} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: ORG
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param organization the organization type object
	 */
	public void setOrganization(OrganizationType organization) {
		organizations.clear();
		addOrganization(organization);
	}

	/**
	 * <p>
	 * Sets the hierarchy of departments to which the person belongs. This is a
	 * convenience method for {@link #setOrganization(OrganizationType)}.
	 * </p>
	 * <p>
	 * Use {@link #addOrganization} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: ORG
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param departments the ordered list of department(s), starting with the
	 * broadest and ending with the most specific (e.g. "Google", "GMail Team",
	 * "Spam Detection Squad")
	 */
	public OrganizationType setOrganization(String... departments) {
		OrganizationType type = new OrganizationType();
		for (String department : departments) {
			type.addValue(department);
		}
		setOrganization(type);
		return type;
	}

	/**
	 * <p>
	 * Gets all CATEGORIES properties.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when there are
	 * alternative representations of this property (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #getCategories}.
	 * </p>
	 * <p>
	 * vCard property name: CATEGORIES
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the CATEGORIES properties
	 * @see CategoriesType#getAltId
	 */
	public List<CategoriesType> getCategoriesList() {
		return categories;
	}

	/**
	 * <p>
	 * Gets the list of keywords (aka "tags") that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * Use {@link #getCategoriesList} to get the alternative representations of
	 * this property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: CATEGORIES
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the categories
	 */
	public CategoriesType getCategories() {
		return categories.isEmpty() ? null : categories.get(0);
	}

	/**
	 * <p>
	 * Adds a CATEGORIES property to the vCard.
	 * </p>
	 * <p>
	 * This method should only be used on the rare occasion when alternative
	 * representations of this property are needed (see:
	 * {@link VCardSubTypes#getAltId explanation of ALTID}). Otherwise, use
	 * {@link #setCategories(CategoriesType)}.
	 * </p>
	 * <p>
	 * vCard property name: CATEGORIES
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param categories the CATEGORIES property to add
	 * @see CategoriesType#setAltId
	 */
	public void addCategories(CategoriesType categories) {
		this.categories.add(categories);
	}

	/**
	 * <p>
	 * Sets the list of keywords (aka "tags") that can be used to describe the
	 * person.
	 * </p>
	 * <p>
	 * Use {@link #addCategories} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: CATEGORIES
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param categories the categories (multiple categories can be added to
	 * this object)
	 */
	public void setCategories(CategoriesType categories) {
		this.categories.clear();
		addCategories(categories);
	}

	/**
	 * <p>
	 * Sets the list of keywords (aka "tags") that can be used to describe the
	 * person. This is a convenience method for
	 * {@link #setCategories(CategoriesType)}.
	 * </p>
	 * <p>
	 * Use {@link #addCategories} to add alternative representations of this
	 * property (see: {@link VCardSubTypes#getAltId explanation of ALTID}).
	 * </p>
	 * <p>
	 * vCard property name: CATEGORIES
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param categories the category or categories (e.g. "swimmer", "biker",
	 * "knitter")
	 * @return the type object that was created
	 */
	public CategoriesType setCategories(String... categories) {
		CategoriesType type = new CategoriesType();
		for (String category : categories) {
			type.addValue(category);
		}
		setCategories(type);
		return type;
	}

	/**
	 * Gets information about the person's agent.
	 * <p>
	 * vCard property name: AGENT
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @return the agent information
	 */
	public AgentType getAgent() {
		return agent;
	}

	/**
	 * Sets information about the person's agent.
	 * <p>
	 * vCard property name: AGENT
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0
	 * </p>
	 * @param agent the agent information
	 */
	public void setAgent(AgentType agent) {
		this.agent = agent;
	}

	/**
	 * Gets the notes. Notes contain free-form, miscellaneous text.
	 * <p>
	 * vCard property name: NOTE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the notes
	 */
	public List<NoteType> getNotes() {
		return notes;
	}

	/**
	 * Adds a note. Notes contain free-form, miscellaneous text.
	 * <p>
	 * vCard property name: NOTE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param note the note to add
	 */
	public void addNote(NoteType note) {
		notes.add(note);
	}

	/**
	 * Adds a note. Notes contain free-form, miscellaneous text. This is a
	 * convenience method for {@link #addNote(NoteType)}.
	 * <p>
	 * vCard property name: NOTE
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
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
	 * Gets the unique identifier of the vCard.
	 * <p>
	 * vCard property name: UID
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the unique identifier
	 */
	public UidType getUid() {
		return uid;
	}

	/**
	 * Sets the unique identifier of the vCard.
	 * <p>
	 * vCard property name: UID
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param uid the unique identifier
	 */
	public void setUid(UidType uid) {
		this.uid = uid;
	}

	/**
	 * Gets the public encryption keys.
	 * <p>
	 * vCard property name: KEY
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @return the keys
	 */
	public List<KeyType> getKeys() {
		return keys;
	}

	/**
	 * Adds a public encryption key.
	 * <p>
	 * vCard property name: KEY
	 * </p>
	 * <p>
	 * vCard versions: 2.1, 3.0, 4.0
	 * </p>
	 * @param key the key to add
	 */
	public void addKey(KeyType key) {
		keys.add(key);
	}

	/**
	 * Gets the instant messaging handles.
	 * <p>
	 * vCard property name: IMPP
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @return the instant messaging handles
	 */
	public List<ImppType> getImpps() {
		return impps;
	}

	/**
	 * Adds an instant messaging handle.
	 * <p>
	 * vCard property name: IMPP
	 * </p>
	 * <p>
	 * vCard versions: 3.0, 4.0
	 * </p>
	 * @param impp the instant messaging handle to add
	 */
	public void addImpp(ImppType impp) {
		impps.add(impp);
	}

	/**
	 * Gets a list of people that the person is related to.
	 * <p>
	 * vCard property name: RELATED
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the person's relations
	 */
	public List<RelatedType> getRelations() {
		return relations;
	}

	/**
	 * Adds someone that the person is related to.
	 * <p>
	 * vCard property name: RELATED
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param related the relation to add
	 */
	public void addRelated(RelatedType related) {
		relations.add(related);
	}

	/**
	 * Gets the languages that the person speaks.
	 * <p>
	 * vCard property name: LANG
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the languages
	 */
	public List<LanguageType> getLanguages() {
		return languages;
	}

	/**
	 * Adds a language that the person speaks.
	 * <p>
	 * vCard property name: LANG
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param language the language to add
	 */
	public void addLanguage(LanguageType language) {
		languages.add(language);
	}

	/**
	 * Adds a language that the person speaks. This is a convenience method for
	 * {@link #addLanguage(LanguageType)}.
	 * <p>
	 * vCard property name: LANG
	 * </p>
	 * <p>
	 * vCard versions: 4.0
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
	 * Gets the URIs that can be used to schedule a meeting with the person on
	 * his or her calendar.
	 * <p>
	 * vCard property name: CALADRURI
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the calendar request URIs
	 */
	public List<CalendarRequestUriType> getCalendarRequestUris() {
		return calendarRequestUris;
	}

	/**
	 * Adds a URI that can be used to schedule a meeting with the person on his
	 * or her calendar.
	 * <p>
	 * vCard property name: CALADRURI
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param calendarRequestUri the calendar request URI to add
	 */
	public void addCalendarRequestUri(CalendarRequestUriType calendarRequestUri) {
		calendarRequestUris.add(calendarRequestUri);
	}

	/**
	 * Gets the URIs that point to the person's calendar.
	 * <p>
	 * vCard property name: CALURI
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the calendar URIs
	 */
	public List<CalendarUriType> getCalendarUris() {
		return calendarUris;
	}

	/**
	 * Adds a URI that points to the person's calendar.
	 * <p>
	 * vCard property name: CALURI
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param calendarUri the calendar URI to add
	 */
	public void addCalendarUri(CalendarUriType calendarUri) {
		calendarUris.add(calendarUri);
	}

	/**
	 * Gets the URLs that can be used to determine when the person is free
	 * and/or busy.
	 * <p>
	 * vCard property name: FBURL
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the free-busy URLs
	 */
	public List<FbUrlType> getFbUrls() {
		return fbUrls;
	}

	/**
	 * Adds a URL that can be used to determine when the person is free and/or
	 * busy.
	 * <p>
	 * vCard property name: FBURL
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param fbUrl the free-busy URL to add
	 */
	public void addFbUrl(FbUrlType fbUrl) {
		fbUrls.add(fbUrl);
	}

	/**
	 * Gets the properties that are used to assign globally-unique identifiers
	 * to individual property instances. CLIENTPIDMAPs are used for merging
	 * together different versions of the same vCard.
	 * <p>
	 * vCard property name: CLIENTPIDMAP
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the client PID maps
	 */
	public List<ClientPidMapType> getClientPidMaps() {
		return clientPidMaps;
	}

	/**
	 * Adds a property that is used to assign a globally-unique identifier to an
	 * individual property instance. CLIENTPIDMAPs are used for merging together
	 * different versions of the same vCard.
	 * <p>
	 * vCard property name: CLIENTPIDMAP
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param clientPidMap the client PID map to add
	 */
	public void addClientPidMap(ClientPidMapType clientPidMap) {
		clientPidMaps.add(clientPidMap);
	}

	/**
	 * Gets any XML data that is attached to the vCard. XML properties may be
	 * present if the vCard was encoded in XML and the XML document contained
	 * non-standard elements. The XML vCard properties in this case would
	 * contain all of the non-standard XML elements.
	 * <p>
	 * vCard property name: XML
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the XML data
	 */
	public List<XmlType> getXmls() {
		return xmls;
	}

	/**
	 * Adds XML data to the vCard. XML properties may be present if the vCard
	 * was encoded in XML and the XML document contained non-standard elements.
	 * The XML vCard properties in this case would contain all of the
	 * non-standard XML elements.
	 * <p>
	 * vCard property name: XML
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param xml the XML data to add
	 */
	public void addXml(XmlType xml) {
		xmls.add(xml);
	}

	/**
	 * Gets the professional subject areas of which the the person is
	 * knowledgeable.
	 * <p>
	 * vCard property name: EXPERTISE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the professional skills
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<ExpertiseType> getExpertise() {
		return expertises;
	}

	/**
	 * Adds a professional subject area of which the the person is
	 * knowledgeable.
	 * <p>
	 * vCard property name: EXPERTISE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param expertise the professional skill to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addExpertise(ExpertiseType expertise) {
		expertises.add(expertise);
	}

	/**
	 * Adds a professional subject area of which the the person is
	 * knowledgeable. This is a convenience method for
	 * {@link #addExpertise(ExpertiseType)}.
	 * <p>
	 * vCard property name: EXPERTISE
	 * </p>
	 * <p>
	 * vCard versions: 4.0
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
	 * Gets the hobbies that the person actively engages in.
	 * <p>
	 * vCard property name: HOBBY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the hobbies
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<HobbyType> getHobbies() {
		return hobbies;
	}

	/**
	 * Adds a hobby that the person actively engages in.
	 * <p>
	 * vCard property name: HOBBY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param hobby the hobby to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addHobby(HobbyType hobby) {
		hobbies.add(hobby);
	}

	/**
	 * Adds a hobby that the person actively engages in. This is a convenience
	 * method for {@link #addHobby(HobbyType)}.
	 * <p>
	 * vCard property name: HOBBY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
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
	 * Gets the person's interests.
	 * <p>
	 * vCard property name: INTEREST
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the interests
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<InterestType> getInterests() {
		return interests;
	}

	/**
	 * Adds an interest.
	 * <p>
	 * vCard property name: INTEREST
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param interest the interest to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addInterest(InterestType interest) {
		interests.add(interest);
	}

	/**
	 * Adds an interest. This is a convenience method for
	 * {@link #addInterest(InterestType)}.
	 * <p>
	 * vCard property name: INTEREST
	 * </p>
	 * <p>
	 * vCard versions: 4.0
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
	 * Gets the organization directories.
	 * <p>
	 * vCard property name: ORG-DIRECTORY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @return the organization directories
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public List<OrgDirectoryType> getOrgDirectories() {
		return orgDirectories;
	}

	/**
	 * Adds an organization directory.
	 * <p>
	 * vCard property name: ORG-DIRECTORY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
	 * </p>
	 * @param orgDirectory the organization directory to add
	 * @see <a href="http://tools.ietf.org/html/rfc6715">RFC 6715</a>
	 */
	public void addOrgDirectory(OrgDirectoryType orgDirectory) {
		orgDirectories.add(orgDirectory);
	}

	/**
	 * Adds an organization directory. This is a convenience method for
	 * {@link #addOrgDirectory(OrgDirectoryType)}.
	 * <p>
	 * vCard property name: ORG-DIRECTORY
	 * </p>
	 * <p>
	 * vCard versions: 4.0
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
	 * Adds an extended type to the vCard.
	 * @param type the extended type to add
	 */
	public void addExtendedType(VCardType type) {
		extendedTypes.put(type.getTypeName(), type);
	}

	/**
	 * Adds an extended type to the vCard.
	 * @param name the name of the extended type. It MUST begin with "X-" (for
	 * example, "X-GENDER").
	 * @param value the value of the extended type
	 * @return the extended type object that was created
	 */
	public RawType addExtendedType(String name, String value) {
		RawType type = new RawType(name, value);
		addExtendedType(type);
		return type;
	}

	/**
	 * Gets all extended types that have a particular name. Use this method to
	 * retrieve the extended types that have NOT been unmarshalled into a
	 * custom, extended type class.
	 * @param typeName the type name
	 * @return the extended types or empty list if none were found
	 */
	public List<RawType> getExtendedType(String typeName) {
		return getExtendedType(typeName, RawType.class);
	}

	/**
	 * Gets all extended types that have a particular name and that are an
	 * instance of a particular class.
	 * @param typeName the type name
	 * @param clazz the type class
	 * @return the extended types or empty list if none were found
	 */
	@SuppressWarnings("unchecked")
	public <T extends VCardType> List<T> getExtendedType(String typeName, Class<T> clazz) {
		List<VCardType> types = extendedTypes.get(typeName);
		List<T> list = new ArrayList<T>();
		for (VCardType type : types) {
			if (clazz.isInstance(type)) {
				T rt = (T) type;
				list.add(rt);
			}
		}
		return list;
	}

	/**
	 * Gets all extended types that are an instance of a particular class.
	 * @param clazz the type class
	 * @return the extended types or empty list of none were found
	 */
	@SuppressWarnings("unchecked")
	public <T extends VCardType> List<T> getExtendedType(Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		for (VCardType type : extendedTypes.values()) {
			if (clazz.isInstance(type)) {
				list.add((T) type);
			}
		}
		return list;
	}

	/**
	 * Gets all extended types.
	 * @return the extended types (key = the type name, value = the list of type
	 * objects that have that name)
	 */
	public Map<String, List<VCardType>> getExtendedTypes() {
		return extendedTypes.getMap();
	}

	/**
	 * Gets all of the vCard's properties. Does not include the "BEGIN", "END",
	 * or "VERSION" properties.
	 * @return the vCard properties
	 */
	public Collection<VCardType> getAllTypes() {
		Collection<VCardType> allTypes = new ArrayList<VCardType>();

		for (Field field : getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				Object value = field.get(this);
				if (value instanceof VCardType) {
					VCardType type = (VCardType) value;
					allTypes.add(type);
				} else if (value instanceof Collection) {
					Collection<?> collection = (Collection<?>) value;
					for (Object obj : collection) {
						if (obj instanceof VCardType) {
							VCardType type = (VCardType) obj;
							allTypes.add(type);
						}
					}
				}
			} catch (IllegalArgumentException e) {
				//shouldn't be thrown because we're passing the correct object into Field.get()
			} catch (IllegalAccessException e) {
				//shouldn't be thrown because we're calling Field.setAccessible(true)
			}
		}

		for (VCardType extendedType : extendedTypes.values()) {
			allTypes.add(extendedType);
		}

		return allTypes;
	}

	/**
	 * Iterates through each of the vCard's properties in no particular order.
	 * Does not include the "BEGIN", "END", or "VERSION" properties.
	 * @return the iterator
	 */
	public Iterator<VCardType> iterator() {
		return getAllTypes().iterator();
	}
}
