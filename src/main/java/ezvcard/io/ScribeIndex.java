package ezvcard.io;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import ezvcard.VCardVersion;
import ezvcard.io.scribe.AddressScribe;
import ezvcard.io.scribe.AgentScribe;
import ezvcard.io.scribe.AnniversaryScribe;
import ezvcard.io.scribe.BirthdayScribe;
import ezvcard.io.scribe.BirthplaceScribe;
import ezvcard.io.scribe.CalendarRequestUriScribe;
import ezvcard.io.scribe.CalendarUriScribe;
import ezvcard.io.scribe.CategoriesScribe;
import ezvcard.io.scribe.ClassificationScribe;
import ezvcard.io.scribe.ClientPidMapScribe;
import ezvcard.io.scribe.DeathdateScribe;
import ezvcard.io.scribe.DeathplaceScribe;
import ezvcard.io.scribe.EmailScribe;
import ezvcard.io.scribe.ExpertiseScribe;
import ezvcard.io.scribe.FormattedNameScribe;
import ezvcard.io.scribe.FreeBusyUrlScribe;
import ezvcard.io.scribe.GenderScribe;
import ezvcard.io.scribe.GeoScribe;
import ezvcard.io.scribe.HobbyScribe;
import ezvcard.io.scribe.ImppScribe;
import ezvcard.io.scribe.InterestScribe;
import ezvcard.io.scribe.KeyScribe;
import ezvcard.io.scribe.KindScribe;
import ezvcard.io.scribe.LabelScribe;
import ezvcard.io.scribe.LanguageScribe;
import ezvcard.io.scribe.LogoScribe;
import ezvcard.io.scribe.MailerScribe;
import ezvcard.io.scribe.MemberScribe;
import ezvcard.io.scribe.NicknameScribe;
import ezvcard.io.scribe.NoteScribe;
import ezvcard.io.scribe.OrgDirectoryScribe;
import ezvcard.io.scribe.OrganizationScribe;
import ezvcard.io.scribe.PhotoScribe;
import ezvcard.io.scribe.ProductIdScribe;
import ezvcard.io.scribe.ProfileScribe;
import ezvcard.io.scribe.RawPropertyScribe;
import ezvcard.io.scribe.RelatedScribe;
import ezvcard.io.scribe.RevisionScribe;
import ezvcard.io.scribe.RoleScribe;
import ezvcard.io.scribe.SortStringScribe;
import ezvcard.io.scribe.SoundScribe;
import ezvcard.io.scribe.SourceDisplayTextScribe;
import ezvcard.io.scribe.SourceScribe;
import ezvcard.io.scribe.StructuredNameScribe;
import ezvcard.io.scribe.TelephoneScribe;
import ezvcard.io.scribe.TimezoneScribe;
import ezvcard.io.scribe.TitleScribe;
import ezvcard.io.scribe.UidScribe;
import ezvcard.io.scribe.UrlScribe;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.io.scribe.XmlScribe;
import ezvcard.types.RawType;
import ezvcard.types.VCardType;
import ezvcard.types.XmlType;

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
 */

