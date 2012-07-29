package ezvcard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import ezvcard.io.VCardReader;
import ezvcard.io.VCardWriter;
import ezvcard.types.AddressType;
import ezvcard.types.AgentType;
import ezvcard.types.BirthdayType;
import ezvcard.types.CalendarRequestUriType;
import ezvcard.types.CalendarUriType;
import ezvcard.types.CategoriesType;
import ezvcard.types.ClassificationType;
import ezvcard.types.DisplayableNameType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GeoType;
import ezvcard.types.ImppType;
import ezvcard.types.KeyType;
import ezvcard.types.KindType;
import ezvcard.types.LabelType;
import ezvcard.types.LogoType;
import ezvcard.types.MailerType;
import ezvcard.types.MemberType;
import ezvcard.types.NicknameType;
import ezvcard.types.NoteType;
import ezvcard.types.OrgType;
import ezvcard.types.PhotoType;
import ezvcard.types.ProdIdType;
import ezvcard.types.ProfileType;
import ezvcard.types.RawType;
import ezvcard.types.RelatedType;
import ezvcard.types.RevisionType;
import ezvcard.types.RoleType;
import ezvcard.types.SortStringType;
import ezvcard.types.SoundType;
import ezvcard.types.SourceType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import ezvcard.types.TimezoneType;
import ezvcard.types.TitleType;
import ezvcard.types.UidType;
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
 * Represents the data in a vCard.
 * @author Michael Angstadt
 */
public class VCard {
	private VCardVersion version = VCardVersion.V3_0;
	private KindType kind;
	private List<MemberType> members = new ArrayList<MemberType>();
	private ProfileType profile;
	private ClassificationType classification;
	private List<SourceType> sources = new ArrayList<SourceType>();
	private DisplayableNameType displayableName;
	private FormattedNameType formattedName;
	private StructuredNameType structuredName;
	private NicknameType nicknames;
	private SortStringType sortString;
	private TitleType title;
	private RoleType role;
	private List<PhotoType> photos = new ArrayList<PhotoType>();
	private List<LogoType> logos = new ArrayList<LogoType>();
	private List<SoundType> sounds = new ArrayList<SoundType>();
	private BirthdayType birthday;
	private RevisionType rev;
	private ProdIdType prodId;
	private List<AddressType> addresses = new ArrayList<AddressType>();
	private List<LabelType> labels = new ArrayList<LabelType>();
	private List<EmailType> emails = new ArrayList<EmailType>();
	private List<TelephoneType> telephoneNumbers = new ArrayList<TelephoneType>();
	private MailerType mailer;
	private List<UrlType> urls = new ArrayList<UrlType>();
	private TimezoneType timezone;
	private GeoType geo;
	private OrgType organizations;
	private CategoriesType categories;
	private AgentType agent;
	private List<NoteType> notes = new ArrayList<NoteType>();
	private List<UidType> uids = new ArrayList<UidType>();
	private List<KeyType> keys = new ArrayList<KeyType>();
	private List<ImppType> impps = new ArrayList<ImppType>();
	private List<RelatedType> relations = new ArrayList<RelatedType>();
	private List<CalendarRequestUriType> calendarRequestUris = new ArrayList<CalendarRequestUriType>();
	private List<CalendarUriType> calendarUris = new ArrayList<CalendarUriType>();
	private ListMultimap<String, VCardType> extendedTypes = ArrayListMultimap.create();

	/**
	 * Parses a vCard string. Use the {@link VCardReader} class for more control
	 * over how the vCard is parsed.
	 * @param str the vCard
	 * @return the parsed vCard
	 * @throws VCardException if there's a problem parsing the vCard
	 */
	public static VCard parse(String str) throws VCardException {
		try {
			VCardReader vcr = new VCardReader(new StringReader(str));
			return vcr.readNext();
		} catch (IOException e) {
			//never thrown because we're reading from string
			return null;
		}
	}

	/**
	 * Parses a vCard file. Use the {@link VCardReader} class for more control
	 * over how the vCard is parsed.
	 * @param file the vCard
	 * @return the parsed vCard
	 * @throws VCardException if there's a problem parsing the vCard
	 * @throws IOException if there's a problem reading the file
	 */
	public static VCard parse(File file) throws VCardException, IOException {
		VCardReader vcr = null;
		try {
			vcr = new VCardReader(new FileReader(file));
			return vcr.readNext();
		} finally {
			IOUtils.closeQuietly(vcr);
		}
	}

