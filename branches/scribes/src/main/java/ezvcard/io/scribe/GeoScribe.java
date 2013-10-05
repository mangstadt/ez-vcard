package ezvcard.io.scribe;

import java.util.List;

import ezvcard.VCardDataType;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.io.CannotParseException;
import ezvcard.io.json.JCardValue;
import ezvcard.io.xml.XCardElement;
import ezvcard.types.GeoType;
import ezvcard.util.GeoUri;
import ezvcard.util.HCardElement;
import ezvcard.util.VCardFloatFormatter;

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
 */

/**
 * Marshals {@link GeoType} properties.
 * @author Michael Angstadt
 */
public class GeoScribe extends VCardPropertyScribe<GeoType> {
	public GeoScribe() {
		super(GeoType.class, "GEO");
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
	protected String _writeText(GeoType property, VCardVersion version) {
		return write(property, version);
	}

	@Override
	protected GeoType _parseText(String value, VCardDataType dataType, VCardVersion version, VCardSubTypes parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value, version, warnings);
	}

	@Override
	protected void _writeXml(GeoType property, XCardElement parent) {
		parent.append(VCardDataType.URI, write(property, parent.version()));
	}

	@Override
	protected GeoType _parseXml(XCardElement element, VCardSubTypes parameters, List<String> warnings) {
		String value = element.first(VCardDataType.URI);
		if (value != null) {
			return parse(value, element.version(), warnings);
		}

		throw missingXmlElements(VCardDataType.URI);
	}

	@Override
	protected GeoType _parseHtml(HCardElement element, List<String> warnings) {
		String latitudeStr = element.firstValue("latitude");
		if (latitudeStr == null) {
			throw new CannotParseException("Latitude missing.");
		}

		Double latitude;
		try {
			latitude = Double.parseDouble(latitudeStr);
		} catch (NumberFormatException e) {
			throw new CannotParseException("Could not parse latitude: " + latitudeStr);
		}

		String longitudeStr = element.firstValue("longitude");
		if (longitudeStr == null) {
			throw new CannotParseException("Longitude missing.");
		}

		Double longitude;
		try {
			longitude = Double.parseDouble(longitudeStr);
		} catch (NumberFormatException e) {
			throw new CannotParseException("Could not parse longitude: " + longitudeStr);
		}

		return new GeoType(latitude, longitude);
	}

	@Override
	protected JCardValue _writeJson(GeoType property) {
		return JCardValue.single(write(property, VCardVersion.V4_0));
	}

	@Override
	protected GeoType _parseJson(JCardValue value, VCardDataType dataType, VCardSubTypes parameters, List<String> warnings) {
		return parse(value.asSingle(), VCardVersion.V4_0, warnings);
	}

	private GeoType parse(String value, VCardVersion version, List<String> warnings) {
		if (value == null || value.length() == 0) {
			return new GeoType(null);
		}

		switch (version) {
		case V2_1:
		case V3_0:
			SemiStructuredIterator it = semistructured(value);
			String latitudeStr = it.next();
			String longitudeStr = it.next();
			if (latitudeStr == null || longitudeStr == null) {
				throw new CannotParseException("Incorrect data format.  Value must contain a latitude and longitude, separated by a semi-colon.");
			}

			Double latitude;
			try {
				latitude = Double.valueOf(latitudeStr);
			} catch (NumberFormatException e) {
				throw new CannotParseException("Could not parse latitude: " + latitudeStr);
			}

			Double longitude;
			try {
				longitude = Double.valueOf(longitudeStr);
			} catch (NumberFormatException e) {
				throw new CannotParseException("Could not parse longtude: " + longitudeStr);
			}

			return new GeoType(latitude, longitude);
		case V4_0:
			try {
				return new GeoType(GeoUri.parse(value));
			} catch (IllegalArgumentException e) {
				throw new CannotParseException("Invalid geo URI.");
			}
		}
		return null;
	}

	private String write(GeoType property, VCardVersion version) {
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
