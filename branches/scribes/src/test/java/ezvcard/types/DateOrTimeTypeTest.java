package ezvcard.types;

import static ezvcard.util.TestUtils.assertWarnings;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.util.PartialDate;

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
 * @author Michael Angstadt
 */
public class DateOrTimeTypeTest {
	@Test
	public void validate() {
		VCard vcard = new VCard();

		DateOrTimeTypeImpl empty = new DateOrTimeTypeImpl();
		assertWarnings(1, empty.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V3_0, vcard));
		assertWarnings(1, empty.validate(VCardVersion.V4_0, vcard));

		DateOrTimeTypeImpl withDate = new DateOrTimeTypeImpl();
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 1980);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DAY_OF_MONTH, 5);
		Date date = c.getTime();
		withDate.setDate(date, false);
		assertWarnings(0, withDate.validate(VCardVersion.V2_1, vcard));
		assertWarnings(0, withDate.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withDate.validate(VCardVersion.V4_0, vcard));

		DateOrTimeTypeImpl withPartialDate = new DateOrTimeTypeImpl();
		withPartialDate.setPartialDate(PartialDate.date(null, 6, 5));
		assertWarnings(1, withPartialDate.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, withPartialDate.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withPartialDate.validate(VCardVersion.V4_0, vcard));

		DateOrTimeTypeImpl withText = new DateOrTimeTypeImpl();
		withText.setText("text");
		assertWarnings(1, withText.validate(VCardVersion.V2_1, vcard));
		assertWarnings(1, withText.validate(VCardVersion.V3_0, vcard));
		assertWarnings(0, withText.validate(VCardVersion.V4_0, vcard));
	}

	private static class DateOrTimeTypeImpl extends DateOrTimeType {
		//empty
	}
}
