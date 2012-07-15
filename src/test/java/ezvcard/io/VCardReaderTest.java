package ezvcard.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.types.UrlType;

/*
Copyright (c) 2012, Michael Angstadt
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
public class VCardReaderTest {
	@Test
	public void evolution() throws Exception {
		VCardReader reader = new VCardReader(new InputStreamReader(getClass().getResourceAsStream("John_Doe_EVOLUTION.vcf")));
		reader.setCompatibilityMode(CompatibilityMode.EVOLUTION);
		VCard vcard = reader.readNext();

		assertTrue(reader.getWarnings().toString(), reader.getWarnings().isEmpty());
		assertEquals(VCardVersion.V3_0, vcard.getVersion());

		//URL
		{
			List<UrlType> c = vcard.getUrls();
			Iterator<UrlType> it = c.iterator();

			UrlType t = it.next();
			assertEquals("http://www.ibm.com", t.getValue());
			//TODO remove quotes
			assertEquals("\"0abc9b8d-0845-47d0-9a91-3db5bb74620d\"", t.getSubTypes().getFirst("X-COUCHDB-UUID"));

			assertFalse(it.hasNext());
		}
	}
}
