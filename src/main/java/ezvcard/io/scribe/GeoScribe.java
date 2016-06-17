package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.html.HCardElement;
import ezvcard.io.json.JCardValue;
import ezvcard.io.text.WriteContext;
import ezvcard.io.xml.XCardElement;
import ezvcard.parameter.VCardParameters;
import ezvcard.property.Geo;
import ezvcard.util.GeoUri;
import ezvcard.util.VCardFloatFormatter;

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
 */

/**
 * Marshals {@link Geo} properties.
 * @author Michael Angstadt
 */
public class GeoScribe extends VCardPropertyScribe<Geo> {
	public GeoScribe() {
		super(Geo.class, "GEO");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		switch (version) {
		case V2_1:
		case V3_0:
			return null;
		case V4_0:
			return VCardDataType.URI;
		}
		return null;
	}

	@Override
	protected String _writeText(Geo property, WriteContext context) {
		return write(property, context.getVersion());
	}

	@Override
	protected Geo _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value, version);
	}

	@Override
	protected void _writeXml(Geo property, XCardElement parent) {
		parent.append(VCardDataType.URI, write(property, parent.version()));
	}

	@Override
	protected Geo _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		String value = element.first(VCardDataType.URI);
		if (value != null) {
			return parse(value, element.version());
		}

		throw missingXmlElements(VCardDataType.URI);
	}

	@Override
	protected Geo _parseHtml(HCardElement element, List<String> warnings) {
		String latitudeStr = element.firstValue("latitude");
		if (latitudeStr == null) {
			throw new CannotParseException(7);
		}

		Double latitude;
		try {
			latitude = Double.parseDouble(latitudeStr);
		} catch (NumberFormatException e) {
			throw new CannotParseException(8, latitudeStr);
		}

		String longitudeStr = element.firstValue("longitude");
		if (longitudeStr == null) {
			throw new CannotParseException(9);
		}

		Double longitude;
		try {
			longitude = Double.parseDouble(longitudeStr);
		} catch (NumberFormatException e) {
			throw new CannotParseException(10, longitudeStr);
		}

		return new Geo(latitude, longitude);
	}

	@Override
	protected JCardValue _writeJson(Geo property) {
		return JCardValue.single(write(property, VCardVersion.V4_0));
	}

	@Override
	protected Geo _parseJson(JCardValue value, VCardDataType dataType, VCardParameters parameters, List<String> warnings) {
		return parse(value.asSingle(), VCardVersion.V4_0);
	}

	private Geo parse(String value, VCardVersion version) {
		if (value == null || value.length() == 0) {
			return new Geo((GeoUri) null);
		}

		switch (version) {
		case V2_1:
		case V3_0:
			SemiStructuredIterator it = semistructured(value);
			String latitudeStr = it.next();
			String longitudeStr = it.next();
			if (latitudeStr == null || longitudeStr == null) {
				throw new CannotParseException(11);
			}

			Double latitude;
			try {
				latitude = Double.valueOf(latitudeStr);
			} catch (NumberFormatException e) {
				throw new CannotParseException(8, latitudeStr);
			}

			Double longitude;
			try {
				longitude = Double.valueOf(longitudeStr);
			} catch (NumberFormatException e) {
				throw new CannotParseException(10, longitudeStr);
			}

			return new Geo(latitude, longitude);
		case V4_0:
			try {
				return new Geo(GeoUri.parse(value));
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(12);
			}
		}
		return null;
	}

	private String write(Geo property, VCardVersion version) {
		if (property.getGeoUri() == null) {
			return "";
		}

		switch (version) {
		case V2_1:
		case V3_0:
			VCardFloatFormatter formatter = new VCardFloatFormatter(6);
			return structured(formatter.format(property.getLatitude()), formatter.format(property.getLongitude()));
		case V4_0:
			return property.getGeoUri().toString(6);
		}
		return null;
	}
}
