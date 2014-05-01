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
import ezvcard.property.*;

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
 * Represents a vCard.
 *
 * @author Pratyush
 */

public class ContactOperations {

    private static final String TAG = ContactOperations.class.getSimpleName();

    private Context mContext;
    String accountname = null;
    String accounttype = null;


    public ContactOperations(Context context) {
        this.mContext = context;
    }

    public ContactOperations(Context context, String accountname, String accounttype) {
        this.mContext = context;
        this.accountname = accountname;
        this.accounttype = accounttype;
    }

    public interface ContactsRestoreCallback {

        void error(Exception e);

        void processing();

        void processed();
    }


    //Gross shit follows .This is huge method but a contact field it self is so huge.

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int insertContact(VCard vCard, ContactsRestoreCallback contactsRestoreCallback) {

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactID = ops.size();
        contactsRestoreCallback.processing();
        // insert display name in the table ContactsContract.Data

        if (vCard != null) {
            String accountName = (!TextUtils.isEmpty(this.accountname)) ? this.accountname : null;
            String accountType = (!TextUtils.isEmpty(this.accounttype)) ? this.accounttype : null;
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                    .build());

            // Insert Structured Name
            ContentValues stucturedNameValues = VcardContactUtil.structuredNameToContentValues(vCard);
            if (stucturedNameValues != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValues(stucturedNameValues)
                        .build());
            }

