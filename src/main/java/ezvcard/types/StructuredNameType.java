package ezvcard.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ezvcard.VCardVersion;
import ezvcard.io.CompatibilityMode;
import ezvcard.util.VCardStringUtils;

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
 * Represents the "N" type.
 * @author Michael Angstadt
 */
public class StructuredNameType extends VCardType {
	public static final String NAME = "N";

	private String family;
	private String given;
	private List<String> additional = new ArrayList<String>();
	private List<String> prefixes = new ArrayList<String>();
	private List<String> suffixes = new ArrayList<String>();

	public StructuredNameType() {
		super(NAME);
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGiven() {
		return given;
	}

	public void setGiven(String given) {
		this.given = given;
	}

	public List<String> getAdditional() {
		return additional;
	}

	public List<String> getPrefixes() {
		return prefixes;
	}

	public List<String> getSuffixes() {
		return suffixes;
	}

	@Override
	protected String doMarshalValue(VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		StringBuilder value = new StringBuilder();
		if (family != null) {
			value.append(VCardStringUtils.escapeText(family));
		}

		value.append(';');
		if (given != null) {
			value.append(VCardStringUtils.escapeText(given));
		}

		value.append(';');
		if (!additional.isEmpty()) {
			for (String s : additional) {
				value.append(VCardStringUtils.escapeText(s)).append(',');
			}
			value.deleteCharAt(value.length() - 1);
		}

		value.append(';');
		if (!prefixes.isEmpty()) {
			for (String s : prefixes) {
				value.append(VCardStringUtils.escapeText(s)).append(',');
			}
			value.deleteCharAt(value.length() - 1);
		}

		value.append(';');
		if (!suffixes.isEmpty()) {
			for (String s : suffixes) {
				value.append(VCardStringUtils.escapeText(s)).append(',');
			}
			value.deleteCharAt(value.length() - 1);
		}

		return value.toString();
	}

	@Override
	protected void doUnmarshalValue(String value, VCardVersion version, List<String> warnings, CompatibilityMode compatibilityMode) {
		//preserve empty items and don't unescape escaped characters(e.g. "additional" might have escaped commas)
		String split[] = VCardStringUtils.splitBy(value, ';', false, false);

		int i = 0;
		family = !split[i].isEmpty() ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		given = (split.length > i && !split[i].isEmpty()) ? VCardStringUtils.unescape(split[i]) : null;
		i++;

		additional = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		i++;

		prefixes = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		i++;

		suffixes = new ArrayList<String>(Arrays.asList(VCardStringUtils.splitBy(split[i], ',', true, true)));
		i++;
	}
}