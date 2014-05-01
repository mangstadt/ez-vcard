package com.ezvcard.android;


import android.content.ContentValues;
import android.provider.ContactsContract;
import android.text.TextUtils;

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
 * Represents AddressType for Android COntacts Contract.
 *
 * @author Julien Garrigou
 */

public enum AddressTypeEnum {

    HOME("home") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);
        }
    },
    WORK("work") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
        }
    },
    BUSINESS("business") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);
        }
    },
    OTHER("other") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER);
        }
    },
    PERSONAL("personal") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            setContentValuesTypeCustom(contentValues, label);
        }
    },
    PRIMARY("primary") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            setContentValuesTypeCustom(contentValues, label);
        }
    },
    UNKNOWN("Unknown") {
        @Override
        public void setContentValuesType(ContentValues contentValues, String label) {
            setContentValuesTypeCustom(contentValues, TextUtils.isEmpty(label) ? UNKNOWN.toString() : label);
        }
    };


    /**
     * @param text
     */
    private AddressTypeEnum(final String text) {
        this.text = text;
    }

    private final String text;


    @Override
    public String toString() {
        return text;
    }

    public abstract void setContentValuesType(ContentValues contentValues, String label);

    private static void setContentValuesTypeCustom(ContentValues contentValues, String label) {
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM);
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, label);
    }
}
