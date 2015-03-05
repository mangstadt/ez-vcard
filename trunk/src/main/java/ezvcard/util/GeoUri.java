package ezvcard.util;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * Represents a URI for encoding a geographical position.
 * </p>
 * <p>
 * Example geo URI: {@code geo:40.714623,-74.006605}
 * </p>
 * <p>
 * This class is immutable. Use the {@link Builder} object to construct a new
 * instance, or the {@link #parse} method to parse a geo URI string.
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * GeoUri uri = new GeoUri.Builder(40.714623, -74.006605).coordC(1.1).build();
 * GeoUri uri = GeoUri.parse(&quot;geo:40.714623,-74.006605,1.1&quot;);
 * GeoUri copy = new GeoUri.Builder(original).coordC(2.1).build();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5870">RFC 5870</a>
 */
public final class GeoUri {
	/**
	 * The coordinate reference system used by GPS (the default).
	 */
	public static final String CRS_WGS84 = "wgs84";

	/**
	 * The characters which are allowed to exist un-encoded inside of a
	 * parameter value.
	 */
	private static final boolean validParamValueChars[] = new boolean[128];
	static {
		for (int i = '0'; i <= '9'; i++) {
			validParamValueChars[i] = true;
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			validParamValueChars[i] = true;
		}
		for (int i = 'a'; i <= 'z'; i++) {
			validParamValueChars[i] = true;
		}
		for (char c : "!$&'()*+-.:[]_~".toCharArray()) {
			validParamValueChars[c] = true;
		}
	}

	/**
	 * Finds hex values in a parameter value.
	 */
	private static final Pattern hexPattern = Pattern.compile("(?i)%([0-9a-f]{2})");

	/**
	 * Validates parameter names.
	 */
	private static final Pattern labelTextPattern = Pattern.compile("(?i)^[-a-z0-9]+$");

	/**
	 * Parses geo URIs.
	 */
	private static final Pattern uriPattern = Pattern.compile("(?i)^geo:(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?)(,(-?\\d+(\\.\\d+)?))?(;(.*))?$");

	private static final String PARAM_CRS = "crs";
	private static final String PARAM_UNCERTAINTY = "u";

	private final Double coordA;
	private final Double coordB;
	private final Double coordC;
	private final String crs;
	private final Double uncertainty;
	private final Map<String, String> parameters;

	private GeoUri(Builder builder) {
		this.coordA = builder.coordA;
		this.coordB = builder.coordB;
		this.coordC = builder.coordC;
		this.crs = builder.crs;
		this.uncertainty = builder.uncertainty;
		this.parameters = Collections.unmodifiableMap(builder.parameters);
	}

	/**
	 * Parses a geo URI string.
	 * @param uri the URI string (e.g. "geo:40.714623,-74.006605")
	 * @return the parsed geo URI
	 * @throws IllegalArgumentException if the string is not a valid geo URI
	 */
	public static GeoUri parse(String uri) {
		Matcher m = uriPattern.matcher(uri);
		if (!m.find()) {
			throw new IllegalArgumentException("Invalid geo URI: " + uri);
		}

		Builder builder = new Builder(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(3)));

		String coordCStr = m.group(6);
		if (coordCStr != null) {
			builder.coordC = Double.valueOf(coordCStr);
		}

		String paramsStr = m.group(9);
		if (paramsStr != null) {
			String paramsArray[] = paramsStr.split(";");

			for (String param : paramsArray) {
				String paramSplit[] = param.split("=", 2);
				String paramName = paramSplit[0];
				String paramValue = (paramSplit.length > 1) ? decodeParamValue(paramSplit[1]) : "";

				if (PARAM_CRS.equalsIgnoreCase(paramName)) {
					builder.crs = paramValue;
					continue;
				}

				if (PARAM_UNCERTAINTY.equalsIgnoreCase(paramName)) {
					try {
						builder.uncertainty = Double.valueOf(paramValue);
						continue;
					} catch (NumberFormatException e) {
						//if it can't be parsed, then treat it as an ordinary parameter
					}
				}

				builder.parameters.put(paramName, paramValue);
			}
		}

		return builder.build();
	}

	/**
	 * Gets the first coordinate (latitude).
	 * @return the first coordinate or null if there is none
	 */
	public Double getCoordA() {
		return coordA;
	}

	/**
	 * Gets the second coordinate (longitude).
	 * @return the second coordinate or null if there is none
	 */
	public Double getCoordB() {
		return coordB;
	}

	/**
	 * Gets the third coordinate (altitude).
	 * @return the third coordinate or null if there is none
	 */
	public Double getCoordC() {
		return coordC;
	}

	/**
	 * Gets the coordinate reference system.
	 * @return the coordinate reference system or null if using the default
	 * (WGS-84)
	 */
	public String getCrs() {
		return crs;
	}

	/**
	 * Gets the uncertainty (how accurate the coordinates are).
	 * @return the uncertainty (in meters) or null if not set
	 */
	public Double getUncertainty() {
		return uncertainty;
	}

	/**
	 * Gets a parameter value.
	 * @param name the parameter name
	 * @return the parameter value or null if not found
	 */
	public String getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Gets all the parameters.
	 * @return all the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Creates a {@link URI} object from this geo URI.
	 * @return the {@link URI} object
	 */
	public URI toUri() {
		return URI.create(toString());
	}

	/**
	 * Converts this geo URI to its string representation.
	 * @return the geo URI's string representation
	 */
	@Override
	public String toString() {
		return toString(6);
	}

	/**
	 * Converts this geo URI to its string representation.
	 * @param decimals the number of decimals to display for floating point
	 * values
	 * @return the geo URI's string representation
	 */
	public String toString(int decimals) {
		VCardFloatFormatter formatter = new VCardFloatFormatter(decimals);
		StringBuilder sb = new StringBuilder("geo:");

		sb.append(formatter.format(coordA));
		sb.append(',');
		sb.append(formatter.format(coordB));

		if (coordC != null) {
			sb.append(',');
			sb.append(coordC);
		}

		//if the CRS is WGS-84, then it doesn't have to be displayed
		if (crs != null && !crs.equalsIgnoreCase(CRS_WGS84)) {
			writeParameter(PARAM_CRS, crs, sb);
		}

		if (uncertainty != null) {
			writeParameter(PARAM_UNCERTAINTY, formatter.format(uncertainty), sb);
		}

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			writeParameter(name, value, sb);
		}

		return sb.toString();
	}

	/**
	 * Writes a parameter to a string.
	 * @param name the parameter name
	 * @param value the parameter value
	 * @param sb the string to write to
	 */
	private void writeParameter(String name, String value, StringBuilder sb) {
		sb.append(';').append(name).append('=').append(encodeParamValue(value));
	}

	private static boolean isLabelText(String text) {
		return labelTextPattern.matcher(text).find();
	}

	private static String encodeParamValue(String value) {
		StringBuilder sb = null;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c < validParamValueChars.length && validParamValueChars[c]) {
				if (sb != null) {
					sb.append(c);
				}
			} else {
				if (sb == null) {
					sb = new StringBuilder(value.substring(0, i));
				}
				String hex = Integer.toString(c, 16);
				sb.append('%').append(hex);
			}
		}
		return (sb == null) ? value : sb.toString();
	}

	private static String decodeParamValue(String value) {
		Matcher m = hexPattern.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int hex = Integer.parseInt(m.group(1), 16);
			m.appendReplacement(sb, "" + (char) hex);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Builder class for {@link GeoUri}.
	 * @author Michael Angstadt
	 */
	public static class Builder {
		private Double coordA;
		private Double coordB;
		private Double coordC;
		private String crs;
		private Double uncertainty;
		private Map<String, String> parameters;

		/**
		 * Creates a new {@link GeoUri} builder.
		 * @param coordA the first coordinate (i.e. latitude)
		 * @param coordB the second coordinate (i.e. longitude)
		 */
		public Builder(Double coordA, Double coordB) {
			parameters = new LinkedHashMap<String, String>(0); //set initial size to 0 because parameters are rarely used
			coordA(coordA);
			coordB(coordB);
		}

		/**
		 * Creates a new {@link GeoUri} builder.
		 * @param original the {@link GeoUri} object to copy from
		 */
		public Builder(GeoUri original) {
			coordA(original.coordA);
			coordB(original.coordB);
			this.coordC = original.coordC;
			this.crs = original.crs;
			this.uncertainty = original.uncertainty;
			this.parameters = new LinkedHashMap<String, String>(original.parameters);
		}

		/**
		 * Sets the first coordinate (latitude).
		 * @param coordA the first coordinate
		 * @return this
		 */
		public Builder coordA(Double coordA) {
			this.coordA = (coordA == null) ? 0.0 : coordA;
			return this;
		}

		/**
		 * Sets the second coordinate (longitude).
		 * @param coordB the second coordinate
		 * @return this
		 */
		public Builder coordB(Double coordB) {
			this.coordB = (coordB == null) ? 0.0 : coordB;
			return this;
		}

		/**
		 * Sets the third coordinate (altitude).
		 * @param coordC the third coordinate or null to remove
		 * @return this
		 */
		public Builder coordC(Double coordC) {
			this.coordC = coordC;
			return this;
		}

		/**
		 * Sets the coordinate reference system.
		 * @param crs the coordinate reference system (can only contain letters,
		 * numbers, and hyphens) or null to use the default (WGS-84)
		 * @throws IllegalArgumentException if the CRS name contains invalid
		 * characters
		 * @return this
		 */
		public Builder crs(String crs) {
			if (crs != null && !isLabelText(crs)) {
				throw new IllegalArgumentException("CRS can only contain letters, numbers, and hypens.");
			}
			this.crs = crs;
			return this;
		}

		/**
		 * Sets the uncertainty (how accurate the coordinates are).
		 * @param uncertainty the uncertainty (in meters) or null to remove
		 * @return this
		 */
		public Builder uncertainty(Double uncertainty) {
			this.uncertainty = uncertainty;
			return this;
		}

		/**
		 * Adds a parameter.
		 * @param name the parameter name (can only contain letters, numbers,
		 * and hyphens)
		 * @param value the parameter value or null to remove the parameter
		 * @throws IllegalArgumentException if the parameter name contains
		 * invalid characters
		 * @return this
		 */
		public Builder parameter(String name, String value) {
			if (!isLabelText(name)) {
				throw new IllegalArgumentException("Parameter names can only contain letters, numbers, and hyphens.");
			}

			if (value == null) {
				parameters.remove(name);
			} else {
				parameters.put(name, value);
			}
			return this;
		}

		/**
		 * Builds the final {@link GeoUri} object.
		 * @return the object
		 */
		public GeoUri build() {
			return new GeoUri(this);
		}
	}
}
