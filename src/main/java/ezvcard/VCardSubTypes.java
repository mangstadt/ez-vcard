package ezvcard;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import ezvcard.parameters.CalscaleParameter;
import ezvcard.parameters.EncodingParameter;
import ezvcard.parameters.TypeParameter;
import ezvcard.parameters.ValueParameter;

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
 * Holds the Sub Types of a vCard Type.
 * @author Michael Angstadt
 */
public class VCardSubTypes {
	private final SetMultimap<String, String> subTypes;

	public VCardSubTypes() {
		subTypes = HashMultimap.create();
	}

	/**
	 * Copy constructor.
	 * @param orig the object to copy
	 */
	public VCardSubTypes(VCardSubTypes orig) {
		subTypes = HashMultimap.create(orig.subTypes);
	}

	/**
	 * Adds a value to a Sub Type.
	 * @param name the Sub Type name
	 * @param value the value to add
	 */
	public void put(String name, String value) {
		subTypes.put(name.toUpperCase(), value);
	}

	/**
	 * Adds a value to a Sub Type, replacing all existing values that the Sub
	 * Type has.
	 * @param name the Sub Type name
	 * @param value the values to replace all existing values with
	 * @return the values of the Sub Type that were replaced
	 */
	public Set<String> replace(String name, String value) {
		Set<String> set = removeAll(name);
		if (value != null) {
			put(name, value);
		}
		return set;
	}

	/**
	 * Removes a Sub Type.
	 * @param name the Sub Type name
	 * @return the values of the Sub Type that were removed
	 */
	public Set<String> removeAll(String name) {
		return subTypes.removeAll(name.toUpperCase());
	}

	/**
	 * Removes a value from a Sub Type.
	 * @param name the Sub Type name
	 * @param value the value to remove
	 */
	public void remove(String name, String value) {
		subTypes.remove(name.toUpperCase(), value);
	}

	/**
	 * Gets the values of a Sub Type
	 * @param name the Sub Type name
	 * @return the values or an empty set if the Sub Type doesn't exist
	 */
	public Set<String> get(String name) {
		return subTypes.get(name.toUpperCase());
	}

	/**
	 * Gets the first value of a Sub Type.
	 * @param name the Sub Type name
	 * @return the first value or null if the Sub Type doesn't exist
	 */
	public String getFirst(String name) {
		Set<String> set = get(name);
		return set.isEmpty() ? null : set.iterator().next();
	}

	/**
	 * Gets the names of all the Sub Types.
	 * @return the names of all the Sub Types or an empty set if there are no
	 * Sub Types
	 */
	public Set<String> getNames() {
		return subTypes.keySet();
	}

	/**
	 * Gets the object used to store the Sub Types.
	 * @return the object used to store the Sub Types
	 */
	public SetMultimap<String, String> getMultimap() {
		return subTypes;
	}

	/**
	 * Gets the ENCODING sub type.
	 * @return the value or null if not found
	 */
	public EncodingParameter getEncoding() {
		String value = getFirst(EncodingParameter.NAME);
		if (value == null) {
			return null;
		}
		EncodingParameter encoding = EncodingParameter.valueOf(value);
		if (encoding == null) {
			encoding = new EncodingParameter(value);
		}
		return encoding;
	}

	/**
	 * Sets the ENCODING sub type.
	 * @param encoding the value or null to remove
	 */
	public void setEncoding(EncodingParameter encoding) {
		replace(EncodingParameter.NAME, (encoding == null) ? null : encoding.getValue());
	}

	/**
	 * Gets the VALUE sub type.
	 * @return the value or null if not found
	 */
	public ValueParameter getValue() {
		String value = getFirst(ValueParameter.NAME);
		if (value == null) {
			return null;
		}
		ValueParameter p = ValueParameter.valueOf(value);
		if (p == null) {
			p = new ValueParameter(value);
		}
		return p;
	}

	/**
	 * Sets the VALUE sub type.
	 * @param value the value or null to remove
	 */
	public void setValue(ValueParameter value) {
		replace(ValueParameter.NAME, (value == null) ? null : value.getValue());
	}

