package ezvcard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
	public static final JCardDataType TEXT = new JCardDataType("text");
	public static final JCardDataType URI = new JCardDataType("uri");
	public static final JCardDataType INTEGER = new JCardDataType("integer");
	public static final JCardDataType BOOLEAN = new JCardDataType("boolean");
	public static final JCardDataType FLOAT = new JCardDataType("float");
	public static final JCardDataType DATE = new JCardDataType("date");
	public static final JCardDataType TIME = new JCardDataType("time");
	public static final JCardDataType DATE_TIME = new JCardDataType("date-time");
	public static final JCardDataType UTC_OFFSET = new JCardDataType("utc-offset");
	public static final JCardDataType LANGUAGE_TAG = new JCardDataType("language-tag");
	static final Collection<JCardDataType> custom = Collections.synchronizedList(new ArrayList<JCardDataType>());

	private final String name;

	/**
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
	public static JCardDataType find(String name) {
		return find(name, false);
	}

	/**
	 * Searches for a data type object by name, and creates a new object if one
	 * cannot be found.
	 * @param name the data type (e.g. "text")
	 * @return the data type object
	 */
	public static JCardDataType get(String name) {
		return find(name, true);
	}

	/**
	 * Searches for a data type object by name.
	 * @param name the data type (e.g. "text")
	 * @param create true to create a new object if one cannot be found, or
	 * false to return null if an object cannot be found
	 * @return the data type object
	 */
	private static JCardDataType find(String name, boolean create) {
		SearchHandler find = new SearchHandler(name);
		forEachDataType(find);

		JCardDataType match = find.match;
		if (create && match == null) {
			match = new JCardDataType(name);
			custom.add(match);
		}
		return match;
	}

	/**
	 * Gets all available data types.
	 * @return all available data types
	 */
	public static Collection<JCardDataType> all() {
		final Collection<JCardDataType> all = new ArrayList<JCardDataType>();

		forEachDataType(new Handler() {
			public boolean handle(JCardDataType dataType) {
				all.add(dataType);
				return true;
			}
		});

		return all;
	}

	private static void forEachDataType(Handler handler) {
		Class<JCardDataType> clazz = JCardDataType.class;
		for (Field field : clazz.getFields()) {
			int modifiers = field.getModifiers();
			//@formatter:off
			if (Modifier.isStatic(modifiers) &&
				Modifier.isPublic(modifiers) &&
				field.getDeclaringClass() == clazz &&
				field.getType() == clazz) {
				//@formatter:on
				try {
					Object obj = field.get(null);
					if (obj != null && !handler.handle((JCardDataType) obj)) {
						return;
					}
				} catch (Exception ex) {
					//reflection error
				}
			}
		}

		for (JCardDataType dataType : custom) {
			if (!handler.handle(dataType)) {
				return;
			}
		}
	}

	private static interface Handler {
		/**
		 * @param dataType
		 * @return true to continue, false to break out of the loop
		 */
		boolean handle(JCardDataType dataType);
	}

	private static class SearchHandler implements Handler {
		public JCardDataType match = null;
		private final String name;

		public SearchHandler(String name) {
			this.name = name;
		}

		public boolean handle(JCardDataType dataType) {
			if (name.equalsIgnoreCase(dataType.name)) {
				match = dataType;
				return false;
			}
			return true;
		}
	}
}
