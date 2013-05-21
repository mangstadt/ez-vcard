package ezvcard.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameters.AddressTypeParameter;
import ezvcard.parameters.EmailTypeParameter;
import ezvcard.parameters.ImageTypeParameter;
import ezvcard.parameters.SoundTypeParameter;
import ezvcard.parameters.TelephoneTypeParameter;
import ezvcard.types.AddressType;
import ezvcard.types.GenderType;
import ezvcard.types.KindType;
import ezvcard.types.PhotoType;
import ezvcard.types.RevisionType;
import ezvcard.types.SoundType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TimezoneType;
import ezvcard.types.UidType;
import freemarker.template.TemplateException;

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
 * Generates a sample vCard.
 * @author Michael Angstadt
 */
public class JohnDoeVCard {
	public static void main(String[] args) throws Exception {
		VCard vcard = new VCard();

		vcard.setKind(KindType.individual());

		vcard.setGender(GenderType.male());

		vcard.addLanguage("en-US");

		StructuredNameType n = new StructuredNameType();
		n.setFamily("Doe");
		n.setGiven("Jonathan");
		n.addPrefix("Mr");
		vcard.setStructuredName(n);

		vcard.setFormattedName("Jonathan Doe");

		vcard.setNickname("John", "Jonny");

		vcard.addTitle("Widget Engineer");

		vcard.setOrganization("Acme Co. Ltd.", "Widget Department");

		AddressType adr = new AddressType();
		adr.setStreetAddress("123 Wall St.");
		adr.setLocality("New York");
		adr.setRegion("NY");
		adr.setPostalCode("12345");
		adr.setCountry("USA");
		adr.setLabel("123 Wall St.\nNew York, NY 12345\nUSA");
		adr.addType(AddressTypeParameter.WORK);
		vcard.addAddress(adr);

		adr = new AddressType();
		adr.setStreetAddress("123 Main St.");
		adr.setLocality("Albany");
		adr.setRegion("NY");
		adr.setPostalCode("54321");
		adr.setCountry("USA");
		adr.setLabel("123 Main St.\nAlbany, NY 54321\nUSA");
		adr.addType(AddressTypeParameter.HOME);
		vcard.addAddress(adr);

		vcard.addTelephoneNumber("1-555-555-1234", TelephoneTypeParameter.WORK);
		vcard.addTelephoneNumber("1-555-555-5678", TelephoneTypeParameter.WORK, TelephoneTypeParameter.CELL);

		vcard.addEmail("johndoe@hotmail.com", EmailTypeParameter.HOME);
		vcard.addEmail("doe.john@acme.com", EmailTypeParameter.WORK);

		vcard.addUrl("http://www.acme-co.com");

		vcard.setCategories("widgetphile", "biker", "vCard expert");

		vcard.setGeo(37.6, -95.67);

		vcard.setTimezone(new TimezoneType(-5, 0, "America/New_York"));

		File file = new File("portrait.jpg");
		PhotoType photo = new PhotoType(file, ImageTypeParameter.JPEG);
		vcard.addPhoto(photo);

		file = new File("pronunciation.ogg");
		SoundType sound = new SoundType(file, SoundTypeParameter.OGG);
		vcard.addSound(sound);

		vcard.setUid(UidType.random());

		vcard.setRevision(RevisionType.now());

		//write vCard
		file = new File("john-doe.vcf");
		writeVCard(vcard, file, VCardVersion.V3_0);
		System.out.println();

		//write xCard
		file = new File("john-doe.xml");
		writeXCard(vcard, file);
		System.out.println();

		//write hCard
		file = new File("john-doe.html");
		writeHCard(vcard, file);
		System.out.println();

		//write jCard
		file = new File("john-doe.json");
		writeJCard(vcard, file);
	}

	private static void writeVCard(VCard vcard, File file, VCardVersion version) throws IOException {
		System.out.println("Writing " + file.getName() + "...");

		List<String> warnings = new ArrayList<String>();
		Ezvcard.write(vcard).version(version).warnings(warnings).go(file);

		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
	}

	private static void writeXCard(VCard vcard, File file) throws IOException, TransformerException {
		System.out.println("Writing " + file.getName() + "...");

		List<String> warnings = new ArrayList<String>();
		Ezvcard.writeXml(vcard).indent(2).warnings(warnings).go(file);

		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
	}

	private static void writeHCard(VCard vcard, File file) throws IOException, TemplateException {
		System.out.println("Writing " + file.getName() + "...");

		Ezvcard.writeHtml(vcard).go(file);
	}

	private static void writeJCard(VCard vcard, File file) throws IOException {
		System.out.println("Writing " + file.getName() + "...");

		List<String> warnings = new ArrayList<String>();
		Ezvcard.writeJson(vcard).indent(true).warnings(warnings).go(file);

		System.out.println("Completed with " + warnings.size() + " warnings.");
		for (String warning : warnings) {
			System.out.println("* " + warning);
		}
	}
}