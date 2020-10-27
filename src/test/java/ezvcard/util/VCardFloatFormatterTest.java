package ezvcard.util;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

/*
 Copyright (c) 2012-2020, Michael Angstadt
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
 */

/**
 * @author Michael Angstadt
 */
public class VCardFloatFormatterTest {
	@Test
	public void format_truncate() {
		VCardFloatFormatter formatter = new VCardFloatFormatter();
		assertEquals("12.888889", formatter.format(12.8888888));
	}

	@Test
	public void format_no_truncate() {
		VCardFloatFormatter formatter = new VCardFloatFormatter();
		assertEquals("12.88", formatter.format(12.88));
	}

	@Test
	public void format_negative() {
		VCardFloatFormatter formatter = new VCardFloatFormatter();
		assertEquals("-12.88", formatter.format(-12.88));
	}

	@Test
	public void format_no_decimals() {
		VCardFloatFormatter formatter = new VCardFloatFormatter();
		assertEquals("12.0", formatter.format(12d));
	}

	@Test
	public void format_different_precision() {
		VCardFloatFormatter formatter = new VCardFloatFormatter(2);
		assertEquals("12.89", formatter.format(12.888));

		formatter = new VCardFloatFormatter(0);
		assertEquals("13", formatter.format(12.888));
	}

	@Test
	public void format_different_locales() {
		Locale defaultLocale = Locale.getDefault();

		try {
			for (Locale locale : Locale.getAvailableLocales()) {
				Locale.setDefault(locale);
				VCardFloatFormatter formatter = new VCardFloatFormatter();
				assertEquals("Test failed for locale: " + locale, "-12.388889", formatter.format(-12.3888888));
			}
		} finally {
			Locale.setDefault(defaultLocale);
		}
	}
}
