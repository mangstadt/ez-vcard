package ezvcard.util;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * Represents a "geo" URI.
 * </p>
 * <p>
 * Example geo URI: <code>geo:12.341,56.784</code>
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5870">RFC 5870</a>
 */
public class GeoUri {
	/**
	 * The coordinate reference system used by GPS (the default).
	 */
	public static final String CRS_WGS84 = "wgs84";

	/**
	 * The non-alphanumeric characters which are allowed to exist inside of a
	 * parameter value.
	 */
	protected static final char validParamValueChars[] = "!$&'()*+-.:[]_~".toCharArray();
	static {
		//make sure the array is sorted for binary search
		Arrays.sort(validParamValueChars);
	}

	/**
	 * Finds hex values in a parameter value.
	 */
	protected static final Pattern hexPattern = Pattern.compile("(?i)%([0-9a-f]{2})");

	protected static final String PARAM_CRS = "crs";
	protected static final String PARAM_UNCERTAINTY = "u";

	protected Double coordA;
	protected Double coordB;
	protected Double coordC;
	protected String crs;
	protected Double uncertainty;
	protected Map<String, String> parameters = new LinkedHashMap<String, String>();

	public GeoUri() {
		//do nothing
	}

	/**
	 * @param coordA the first coordinate (latitude, required)
	 * @param coordB the second coordinate (longitude, required)
	 * @param coordC the third coordinate (altitude, optional)
	 * @param crs the coordinate system (optional, defaults to WGS-84)
	 * @param uncertainty the accuracy of the coordinates (in meters, optional)
	 */
	public GeoUri(Double coordA, Double coordB, Double coordC, String crs, Double uncertainty) {
		this.coordA = coordA;
		this.coordB = coordB;
		this.coordC = coordC;
		this.crs = crs;
		this.uncertainty = uncertainty;
	}

