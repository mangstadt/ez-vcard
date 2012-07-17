package ezvcard.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;

import ezvcard.VCard;
import ezvcard.VCardException;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.KeyTypeParameter;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.parameters.ValueParameter;
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
import ezvcard.types.NameType;
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
import ezvcard.util.VCardStringUtils;

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
 * Unmarshals vCards.
 * @author Michael Angstadt
 */
public class VCardReader implements Closeable {
	private CompatibilityMode compatibilityMode = CompatibilityMode.RFC2426;
	private List<String> warnings = new ArrayList<String>();
	private VCardVersion version;
	private boolean endFound = false;
	private Map<String, Class<? extends VCardType>> customTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	private FoldedLineReader reader;

	public VCardReader(Reader reader) {
		this.reader = new FoldedLineReader(reader);
	}

	public CompatibilityMode getCompatibilityMode() {
		return compatibilityMode;
	}

	public void setCompatibilityMode(CompatibilityMode compatibilityMode) {
		this.compatibilityMode = compatibilityMode;
	}

	/**
	 * Registers a custom type class. These types will be unmarshalled into
	 * instances of this class.
	 * @param clazz the custom type class. It MUST contain a public, no-arg
	 * constructor.
	 */
	public void registerCustomType(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			customTypeClasses.put(t.getTypeName().toUpperCase(), clazz);
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	/**
	 * Reads the next vCard.
	 * @return the next vCard or null if there are no more
	 * @throws VCardException if there's a problem parsing the vCard
	 * @throws IOException if there's a problem reading from the stream
	 */
	public VCard readNext() throws VCardException, IOException {
		warnings.clear();
		version = null;
		endFound = false;

		VCard vcard = new VCard();

		int typesRead = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			//ignore empty lines
			if (line.length() == 0) {
				continue;
			}

			String[] colonSplit = line.split(":", 2);
			if (colonSplit.length < 2) {
				warnings.add("Skipping malformed vCard line: \"" + line + "\"");
				continue;
			}

			String beforeColon = colonSplit[0];
			String value = VCardStringUtils.ltrim(colonSplit[1]); //remove the whitespace between the colon and the value
			String[] semicolonSplit = VCardStringUtils.splitBy(beforeColon, ';', true, false);

			//get the group name and the type name
			String[] dotSplit = semicolonSplit[0].split("\\.", 2);
			String typeName, groupName;
			if (dotSplit.length == 1) {
				groupName = null;
				typeName = dotSplit[0];
			} else {
				groupName = dotSplit[0];
				typeName = dotSplit[1];
			}
			typeName = typeName.toUpperCase();

			//set the sub types
			//TODO values can be surrounded by double quotes (see RFC 2426 p.29 -- "param-value = ptext / quoted-string")
			//TODO "A Semi-colon in a property parameter value must be escaped with a Backslash character" 2.1 p.6 
			VCardSubTypes subTypes = new VCardSubTypes();
			for (int i = 1; i < semicolonSplit.length; i++) {
				String[] equalsSplit = semicolonSplit[i].split("=", 2);

				//if there is no "=", it means the property doesn't have a name
				//make a guess at what the parameter name is
				//v3.0 requires all sub types to have names, but v2.1 does not
				if (equalsSplit.length == 1) {
					String subTypeValue = equalsSplit[0];

					String subTypeName;
					if (ValueParameter.valueOf(subTypeValue) != null) {
						subTypeName = ValueParameter.NAME;
					} else if (EncodingParameter.valueOf(subTypeValue) != null) {
						subTypeName = EncodingParameter.NAME;
					} else if (AddressType.NAME.equals(typeName)) {
						//for ADR, assume it's TYPE
						subTypeName = AddressTypeParameter.NAME;
					} else if (TelephoneType.NAME.equals(typeName)) {
						//for TEL, assume it's TYPE
						subTypeName = TelephoneTypeParameter.NAME;
					} else if (SoundType.NAME.equals(typeName)) {
						//for SOUND, assume it's TYPE
						subTypeName = SoundTypeParameter.NAME;
					} else if (PhotoType.NAME.equals(typeName)) {
						//for PHOTO, assume it's TYPE
						subTypeName = ImageTypeParameter.NAME;
					} else if (LogoType.NAME.equals(typeName)) {
						//for LOGO, assume it's TYPE
						subTypeName = ImageTypeParameter.NAME;
					} else if (KeyType.NAME.equals(typeName)) {
						//for KEY, assume it's TYPE
						subTypeName = KeyTypeParameter.NAME;
					} else {
						subTypeName = subTypeValue;
						warnings.add("Nameless sub type value \"" + subTypeValue + "\" could not be identified.");
					}

					subTypes.put(subTypeName, subTypeValue);
				} else {
					String subTypeName = equalsSplit[0].toUpperCase();
					String subTypeValue = equalsSplit[1].toLowerCase();

					//split the value up if it's a comma-delimited list (i.e. the "TYPE" sub type)
					String commaSplit[] = VCardStringUtils.splitBy(subTypeValue, ',', true, true);
					for (String s : commaSplit) {
						subTypes.put(subTypeName, s);
					}
				}
			}

			//if the value is encoded in "quoted-printable", decode it
			//"quoted-printable" encoding is only supported in v2.1
			if (subTypes.getEncoding() == EncodingParameter.QUOTED_PRINTABLE) {
				QuotedPrintableCodec codec = new QuotedPrintableCodec();
				String charset = subTypes.getCharset();
				try {
					value = (charset == null) ? codec.decode(value) : codec.decode(value, charset);
				} catch (DecoderException e) {
					warnings.add("The value of the " + typeName + " type was marked as \"quoted-printable\", but it could not be decoded.  Assuming that the value is plain text.");
				}
				subTypes.setEncoding(null); //remove encoding sub type
			}

			//vCard should start with "BEGIN"
			if (typesRead == 0 && !"BEGIN".equals(typeName)) {
				warnings.add("vCard does not start with \"BEGIN\".");
			}

			try {
				//create the Type object
				VCardType type = createAndAddToVCard(vcard, typeName, subTypes, value);

				if (type != null) {
					//set the group
					type.setGroup(groupName);

					//unmarshal the text string into the object
					List<String> warnings = new ArrayList<String>();
					type.unmarshalValue(subTypes, value, version, warnings, compatibilityMode);
					this.warnings.addAll(warnings);

					if (endFound) {
						return vcard;
					}
				}
			} catch (Throwable e) {
				if (e instanceof VCardException) {
					throw (VCardException) e;
				} else {
					throw new VCardException("Error unmarshalling type \"" + typeName + "\".", e);
				}
			} finally {
				typesRead++;
			}
		}

		if (typesRead == 0) {
			//end of stream reached
			return null;
		}

		if (!endFound) {
			warnings.add("vCard does not end with the END type.");
		}

		return vcard;
	}

