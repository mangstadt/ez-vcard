package ezvcard.util;

import java.util.Collection;

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
 * Contains all the jCard data types.
 * @author Michael Angstadt
 */
public class JCardDataType {
	private static final CaseClassesImpl enums = new CaseClassesImpl();

	public static final JCardDataType TEXT = new JCardDataType("text");
	public static final JCardDataType URI = new JCardDataType("uri");
	public static final JCardDataType INTEGER = new JCardDataType("integer");
	public static final JCardDataType BOOLEAN = new JCardDataType("boolean");
	public static final JCardDataType FLOAT = new JCardDataType("float");
	public static final JCardDataType DATE = new JCardDataType("date");
	public static final JCardDataType TIME = new JCardDataType("time");
	public static final JCardDataType DATE_TIME = new JCardDataType("date-time");
	public static final JCardDataType DATE_AND_OR_TIME = new JCardDataType("date-and-or-time");
	public static final JCardDataType TIMESTAMP = new JCardDataType("timestamp");
	public static final JCardDataType UTC_OFFSET = new JCardDataType("utc-offset");
	public static final JCardDataType LANGUAGE_TAG = new JCardDataType("language-tag");

	private final String name;

	/**
	 * Creates a hCard data type.
	 * @param name the data type name (e.g. "text")
	 */
	private JCardDataType(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of the data type
	 * @return the data type name (e.g. "text")
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Searches for a data type object by name.
	 * @param name the data type (e.g. "text")
	 * @return the data type object
	 */
	public static JCardDataType find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a data type object by name, and creates a new object if one
	 * cannot be found. All objects are guaranteed to be unique, so they can be
	 * compared with
	 * @param name the data type (e.g. "text")
	 * @return the data type object
	 */
	public static JCardDataType get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the standard data types.
	 * @return all standard data types
	 */
	public static Collection<JCardDataType> all() {
		return enums.all();
	}

	private static class CaseClassesImpl extends CaseClasses<JCardDataType, String> {
		public CaseClassesImpl() {
			super(JCardDataType.class);
		}

		@Override
		protected JCardDataType create(String value) {
			return new JCardDataType(value);
		}

		@Override
		protected boolean matches(JCardDataType object, String value) {
			return object.getName().equalsIgnoreCase(value);
		}
	}
}
