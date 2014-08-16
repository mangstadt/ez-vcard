package ezvcard.android;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import ezvcard.VCard;
import ezvcard.android.AndroidCustomField;
import ezvcard.android.VcardContactUtil;
import ezvcard.parameter.AddressType;
import ezvcard.property.Address;
import ezvcard.property.Birthday;
import ezvcard.property.Email;
import ezvcard.property.FormattedName;
import ezvcard.property.Impp;
import ezvcard.property.Nickname;
import ezvcard.property.Note;
import ezvcard.property.Organization;
import ezvcard.property.Photo;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.Title;
import ezvcard.property.Url;
import ezvcard.util.TelUri;

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
 * Inserts a {@link VCard} into an Android database.
 *
 * @author Pratyush
 * @author Michael Angstadt
 */
public class ContactOperations {
	private static final String TAG = ContactOperations.class.getSimpleName();
	private static final int rawContactID = 0;

	private final Context context;
	private final String accountName;
	private final String accountType;

	private VCard vcard;
	private ArrayList<ContentProviderOperation> operations;

	public ContactOperations(Context context) {
		this(context, null, null);
	}

	public ContactOperations(Context context, String accountName, String accountType) {
		this.context = context;
		this.accountName = accountName;
		this.accountType = accountType;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void insertContact(VCard vcard) throws RemoteException, OperationApplicationException {
		this.vcard = vcard;
		this.operations = new ArrayList<ContentProviderOperation>();

		// TODO handle Raw properties - Raw properties include various extension which start with "X-" like X-ASSISTANT, X-AIM, X-SPOUSE

		insertAccountInfo();
		insertName();
		insertNickname();
		insertPhones();
		insertEmails();
		insertAddresses();
		insertIms();

		// handle Android Custom fields..This is only valid for Android generated Vcards. As the Android would
		// generate NickName, ContactEvents other than Birthday and RelationShip with this "X-ANDROID-CUSTOM" name
		insertCustomFields();

		// handle Iphone kinda of group properties. which are grouped together.
		insertGroupedProperties();

		//TODO Vcard 4.0 may have more than 1 birthday so lets get the list and always use the very first one ..
		//TODO Should we handle date formats ...???
		insertBirthdays();

		insertWebsites();
		insertNotes();
		insertPhotos();
		insertOrganization();

		// Executing all the insert operations as a single database transaction
		context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
	}

	private void insertAccountInfo() {
		String accountName = TextUtils.isEmpty(this.accountName) ? null : this.accountName;
		String accountType = TextUtils.isEmpty(this.accountType) ? null : this.accountType;

		operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI).withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType).withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName).build());
	}

	private void insertName() {
		ContentValues contentValues = new ContentValues();

		StructuredName n = vcard.getStructuredName();
		String firstName = (n == null) ? null : n.getGiven();
		String lastName = (n == null) ? null : n.getFamily();

		FormattedName fn = vcard.getFormattedName();
		String formattedName = (fn == null) ? null : fn.getValue();

		String firstPhoneticName = null;
		RawProperty firstphoneticNameprop = vcard.getExtendedProperty("X-PHONETIC-FIRST-NAME");
		if (firstphoneticNameprop != null) {
			firstPhoneticName = firstphoneticNameprop.getValue();
		}

		String lastPhoneticName = null;
		RawProperty lastPhoneticNameProp = vcard.getExtendedProperty("X-PHONETIC-LAST-NAME");
		if (lastPhoneticNameProp != null) {
			lastPhoneticName = lastPhoneticNameProp.getValue();
		}

		//TODO for now always get the first prefix
		String namePrefix = null;
		String nameSuffix = null;
		if (n != null) {
			List<String> prefixes = n.getPrefixes();
			if (prefixes != null && !prefixes.isEmpty()) {
				namePrefix = prefixes.get(0);
			}

			List<String> suffixes = n.getSuffixes();
			if (suffixes != null && !suffixes.isEmpty()) {
				nameSuffix = suffixes.get(0);
			}
		}

		String displayName;
		if (TextUtils.isEmpty(formattedName)) {
			displayName = VcardContactUtil.joinNameComponents(namePrefix, firstName, null, lastName, nameSuffix);
		} else {
			displayName = formattedName;
		}

		if (!TextUtils.isEmpty(firstName)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName);
		}
		if (!TextUtils.isEmpty(lastName)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName);
		}
		if (!TextUtils.isEmpty(namePrefix)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.PREFIX, namePrefix);
		}
		if (!TextUtils.isEmpty(nameSuffix)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, nameSuffix);
		}
		if (!TextUtils.isEmpty(firstPhoneticName)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, firstPhoneticName);
		}
		if (!TextUtils.isEmpty(lastPhoneticName)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, lastPhoneticName);
		}
		if (!TextUtils.isEmpty(displayName)) {
			contentValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName);
		}

		if (contentValues.size() == 0) {
			return;
		}

		contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
		operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).withValues(contentValues).build());
	}

	private void insertNickname() {
		List<Nickname> nicknameList = vcard.getNicknames();
		for (Nickname nickname : nicknameList) {
			if (nickname == null || nickname.getValues().isEmpty()) {
				continue;
			}

			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickname.getValues().get(0)).build());
		}
	}

	private void insertPhones() {
		List<Telephone> telephoneList = vcard.getTelephoneNumbers();
		for (Telephone telephone : telephoneList) {
			if (telephone == null) {
				continue;
			}

			int phoneKind = VcardContactUtil.getPhoneType(telephone);

			String value = telephone.getText();
			if (value == null) {
				TelUri uri = telephone.getUri();
				if (uri != null) {
					value = uri.toString();
				}
			}

			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, value).withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneKind).build());
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void insertEmails() {
		List<Email> emailList = vcard.getEmails();
		for (Email email : emailList) {
			if (email == null) {
				continue;
			}

			int emailKind = VcardContactUtil.getEmailType(email);
			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.getValue()).withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailKind).build());
		}
	}

	private void insertAddresses() {
		List<Address> addressList = vcard.getAddresses();
		for (Address address : addressList) {
			if (address == null) {
				continue;
			}

			ContentValues contentValues = new ContentValues();
			String street = address.getStreetAddress();
			String poBox = address.getPoBox();
			String city = address.getLocality();
			String state = address.getRegion();
			String zipCode = address.getPostalCode();
			String country = address.getCountry();
			int addressKind = VcardContactUtil.getAddressType(address);

			contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, addressKind);
			if (addressKind == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM) {
				Set<AddressType> types = address.getTypes();
				String label = types.isEmpty() ? "unknown" : types.iterator().next().getValue();
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, label);
			}

			if (!TextUtils.isEmpty(street)) {
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.STREET, street);
			}
			if (!TextUtils.isEmpty(poBox)) {
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.POBOX, poBox);
			}
			// TODO No NEIGHBORHOOD info ...
			if (!TextUtils.isEmpty(city)) {
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, city);
			}
			if (!TextUtils.isEmpty(state)) {
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.REGION, state);
			}
			if (!TextUtils.isEmpty(zipCode)) {
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, zipCode);
			}
			if (!TextUtils.isEmpty(country)) {
				contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, country);
			}

			if (contentValues.size() == 0) {
				continue;
			}

			contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValues(contentValues).build());
		}
	}

	private void insertIms() {
		//handle extended properties
		for (Map.Entry<String, Integer> entry : VcardContactUtil.getImPropertyNameMappings().entrySet()) {
			String propertyName = entry.getKey();
			Integer protocolType = entry.getValue();
			List<RawProperty> rawProperties = vcard.getExtendedProperties(propertyName);
			for (RawProperty rawProperty : rawProperties) {
				String imAddress = rawProperty.getValue();
				operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Im.DATA, imAddress).withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, protocolType).build());
			}
		}

		//handle IMPP properties
		List<Impp> imppList = vcard.getImpps();
		for (Impp impp : imppList) {
			if (impp == null) {
				continue;
			}

			String immpAddress = impp.getHandle();
			int immpProtocolType = VcardContactUtil.getIMTypeFromProtocol(impp.getProtocol());
			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Im.DATA, immpAddress).withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, immpProtocolType).build());
		}
	}

	private void insertCustomFields() {
		List<AndroidCustomField> customFields = vcard.getProperties(AndroidCustomField.class);
		for (AndroidCustomField customField : customFields) {
			List<String> values = customField.getValues();
			if (values.isEmpty()) {
				continue;
			}

			ContentProviderOperation op = null;
			if (customField.isNickname()) {
				op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Nickname.NAME, values.get(0)).build();
			} else if (customField.isContactEvent()) {
				op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Event.START_DATE, values.get(0)).withValue(ContactsContract.CommonDataKinds.Event.TYPE, values.get(1)).build();
			} else if (customField.isRelation()) {
				op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Relation.NAME, values.get(0)).withValue(ContactsContract.CommonDataKinds.Relation.TYPE, values.get(1)).build();
			}

			if (op != null) {
				operations.add(op);
			}
		}
	}

	private void insertGroupedProperties() {
		List<RawProperty> extendedProperties = vcard.getExtendedProperties();
		Map<String, List<RawProperty>> orderedByGroup = orderPropertiesByGroup(extendedProperties);
		final int ABDATE = 1, ABRELATEDNAMES = 2;

		for (List<RawProperty> properties : orderedByGroup.values()) {
			if (properties.size() < 2) {
				continue;
			}

			String label = null;
			String val = null;
			int mime = 0;
			for (RawProperty property : properties) {
				String name = property.getPropertyName();

				if (name.equalsIgnoreCase("X-ABDATE")) {
					label = property.getValue(); //date
					mime = ABDATE;
					continue;
				}

				if (name.equalsIgnoreCase("X-ABRELATEDNAMES")) {
					label = property.getValue(); //name
					mime = ABRELATEDNAMES;
					continue;
				}

				if (name.equalsIgnoreCase("X-ABLABEL")) {
					val = property.getValue(); // type of value ..Birthday,anniversary
					continue;
				}
			}

			switch (mime) {
			case ABDATE:
				if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(val)) {
					int type = VcardContactUtil.getDateType(val);
					operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Event.START_DATE, label).withValue(ContactsContract.CommonDataKinds.Event.TYPE, type).build());
				}
				break;
			case ABRELATEDNAMES:
				if (val != null) {
					ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Nickname.NAME, label);

					if (!val.equals("Nickname")) {
						int type = VcardContactUtil.getNameType(val);
						builder.withValue(ContactsContract.CommonDataKinds.Relation.TYPE, type);
					}

					operations.add(builder.build());
				}
				break;
			}
		}
	}

	private void insertBirthdays() {
		List<Birthday> birthdayList = vcard.getBirthdays();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		for (Birthday birthday : birthdayList) {
			if (birthday == null) {
				continue;
			}

			Date date = birthday.getDate();
			if (date == null) {
				continue;
			}

			String formattedBday = df.format(date);
			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY).withValue(ContactsContract.CommonDataKinds.Event.START_DATE, formattedBday).build());
		}
	}

	private void insertWebsites() {
		List<Url> urls = vcard.getUrls();
		for (Url url : urls) {
			if (url == null) {
				continue;
			}

			String urlValue = url.getValue();
			int type = VcardContactUtil.getWebSiteType(url.getType());
			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Website.URL, urlValue).withValue(ContactsContract.CommonDataKinds.Website.TYPE, type).build());
		}
	}

	private void insertNotes() {
		List<Note> notes = vcard.getNotes();
		for (Note note : notes) {
			if (note == null) {
				continue;
			}

			String noteValue = note.getValue();
			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Note.NOTE, noteValue).build());
		}
	}

	private void insertPhotos() {
		List<Photo> photoList = vcard.getPhotos();
		for (Photo photo : photoList) {
			if (photo == null) {
				continue;
			}

			byte[] data = photo.getData();
			if (data == null) {
				continue;
			}

			operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, data).build());
		}
	}

	private void insertOrganization() {
		String orgName = null;
		Organization organization = vcard.getOrganization();
		if (organization != null) {
			List<String> values = organization.getValues();
			if (!values.isEmpty()) {
				orgName = values.get(0);
			}
		}

		String jobTitle = null;
		List<Title> titleList = vcard.getTitles();
		if (!titleList.isEmpty()) {
			jobTitle = titleList.get(0).getValue();
		}

		ContentValues contentValues = new ContentValues();
		if (!TextUtils.isEmpty(orgName)) {
			contentValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, orgName);
		}
		if (!TextUtils.isEmpty(jobTitle)) {
			contentValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle);
		}

		if (contentValues.size() == 0) {
			return;
		}

		contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
		operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID).withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE).withValues(contentValues).build());
	}

	private Map<String, List<RawProperty>> orderPropertiesByGroup(List<RawProperty> rawProperties) {
		Map<String, List<RawProperty>> groupedProperties = new HashMap<String, List<RawProperty>>();

		for (RawProperty rawProperty : rawProperties) {
			String group = rawProperty.getGroup();
			if (TextUtils.isEmpty(group)) {
				continue;
			}

			List<RawProperty> groupPropertiesList = groupedProperties.get(group);
			if (groupPropertiesList == null) {
				groupPropertiesList = new ArrayList<RawProperty>();
				groupedProperties.put(group, groupPropertiesList);
			}
			groupPropertiesList.add(rawProperty);
		}

		return groupedProperties;
	}
}