	/**
	 * Writes this vCard to a string. Use the {@link VCardWriter} class for more
	 * control over how the vCard is written.
	 * @throws VCardException if there's a problem writing the vCard
	 */
	public String write() throws VCardException {
		StringWriter sw = new StringWriter();
		try {
			VCardWriter writer = new VCardWriter(sw, (version == null) ? VCardVersion.V3_0 : version);
			writer.write(this);
		} catch (IOException e) {
			//never thrown because we're writing to string
		}
		return sw.toString();
	}

	/**
	 * Writes this vCard to a file. Use the {@link VCardWriter} class for more
	 * control over how the vCard is written.
	 * @param file the file to write to
	 * @throws VCardException if there's a problem writing the vCard
	 * @throws IOException if there's a problem writing to the file
	 */
	public void write(File file) throws VCardException, IOException {
		VCardWriter vcw = null;
		try {
			vcw = new VCardWriter(new FileWriter(file), (version == null) ? VCardVersion.V3_0 : version);
			vcw.write(this);
		} finally {
			IOUtils.closeQuietly(vcw);
		}
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
	 * {@link VCardWriter} class, please use the
	 * {@link VCardWriter#setTargetVersion setTargetVersion} method to define
	 * what version the vCard should be marshalled as. {@link VCardWriter}
	 * <b>does not</b> look at the version that is set on the VCard object.
	 * @param version the vCard version
	 */
	public void setVersion(VCardVersion version) {
		this.version = version;
	}

	public KindType getKind() {
		return kind;
	}

	public List<MemberType> getMembers() {
		return members;
	}

	public void addMember(MemberType member) {
		this.members.add(member);
	}

	public void setKind(KindType kind) {
		this.kind = kind;
	}

	public ProfileType getProfile() {
		return profile;
	}

	public void setProfile(ProfileType profile) {
		this.profile = profile;
	}

	public ClassificationType getClassification() {
		return classification;
	}

	public void setClassification(ClassificationType classification) {
		this.classification = classification;
	}

	public List<SourceType> getSources() {
		return sources;
	}

	public void addSource(SourceType source) {
		this.sources.add(source);
	}

	public DisplayableNameType getDisplayableName() {
		return displayableName;
	}

	public void setDisplayableName(DisplayableNameType displayableName) {
		this.displayableName = displayableName;
	}

	public FormattedNameType getFormattedName() {
		return formattedName;
	}

	public void setFormattedName(FormattedNameType formattedName) {
		this.formattedName = formattedName;
	}

	public StructuredNameType getStructuredName() {
		return structuredName;
	}

	public void setStructuredName(StructuredNameType structuredName) {
		this.structuredName = structuredName;
	}

	public NicknameType getNicknames() {
		return nicknames;
	}

	public void setNicknames(NicknameType nicknames) {
		this.nicknames = nicknames;
	}

	public SortStringType getSortString() {
		return sortString;
	}

	public void setSortString(SortStringType sortString) {
		this.sortString = sortString;
	}

	public TitleType getTitle() {
		return title;
	}

	public void setTitle(TitleType title) {
		this.title = title;
	}

	public RoleType getRole() {
		return role;
	}

	public void setRole(RoleType role) {
		this.role = role;
	}

	public List<PhotoType> getPhotos() {
		return photos;
	}

	public void addPhoto(PhotoType photo) {
		this.photos.add(photo);
	}

	public List<LogoType> getLogos() {
		return logos;
	}

	public void addLogo(LogoType logo) {
		this.logos.add(logo);
	}

	public List<SoundType> getSounds() {
		return sounds;
	}

	public void addSound(SoundType sound) {
		this.sounds.add(sound);
	}

	public BirthdayType getBirthday() {
		return birthday;
	}

	public void setBirthday(BirthdayType birthday) {
		this.birthday = birthday;
	}

	public RevisionType getRevision() {
		return rev;
	}

	public void setRevision(RevisionType rev) {
		this.rev = rev;
	}

	public ProdIdType getProdId() {
		return prodId;
	}

	public void setProdId(ProdIdType prodId) {
		this.prodId = prodId;
	}

	public List<AddressType> getAddresses() {
		return addresses;
	}

	public void addAddress(AddressType address) {
		this.addresses.add(address);
	}

	/**
	 * Gets all LABELs that could not be assigned to an ADR.
	 * @return the orphaned labels
	 */
	public List<LabelType> getOrphanedLabels() {
		return labels;
	}

	/**
	 * Adds a LABEL to the vCard which is not associated with any ADR. Use of
	 * this method is discouraged. To add a LABEL to an ADR, use the
	 * {@link AddressType#setLabel()} method.
	 * @param label
	 */
	public void addOrphanedLabel(LabelType label) {
		this.labels.add(label);
	}

	public List<EmailType> getEmails() {
		return emails;
	}

	public void addEmail(EmailType email) {
		this.emails.add(email);
	}

	public List<TelephoneType> getTelephoneNumbers() {
		return telephoneNumbers;
	}

	public void addTelephoneNumber(TelephoneType telephoneNumber) {
		this.telephoneNumbers.add(telephoneNumber);
	}

	public MailerType getMailer() {
		return mailer;
	}

	public void setMailer(MailerType mailer) {
		this.mailer = mailer;
	}

	public List<UrlType> getUrls() {
		return urls;
	}

	public void addUrl(UrlType url) {
		this.urls.add(url);
	}

	public TimezoneType getTimezone() {
		return timezone;
	}

	public void setTimezone(TimezoneType timezone) {
		this.timezone = timezone;
	}

	public GeoType getGeo() {
		return geo;
	}

	public void setGeo(GeoType geo) {
		this.geo = geo;
	}

	public OrgType getOrganizations() {
		return organizations;
	}

	public void setOrganizations(OrgType organizations) {
		this.organizations = organizations;
	}

	public CategoriesType getCategories() {
		return categories;
	}

	public void setCategories(CategoriesType categories) {
		this.categories = categories;
	}

	public AgentType getAgent() {
		return agent;
	}

	public void setAgent(AgentType agent) {
		this.agent = agent;
	}

	public List<NoteType> getNotes() {
		return notes;
	}

	public void addNote(NoteType note) {
		this.notes.add(note);
	}

	public List<UidType> getUids() {
		return uids;
	}

	public void addUid(UidType uid) {
		this.uids.add(uid);
	}

	public List<KeyType> getKeys() {
		return keys;
	}

	public void addKey(KeyType key) {
		this.keys.add(key);
	}

	public List<ImppType> getImpps() {
		return impps;
	}

	public void addImpp(ImppType impp) {
		this.impps.add(impp);
	}

	public List<RelatedType> getRelations() {
		return relations;
	}

	public void addRelation(RelatedType relation) {
		this.relations.add(relation);
	}

	public List<CalendarRequestUriType> getCalendarRequestUris() {
		return calendarRequestUris;
	}

	public void addCalendarRequestUris(CalendarRequestUriType calendarRequestUri) {
		this.calendarRequestUris.add(calendarRequestUri);
	}

	public List<CalendarUriType> getCalendarUris() {
		return calendarUris;
	}

	public void addCalendarUris(CalendarUriType calendarUri) {
		this.calendarUris.add(calendarUri);
	}

	/**
	 * Adds an extended type to the vCard.
	 * @param type the extended type to add
	 */
	public void addExtendedType(VCardType type) {
		extendedTypes.put(type.getTypeName(), type);
	}

	/**
	 * Gets all extended types that have the given name.
	 * @param typeName the type name
	 * @return the extended types or empty list if none were found
	 */
	public List<RawType> getExtendedType(String typeName) {
		List<VCardType> types = extendedTypes.get(typeName);
		List<RawType> list = new ArrayList<RawType>(types.size());
		for (VCardType type : types) {
			if (type instanceof RawType) {
				RawType rt = (RawType) type;
				list.add(rt);
			}
		}
		return list;
	}

	/**
	 * Gets all extended types that are wrapped in an extended type class.
	 * @param clazz the class that the extended type values are wrapped in
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
	 * Gets all of the extended types.
	 * @return all of the extended types
	 */
	public ListMultimap<String, VCardType> getExtendedTypes() {
		return extendedTypes;
	}
}
