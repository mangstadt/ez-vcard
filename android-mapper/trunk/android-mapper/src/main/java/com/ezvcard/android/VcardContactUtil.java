package com.ezvcard.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.provider.ContactsContract;
import android.text.TextUtils;
import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Birthday;
import ezvcard.property.RawProperty;

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
 * VcardContactUtil.. This class does a lot of mapping between the vcard contact
 * data types to Android ContactsContract datatypes.
 *
 * @author Pratyush,Julien Garrigou
 */

public class VcardContactUtil {
    private static final Map<TelephoneType, Integer> phoneTypeMappings;
    static {
    	Map<TelephoneType, Integer> m = new HashMap<TelephoneType, Integer>();
    	m.put(TelephoneType.BBS, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
        m.put(TelephoneType.CAR, ContactsContract.CommonDataKinds.Phone.TYPE_CAR);
        m.put(TelephoneType.CELL, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        m.put(TelephoneType.FAX, ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME);
        m.put(TelephoneType.HOME, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        m.put(TelephoneType.ISDN, ContactsContract.CommonDataKinds.Phone.TYPE_ISDN);
        m.put(TelephoneType.MODEM, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        m.put(TelephoneType.PAGER, ContactsContract.CommonDataKinds.Phone.TYPE_PAGER);
        m.put(TelephoneType.MSG, ContactsContract.CommonDataKinds.Phone.TYPE_MMS);
        m.put(TelephoneType.PCS, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        m.put(TelephoneType.TEXT, ContactsContract.CommonDataKinds.Phone.TYPE_MMS);
        m.put(TelephoneType.TEXTPHONE, ContactsContract.CommonDataKinds.Phone.TYPE_MMS);
        m.put(TelephoneType.VIDEO, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
        m.put(TelephoneType.WORK, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        m.put(TelephoneType.VOICE, ContactsContract.CommonDataKinds.Phone.TYPE_OTHER);
    	phoneTypeMappings = Collections.unmodifiableMap(m);
    }
    
    private static final Map<String, Integer> websiteTypeMappings;
    static {
    	Map<String, Integer> m = new HashMap<String, Integer>();
    	m.put("home", ContactsContract.CommonDataKinds.Website.TYPE_HOME);
    	m.put("work", ContactsContract.CommonDataKinds.Website.TYPE_WORK);
    	m.put("homepage", ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE);
    	m.put("profile", ContactsContract.CommonDataKinds.Website.TYPE_PROFILE);
    	websiteTypeMappings = Collections.unmodifiableMap(m);
    }
    
    private static final Map<EmailType, Integer> emailTypeMappings;
    static {
    	Map<EmailType, Integer> m = new HashMap<EmailType, Integer>();
        m.put(EmailType.HOME, ContactsContract.CommonDataKinds.Email.TYPE_HOME);
        m.put(EmailType.WORK, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        emailTypeMappings = Collections.unmodifiableMap(m);
    }

    public static final String CUSTOM_TYPE_NICKNAME = "nickname";
    public static final String CUSTOM_TYPE_CONTACT_EVENT = "contact_event";
    public static final String CUSTOM_TYPE_RELATION = "relation";

    private static final String AIM = "aim";
    private static final String ICQ = "icq";
    private static final String IRC = "irc";
    private static final String MSN = "msnim";
    private static final String SIP = "sip";
    private static final String SKYPE = "skype";
    private static final String XMPP = "xmpp";
    private static final String YAHOO = "ymsgr";

    public static final int ABDATE = 1;
    public static final int ABRELATEDNAMES = 2;

    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<T>(set);
    }

    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();
        for (T t : list1) {
            if (list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    public static List<String> getPropertyNamesFromRawProperties(List<RawProperty> rawProperties) {
        List<String> propertynames = new ArrayList<String>();
        if (rawProperties != null && rawProperties.size() > 0) {
            for (RawProperty rawProperty : rawProperties) {
                if (rawProperty != null && rawProperty.getPropertyName() != null) {
                    propertynames.add(rawProperty.getPropertyName());
                }
            }
        }
        return propertynames;
    }

    public static int getWebSiteType(String type) {
    	if (type == null){
    		return ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM;
    	}

    	type = type.toLowerCase();
    	Integer value = websiteTypeMappings.get(type);
    	return (value == null) ? ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM : value;
    }

    public static int getDateType(String type) {
        if (type == null) {
            return ContactsContract.CommonDataKinds.Event.TYPE_OTHER;
        }
        if (type.contains("Anniversary")) {
            return ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY; //TODO parse "ANNIVERSARY" vCard property
        } else if (type.contains("Other")) {
            return ContactsContract.CommonDataKinds.Event.TYPE_OTHER;
        } else {
            return ContactsContract.CommonDataKinds.Event.TYPE_OTHER;
        }
    }

    public static int getNameType(String type) {
        if (type == null) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM;
        }
        if (type.contains("Friend")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_FRIEND;
        } else if (type.contains("Father")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_FATHER;
        } else if (type.contains("Spouse")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_SPOUSE;
        } else if (type.contains("Mother")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_MOTHER;
        } else if (type.contains("Brother")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_BROTHER;
        } else if (type.contains("Parent")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_PARENT;
        } else if (type.contains("Sister")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_SISTER;
        } else if (type.contains("Child")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_CHILD;
        } else if (type.contains("Assistant")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_ASSISTANT;
        } else if (type.contains("Partner")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_PARTNER;
        } else if (type.contains("Manager")) {
            return ContactsContract.CommonDataKinds.Relation.TYPE_MANAGER;
        } else {
            return ContactsContract.CommonDataKinds.Relation.TYPE_CUSTOM;
        }
    }

    public static int getIMTypeFromName(String IMName) {
        if (IMName.equals("X-AIM") || IMName.equals(AIM)) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM;
        } else if (IMName.equals("X-ICQ") || IMName.equals(ICQ)) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ;
        } else if (IMName.equals("X-GOOGLE-TALK")) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM;
        } else if (IMName.equals("X-JABBER")) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER;
        } else if (IMName.equals("X-MSN") || IMName.equals(MSN)) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN;
        } else if (IMName.equals("X-YAHOO") || IMName.equals(YAHOO)) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO;
        } else if (IMName.equals("X-SKYPE-USERNAME") || IMName.equals(SKYPE) || IMName.equals("X-SKYPE")) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE;
        } else if (IMName.equals("X-QQ")) {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ;
        } else {
            return ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM;
        }
    }

    public static int getPhoneType(TelephoneType type) {
        if (type == null) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
        }
        
        Integer value = phoneTypeMappings.get(type);
    	return (value == null) ? ContactsContract.CommonDataKinds.Phone.TYPE_OTHER : value;
    }

