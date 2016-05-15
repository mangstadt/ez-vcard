package ezvcard.io.scribe;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import ezvcard.VCardVersion;
import ezvcard.property.RawProperty;
import ezvcard.property.VCardProperty;
import ezvcard.property.Xml;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 */

/**
 * <p>
 * Manages a collection of property scribes (aka "marshallers" or "serializers")
 * to use when reading or writing a vCard. The same instance of this object can
 * be re-used across multiple vCard reader/writer objects. This is useful if you
 * have custom scribe classes defined, as it allows you to only define them once
 * instead of each time a vCard reader/writer object is created.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //init the index
 * ScribeIndex index = new ScribeIndex();
 * index.register(new CustomPropertyScribe());
 * index.register(new AnotherCustomPropertyScribe());
 * 
 * //inject the ScribeIndex into a plain-text vCard reader class and read the vCard data stream
 * VCardReader vcardReader = new VCardReader(...);
 * vcardReader.setScribeIndex(index);
 * List&lt;VCard&gt; vcards = new ArrayList&lt;VCard&gt;();
 * VCard vcard;
 * while ((vcards = vcardReader.readNext()) != null){
 *   vcards.add(vcard);
 * }
 * vcardReader.close();
 * 
 * //inject the same ScribeIndex instance into a jCard writer and write the vCards
 * JCardWriter jcardWriter = new JCardWriter(...);
 * jcardWriter.setScribeIndex(index);
 * for (VCard vcard : vcards){
 *   jcardWriter.write(vcard);
 * }
 * jcardWriter.close();
 * </pre>
 * @author Michael Angstadt
 */
public class ScribeIndex {
	//define standard property scribes
	private static final Map<String, VCardPropertyScribe<? extends VCardProperty>> standardByName = new HashMap<String, VCardPropertyScribe<? extends VCardProperty>>();
	private static final Map<Class<? extends VCardProperty>, VCardPropertyScribe<? extends VCardProperty>> standardByClass = new HashMap<Class<? extends VCardProperty>, VCardPropertyScribe<? extends VCardProperty>>();
	private static final Map<QName, VCardPropertyScribe<? extends VCardProperty>> standardByQName = new HashMap<QName, VCardPropertyScribe<? extends VCardProperty>>();
	static {
		//2.1, RFC 2426, RFC 6350
		registerStandard(new AddressScribe());
		registerStandard(new AgentScribe());
		registerStandard(new AnniversaryScribe());
		registerStandard(new BirthdayScribe());
		registerStandard(new CalendarRequestUriScribe());
		registerStandard(new CalendarUriScribe());
		registerStandard(new CategoriesScribe());
		registerStandard(new ClassificationScribe());
		registerStandard(new ClientPidMapScribe());
		registerStandard(new EmailScribe());
		registerStandard(new FreeBusyUrlScribe());
		registerStandard(new FormattedNameScribe());
		registerStandard(new GenderScribe());
		registerStandard(new GeoScribe());
		registerStandard(new ImppScribe());
		registerStandard(new KeyScribe());
		registerStandard(new KindScribe());
		registerStandard(new LabelScribe());
		registerStandard(new LanguageScribe());
		registerStandard(new LogoScribe());
		registerStandard(new MailerScribe());
		registerStandard(new MemberScribe());
		registerStandard(new NicknameScribe());
		registerStandard(new NoteScribe());
		registerStandard(new OrganizationScribe());
		registerStandard(new PhotoScribe());
		registerStandard(new ProductIdScribe());
		registerStandard(new ProfileScribe());
		registerStandard(new RelatedScribe());
		registerStandard(new RevisionScribe());
		registerStandard(new RoleScribe());
		registerStandard(new SortStringScribe());
		registerStandard(new SoundScribe());
		registerStandard(new SourceDisplayTextScribe());
		registerStandard(new SourceScribe());
		registerStandard(new StructuredNameScribe());
		registerStandard(new TelephoneScribe());
		registerStandard(new TimezoneScribe());
		registerStandard(new TitleScribe());
		registerStandard(new UidScribe());
		registerStandard(new UrlScribe());

		//RFC 6351
		registerStandard(new XmlScribe());

		//RFC 6474
		registerStandard(new BirthplaceScribe());
		registerStandard(new DeathdateScribe());
		registerStandard(new DeathplaceScribe());

		//RFC 6715
		registerStandard(new ExpertiseScribe());
		registerStandard(new OrgDirectoryScribe());
		registerStandard(new InterestScribe());
		registerStandard(new HobbyScribe());
	}

