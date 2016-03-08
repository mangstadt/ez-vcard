package ezvcard.util;

import static ezvcard.util.TestUtils.assertEqualsAndHash;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.ClassRule;
import org.junit.Test;

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
public class GeoUriTest {
	//Germany uses "," as the decimal separator, but "." should still be used in a geo URI
	@ClassRule
	public static final DefaultLocaleRule localeRule = new DefaultLocaleRule(Locale.GERMANY);

	@Test
	public void parse_all() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78,-21.43;crs=wgs84;u=12;param=value");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals(-21.43, uri.getCoordC().doubleValue(), 0.01);
		assertEquals("wgs84", uri.getCrs());
		assertEquals(12, uri.getUncertainty().doubleValue(), 0.01);
		assertEquals("value", uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("param", "value");
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_no_params() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78,-21.43;crs=wgs84;u=12");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals(-21.43, uri.getCoordC().doubleValue(), 0.01);
		assertEquals("wgs84", uri.getCrs());
		assertEquals(12, uri.getUncertainty().doubleValue(), 0.01);
		assertNull(uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_no_params_or_u() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78,-21.43;crs=wgs84");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals(-21.43, uri.getCoordC().doubleValue(), 0.01);
		assertEquals("wgs84", uri.getCrs());
		assertNull(uri.getUncertainty());
		assertNull(uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_no_params_or_crs() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78,-21.43;u=12");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals(-21.43, uri.getCoordC().doubleValue(), 0.01);
		assertNull(uri.getCrs());
		assertEquals(12, uri.getUncertainty().doubleValue(), 0.01);
		assertNull(uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_no_params_or_u_or_crs() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78,-21.43");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals(-21.43, uri.getCoordC().doubleValue(), 0.01);
		assertNull(uri.getCrs());
		assertNull(uri.getUncertainty());
		assertNull(uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_no_params_or_u_or_crs_or_coordC() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertNull(uri.getCoordC());
		assertNull(uri.getCrs());
		assertNull(uri.getUncertainty());
		assertNull(uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		assertEquals(params, uri.getParameters());
	}

	@Test
	public void parse_invalid_uncertainty() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78;u=invalid");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertNull(uri.getCoordC());
		assertNull(uri.getCrs());
		assertNull(uri.getUncertainty());

		Map<String, String> expectedParams = new HashMap<String, String>();
		expectedParams.put("u", "invalid");
		assertEquals(expectedParams, uri.getParameters());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_no_params_or_u_or_crs_or_coordsC_or_coordB() {
		GeoUri.parse("geo:12.34");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_no_params_or_u_or_crs_or_coordsC_or_coordB_or_coordA() {
		GeoUri.parse("geo:");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_not_geo_uri() {
		GeoUri.parse("http://www.ietf.org");
	}

	@Test
	public void parse_decode_special_chars_in_param_value() {
		GeoUri uri = GeoUri.parse("geo:12.34,56.78;param=with%20%3d%20special%20&%20chars");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals("with = special & chars", uri.getParameter("param"));
	}

	@Test
	public void builder_crs() {
		GeoUri uri = new GeoUri.Builder(12.34, 56.78).crs("123-valid").build();
		assertEquals("123-valid", uri.getCrs());
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_crs_validate_chars() {
		new GeoUri.Builder(12.34, 56.78).crs("!not-valid!");
	}

	@Test
	public void builder_uncertainty() {
		GeoUri uri = new GeoUri.Builder(12.34, 56.78).uncertainty(12.0).build();
		assertEquals(12.0, uri.getUncertainty().doubleValue(), 0.01);
	}

	@Test
	public void builder_parameter() {
		GeoUri uri = new GeoUri.Builder(12.34, 56.78).parameter("one", "1").parameter("two", "2").parameter("one", null).build();
		assertNull(uri.getParameter("one")); //parameter was removed
		assertEquals("2", uri.getParameter("two"));
		assertNull(uri.getParameter("three"));

		Map<String, String> expected = new HashMap<String, String>();
		expected.put("two", "2");
		assertEquals(expected, uri.getParameters());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getParameters_unmodifiable() {
		GeoUri uri = new GeoUri.Builder(12.34, 56.78).build();
		uri.getParameters().put("one", "1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void builder_parameter_validate_param_name_chars() {
		new GeoUri.Builder(12.34, 56.78).parameter("!not-valid!", "value");
	}

	@Test
	public void builder_parameter_preserve_insertion_order() {
		GeoUri.Builder builder = new GeoUri.Builder(12.34, 56.78);
		for (int i = 9; i >= 0; i--) {
			builder.parameter(i + "p", i + "v");
		}

		GeoUri uri = builder.build();
		int i = 9;
		for (Map.Entry<String, String> param : uri.getParameters().entrySet()) {
			assertEquals(i + "p", param.getKey());
			assertEquals(i + "v", param.getValue());
			i--;
		}

		assertEquals("geo:12.34,56.78;9p=9v;8p=8v;7p=7v;6p=6v;5p=5v;4p=4v;3p=3v;2p=2v;1p=1v;0p=0v", uri.toString());
	}

	@Test
	public void builder_copy_constructor() {
		GeoUri orig = new GeoUri.Builder(12.34, 56.78).coordC(90.12).crs("crs").parameter("param", "value").uncertainty(0.01).build();
		GeoUri copy = new GeoUri.Builder(orig).build();

		assertEquals(orig.getCoordA(), copy.getCoordA());
		assertEquals(orig.getCoordB(), copy.getCoordB());
		assertEquals(orig.getCoordC(), copy.getCoordC());
		assertEquals(orig.getCrs(), copy.getCrs());
		assertEquals(orig.getParameters(), copy.getParameters());
		assertEquals(orig.getUncertainty(), copy.getUncertainty());
	}

	@Test
	public void toUri() {
		GeoUri uri = new GeoUri.Builder(12.34, 45.67).build();

		URI theUri = uri.toUri();
		assertEquals("geo", theUri.getScheme());
		assertEquals("12.34,45.67", theUri.getRawSchemeSpecificPart());
	}

	@Test
	public void toString_() {
		GeoUri uri = new GeoUri.Builder(12.34, 45.67).coordC(-21.43).crs("theCrs").uncertainty(12.0).parameter("param", "value").parameter("param2", "value2").build();
		assertEquals("geo:12.34,45.67,-21.43;crs=theCrs;u=12.0;param=value;param2=value2", uri.toString());
	}

	@Test
	public void toString_special_chars_in_param_value() {
		GeoUri uri = new GeoUri.Builder(12.34, 45.67).parameter("param", "with = special & chars " + (char) 128).build();
		assertEquals("geo:12.34,45.67;param=with%20%3d%20special%20&%20chars%20%80", uri.toString());
	}

	@Test
	public void toString_missing_required_fields() {
		GeoUri uri = new GeoUri.Builder(null, null).build();
		assertEquals("geo:0.0,0.0", uri.toString());

		uri = new GeoUri.Builder(12.34, null).build();
		assertEquals("geo:12.34,0.0", uri.toString());

		uri = new GeoUri.Builder(null, 45.67).build();
		assertEquals("geo:0.0,45.67", uri.toString());
	}

	@Test
	public void toString_do_not_display_wgs84() {
		GeoUri uri = new GeoUri.Builder(12.34, 45.67).crs("wgs84").build();
		assertEquals("geo:12.34,45.67", uri.toString());

		//case-insensitive
		uri = new GeoUri.Builder(12.34, 45.67).crs("WgS84").build();
		assertEquals("geo:12.34,45.67", uri.toString());
	}

	@Test
	public void toString_precision() {
		GeoUri uri = new GeoUri.Builder(12.3488888888, 45.6711111111).build();
		assertEquals("geo:12.348889,45.671111", uri.toString());
		assertEquals("geo:12.3489,45.6711", uri.toString(4));
		assertEquals("geo:12,46", uri.toString(0));
		assertEquals("geo:12,46", uri.toString(-1));
	}

	@Test
	public void equals_contract() {
		EqualsVerifier.forClass(GeoUri.class).usingGetClass().verify();
	}

	@Test
	public void equals_ignore_case() {
		GeoUri one = new GeoUri.Builder(1.0, 2.0).crs("crs").parameter("name", "value").build();
		GeoUri two = new GeoUri.Builder(1.0, 2.0).crs("CRS").parameter("NAME", "VALUE").build();
		assertEqualsAndHash(one, two);
	}
}