    public static int getEmailType(EmailType type) {
        if (type == null) {
            return ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
        }

        Integer value = emailTypeMappings.get(type);
    	return (value == null) ? ContactsContract.CommonDataKinds.Email.TYPE_OTHER : value;
    }

    /**
     * Concatenates parts of a full name inserting spaces and commas as specified.
     */
    public static String join(String prefix, String part1, String part2,
                              String part3, String suffix,
                              boolean useSpace,
                              boolean useCommaAfterPart1,
                              boolean useCommaAfterPart3) {

        prefix = prefix == null ? null : prefix.trim();
        part1 = part1 == null ? null : part1.trim();
        part2 = part2 == null ? null : part2.trim();
        part3 = part3 == null ? null : part3.trim();
        suffix = suffix == null ? null : suffix.trim();
        boolean hasPrefix = !TextUtils.isEmpty(prefix);
        boolean hasPart1 = !TextUtils.isEmpty(part1);
        boolean hasPart2 = !TextUtils.isEmpty(part2);
        boolean hasPart3 = !TextUtils.isEmpty(part3);
        boolean hasSuffix = !TextUtils.isEmpty(suffix);
        boolean isSingleWord = true;
        String singleWord = null;
        if (hasPrefix) {
            singleWord = prefix;
        }
        if (hasPart1) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = part1;
            }
        }
        if (hasPart2) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = part2;
            }
        }
        if (hasPart3) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = part3;
            }
        }
        if (hasSuffix) {
            if (singleWord != null) {
                isSingleWord = false;
            } else {
                singleWord = suffix;
            }
        }
        if (isSingleWord) {
            return singleWord;
        }
        StringBuilder sb = new StringBuilder();
        if (hasPrefix) {
            sb.append(prefix);
        }
        if (hasPart1) {
            if (hasPrefix) {
                sb.append(' ');
            }
            sb.append(part1);
        }
        if (hasPart2) {
            if (hasPrefix || hasPart1) {
                if (useCommaAfterPart1) {
                    sb.append(',');
                }
                if (useSpace) {
                    sb.append(' ');
                }
            }
            sb.append(part2);
        }
        if (hasPart3) {
            if (hasPrefix || hasPart1 || hasPart2) {
                if (useSpace) {
                    sb.append(' ');
                }
            }
            sb.append(part3);
        }
        if (hasSuffix) {
            if (hasPrefix || hasPart1 || hasPart2 || hasPart3) {
                if (useCommaAfterPart3) {
                    sb.append(',');
                }
                if (useSpace) {
                    sb.append(' ');
                }
            }
            sb.append(suffix);
        }
        return sb.toString();
    }

    public static String getDisplayName(VCard vCard) {
    	if (vCard == null){
    		return null;
    	}

        String displayName = null;
        String namePrefix = null;
        String nameSuffix = null;
        String formattedName = (vCard.getFormattedName() != null) ? vCard.getFormattedName().getValue() : null;
        if (vCard.getStructuredName() != null) {
            String firstName = vCard.getStructuredName().getGiven();
            String lastName = vCard.getStructuredName().getFamily();
            //For now always get the first prefix
            List<String> prefixes = vCard.getStructuredName().getPrefixes();
            List<String> suffixes = vCard.getStructuredName().getSuffixes();
            if (prefixes != null && prefixes.size() > 0) {
                namePrefix = vCard.getStructuredName().getPrefixes().get(0);
            }
            if (suffixes != null && suffixes.size() > 0) {
                nameSuffix = vCard.getStructuredName().getSuffixes().get(0);
            }
            if (TextUtils.isEmpty(formattedName)) {
                displayName = join(namePrefix, firstName, null, lastName, nameSuffix, true, false, true);
            } else {
                displayName = formattedName;
            }
        }
        return displayName;
    }


    public static String getFormattedBirthday(Birthday birthday) {

        Date birthdate = birthday.getDate();
        String monthValue;
        String dayvalue;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthdate);

        String year = String.valueOf(calendar.get(Calendar.YEAR));
        int month = calendar.get(Calendar.MONTH) + 1;
        if (month < 10) {
            monthValue = "0" + String.valueOf(month);
        } else {
            monthValue = String.valueOf(month);
        }
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            dayvalue = "0" + String.valueOf(day);
        } else {
            dayvalue = String.valueOf(day);
        }
        return year + "-" + monthValue + "-" + dayvalue;
    }

}
