package ezvcard.io.scribe;

import static ezvcard.util.StringUtils.join;

import java.util.List;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueBuilder;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.StructuredValueBuilder;
import com.github.mangstadt.vinnie.io.VObjectPropertyValues.StructuredValueIterator;

import ezvcard.VCard;
import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.ParseContext;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Address;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * Marshals {@link Address} properties.
 * @author Michael Angstadt
 */
public class AddressScribe extends VCardPropertyScribe<Address> {
	public AddressScribe() {
		super(Address.class, "ADR");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected void _prepareParameters(Address property, VCardParameters copy, VCardVersion version, VCard vcard) {
		handlePrefParam(property, copy, version, vcard);

		if (version == VCardVersion.V2_1 || version == VCardVersion.V3_0) {
			/*
			 * Remove the LABEL parameter. By the time this line of code is
			 * reached, VCardWriter will have created a LABEL property from this
			 * property's LABEL parameter
			 */
			copy.setLabel(null);
		}
	}

	@Override
	protected String _writeText(Address property, WriteContext context) {
		/*
		 * StructuredValueBuilder cannot be used with 2.1 because it escapes
		 * comma characters. For example, if someone's street address is
		 * "Foo,bar Lane", the comma character must NOT be escaped when written
		 * to a 2.1 vCard.
		 * 
		 * The reason commas are not escaped in 2.1 is because 2.1 does not
		 * allow multi-valued components like 3.0 and 4.0 do (for example,
		 * multiple street addresses).
		 * 
		 * If an Address object has multi-valued components, and it is being
		 * written to a 2.1 vCard, then ez-vcard will comma-delimit them to
		 * prevent data loss. But this is not part of the 2.1 syntax.
		 */
		if (context.getVersion() == VCardVersion.V2_1) {
			SemiStructuredValueBuilder builder = new SemiStructuredValueBuilder();
			builder.append(join(property.getPoBoxes(), ","));
			builder.append(join(property.getExtendedAddresses(), ","));
			builder.append(join(property.getStreetAddresses(), ","));
			builder.append(join(property.getLocalities(), ","));
			builder.append(join(property.getRegions(), ","));
			builder.append(join(property.getPostalCodes(), ","));
			builder.append(join(property.getCountries(), ","));
			return builder.build(false, context.isIncludeTrailingSemicolons());
		} else {
			StructuredValueBuilder builder = new StructuredValueBuilder();
			builder.append(property.getPoBoxes());
			builder.append(property.getExtendedAddresses());
			builder.append(property.getStreetAddresses());
			builder.append(property.getLocalities());
			builder.append(property.getRegions());
			builder.append(property.getPostalCodes());
			builder.append(property.getCountries());
			return builder.build(context.isIncludeTrailingSemicolons());
		}
	}

	@Override
	protected Address _parseText(String value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		if (context.getVersion() == VCardVersion.V2_1) {
			/*
			 * 2.1 does not recognize multi-valued components.
			 */
			SemiStructuredValueIterator it = new SemiStructuredValueIterator(value);
			return parseSemiStructuredValue(it);
		} else {
			StructuredValueIterator it = new StructuredValueIterator(value);
			return parseStructuredValue(it);
		}
	}

	@Override
	protected void _writeXml(Address property, XCardElement parent) {
		parent.append("pobox", property.getPoBoxes()); //Note: The XML element must always be added, even if the value is null
		parent.append("ext", property.getExtendedAddresses());
		parent.append("street", property.getStreetAddresses());
		parent.append("locality", property.getLocalities());
		parent.append("region", property.getRegions());
		parent.append("code", property.getPostalCodes());
		parent.append("country", property.getCountries());
	}

	@Override
	protected Address _parseXml(XCardElement element, VCardParameters parameters, ParseContext context) {
		Address property = new Address();
		property.getPoBoxes().addAll(sanitizeXml(element, "pobox"));
		property.getExtendedAddresses().addAll(sanitizeXml(element, "ext"));
		property.getStreetAddresses().addAll(sanitizeXml(element, "street"));
		property.getLocalities().addAll(sanitizeXml(element, "locality"));
		property.getRegions().addAll(sanitizeXml(element, "region"));
		property.getPostalCodes().addAll(sanitizeXml(element, "code"));
		property.getCountries().addAll(sanitizeXml(element, "country"));
		return property;
	}

	private List<String> sanitizeXml(XCardElement element, String name) {
		return element.all(name);
	}

	@Override
	protected Address _parseHtml(HCardElement element, ParseContext context) {
		Address property = new Address();
		property.getPoBoxes().addAll(element.allValues("post-office-box"));
		property.getExtendedAddresses().addAll(element.allValues("extended-address"));
		property.getStreetAddresses().addAll(element.allValues("street-address"));
		property.getLocalities().addAll(element.allValues("locality"));
		property.getRegions().addAll(element.allValues("region"));
		property.getPostalCodes().addAll(element.allValues("postal-code"));
		property.getCountries().addAll(element.allValues("country-name"));

		List<String> types = element.types();
		property.getParameters().putAll(VCardParameters.TYPE, types);

		return property;
	}

	@Override
	protected JCardValue _writeJson(Address property) {
		//@formatter:off
		return JCardValue.structured(
			property.getPoBoxes(),
			property.getExtendedAddresses(),
			property.getStreetAddresses(),
			property.getLocalities(),
			property.getRegions(),
			property.getPostalCodes(),
			property.getCountries()
		);
		//@formatter:on
	}

	@Override
	protected Address _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, ParseContext context) {
		StructuredValueIterator it = new StructuredValueIterator(value.asStructured());
		return parseStructuredValue(it);
	}

	private static Address parseStructuredValue(StructuredValueIterator it) {
		Address property = new Address();

		property.getPoBoxes().addAll(it.nextComponent());
		property.getExtendedAddresses().addAll(it.nextComponent());
		property.getStreetAddresses().addAll(it.nextComponent());
		property.getLocalities().addAll(it.nextComponent());
		property.getRegions().addAll(it.nextComponent());
		property.getPostalCodes().addAll(it.nextComponent());
		property.getCountries().addAll(it.nextComponent());

		return property;
	}

	private static Address parseSemiStructuredValue(SemiStructuredValueIterator it) {
		Address property = new Address();

		String next = it.next();
		if (next != null) {
			property.getPoBoxes().add(next);
		}

		next = it.next();
		if (next != null) {
			property.getExtendedAddresses().add(next);
		}

		next = it.next();
		if (next != null) {
			property.getStreetAddresses().add(next);
		}

		next = it.next();
		if (next != null) {
			property.getLocalities().add(next);
		}

		next = it.next();
		if (next != null) {
			property.getRegions().add(next);
		}

		next = it.next();
		if (next != null) {
			property.getPostalCodes().add(next);
		}

		next = it.next();
		if (next != null) {
			property.getCountries().add(next);
		}

		return property;
	}
}