	/**
	 * Gets the CHARSET sub type.
	 * @return the value or null if not found
	 */
	public String getCharset() {
		return getFirst("CHARSET");
	}

	/**
	 * Sets the CHARSET sub type
	 * @param charset the value or null to remove
	 */
	public void setCharset(String charset) {
		replace("CHARSET", charset);
	}

	/**
	 * Gets the LANGUAGE sub type.
	 * @return the value or null if not found
	 */
	public String getLanguage() {
		return getFirst("LANGUAGE");
	}

	/**
	 * Sets the LANGUAGE sub type.
	 * @param language the value or null to remove
	 */
	public void setLanguage(String language) {
		replace("LANGUAGE", language);
	}

	/**
	 * Gets all TYPE sub types.
	 * @return the values or empty set if not found
	 */
	public Set<String> getTypes() {
		Set<String> types = new HashSet<String>();
		for (String value : get(TypeParameter.NAME)) {
			types.add(value);
		}
		return types;
	}

	/**
	 * Adds a TYPE sub type
	 * @param type the value
	 */
	public void addType(String type) {
		put(TypeParameter.NAME, type);
	}

	/**
	 * Gets the first TYPE sub type.
	 * @return the value or null if not found.
	 */
	public String getType() {
		Set<String> types = getTypes();
		return types.isEmpty() ? null : types.iterator().next();
	}

	/**
	 * Sets the TYPE sub type.
	 * @param type the value or null to remove
	 */
	public void setType(String type) {
		replace(TypeParameter.NAME, type);
	}

	/**
	 * Removes a TYPE sub type.
	 * @param type the value to remove
	 */
	public void removeType(String type) {
		remove(TypeParameter.NAME, type);
	}

	public Integer getPref() {
		String pref = getFirst("PREF");
		if (pref == null) {
			return null;
		}

		try {
			return Integer.valueOf(pref);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public void setPref(Integer pref) {
		String value = (pref == null) ? null : pref.toString();
		replace("PREF", value);
	}

	public String getAltId() {
		return getFirst("ALTID");
	}

	public void setAltId(String altId) {
		replace("ALTID", altId);
	}

	public double[] getGeo() {
		String value = getFirst("GEO");
		if (value == null) {
			return null;
		}

		Pattern p = Pattern.compile("^geo:(.*?),(.*)$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(value);
		if (m.find()) {
			try {
				double latitude = Double.parseDouble(m.group(1));
				double longitude = Double.parseDouble(m.group(2));
				return new double[] { latitude, longitude };
			} catch (NumberFormatException e) {

			}
		}
		return null;
	}

	public void setGeo(double latitude, double longitude) {
		NumberFormat nf = new DecimalFormat("0.####");
		String value = "geo:" + nf.format(latitude) + "," + nf.format(longitude);
		replace("GEO", value);
	}

	public String getSortAs() {
		return getFirst("SORT-AS");
	}

	public void setSortAs(String sortAs) {
		replace("SORT-AS", sortAs);
	}

	/**
	 * Gets the CALSCALE sub type.
	 * @return the value or null if not found
	 */
	public CalscaleParameter getCalscale() {
		String value = getFirst(CalscaleParameter.NAME);
		if (value == null) {
			return null;
		}
		CalscaleParameter p = CalscaleParameter.valueOf(value);
		if (p == null) {
			p = new CalscaleParameter(value);
		}
		return p;
	}

	/**
	 * Sets the CALSCALE sub type.
	 * @param value the value or null to remove
	 */
	public void setCalscale(CalscaleParameter value) {
		replace(CalscaleParameter.NAME, (value == null) ? null : value.getValue());
	}

	public Set<String> getPids() {
		return get("PID");
	}

	public void addPid(String pid) {
		put("PID", pid);
	}

	public void removePid(String pid) {
		remove("PID", pid);
	}

	public String getMediaType() {
		return getFirst("MEDIATYPE");
	}

	public void setMediaType(String mediaType) {
		replace("MEDIATYPE", mediaType);
	}
}
