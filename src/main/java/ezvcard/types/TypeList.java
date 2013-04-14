package ezvcard.types;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ezvcard.VCard;

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

	/**
	 * Maps each type class to the method that is used to add an instance of the
	 * type class to a {@link VCard} object. For example the {@link NoteType}
	 * class is mapped to the {@link VCard#addNote(NoteType)} method.
	 */
	private final static Map<Class<? extends VCardType>, Method> typeClassToAddMethod;

	static {
		/*
		 * This Map lists ALL of the type classes for the standard vCard types.
		 * 
		 * Key = The type class.
		 * 
		 * Value = The name of the method in the "VCard" class that's used for
		 * adding instances of these type classes to an instance of the "VCard"
		 * class. If null, it will guess the method name using the name of the
		 * type class
		 * 
		 * Requirements for each type class:
		 * 
		 * 1. MUST have a public, no-arg constructor
		 * 
		 * 2. MUST have a public, static "NAME" field
		 * 
		 * Requirements for each "VCard" class method:
		 * 
		 * 1. MUST be public
		 */
		Map<Class<? extends VCardType>, String> typeClasses = new HashMap<Class<? extends VCardType>, String>();
		typeClasses.put(AddressType.class, null);
		typeClasses.put(AgentType.class, null);
		typeClasses.put(AnniversaryType.class, null);
		typeClasses.put(BirthdayType.class, null);
		typeClasses.put(BirthplaceType.class, null);
		typeClasses.put(CalendarRequestUriType.class, null);
		typeClasses.put(CalendarUriType.class, null);
		typeClasses.put(CategoriesType.class, null);
		typeClasses.put(ClassificationType.class, null);
		typeClasses.put(ClientPidMapType.class, null);
		typeClasses.put(DeathdateType.class, null);
		typeClasses.put(DeathplaceType.class, null);
		typeClasses.put(EmailType.class, null);
		typeClasses.put(ExpertiseType.class, null);
		typeClasses.put(FbUrlType.class, null);
		typeClasses.put(FormattedNameType.class, null);
		typeClasses.put(GenderType.class, null);
		typeClasses.put(GeoType.class, null);
		typeClasses.put(HobbyType.class, null);
		typeClasses.put(ImppType.class, null);
		typeClasses.put(InterestType.class, null);
		typeClasses.put(KeyType.class, null);
		typeClasses.put(KindType.class, null);
		typeClasses.put(LabelType.class, "addOrphanedLabel");
		typeClasses.put(LanguageType.class, null);
		typeClasses.put(LogoType.class, null);
		typeClasses.put(MailerType.class, null);
		typeClasses.put(MemberType.class, null);
		typeClasses.put(NicknameType.class, null);
		typeClasses.put(NoteType.class, null);
		typeClasses.put(OrganizationType.class, null);
		typeClasses.put(OrgDirectoryType.class, null);
		typeClasses.put(PhotoType.class, null);
		typeClasses.put(ProdIdType.class, null);
		typeClasses.put(ProfileType.class, null);
		typeClasses.put(RelatedType.class, null);
		typeClasses.put(RevisionType.class, null);
		typeClasses.put(RoleType.class, null);
		typeClasses.put(SortStringType.class, null);
		typeClasses.put(SoundType.class, null);
		typeClasses.put(SourceDisplayTextType.class, null);
		typeClasses.put(SourceType.class, null);
		typeClasses.put(StructuredNameType.class, null);
		typeClasses.put(TelephoneType.class, "addTelephoneNumber");
		typeClasses.put(TimezoneType.class, null);
		typeClasses.put(TitleType.class, null);
		typeClasses.put(UidType.class, null);
		typeClasses.put(UrlType.class, null);
		typeClasses.put(XmlType.class, null);

		Map<String, Class<? extends VCardType>> _nameToTypeClass = new HashMap<String, Class<? extends VCardType>>();
		Map<Class<? extends VCardType>, Method> _typeClassToAddMethod = new HashMap<Class<? extends VCardType>, Method>();
		for (Map.Entry<Class<? extends VCardType>, String> entry : typeClasses.entrySet()) {
			Class<? extends VCardType> clazz = entry.getKey();
			String methodName = entry.getValue();
			try {
				String typeName = (String) clazz.getField("NAME").get(null);
				typeName = typeName.toUpperCase();

				Method method;
				if (methodName == null) {
					//get the suffix of the method name using the class name
					//e.g. "NoteType" --> "Note"
					String simpleName = clazz.getSimpleName();
					int pos = simpleName.lastIndexOf("Type");
					String methodNameSuffix = simpleName.substring(0, pos);

					//try "addNote" first
					methodName = "add" + methodNameSuffix;
					try {
						method = VCard.class.getMethod(methodName, clazz);
					} catch (Exception e) {
						//try "setNote"
						methodName = "set" + methodNameSuffix;
						method = VCard.class.getMethod(methodName, clazz);
					}
				} else {
					method = VCard.class.getMethod(methodName, clazz);
				}

				_nameToTypeClass.put(typeName, clazz);
				_typeClassToAddMethod.put(clazz, method);
			} catch (Exception e) {
				//a reflection problem occurred
				//this exception should NEVER be thrown if the above rules are followed
				throw new RuntimeException(e);
			}
		}

		nameToTypeClass = Collections.unmodifiableMap(_nameToTypeClass);
		typeClassToAddMethod = Collections.unmodifiableMap(_typeClassToAddMethod);
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

	/**
	 * Gets the method of the {@link VCard} class that's used to add a vCard
	 * type object to the {@link VCard}.
	 * @param typeClass the type class
	 * @return the method or null if not found
	 */
	public static Method getAddMethod(Class<? extends VCardType> typeClass) {
		return typeClassToAddMethod.get(typeClass);
	}
}
