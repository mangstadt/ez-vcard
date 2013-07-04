package ezvcard.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Contains listings of all the vCard type classes in this library.
 * @author Michael Angstadt
 */
public class TypeList {
	/**
	 * Maps each type name to its corresponding type class. For example, "NOTE"
	 * is mapped to {@link NoteType}".
	 */
	private final static Map<String, Class<? extends VCardType>> nameToTypeClass;

	static {
		/*
		 * This list contains all of the type classes for the standard vCard
		 * types.
		 * 
		 * Requirements for each type class:
		 * 
		 * 1. MUST have a public, no-arg constructor
		 * 
		 * 2. MUST have a public, static "NAME" field
		 */
		List<Class<? extends VCardType>> typeClasses = new ArrayList<Class<? extends VCardType>>();
		typeClasses.add(AddressType.class);
		typeClasses.add(AgentType.class);
		typeClasses.add(AnniversaryType.class);
		typeClasses.add(BirthdayType.class);
		typeClasses.add(BirthplaceType.class);
		typeClasses.add(CalendarRequestUriType.class);
		typeClasses.add(CalendarUriType.class);
		typeClasses.add(CategoriesType.class);
		typeClasses.add(ClassificationType.class);
		typeClasses.add(ClientPidMapType.class);
		typeClasses.add(DeathdateType.class);
		typeClasses.add(DeathplaceType.class);
		typeClasses.add(EmailType.class);
		typeClasses.add(ExpertiseType.class);
		typeClasses.add(FbUrlType.class);
		typeClasses.add(FormattedNameType.class);
		typeClasses.add(GenderType.class);
		typeClasses.add(GeoType.class);
		typeClasses.add(HobbyType.class);
		typeClasses.add(ImppType.class);
		typeClasses.add(InterestType.class);
		typeClasses.add(KeyType.class);
		typeClasses.add(KindType.class);
		typeClasses.add(LabelType.class);
		typeClasses.add(LanguageType.class);
		typeClasses.add(LogoType.class);
		typeClasses.add(MailerType.class);
		typeClasses.add(MemberType.class);
		typeClasses.add(NicknameType.class);
		typeClasses.add(NoteType.class);
		typeClasses.add(OrganizationType.class);
		typeClasses.add(OrgDirectoryType.class);
		typeClasses.add(PhotoType.class);
		typeClasses.add(ProdIdType.class);
		typeClasses.add(ProfileType.class);
		typeClasses.add(RelatedType.class);
		typeClasses.add(RevisionType.class);
		typeClasses.add(RoleType.class);
		typeClasses.add(SortStringType.class);
		typeClasses.add(SoundType.class);
		typeClasses.add(SourceDisplayTextType.class);
		typeClasses.add(SourceType.class);
		typeClasses.add(StructuredNameType.class);
		typeClasses.add(TelephoneType.class);
		typeClasses.add(TimezoneType.class);
		typeClasses.add(TitleType.class);
		typeClasses.add(UidType.class);
		typeClasses.add(UrlType.class);
		typeClasses.add(XmlType.class);

		Map<String, Class<? extends VCardType>> _nameToTypeClass = new HashMap<String, Class<? extends VCardType>>();
		for (Class<? extends VCardType> clazz : typeClasses) {
			try {
				String typeName = (String) clazz.getField("NAME").get(null);
				typeName = typeName.toUpperCase();
				_nameToTypeClass.put(typeName, clazz);
			} catch (Exception e) {
				//a reflection problem occurred
				//this exception should NEVER be thrown if the above rules are followed
				throw new RuntimeException(e);
			}
		}

		nameToTypeClass = Collections.unmodifiableMap(_nameToTypeClass);
	}

	/**
	 * Gets the class that represents a vCard type.
	 * @param typeName the name of the vCard type (e.g. "ADR")
	 * @return the type class or null if not found
	 */
	public static Class<? extends VCardType> getTypeClass(String typeName) {
		typeName = typeName.toUpperCase();
		return nameToTypeClass.get(typeName);
	}

	/**
	 * Gets the class that represents a vCard type using its hCard type name.
	 * @param typeName the hCard name of the vCard type (e.g. "adr")
	 * @return the type class or null if not found
	 */
	public static Class<? extends VCardType> getTypeClassByHCardTypeName(String typeName) {
		if ("category".equalsIgnoreCase(typeName)) {
			return CategoriesType.class;
		}
		return getTypeClass(typeName);
	}

	private TypeList() {
		//hide
	}
}
