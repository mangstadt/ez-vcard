package ezvcard.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

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
public class GeoUriTest {
	private final Locale defaultLocale = Locale.getDefault();

	@After
	public void after() {
		Locale.setDefault(defaultLocale);
	}

	@Test
	public void buildNumberFormat() {
		NumberFormat nf = GeoUri.buildNumberFormat(0);
		assertEquals("12", nf.format(12.388));

		nf = GeoUri.buildNumberFormat(-1);
		assertEquals("12", nf.format(12.388));

		nf = GeoUri.buildNumberFormat(1);
		assertEquals("12.4", nf.format(12.388));

		nf = GeoUri.buildNumberFormat(2);
		assertEquals("12.39", nf.format(12.388));

		nf = GeoUri.buildNumberFormat(3);
		assertEquals("12.388", nf.format(12.388));
	}

	@Test
	public void buildNumberFormat_other_locale() {
		//Germany uses "," as the decimal separator, but "." should still be used in a geo URI
		Locale.setDefault(Locale.GERMANY);

		NumberFormat nf = GeoUri.buildNumberFormat(2);
		assertEquals("-12.39", nf.format(-12.388));
	}

	@Test
	public void parse_all() {
		GeoUri uri = new GeoUri("geo:12.34,56.78,-21.43;crs=wgs84;u=12;param=value");
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
		GeoUri uri = new GeoUri("geo:12.34,56.78,-21.43;crs=wgs84;u=12");
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
		GeoUri uri = new GeoUri("geo:12.34,56.78,-21.43;crs=wgs84");
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
		GeoUri uri = new GeoUri("geo:12.34,56.78,-21.43;u=12");
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
		GeoUri uri = new GeoUri("geo:12.34,56.78,-21.43");
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
		GeoUri uri = new GeoUri("geo:12.34,56.78");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertNull(uri.getCoordC());
		assertNull(uri.getCrs());
		assertNull(uri.getUncertainty());
		assertNull(uri.getParameter("param"));
		Map<String, String> params = new LinkedHashMap<String, String>();
		assertEquals(params, uri.getParameters());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_no_params_or_u_or_crs_or_coordsC_or_coordB() {
		new GeoUri("geo:12.34");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_no_params_or_u_or_crs_or_coordsC_or_coordB_or_coordA() {
		new GeoUri("geo:");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_not_geo_uri() {
		new GeoUri("http://www.ietf.org");
	}

	@Test
	public void parse_decode_special_chars_in_param_value() {
		GeoUri uri = new GeoUri("geo:12.34,56.78;param=with%20%3d%20special%20&%20chars");
		assertEquals(12.34, uri.getCoordA(), 0.01);
		assertEquals(56.78, uri.getCoordB(), 0.01);
		assertEquals("with = special & chars", uri.getParameter("param"));
	}

	@Test
	public void setCrs() {
		GeoUri uri = new GeoUri();
		uri.setCrs("123-valid");
		assertEquals("123-valid", uri.getCrs());
	}

	@Test(expected = IllegalArgumentException.class)
	public void setCrs_validate_chars() {
		GeoUri uri = new GeoUri();
		uri.setCrs("!not-valid!");
	}

	@Test
	public void setUncertainty() {
		GeoUri uri = new GeoUri();
		uri.setUncertainty(12.0);
		assertEquals(12.0, uri.getUncertainty().doubleValue(), 0.01);
	}

	@Test
	public void getParameter() {
		GeoUri uri = new GeoUri();
		uri.addParameter("param", "value");
		assertEquals("value", uri.getParameter("param"));
	}

	@Test
	public void getParameter_non_existant_param() {
		GeoUri uri = new GeoUri();
		assertNull(uri.getParameter("non-existant"));
	}

	@Test
	public void addParameter() {
		getParameter();
	}

	@Test(expected = IllegalArgumentException.class)
	public void addParameter_validate_param_name_chars() {
		GeoUri uri = new GeoUri();
		uri.addParameter("!not-valid!", "the value");
	}

	@Test
	public void addParameter_preserve_insertion_order() {
		GeoUri uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(56.78);
		for (int i = 9; i >= 0; i--) {
			uri.addParameter(i + "p", i + "v");
		}

		int i = 9;
		for (Map.Entry<String, String> param : uri.getParameters().entrySet()) {
			assertEquals(i + "p", param.getKey());
			assertEquals(i + "v", param.getValue());
			i--;
		}

		assertEquals("geo:12.34,56.78;9p=9v;8p=8v;7p=7v;6p=6v;5p=5v;4p=4v;3p=3v;2p=2v;1p=1v;0p=0v", uri.toString());
	}

	@Test
	public void getParameters() {
		GeoUri uri = new GeoUri();
		uri.addParameter("param", "value");
		uri.addParameter("param2", "value2");

		Map<String, String> parameters = uri.getParameters();
		assertEquals(2, parameters.size());
		assertEquals("value", parameters.get("param"));
		assertEquals("value2", parameters.get("param2"));
	}

	@Test
	public void toUri() {
		GeoUri uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(45.67);

		URI theUri = uri.toUri();
		assertEquals("geo", theUri.getScheme());
		assertEquals("12.34,45.67", theUri.getRawSchemeSpecificPart());
	}

	@Test
	public void toString_() {
		GeoUri uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(45.67);
		uri.setCoordC(-21.43);
		uri.setCrs("theCrs");
		uri.setUncertainty(12.0);
		uri.addParameter("param", "value");
		uri.addParameter("param2", "value2");
		assertEquals("geo:12.34,45.67,-21.43;crs=theCrs;u=12;param=value;param2=value2", uri.toString());
	}

	@Test
	public void toString_special_chars_in_param_value() {
		GeoUri uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(45.67);
		uri.addParameter("param", "with = special & chars");
		assertEquals("geo:12.34,45.67;param=with%20%3d%20special%20&%20chars", uri.toString());
	}

	@Test
	public void isValid() {
		GeoUri uri = new GeoUri();
		assertFalse(uri.isValid());

		uri = new GeoUri();
		uri.setCoordA(12.34);
		assertFalse(uri.isValid());

		uri = new GeoUri();
		uri.setCoordB(12.34);
		assertFalse(uri.isValid());

		uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(56.78);
		assertTrue(uri.isValid());
	}

	@Test
	public void toString_missing_required_fields() {
		GeoUri uri = new GeoUri();
		assertEquals("geo:,", uri.toString());

		uri = new GeoUri();
		uri.setCoordA(12.34);
		assertEquals("geo:12.34,", uri.toString());

		uri = new GeoUri();
		uri.setCoordB(45.67);
		assertEquals("geo:,45.67", uri.toString());
	}

	@Test
	public void toString_do_not_display_wgs84() {
		GeoUri uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(45.67);

		uri.setCrs("wgs84");
		assertEquals("geo:12.34,45.67", uri.toString());

		//case-insensitive
		uri.setCrs("WgS84");
		assertEquals("geo:12.34,45.67", uri.toString());
	}

	@Test
	public void toString_precision() {
		GeoUri uri = new GeoUri();
		uri.setCoordA(12.3488888888);
		uri.setCoordB(45.6711111111);
		assertEquals("geo:12.348889,45.671111", uri.toString());
		assertEquals("geo:12.3489,45.6711", uri.toString(4));
		assertEquals("geo:12,46", uri.toString(0));
		assertEquals("geo:12,46", uri.toString(-1));
	}

	@Test
	public void toString_other_locale() {
		//Germany uses "," as the decimal separator, but "." should still be used in a geo URI
		Locale.setDefault(Locale.GERMANY);

		GeoUri uri = new GeoUri();
		uri.setCoordA(12.34);
		uri.setCoordB(45.67);
		uri.setCoordC(-21.43);
		uri.setUncertainty(12.0);
		assertEquals("geo:12.34,45.67,-21.43;u=12", uri.toString());
	}

	@Test
	public void equals() {
		//TODO implement
	}
}
