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
import ezvcard.types.CategoriesType;
import ezvcard.types.ClassType;
import ezvcard.types.EmailType;
import ezvcard.types.FormattedNameType;
import ezvcard.types.GeoType;
import ezvcard.types.ImppType;
import ezvcard.types.KeyType;
import ezvcard.types.LabelType;
import ezvcard.types.LogoType;
import ezvcard.types.MailerType;
import ezvcard.types.DisplayableNameType;
import ezvcard.types.NicknameType;
import ezvcard.types.NoteType;
import ezvcard.types.OrgType;
import ezvcard.types.PhotoType;
import ezvcard.types.ProdIdType;
import ezvcard.types.ProfileType;
import ezvcard.types.RawType;
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
	private ProfileType profile;
	private ClassType classType;
	private SourceType source;
	private DisplayableNameType name;
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
	private ListMultimap<String, VCardType> customTypes = ArrayListMultimap.create();

	public static VCard parse(String str) throws VCardException {
		try {
			VCardReader vcr = new VCardReader(new StringReader(str));
			return vcr.readNext();
		} catch (IOException e) {
			//reading from string
			return null;
		}
	}

	public static VCard parse(File file) throws VCardException, IOException {
		VCardReader vcr = null;
		try {
			vcr = new VCardReader(new FileReader(file));
			return vcr.readNext();
		} finally {
			IOUtils.closeQuietly(vcr);
		}
	}

	public String write() throws VCardException {
		StringWriter sw = new StringWriter();
		try {
			VCardWriter writer = new VCardWriter(sw, (version == null) ? VCardVersion.V3_0 : version);
			writer.write(this);
		} catch (IOException e) {
			//writing to string
		}
		return sw.toString();
	}

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
	 * Gets the version attached to this vCard. If this VCard object was
	 * unmarshalled from a data stream, then this method gets the vCard version
	 * that it was parsed from.
	 * @return the vCard version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of this vCard. When marshalling a vCard, please use the
	 * {@link VCardWriter#setTargetVersion} method to define what version the
	 * vCard should be marshalled as. The marshalling process <b>does not</b>
	 * look at the version that is set on the VCard object.
	 * @param version the vCard version
	 */
	public void setVersion(VCardVersion version) {
		this.version = version;
	}

	public ProfileType getProfile() {
		return profile;
	}

	public void setProfile(ProfileType profile) {
		this.profile = profile;
	}

	public ClassType getClassType() {
		return classType;
	}

	public void setClassType(ClassType classType) {
		this.classType = classType;
	}

	public SourceType getSource() {
		return source;
	}

	public void setSource(SourceType source) {
		this.source = source;
	}

	public DisplayableNameType getName() {
		return name;
	}

	public void setName(DisplayableNameType name) {
		this.name = name;
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

	public void setPhotos(List<PhotoType> photos) {
		this.photos = photos;
	}

	public List<LogoType> getLogos() {
		return logos;
	}

	public void setLogos(List<LogoType> logos) {
		this.logos = logos;
	}

	public List<SoundType> getSounds() {
		return sounds;
	}

	public void setSounds(List<SoundType> sounds) {
		this.sounds = sounds;
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

	public void setAddresses(List<AddressType> addresses) {
		this.addresses = addresses;
	}

	public List<LabelType> getLabels() {
		return labels;
	}

	public void setLabels(List<LabelType> labels) {
		this.labels = labels;
	}

	public List<EmailType> getEmails() {
		return emails;
	}

	public void setEmails(List<EmailType> emails) {
		this.emails = emails;
	}

	public List<TelephoneType> getTelephoneNumbers() {
		return telephoneNumbers;
	}

	public void setTelephoneNumbers(List<TelephoneType> telephoneNumbers) {
		this.telephoneNumbers = telephoneNumbers;
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

	public void setUrls(List<UrlType> urls) {
		this.urls = urls;
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

	public void setNotes(List<NoteType> notes) {
		this.notes = notes;
	}

	public List<UidType> getUids() {
		return uids;
	}

	public void setUids(List<UidType> uids) {
		this.uids = uids;
	}

	public List<KeyType> getKeys() {
		return keys;
	}

	public void setKeys(List<KeyType> keys) {
		this.keys = keys;
	}

	public List<ImppType> getImpps() {
		return impps;
	}

	public void setImpps(List<ImppType> impps) {
		this.impps = impps;
	}

	/**
	 * Adds a custom type to the vCard.
	 * @param type the custom type to add
	 */
	public void addCustomType(VCardType type) {
		customTypes.put(type.getTypeName(), type);
	}

	/**
	 * Gets all custom types that have the given name.
	 * @param typeName the type name
	 * @return the custom types or empty list if none were found
	 */
	public List<RawType> getCustomType(String typeName) {
		List<VCardType> types = customTypes.get(typeName);
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
	 * Gets all custom types that are wrapped in a custom type class.
	 * @param clazz the class that the custom type values are wrapped in
	 * @return the custom types or empty list of none were found
	 */
	@SuppressWarnings("unchecked")
	public <T extends VCardType> List<T> getCustomType(Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		for (VCardType type : customTypes.values()) {
			if (clazz.isInstance(type)) {
				list.add((T) type);
			}
		}
		return list;
	}

	/**
	 * Gets all of the custom types.
	 * @return all of the custom types
	 */
	public ListMultimap<String, VCardType> getCustomTypes() {
		return customTypes;
	}
}