	private final Map<String, VCardPropertyScribe<? extends VCardProperty>> extendedByName = new HashMap<String, VCardPropertyScribe<? extends VCardProperty>>(0);
	private final Map<Class<? extends VCardProperty>, VCardPropertyScribe<? extends VCardProperty>> extendedByClass = new HashMap<Class<? extends VCardProperty>, VCardPropertyScribe<? extends VCardProperty>>(0);
	private final Map<QName, VCardPropertyScribe<? extends VCardProperty>> extendedByQName = new HashMap<QName, VCardPropertyScribe<? extends VCardProperty>>(0);

	/**
	 * Gets a property scribe by name.
	 * @param propertyName the property name (case-insensitive, e.g. "FN")
	 * @return the property scribe or null if not found
	 */
	public VCardPropertyScribe<? extends VCardProperty> getPropertyScribe(String propertyName) {
		propertyName = propertyName.toUpperCase();

		VCardPropertyScribe<? extends VCardProperty> scribe = extendedByName.get(propertyName);
		if (scribe != null) {
			return scribe;
		}

		return standardByName.get(propertyName);
	}

	/**
	 * Determines if a scribe exists for a given property instance.
	 * @param property the property
	 * @return true if a scribe exists, false if not
	 */
	public boolean hasPropertyScribe(VCardProperty property) {
		if (property instanceof RawProperty) {
			return true;
		}

		return getPropertyScribe(property.getClass()) != null;
	}

	/**
	 * Gets a property scribe by class.
	 * @param clazz the property class
	 * @return the property scribe or null if not found
	 */
	public VCardPropertyScribe<? extends VCardProperty> getPropertyScribe(Class<? extends VCardProperty> clazz) {
		VCardPropertyScribe<? extends VCardProperty> scribe = extendedByClass.get(clazz);
		if (scribe != null) {
			return scribe;
		}

		return standardByClass.get(clazz);
	}

	/**
	 * Gets the appropriate property scribe for a given property instance.
	 * @param property the property instance
	 * @return the property scribe or null if not found
	 */
	public VCardPropertyScribe<? extends VCardProperty> getPropertyScribe(VCardProperty property) {
		if (property instanceof RawProperty) {
			RawProperty raw = (RawProperty) property;
			return new RawPropertyScribe(raw.getPropertyName());
		}

		return getPropertyScribe(property.getClass());
	}

	/**
	 * Gets a property scribe by XML local name and namespace.
	 * @param qname the XML local name and namespace
	 * @return the property scribe or a {@link XmlScribe} if not found
	 */
	public VCardPropertyScribe<? extends VCardProperty> getPropertyScribe(QName qname) {
		VCardPropertyScribe<? extends VCardProperty> scribe = extendedByQName.get(qname);
		if (scribe != null) {
			return scribe;
		}

		scribe = standardByQName.get(qname);
		if (scribe != null) {
			return scribe;
		}

		if (VCardVersion.V4_0.getXmlNamespace().equals(qname.getNamespaceURI())) {
			return new RawPropertyScribe(qname.getLocalPart().toUpperCase());
		}

		return getPropertyScribe(Xml.class);
	}

	/**
	 * Registers a property scribe.
	 * @param scribe the scribe to register
	 */
	public void register(VCardPropertyScribe<? extends VCardProperty> scribe) {
		extendedByName.put(scribe.getPropertyName().toUpperCase(), scribe);
		extendedByClass.put(scribe.getPropertyClass(), scribe);
		extendedByQName.put(scribe.getQName(), scribe);
	}

	/**
	 * Unregisters a property scribe.
	 * @param scribe the scribe to unregister
	 */
	public void unregister(VCardPropertyScribe<? extends VCardProperty> scribe) {
		extendedByName.remove(scribe.getPropertyName().toUpperCase());
		extendedByClass.remove(scribe.getPropertyClass());
		extendedByQName.remove(scribe.getQName());
	}

	private static void registerStandard(VCardPropertyScribe<? extends VCardProperty> scribe) {
		standardByName.put(scribe.getPropertyName().toUpperCase(), scribe);
		standardByClass.put(scribe.getPropertyClass(), scribe);
		standardByQName.put(scribe.getQName(), scribe);
	}
}
