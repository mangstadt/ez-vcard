package com.ezvcard.android;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.property.*;

import java.text.SimpleDateFormat;
import java.util.*;

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

    private final Context context;
    private final String accountName;
    private final String accountType;
    private int rawContactID = 0;

    public ContactOperations(Context context) {
        this(context, null, null);
    }

    public ContactOperations(Context context, String accountName, String accountType) {
        this.context = context;
        this.accountName = accountName;
        this.accountType = accountType;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int insertContact(VCard vCard) throws RemoteException, OperationApplicationException {
    	if (vCard == null){
    		Log.d(TAG, "The vcard is null or It must be a duplicate Contact hence we could not insert the contact");
            return 0;
    	}

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        rawContactID = ops.size();
        
        // TODO handle Raw properties - Raw properties include various extension which start with "X-" like X-ASSISTANT, X-AIM, X-SPOUSE

        insertAccountInfo(ops);
        insertName(vCard, ops);
        insertNickname(vCard, ops);
        insertPhones(vCard, ops);
        insertEmails(vCard, ops);
        insertAddresses(vCard, ops);
        insertIms(vCard, ops);

        // handle Android Custom fields..This is only valid for Android generated Vcards. As the Android would
        // generate NickName, ContactEvents other than Birthday and RelationShip with this "X-ANDROID-CUSTOM" name
        insertCustomFields(vCard, ops);

        // handle Iphone kinda of group properties. which are grouped together.
        insertGroupedProperties(vCard, ops);
        
        //TODO Vcard 4.0 may have more than 1 birthday so lets get the list and always use the very first one ..
        //TODO Should we handle date formats ...???
        insertBirthdays(vCard, ops);
        
        insertWebsites(vCard, ops);
        insertNotes(vCard, ops);
        insertPhotos(vCard, ops);
        insertOrganization(vCard, ops);

        // Executing all the insert operations as a single database transaction
        context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        return 0;
    }
    
    private void insertAccountInfo(ArrayList<ContentProviderOperation> ops){
    	String accountName = TextUtils.isEmpty(this.accountName) ? null : this.accountName;
        String accountType = TextUtils.isEmpty(this.accountType) ? null : this.accountType;

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
            .build());
    }
    
    private void insertName(VCard vCard, ArrayList<ContentProviderOperation> ops){
        ContentValues contentValues = new ContentValues();
        
        StructuredName n = vCard.getStructuredName();
        String firstName = (n == null) ? null : n.getGiven();
        String lastName = (n == null) ? null : n.getFamily();
        
        FormattedName fn = vCard.getFormattedName();
        String formattedName = (fn == null) ? null : vCard.getFormattedName().getValue();

        String firstPhoneticName = null;
        RawProperty firstphoneticNameprop = vCard.getExtendedProperty("X-PHONETIC-FIRST-NAME");
        if (firstphoneticNameprop != null) {
            firstPhoneticName = firstphoneticNameprop.getValue();
        }
        
        String lastPhoneticName = null;
        RawProperty lastPhoneticNameProp = vCard.getExtendedProperty("X-PHONETIC-LAST-NAME");
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
            displayName = VcardContactUtil.join(namePrefix, firstName, null, lastName, nameSuffix, true, false, true);
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

        if (contentValues.size() == 0){
        	return;
        }

        contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
	    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValues(contentValues)
            .build());
    }
    
    private void insertNickname(VCard vCard, ArrayList<ContentProviderOperation> ops){
        List<Nickname> nicknameList = vCard.getNicknames();
        for (Nickname nickname : nicknameList) {
            if (nickname == null || nickname.getValues().isEmpty()) {
            	continue;
            }
            
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickname.getValues().get(0))
                .build());
        }
    }
    
    private void insertPhones(VCard vCard, ArrayList<ContentProviderOperation> ops){
        List<Telephone> telephoneList = vCard.getTelephoneNumbers();
        for (Telephone telephone : telephoneList) {
        	if (telephone == null){
        		continue;
        	}

            int phoneKind = VcardContactUtil.getPhoneType(telephone);
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telephone.getText()) //TODO could be a URI
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneKind)
                .build());
        }
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void insertEmails(VCard vCard, ArrayList<ContentProviderOperation> ops){
        List<Email> emailList = vCard.getEmails();
        for (Email email : emailList) {
        	if (email == null){
        		continue;
        	}

            int emailKind = VcardContactUtil.getEmailType(email);
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.getValue())
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailKind)
                .build());
        }
    }
    
    private void insertAddresses(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	 List<Address> addressList = vCard.getAddresses();
         for (Address address : addressList) {
         	if (address == null){
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
            if (addressKind == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM){
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

            if (contentValues.size() == 0){
            	continue;
            }

            contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValues(contentValues)
                .build());
         }
    }
    
    private void insertIms(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	//handle extended properties
        for (Map.Entry<String, Integer> entry : VcardContactUtil.getImPropertyNameMappings().entrySet()) {
        	String propertyName = entry.getKey();
        	Integer protocolType = entry.getValue();
            List<RawProperty> rawProperties = vCard.getExtendedProperties(propertyName);
            for (RawProperty rawProperty : rawProperties){
	            String imAddress = rawProperty.getValue();
	            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Im.DATA, imAddress)
                    .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, protocolType)
                    .build());
	        }
        }

        //handle IMPP properties
        List<Impp> imppList = vCard.getImpps();
        for (Impp impp : imppList) {
        	if (impp == null){
        		continue;
        	}

            String immpAddress = impp.getHandle();
            int immpProtocolType = VcardContactUtil.getIMTypeFromProtocol(impp.getProtocol());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Im.DATA, immpAddress)
                .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, immpProtocolType)
                .build());
        }
    }
    
    private void insertCustomFields(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	List<AndroidCustomField> customFields = vCard.getProperties(AndroidCustomField.class);
        for (AndroidCustomField customField : customFields) {
			List<String> values = customField.getValues();
			if (values.isEmpty()) {
				continue;
			}

			ContentProviderOperation op = null;
			if (customField.isNickname()){
				op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
					.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Nickname.NAME, values.get(0))
					.build();
			} else if (customField.isContactEvent()){
				op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
					.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Event.START_DATE, values.get(0))
					.withValue(ContactsContract.CommonDataKinds.Event.TYPE, values.get(1))
					.build();
			} else if (customField.isRelation()){
				op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
					.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Relation.NAME, values.get(0))
					.withValue(ContactsContract.CommonDataKinds.Relation.TYPE, values.get(1))
					.build();
			}

			if (op != null) {
				ops.add(op);
			}
		}
    }
    
    private void insertGroupedProperties(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	List<RawProperty> extendedProperties = vCard.getExtendedProperties();
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
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, label)
                            .withValue(ContactsContract.CommonDataKinds.Event.TYPE, type)
                            .build());
                    }
                    break;
                case ABRELATEDNAMES:
                    if (val != null) {
                    	ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
	                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
	                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
	                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, label);
                    	
                        if (!val.equals("Nickname")) {
                            int type = VcardContactUtil.getNameType(val);
                            builder.withValue(ContactsContract.CommonDataKinds.Relation.TYPE, type);
                        }
                        
                        ops.add(builder.build());
                    }
                    break;
            }
        }
    }
    
    private void insertBirthdays(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	List<Birthday> birthdayList = vCard.getBirthdays();
        for (Birthday birthday : birthdayList) {
        	if (birthday == null){
        		continue;
        	}

        	Date date = birthday.getDate();
        	if (date == null){
        		continue;
        	}

        	String formattedBday = new SimpleDateFormat("yyyy-MM-dd").format(date);
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
                .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, formattedBday)
                .build());
        }
    }
    
    private void insertWebsites(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	 List<Url> urls = vCard.getUrls();
         for (Url url : urls) {
         	if (url == null){
         		continue;
         	}

            String urlValue = url.getValue();
            int type = VcardContactUtil.getWebSiteType(url.getType());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                 .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                 .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                 .withValue(ContactsContract.CommonDataKinds.Website.URL, urlValue)
                 .withValue(ContactsContract.CommonDataKinds.Website.TYPE, type)
                 .build());
         }
    }
    
    private void insertNotes(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	List<Note> notes = vCard.getNotes();
        for (Note note : notes) {
        	if (note == null){
        		continue;
        	}

            String noteValue = note.getValue();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, noteValue)
                .build());
        }
    }
    
    private void insertPhotos(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	List<Photo> photoList = vCard.getPhotos();
        for (Photo photo : photoList) {
        	if (photo == null){
        		continue;
        	}

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photo.getData())
                .build());
        }
    }
    
    private void insertOrganization(VCard vCard, ArrayList<ContentProviderOperation> ops){
    	List<Organization> organizationList = vCard.getOrganizations();
        List<Title> titleList = vCard.getTitles();
        if (organizationList.isEmpty() && titleList.isEmpty()) {
        	return;
        }

        String orgName = null;
        if (!organizationList.isEmpty()) {
        	orgName = organizationList.get(0).getValues().get(0);
        }
        
        String jobTitle = null;
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

        if (contentValues.size() == 0){
        	return;
        }

        contentValues.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
            .withValues(contentValues)
            .build());
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
