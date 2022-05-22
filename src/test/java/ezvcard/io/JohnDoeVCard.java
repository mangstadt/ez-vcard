package ezvcard.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.ImageType;
import ezvcard.parameter.SoundType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Gender;
import ezvcard.property.Kind;
import ezvcard.property.Photo;
import ezvcard.property.Revision;
import ezvcard.property.Sound;
import ezvcard.property.StructuredName;
import ezvcard.property.Timezone;
import ezvcard.property.Uid;

/*
 Copyright (c) 2012-2021, Michael Angstadt
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
	public static void main(String[] args) throws Throwable {
		VCard vcard = createVCard();

		//validate vCard for version 2.1
		System.out.println("Version 2.1 validation warnings:");
		System.out.println(vcard.validate(VCardVersion.V2_1));
		System.out.println();

		//validate vCard for version 3.0
		System.out.println("Version 3.0 validation warnings:");
		System.out.println(vcard.validate(VCardVersion.V3_0));
		System.out.println();

		//validate vCard for version 4.0 (xCard and jCard use this version)
		System.out.println("Version 4.0 validation warnings:");
		System.out.println(vcard.validate(VCardVersion.V4_0));

		//write vCard
		Path file = Paths.get("john-doe.21.vcf");
		System.out.println("Writing " + file + "...");
		Ezvcard.write(vcard).version(VCardVersion.V2_1).go(file);

		file = Paths.get("john-doe.3.vcf");
		System.out.println("Writing " + file + "...");
		Ezvcard.write(vcard).version(VCardVersion.V3_0).go(file);

		file = Paths.get("john-doe.4.vcf");
		System.out.println("Writing " + file + "...");
		Ezvcard.write(vcard).version(VCardVersion.V4_0).go(file);

		//write xCard
		file = Paths.get("john-doe.xml");
		System.out.println("Writing " + file + "...");
		Ezvcard.writeXml(vcard).indent(2).go(file);

		//write hCard
		file = Paths.get("john-doe.html");
		System.out.println("Writing " + file + "...");
		Ezvcard.writeHtml(vcard).go(file);

		//write jCard
		file = Paths.get("john-doe.json");
		System.out.println("Writing " + file + "...");
		Ezvcard.writeJson(vcard).prettyPrint(true).go(file);
	}

	private static VCard createVCard() throws IOException {
		VCard vcard = new VCard();

		vcard.setKind(Kind.individual());

		vcard.setGender(Gender.male());

		vcard.addLanguage("en-US");

		StructuredName n = new StructuredName();
		n.setFamily("Doe");
		n.setGiven("Jonathan");
		n.getPrefixes().add("Mr");
		vcard.setStructuredName(n);

		vcard.setFormattedName("Jonathan Doe");

		vcard.setNickname("John", "Jonny");
		
		vcard.setBirthday(new Birthday(LocalDate.of(1980, 1, 1)));

		vcard.addTitle("Widget Engineer");

		vcard.setOrganization("Acme Co. Ltd.", "Widget Department");

		Address adr = new Address();
		adr.setStreetAddress("123 Wall St.");
		adr.setLocality("New York");
		adr.setRegion("NY");
		adr.setPostalCode("12345");
		adr.setCountry("USA");
		adr.setLabel("123 Wall St.\nNew York, NY 12345\nUSA");
		adr.getTypes().add(AddressType.WORK);
		vcard.addAddress(adr);

		adr = new Address();
		adr.setStreetAddress("123 Main St.");
		adr.setLocality("Albany");
		adr.setRegion("NY");
		adr.setPostalCode("54321");
		adr.setCountry("USA");
		adr.setLabel("123 Main St.\nAlbany, NY 54321\nUSA");
		adr.getTypes().add(AddressType.HOME);
		vcard.addAddress(adr);

		vcard.addTelephoneNumber("1-555-555-1234", TelephoneType.WORK);
		vcard.addTelephoneNumber("1-555-555-5678", TelephoneType.WORK, TelephoneType.CELL);

		vcard.addEmail("johndoe@hotmail.com", EmailType.HOME);
		vcard.addEmail("doe.john@acme.com", EmailType.WORK);

		vcard.addUrl("http://www.acme-co.com");

		vcard.setCategories("widgetphile", "biker", "vCard expert");

		vcard.setGeo(37.6, -95.67);

		vcard.setTimezone(new Timezone(ZoneOffset.ofHours(-5), "America/New_York"));

		Path file = Paths.get("portrait.jpg");
		Photo photo = new Photo(file, ImageType.JPEG);
		vcard.addPhoto(photo);

		file = Paths.get("pronunciation.ogg");
		Sound sound = new Sound(file, SoundType.OGG);
		vcard.addSound(sound);

		vcard.setUid(Uid.random());

		vcard.setRevision(Revision.now());

		return vcard;
	}
}