	/**
	 * Parses a geo URI string.
	 * @param uri the URI string
	 * @throws IllegalArgumentException if the string is not a valid geo URI
	 */
	public GeoUri(String uri) {
		Pattern p = Pattern.compile("(?i)^geo:(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?)(,(-?\\d+(\\.\\d+)?))?(;(.*))?$");
		Matcher m = p.matcher(uri);
		if (m.find()) {
			coordA = Double.parseDouble(m.group(1));
			coordB = Double.parseDouble(m.group(3));

			String coordCStr = m.group(6);
			if (coordCStr != null) {
				coordC = Double.valueOf(coordCStr);
			}

			String paramsStr = m.group(9);
			if (paramsStr != null) {
				String paramsArray[] = paramsStr.split(";");

				for (String param : paramsArray) {
					String paramSplit[] = param.split("=", 2);
					String paramName = paramSplit[0];
					String paramValue = paramSplit.length > 1 ? paramSplit[1] : "";
					if (PARAM_CRS.equalsIgnoreCase(paramName)) {
						crs = paramValue;
					} else if (PARAM_UNCERTAINTY.equalsIgnoreCase(paramName)) {
						uncertainty = Double.valueOf(paramValue);
					} else {
						paramValue = decodeParamValue(paramValue);
						parameters.put(paramName, paramValue);
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Invalid geo URI: " + uri);
		}
	}

	/**
	 * Gets the first coordinate (latitude).
	 * @return the first coordinate or null if there is none
	 */
	public Double getCoordA() {
		return coordA;
	}

	/**
	 * Sets the first coordinate (latitude).
	 * @param coordA the first coordinate
	 */
	public void setCoordA(Double coordA) {
		this.coordA = coordA;
	}

	/**
	 * Gets the second coordinate (longitude).
	 * @return the second coordinate or null if there is none
	 */
	public Double getCoordB() {
		return coordB;
	}

	/**
	 * Sets the second coordinate (longitude).
	 * @param coordB the second coordinate
	 */
	public void setCoordB(Double coordB) {
		this.coordB = coordB;
	}

	/**
	 * Gets the third coordinate (altitude).
	 * @return the third coordinate or null if there is none
	 */
	public Double getCoordC() {
		return coordC;
	}

	/**
	 * Sets the third coordinate (altitude).
	 * @param coordC the third coordinate or null to remove
	 */
	public void setCoordC(Double coordC) {
		this.coordC = coordC;
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
	 * Sets the coordinate reference system.
	 * @param crs the coordinate reference system (can only contain letters,
	 * numbers, and hyphens) or null to use the default (WGS-84)
	 * @throws IllegalArgumentException if the CRS name contains invalid
	 * characters
	 */
	public void setCrs(String crs) {
		if (crs != null && !isLabelText(crs)) {
			throw new IllegalArgumentException("CRS can only contain letters, numbers, and hypens.");
		}
		this.crs = crs;
	}

	/**
	 * Gets the uncertainty (how accurate the coordinates are).
	 * @return the uncertainty (in meters) or null if not set
	 */
	public Double getUncertainty() {
		return uncertainty;
	}

	/**
	 * Sets the uncertainty (how accurate the coordinates are).
	 * @param uncertainty the uncertainty (in meters) or null to remove
	 */
	public void setUncertainty(Double uncertainty) {
		this.uncertainty = uncertainty;
	}

	/**
	 * Adds a parameter.
	 * @param name the parameter name (can only contain letters, numbers, and
	 * hyphens)
	 * @param value the parameter value
	 * @throws IllegalArgumentException if the parameter name contains invalid
	 * characters
	 */
	public void addParameter(String name, String value) {
		if (!isLabelText(name)) {
			throw new IllegalArgumentException("Parameter names can only contain letters, numbers, and hyphens.");
		}

		if (value == null) {
			value = "";
		}

		parameters.put(name, value);
	}

	/**
	 * Removes a parameter.
	 * @param name the name of the parameter to remove
	 */
	public void removeParameter(String name) {
		parameters.remove(name);
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
		return new LinkedHashMap<String, String>(parameters);
	}

	/**
	 * Creates a {@link URI} object from this geo URI.
	 * @return the {@link URI} object
	 */
	public URI toUri() {
		return URI.create(toString());
	}

	/**
	 * Determines if the geo URI is valid or not (i.e. if both an A and B
	 * coordinate is present).
	 * @return true if it is valid, false if not
	 */
	public boolean isValid() {
		return coordA != null && coordB != null;
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
		NumberFormat nf = buildNumberFormat(decimals);
		StringBuilder sb = new StringBuilder("geo:");

		if (coordA != null) {
			sb.append(nf.format(coordA));
		}

		sb.append(',');
		if (coordB != null) {
			sb.append(nf.format(coordB));
		}

		if (coordC != null) {
			sb.append(',');
			sb.append(coordC);
		}

		//if the CRS is WGS-84, then it doesn't have to be displayed
		if (crs != null && !crs.equalsIgnoreCase(CRS_WGS84)) {
			sb.append(';').append(PARAM_CRS).append('=');
			sb.append(crs);
		}

		if (uncertainty != null) {
			sb.append(';').append(PARAM_UNCERTAINTY).append('=');
			sb.append(nf.format(uncertainty));
		}

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			sb.append(';');
			sb.append(name); //note: the param name is validated in "addParameter()"
			sb.append('=');
			sb.append(encodeParamValue(value));
		}

		return sb.toString();
	}

	protected boolean isLabelText(String text) {
		return text.matches("(?i)[-a-z0-9]+");
	}

	protected String encodeParamValue(String value) {
		StringBuilder sb = new StringBuilder(value.length());
		for (char c : value.toCharArray()) {
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || Arrays.binarySearch(validParamValueChars, c) >= 0) {
				sb.append(c);
			} else {
				int i = (int) c;
				sb.append('%');
				sb.append(Integer.toString(i, 16));
			}
		}
		return sb.toString();
	}

	protected String decodeParamValue(String value) {
		Matcher m = hexPattern.matcher(value);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			int hex = Integer.parseInt(m.group(1), 16);
			m.appendReplacement(sb, "" + (char) hex);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	protected NumberFormat buildNumberFormat(int decimals) {
		StringBuilder sb = new StringBuilder();
		sb.append('0');
		if (decimals > 0) {
			sb.append('.');
			for (int i = 0; i < decimals; i++) {
				sb.append('#');
			}
		}
		return new DecimalFormat(sb.toString());
	}

	@Override
	public boolean equals(Object obj) {
		//TODO implement
		return super.equals(obj);
	}
}