	/**
	 * Creates the appropriate VCardType instance and adds it to the vCard. This
	 * method does not unmarshal the type, it just creates the type object and
	 * adds it to the VCard bean.
	 * @param vcard the vCard
	 * @param name the Type's name
	 * @param subTypes the Type's Sub Types
	 * @param value the Type's value
	 * @return the Type that was created or null if a Type object wasn't created
	 * @throws VCardException
	 */
	//Note: If this method has a compile error, make sure that you're returning the
	//      Type that you created in its "if" block
	private VCardType createAndAddToVCard(VCard vcard, String name, VCardSubTypes subTypes, String value) throws VCardException {
		if ("BEGIN".equals(name)) {
			if (!"vcard".equalsIgnoreCase(value)) {
				warnings.add("The value of the BEGIN type should be \"vcard\", but it is \"" + value + "\".");
			}
			return null;
		} else if ("END".equals(name)) {
			endFound = true;
			if (!"vcard".equalsIgnoreCase(value)) {
				warnings.add("The value of the END type should be \"vcard\", but it is \"" + value + "\".");
			}
			return null;
		} else if ("VERSION".equals(name)) {
			if (version == null) {
				version = VCardVersion.valueOfByStr(value);
				if (version == null) {
					warnings.add("Invalid VERSION: " + value);
				}
			} else {
				warnings.add("Additional VERSION type encountered: \"" + value + "\".  It will be ignored.");
			}
			vcard.setVersion(version);
			return null;
		} else if (ProfileType.NAME.equals(name)) {
			ProfileType t = new ProfileType();
			vcard.setProfile(t);
			return t;
		} else if (ClassType.NAME.equals(name)) {
			ClassType t = new ClassType();
			vcard.setClassType(t);
			return t;
		} else if (SourceType.NAME.equals(name)) {
			SourceType t = new SourceType();
			vcard.setSource(t);
			return t;
		} else if (NameType.NAME.equals(name)) {
			NameType t = new NameType();
			vcard.setName(t);
			return t;
		} else if (FormattedNameType.NAME.equals(name)) {
			FormattedNameType t = new FormattedNameType();
			vcard.setFormattedName(t);
			return t;
		} else if (StructuredNameType.NAME.equals(name)) {
			StructuredNameType t = new StructuredNameType();
			vcard.setStructuredName(t);
			return t;
		} else if (ProdIdType.NAME.equals(name)) {
			ProdIdType t = new ProdIdType();
			vcard.setProdId(t);
			return t;
		} else if (NicknameType.NAME.equals(name)) {
			NicknameType t = new NicknameType();
			vcard.setNicknames(t);
			return t;
		} else if (SortStringType.NAME.equals(name)) {
			SortStringType t = new SortStringType();
			vcard.setSortString(t);
			return t;
		} else if (TitleType.NAME.equals(name)) {
			TitleType t = new TitleType();
			vcard.setTitle(t);
			return t;
		} else if (RoleType.NAME.equals(name)) {
			RoleType t = new RoleType();
			vcard.setRole(t);
			return t;
		} else if (PhotoType.NAME.equals(name)) {
			PhotoType t = new PhotoType();
			vcard.getPhotos().add(t);
			return t;
		} else if (LogoType.NAME.equals(name)) {
			LogoType t = new LogoType();
			vcard.getLogos().add(t);
			return t;
		} else if (SoundType.NAME.equals(name)) {
			SoundType t = new SoundType();
			vcard.getSounds().add(t);
			return t;
		} else if (BirthdayType.NAME.equals(name)) {
			BirthdayType t = new BirthdayType();
			vcard.setBirthday(t);
			return t;
		} else if (RevisionType.NAME.equals(name)) {
			RevisionType t = new RevisionType();
			vcard.setRev(t);
			return t;
		} else if (ProdIdType.NAME.equals(name)) {
			ProdIdType t = new ProdIdType();
			vcard.setProdId(t);
			return t;
		} else if (AddressType.NAME.equals(name)) {
			AddressType t = new AddressType();
			vcard.getAddresses().add(t);
			return t;
		} else if (LabelType.NAME.equals(name)) {
			LabelType t = new LabelType();
			vcard.getLabels().add(t);
			return t;
		} else if (EmailType.NAME.equals(name)) {
			EmailType t = new EmailType();
			vcard.getEmails().add(t);
			return t;
		} else if (TelephoneType.NAME.equals(name)) {
			TelephoneType t = new TelephoneType();
			vcard.getPhoneNumbers().add(t);
			return t;
		} else if (MailerType.NAME.equals(name)) {
			MailerType t = new MailerType();
			vcard.setMailer(t);
			return t;
		} else if (UrlType.NAME.equals(name)) {
			UrlType t = new UrlType();
			vcard.getUrls().add(t);
			return t;
		} else if (TimezoneType.NAME.equals(name)) {
			TimezoneType t = new TimezoneType();
			vcard.setTimezone(t);
			return t;
		} else if (GeoType.NAME.equals(name)) {
			GeoType t = new GeoType();
			vcard.setGeo(t);
			return t;
		} else if (OrgType.NAME.equals(name)) {
			OrgType t = new OrgType();
			vcard.setOrganizations(t);
			return t;
		} else if (CategoriesType.NAME.equals(name)) {
			CategoriesType t = new CategoriesType();
			vcard.setCategories(t);
			return t;
		} else if (AgentType.NAME.equals(name)) {
			//TODO support v2.1 AGENT types
			AgentType t = new AgentType();
			vcard.setAgent(t);
			return t;
		} else if (NoteType.NAME.equals(name)) {
			NoteType t = new NoteType();
			vcard.getNotes().add(t);
			return t;
		} else if (UidType.NAME.equals(name)) {
			UidType t = new UidType();
			vcard.getUids().add(t);
			return t;
		} else if (KeyType.NAME.equals(name)) {
			KeyType t = new KeyType();
			vcard.getKeys().add(t);
			return t;
		} else if (ImppType.NAME.equals(name)) {
			ImppType t = new ImppType();
			vcard.getImpps().add(t);
			return t;
		} else {
			Class<? extends VCardType> customTypeClass = customTypeClasses.get(name);
			VCardType t = null;
			if (customTypeClass != null) {
				try {
					t = customTypeClass.newInstance();
					vcard.addCustomType(t);
				} catch (Exception e) {
					throw new VCardException("Custom type class \"" + customTypeClass.getName() + "\" must have a public, no-arg constructor.");
				}
			} else {
				t = new RawType(name); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
				vcard.addCustomType(t);

				if (!name.startsWith("X-")) {
					warnings.add("Non-standard type \"" + name + "\" found.  Treating it as a custom type.");
				}
			}
			return t;
		}
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
