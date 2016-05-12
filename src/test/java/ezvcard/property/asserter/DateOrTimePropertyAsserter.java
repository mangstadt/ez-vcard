package ezvcard.property.asserter;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import ezvcard.property.DateOrTimeProperty;
import ezvcard.util.PartialDate;
import ezvcard.util.TestUtils;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
public class DateOrTimePropertyAsserter<T extends DateOrTimeProperty> extends PropertyAsserter<DateOrTimePropertyAsserter<T>, T> {
	private Date date;
	private PartialDate partialDate;

	public DateOrTimePropertyAsserter(List<T> properties, VCardAsserter asserter) {
		super(properties, asserter);
	}

	public DateOrTimePropertyAsserter<T> date(String dateStr) {
		this.date = TestUtils.date(dateStr);
		return this_;
	}

	public DateOrTimePropertyAsserter<T> partialDate(PartialDate partialDate) {
		this.partialDate = partialDate;
		return this_;
	}

	@Override
	protected void _run(T property) {
		assertEquals(date, property.getDate());
		assertEquals(partialDate, property.getPartialDate());
	}

	@Override
	protected void _reset() {
		date = null;
		partialDate = null;
	}
}