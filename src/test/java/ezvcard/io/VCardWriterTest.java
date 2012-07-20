package ezvcard.io;

import java.io.OutputStreamWriter;

import org.junit.Test;

import ezvcard.VCard;
import ezvcard.types.FormattedNameType;
import ezvcard.types.PhotoType;
import ezvcard.types.StructuredNameType;

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
public class VCardWriterTest {
	@Test
	public void test() throws Exception {
		VCard vcard = new VCard();

		StructuredNameType n = new StructuredNameType();
		n.setGiven("Michael");
		n.setFamily("Angstadt");
		vcard.setStructuredName(n);

		FormattedNameType fn = new FormattedNameType("Michael Angstadt");
		vcard.setFormattedName(fn);

		PhotoType photo = new PhotoType();
		photo.setUrl("http://example.com/image.jpg");
		vcard.getPhotos().add(photo);

		VCardWriter vcw = new VCardWriter(new OutputStreamWriter(System.out));
		vcw.write(vcard);
	}
}
