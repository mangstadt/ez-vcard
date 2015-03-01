package ezvcard.property;

import static ezvcard.util.TestUtils.assertValidate;
import static ezvcard.util.TestUtils.date;

import java.util.Date;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.util.PartialDate;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
public class DateOrTimePropertyTest {
	@Test
	public void validate() {
		DateOrTimeTypeImpl empty = new DateOrTimeTypeImpl();
		assertValidate(empty).run(8);

		DateOrTimeTypeImpl withDate = new DateOrTimeTypeImpl();
		Date date = date("1980-06-05");
		withDate.setDate(date, false);
		assertValidate(withDate).run();

		DateOrTimeTypeImpl withPartialDate = new DateOrTimeTypeImpl();
		withPartialDate.setPartialDate(PartialDate.builder().month(6).date(5).build());
		assertValidate(withPartialDate).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(12);
		assertValidate(withPartialDate).versions(VCardVersion.V4_0).run();

		DateOrTimeTypeImpl withText = new DateOrTimeTypeImpl();
		withText.setText("text");
		assertValidate(withText).versions(VCardVersion.V2_1, VCardVersion.V3_0).run(11);
		assertValidate(withText).versions(VCardVersion.V4_0).run();
	}

	private static class DateOrTimeTypeImpl extends DateOrTimeProperty {
		public DateOrTimeTypeImpl() {
			super((Date) null);
		}
	}
}
