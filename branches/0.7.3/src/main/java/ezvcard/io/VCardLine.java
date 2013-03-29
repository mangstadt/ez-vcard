package ezvcard.io;

import java.util.ArrayList;
import java.util.List;

import ezvcard.VCardVersion;
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
 * Represents the components that make up an unfolded vCard line, such as type
 * name and value.
 * @author Michael Angstadt
 */
public class VCardLine {
	private String group;
	private String typeName;
	private List<List<String>> subTypes = new ArrayList<List<String>>();
	private String value;

	private VCardLine() {
		//hide constructor
	}

	/**
	 * Parses an unfolded vCard line. It just parses the components out, it
	 * doesn't modify the components in any way.
	 * @param line the unfolded line to parse
	 * @param version the version of the vCard that's being parsed
	 * @param caretDecodingEnabled true to enable circumflex accent decoding in
	 * 3.0 and 4.0 parameter values, false not to
	 * @return the parsed components or null if the line is not a valid vCard
	 * line
	 */
	public static VCardLine parse(String line, VCardVersion version, boolean caretDecodingEnabled) {
		VCardLine lineObj = new VCardLine();

		char escapeChar = 0; //is the next char escaped?
		boolean inQuotes = false; //are we inside of double quotes?
		StringBuilder buf = new StringBuilder();
		List<String> curSubType = new ArrayList<String>();
		curSubType.add(null);
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (escapeChar != 0) {
				if (escapeChar == '\\') {
					if (ch == '\\') {
						buf.append(ch);
					} else if (ch == 'n' || ch == 'N') {
						//newlines appear as "\n" or "\N" (see RFC 2426 p.7)
						buf.append(System.getProperty("line.separator"));
					} else if (ch == '"' && version != VCardVersion.V2_1) {
						//double quotes don't need to be escaped in 2.1 parameter values because they have no special meaning
						buf.append(ch);
					} else if (ch == ';' && version == VCardVersion.V2_1) {
						//semi-colons can only be escaped in 2.1 parameter values (see section 2 of specs)
						//if a 3.0/4.0 param value has semi-colons, the value should be surrounded in double quotes
						buf.append(ch);
					} else {
						//treat the escape character as a normal character because it's not a valid escape sequence
						buf.append(escapeChar).append(ch);
					}
				} else if (escapeChar == '^') {
					if (ch == '^') {
						buf.append(ch);
					} else if (ch == 'n') {
						buf.append(System.getProperty("line.separator"));
					} else if (ch == '\'') {
						buf.append('"');
					} else {
						//treat the escape character as a normal character because it's not a valid escape sequence
						buf.append(escapeChar).append(ch);
					}
				}
				escapeChar = 0;
			} else if (ch == '\\' || (ch == '^' && version != VCardVersion.V2_1 && caretDecodingEnabled)) {
				escapeChar = ch;
			} else if (ch == '.' && lineObj.group == null && lineObj.typeName == null) {
				lineObj.group = buf.toString();
				buf.setLength(0);
			} else if ((ch == ';' || ch == ':') && !inQuotes) {
				if (lineObj.typeName == null) {
					lineObj.typeName = buf.toString();
				} else {
					//sub type value
					String subTypeValue = buf.toString();
					if (version == VCardVersion.V2_1) {
						//2.1 allows whitespace to surround the "=", so remove it
						subTypeValue = VCardStringUtils.ltrim(subTypeValue);
					}
					curSubType.add(subTypeValue);

					lineObj.subTypes.add(curSubType);

					curSubType = new ArrayList<String>();
					curSubType.add(null);
				}
				buf.setLength(0);

				if (ch == ':') {
					if (i < line.length() - 1) {
						lineObj.value = line.substring(i + 1);
					} else {
						lineObj.value = "";
					}
					break;
				}
			} else if (ch == ',' && !inQuotes && version != VCardVersion.V2_1) {
				//multi-valued sub type
				curSubType.add(buf.toString());
				buf.setLength(0);
			} else if (ch == '=' && curSubType.get(0) == null) {
				//sub type name
				String subTypeName = buf.toString();
				if (version == VCardVersion.V2_1) {
					//2.1 allows whitespace to surround the "=", so remove it
					subTypeName = VCardStringUtils.rtrim(subTypeName);
				}
				curSubType.set(0, subTypeName);

				buf.setLength(0);
			} else if (ch == '"' && version != VCardVersion.V2_1) {
				//2.1 doesn't use the quoting mechanism
				inQuotes = !inQuotes;
			} else {
				buf.append(ch);
			}
		}

		if (lineObj.typeName == null || lineObj.value == null) {
			return null;
		}
		return lineObj;
	}

	/**
	 * Gets the group.
	 * @return the group or null if the type doesn't below to a group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Gets the type name.
	 * @return the type name
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Gets the sub types.
	 * @return the sub types. Index 0 of each list is the sub type name. The
	 * rest of the list contains the sub type values (there will always be at
	 * least one value and there may be more if the sub type is multi-valued).
	 * If the sub type is nameless, then index 0 will be null (sub types can be
	 * nameless in v2.1, e.g. "ADR;WORK;DOM:")
	 */
	public List<List<String>> getSubTypes() {
		return subTypes;
	}

	/**
	 * Gets the value.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
