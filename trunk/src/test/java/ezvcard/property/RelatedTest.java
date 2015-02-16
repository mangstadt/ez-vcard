package ezvcard.property;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardVersion;
import ezvcard.util.TelUri;

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
public class RelatedTest {
	private final String text = "Edna Smith";
	private final String uri = "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af";

	@Test
	public void validate() {
		Related empty = new Related();
		assertValidate(empty).versions(VCardVersion.V2_1).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V3_0).run(2, 8);
		assertValidate(empty).versions(VCardVersion.V4_0).run(8);

		Related withText = new Related();
		withText.setText(text);
		assertValidate(withText).versions(VCardVersion.V2_1).run(2);
		assertValidate(withText).versions(VCardVersion.V3_0).run(2);
		assertValidate(withText).versions(VCardVersion.V4_0).run();

		Related withUri = new Related();
		withUri.setUri(uri);
		assertValidate(withUri).versions(VCardVersion.V2_1).run(2);
		assertValidate(withUri).versions(VCardVersion.V3_0).run(2);
		assertValidate(withUri).versions(VCardVersion.V4_0).run();
	}

	@Test
	public void setUri() {
		Related t = new Related();
		t.setText(text);
		t.setUri(uri);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
	}

	@Test
	public void email() {
		Related t = Related.email("john.doe@example.com");
		assertNull(t.getText());
		assertEquals("mailto:john.doe@example.com", t.getUri());
	}

	@Test
	public void im() {
		Related t = Related.im("aim", "john.doe");
		assertNull(t.getText());
		assertEquals("aim:john.doe", t.getUri());
	}

	@Test
	public void telephone() {
		Related t = Related.telephone(new TelUri.Builder("+1-555-555-5555").build());
		assertNull(t.getText());
		assertEquals("tel:+1-555-555-5555", t.getUri());
	}

	@Test
	public void setText() {
		Related t = new Related();
		t.setUri(uri);
		t.setText(text);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
	}
}
