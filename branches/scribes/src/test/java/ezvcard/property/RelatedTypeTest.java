package ezvcard.property;

import static ezvcard.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ezvcard.VCardVersion;

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
public class RelatedTypeTest {
	private final String text = "Edna Smith";
	private final String uri = "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af";

	@Test
	public void validate() {
		RelatedType empty = new RelatedType();
		assertValidate(empty).versions(VCardVersion.V2_1).run(2);
		assertValidate(empty).versions(VCardVersion.V3_0).run(2);
		assertValidate(empty).versions(VCardVersion.V4_0).run(1);

		RelatedType withText = new RelatedType();
		withText.setText(text);
		assertValidate(withText).versions(VCardVersion.V2_1).run(1);
		assertValidate(withText).versions(VCardVersion.V3_0).run(1);
		assertValidate(withText).versions(VCardVersion.V4_0).run(0);

		RelatedType withUri = new RelatedType();
		withUri.setUri(uri);
		assertValidate(withUri).versions(VCardVersion.V2_1).run(1);
		assertValidate(withUri).versions(VCardVersion.V3_0).run(1);
		assertValidate(withUri).versions(VCardVersion.V4_0).run(0);
	}

	@Test
	public void setUri() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUri(uri);

		assertNull(t.getText());
		assertEquals(uri, t.getUri());
	}

	@Test
	public void setUriEmail() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUriEmail("john.doe@example.com");

		assertNull(t.getText());
		assertEquals("mailto:john.doe@example.com", t.getUri());
	}

	@Test
	public void setUriIM() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUriIM("aim", "john.doe");

		assertNull(t.getText());
		assertEquals("aim:john.doe", t.getUri());
	}

	@Test
	public void setUriTelephone() {
		RelatedType t = new RelatedType();
		t.setText(text);
		t.setUriTelephone("555-555-5555");

		assertNull(t.getText());
		assertEquals("tel:555-555-5555", t.getUri());
	}

	@Test
	public void setText() {
		RelatedType t = new RelatedType();
		t.setUri(uri);
		t.setText(text);

		assertEquals(text, t.getText());
		assertNull(t.getUri());
	}
}
