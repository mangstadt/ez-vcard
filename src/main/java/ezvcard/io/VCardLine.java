package ezvcard.io;

import java.util.ArrayList;
import java.util.List;

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
	private List<String[]> subTypes = new ArrayList<String[]>();
	private String value;

	private VCardLine() {
		//hide constructor
	}

	/**
	 * Parses an unfolded vCard line. It just parses the components out, it
	 * doesn't modify the components in any way.
	 * @param line the unfoled line to parse
	 * @return the parsed components or null if the line is not a valid vCard
	 * line
	 */
	public static VCardLine parse(String line) {
		VCardLine lineObj = new VCardLine();

		boolean escaped = false; //is the next char escaped?
		boolean inQuotes = false; //are we inside of double quotes?
		StringBuilder buf = new StringBuilder();
		String[] curSubType = new String[2];
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (escaped) {
				if (ch == 'n' || ch == 'N') {
					//newlines appear as "\n" or "\N" (see RFC 2426 p.7)
					buf.append("\r\n");
				} else {
					buf.append(ch);
				}
				escaped = false;
			} else if (ch == '\\') {
				escaped = true;
			} else if (ch == '.') {
				if (lineObj.group == null && lineObj.typeName == null) {
					lineObj.group = buf.toString();
					buf = new StringBuilder();
				}
			} else if ((ch == ';' || ch == ':') && !inQuotes) {
				if (lineObj.typeName == null) {
					lineObj.typeName = buf.toString();
				} else {
					//sub type value
					curSubType[1] = buf.toString();

					lineObj.subTypes.add(curSubType);
					curSubType = new String[2];
				}
				buf = new StringBuilder();

				if (ch == ':') {
					if (i < line.length() - 1) {
						lineObj.value = line.substring(i + 1);
					} else {
						lineObj.value = "";
					}
					break;
				}
			} else if (ch == '=' && !inQuotes) {
				//sub type name
				curSubType[0] = buf.toString();
				buf = new StringBuilder();
			} else if (ch == '"') {
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
	 * @return the sub types. Index 0 is the name and index 1 is the value. If
	 * the sub type is nameless, then index 0 will be null. Sub types can be
	 * nameless in v2.1 (e.g. "ADR;WORK;DOM:")
	 */
	public List<String[]> getSubTypes() {
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