            // Insert Nick Names -- This is only Supported in Vcard 4.0
            List<Nickname> nicknameList = vCard.getNicknames();
            for (Nickname nickname : nicknameList) {
                if (nickname != null && nickname.getValues().size() > 0) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickname.getValues().get(0))
                            .build());
                }
            }
            // Insert Phone Number
            List<Telephone> telephoneList = vCard.getTelephoneNumbers();
            for (Telephone telephone : telephoneList) {
                if (telephone != null) {
                    String type = telephone.getParameters().getType();
                    int phoneKind = VcardContactUtil.getPhoneType(type);
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telephone.getText())
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneKind)
                            .build());
                }
            }
            // Insert Email addresses
            List<Email> emailList = vCard.getEmails();
            for (Email email : emailList) {
                if (email != null) {
                    String emailType = email.getParameters().getType();
                    int emailKind = VcardContactUtil.getEmailType(emailType);
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.getValue())
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailKind)
                            .build());
                }
            }
            // Insert Address
            List<Address> addressList = vCard.getAddresses();
            for (Address address : addressList) {
                if (address != null) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValues(VcardContactUtil.addressToContentValues(address))
                            .build());
                }
            }

            // handle Raw properties - Raw properties include various extension which start with X-
            // like X-ASSISTANT, X-AIM, X-SPOUSE

            // get all the extended properties from the vcard parser
            List<RawProperty> rawProperties = vCard.getExtendedProperties();
            //Get just the propertynames from the rawproperties
            List<String> rawPropertyNames = VcardContactUtil.getPropertyNamesFromRawProperties(rawProperties);
            // Intersect the Instant Messaging properties set from the all of the RawProperties
            List<String> Ims = VcardContactUtil.intersection(rawPropertyNames, VcardContactUtil.supportedIMList);
            //Handle Im insertions here

            for (String Im : Ims) {
                if (Im != null) {
                    // now get the actual raw property
                    RawProperty rawProperty = vCard.getExtendedProperty(Im);
                    if (rawProperty != null) {
                        String imAddress = rawProperty.getValue();
                        int protocolType = VcardContactUtil.getIMTypeFromName(Im);
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Im.DATA, imAddress)
                                .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, protocolType)
                                .build());
                    }
                }
            }

            //Lets handle IMS for different kind of vcards other than ANdroid - Here probably the source could be IPhone or any other device
            List<Impp> imppList = vCard.getImpps();
            for (Impp impp : imppList) {
                if (impp != null) {
                    String immpAddress = impp.getHandle();
                    int immpProtocolType = VcardContactUtil.getIMTypeFromName(impp.getProtocol());
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Im.DATA, immpAddress)
                            .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, immpProtocolType)
                            .build());
                }
            }

            // handle Android Custom fields..This is only valid for Android generated Vcards. As the Android would
            // generate NickName, ContactEvents other than Birthday and RelationShip with this "X-ANDROID-CUSTOM" name
            
            List<AndroidCustomField> customFields = vCard.getProperties(AndroidCustomField.class);
            for (AndroidCustomField customField : customFields) {
    			List<String> values = customField.getValues();
    			if (values.isEmpty()) {
    				continue;
    			}

    			//@formatter:off
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
    			//@formatter:on

    			if (op != null) {
    				ops.add(op);
    			}
    		}

            // handle Iphone kinda of group properties. which are grouped togethar.

            Map<String, List<VcardItemGroupProperties>> orderedByGroupMap = orderVcardByGroup(rawProperties);

            for (Map.Entry<String, List<VcardItemGroupProperties>> property : orderedByGroupMap.entrySet()) {
                List<VcardItemGroupProperties> itemGroupProperties = property.getValue();

                if (itemGroupProperties != null && itemGroupProperties.size() >= 2) {
                    String label = null;
                    String val = null;
                    int mime = 0;
                    for (VcardItemGroupProperties vcardproperty : itemGroupProperties) {
                        String dataType = vcardproperty.getPropname();
                        if (dataType.equals("X-ABDATE")) {         //label
                            label = vcardproperty.getProperty_value(); //date
                            mime = VcardContactUtil.ABDATE;
                        } else if (dataType.equals("X-ABRELATEDNAMES")) {
                            label = vcardproperty.getProperty_value(); //name
                            mime = VcardContactUtil.ABRELATEDNAMES;
                        } else if (dataType.equals("X-ABLabel")) {
                            val = vcardproperty.getProperty_value(); // type of value ..Birthday,anniversary
                        }
                    }
                    switch (mime) {
                        case VcardContactUtil.ABDATE:
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
                        case VcardContactUtil.ABRELATEDNAMES:
                            if (val != null) {
                                if (val.equals("Nickname")) {
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, label)
                                            .build());
                                } else {
                                    int type = VcardContactUtil.getNameType(val);
                                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE)
                                            .withValue(ContactsContract.CommonDataKinds.Relation.NAME, label)
                                            .withValue(ContactsContract.CommonDataKinds.Relation.TYPE, type)
                                            .build());
                                }
                            }
                            break;
                    }
                }
            }
            // handle Birthdays
            //Vcard 4.0 may have more than 1 birthday so lets get the list and always use the very first one ..
            //Should we handle date formats ...???
            List<Birthday> birthdayList = vCard.getBirthdays();
            for (Birthday birthday : birthdayList) {
                if (birthday != null) {
                    String formattedBday = VcardContactUtil.getFormattedBirthday(birthday);
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Event.TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)
                            .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, formattedBday)
                            .build());
                }
            }
            // handle Websites
            List<Url> urls = vCard.getUrls();
            for (Url url : urls) {
                if (url != null) {
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
            // Insert Notes
            List<Note> notes = vCard.getNotes();
            if (notes != null) {
                for (Note note : notes) {
                    if (note != null) {
                        String noteValue = note.getValue();
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, noteValue)
                                .build());
                    }
                }
            }
            // Insert Contact photos here
            List<Photo> photoList = vCard.getPhotos();
            for (Photo photo : photoList) {
                if (photo != null) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photo.getData())
                            .build());
                }
            }
            // handle Organization and Title relation ship here
            List<Organization> organizationList = vCard.getOrganizations();
            List<Title> titleList = vCard.getTitles();
            if (organizationList.size() > 0 || titleList.size() > 0) {
                Organization organization = null;
                Title title = null;
                if (organizationList.size() > 0) {
                    organization = organizationList.get(0);
                }
                if (titleList.size() > 0) {
                    title = titleList.get(0);
                }
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValues(VcardContactUtil.organizationToContentValues(organization, title))
                        .build());
            }
            try {
                // Executing all the insert operations as a single database transaction
                mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                contactsRestoreCallback.processed();
            } catch (RemoteException e) {
                e.printStackTrace();
                contactsRestoreCallback.error(e);
            } catch (OperationApplicationException e) {
                e.printStackTrace();
                contactsRestoreCallback.error(e);
            } catch (Exception e) {
                e.printStackTrace();
                contactsRestoreCallback.error(e);
            }

        } else {
            Log.d(TAG, "The vcard is null or It must be a duplicate Contact hence we could not insert the contact");
            contactsRestoreCallback.processed();
        }
        return 0;
    }


    private Map<String, List<VcardItemGroupProperties>> orderVcardByGroup(List<RawProperty> rawProperties) {
        Map<String, List<VcardItemGroupProperties>> groupPropertiesMap = new HashMap<String, List<VcardItemGroupProperties>>();

        for (RawProperty rawProperty : rawProperties) {
            String group = rawProperty.getGroup();
            if (!TextUtils.isEmpty(group)) {
                List<VcardItemGroupProperties> groupPropertiesList = groupPropertiesMap.get(group);
                if (groupPropertiesList == null) {
                    groupPropertiesList = new ArrayList<VcardItemGroupProperties>();
                }
                VcardItemGroupProperties groupProperties = new VcardItemGroupProperties();
                groupProperties.setPropname(rawProperty.getPropertyName());
                groupProperties.setProperty_value(rawProperty.getValue());
                groupPropertiesList.add(groupProperties);
                //update the map
                groupPropertiesMap.put(group, groupPropertiesList);
            }
        }
        return groupPropertiesMap;
    }


}