/**
 * <p>
 * Manages a listing of property scribes (marshallers). This is useful for
 * injecting the scribes of any extended properties you have defined into a
 * reader or writer object. The same object instance can be reused and injected
 * into multiple reader/writer classes.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * //init the index
 * ScribeIndex index = new ScribeIndex();
 * index.register(new CustomPropertyScribe());
 * index.register(new AnotherCustomPropertyScribe());
 * 
 * //inject into a reader class
 * VCardReader vcardReader = new VCardReader(...);
 * vcardReader.setScribeIndex(index);
 * List&lt;VCard&gt; vcards = new ArrayList&lt;VCard&gt;();
 * VCard vcards;
 * while ((vcards = vcardReader.readNext()) != null){
 *   vcards.add(vcard);
 * }
 * 
 * //inject the same instance in another reader/writer class
 * JCardWriter jcardWriter = new JCardWriter(...);
 * jcardWriter.setScribeIndex(index);
 * for (VCard vcard : vcards){
 *   jcardWriter.write(vcard);
 * }
 * jcardWriter.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public class ScribeIndex {
	//define standard property marshallers
	private static final Map<String, VCardPropertyScribe<? extends VCardType>> standardByName = new HashMap<String, VCardPropertyScribe<? extends VCardType>>();
	private static final Map<Class<? extends VCardType>, VCardPropertyScribe<? extends VCardType>> standardByClass = new HashMap<Class<? extends VCardType>, VCardPropertyScribe<? extends VCardType>>();
	private static final Map<QName, VCardPropertyScribe<? extends VCardType>> standardByQName = new HashMap<QName, VCardPropertyScribe<? extends VCardType>>();
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

	private final Map<String, VCardPropertyScribe<? extends VCardType>> extendedByName = new HashMap<String, VCardPropertyScribe<? extends VCardType>>(0);
	private final Map<Class<? extends VCardType>, VCardPropertyScribe<? extends VCardType>> extendedByClass = new HashMap<Class<? extends VCardType>, VCardPropertyScribe<? extends VCardType>>(0);
	private final Map<QName, VCardPropertyScribe<? extends VCardType>> extendedByQName = new HashMap<QName, VCardPropertyScribe<? extends VCardType>>(0);

	/**
	 * Gets a property scribe by name.
	 * @param propertyName the property name (case-insensitive, e.g. "FN")
	 * @return the property scribe or null if not found
	 */
	public VCardPropertyScribe<? extends VCardType> getPropertyScribe(String propertyName) {
		propertyName = propertyName.toUpperCase();

		VCardPropertyScribe<? extends VCardType> marshaller = extendedByName.get(propertyName);
		if (marshaller != null) {
			return marshaller;
		}

		return standardByName.get(propertyName);
	}

	/**
	 * Gets a property scribe by class.
	 * @param clazz the property class
	 * @return the property scribe or null if not found
	 */
	public VCardPropertyScribe<? extends VCardType> getPropertyScribe(Class<? extends VCardType> clazz) {
		VCardPropertyScribe<? extends VCardType> marshaller = extendedByClass.get(clazz);
		if (marshaller != null) {
			return marshaller;
		}

		return standardByClass.get(clazz);
	}

	/**
	 * Gets the appropriate property scribe for a given property instance.
	 * @param property the property instance
	 * @return the property scribe or null if not found
	 */
	public VCardPropertyScribe<? extends VCardType> getPropertyScribe(VCardType property) {
		if (property instanceof RawType) {
			RawType raw = (RawType) property;
			return new RawPropertyScribe(raw.getPropertyName());
		}

		return getPropertyScribe(property.getClass());
	}

	/**
	 * Gets a property scribe by XML local name and namespace.
	 * @param qname the XML local name and namespace
	 * @return the property scribe or a {@link XmlScribe} if not found
	 */
	public VCardPropertyScribe<? extends VCardType> getPropertyScribe(QName qname) {
		VCardPropertyScribe<? extends VCardType> marshaller = extendedByQName.get(qname);
		if (marshaller != null) {
			return marshaller;
		}

		marshaller = standardByQName.get(qname);
		if (marshaller != null) {
			return marshaller;
		}

		if (VCardVersion.V4_0.getXmlNamespace().equals(qname.getNamespaceURI())) {
			return new RawPropertyScribe(qname.getLocalPart().toUpperCase());
		}

		return getPropertyScribe(XmlType.class);
	}

	/**
	 * Registers a property scribe.
	 * @param scribe the scribe to register
	 */
	public void register(VCardPropertyScribe<? extends VCardType> scribe) {
		extendedByName.put(scribe.getPropertyName().toUpperCase(), scribe);
		extendedByClass.put(scribe.getPropertyClass(), scribe);
		extendedByQName.put(scribe.getQName(), scribe);
	}

	/**
	 * Unregisters a property scribe.
	 * @param scribe the scribe to unregister
	 */
	public void unregister(VCardPropertyScribe<? extends VCardType> scribe) {
		extendedByName.remove(scribe.getPropertyName().toUpperCase());
		extendedByClass.remove(scribe.getPropertyClass());
		extendedByQName.remove(scribe.getQName());
	}

	private static void registerStandard(VCardPropertyScribe<? extends VCardType> scribe) {
		standardByName.put(scribe.getPropertyName().toUpperCase(), scribe);
		standardByClass.put(scribe.getPropertyClass(), scribe);
		standardByQName.put(scribe.getQName(), scribe);
	}
}
