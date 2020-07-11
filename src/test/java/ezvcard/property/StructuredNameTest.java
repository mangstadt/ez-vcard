package ezvcard.property;

import static ezvcard.VCardVersion.V2_1;
import static ezvcard.VCardVersion.V3_0;
import static ezvcard.VCardVersion.V4_0;
import static ezvcard.property.PropertySensei.assertCopy;
import static ezvcard.property.PropertySensei.assertValidate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class StructuredNameTest {
	@Test
	public void validate() {
		//zero values
		assertValidate(new StructuredName()).versions().run();

		//single values
		List<StructuredName> properties = new ArrayList<StructuredName>();
		{
			StructuredName property = new StructuredName();
			property.getAdditionalNames().add("one");
			properties.add(property);

			property = new StructuredName();
			property.getPrefixes().add("one");
			properties.add(property);

			property = new StructuredName();
			property.getSuffixes().add("one");
			properties.add(property);
		}
		for (StructuredName property : properties) {
			assertValidate(property).run();
		}

		//multiple values
		properties = new ArrayList<StructuredName>();
		{
			StructuredName property = new StructuredName();
			property.getAdditionalNames().addAll(Arrays.asList("one", "two"));
			properties.add(property);

			property = new StructuredName();
			property.getPrefixes().addAll(Arrays.asList("one", "two"));
			properties.add(property);

			property = new StructuredName();
			property.getSuffixes().addAll(Arrays.asList("one", "two"));
			properties.add(property);
		}
		for (StructuredName property : properties) {
			assertValidate(property).versions(V2_1).run(34);
			assertValidate(property).versions(V3_0, V4_0).run();
		}
	}

	@Test
	public void copy() {
		StructuredName original = new StructuredName();
		original.getAdditionalNames().add("one");
		original.getPrefixes().add("two");
		original.getSuffixes().add("three");

		//@formatter:off
		assertCopy(original)
		.notSame("getAdditionalNames")
		.notSame("getPrefixes")
		.notSame("getSuffixes");
		//@formatter:on
	}
}
