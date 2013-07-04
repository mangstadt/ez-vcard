package ezvcard.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import ezvcard.VCard;
import ezvcard.VCardSubTypes;
import ezvcard.VCardVersion;
import ezvcard.types.RawType;
import ezvcard.types.TypeList;
import ezvcard.types.VCardType;
import ezvcard.util.JCardDataType;
import ezvcard.util.JCardValue;

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
 * Parses {@link VCard} objects from a JSON data stream (jCard format).
 * @author Michael Angstadt
 * @see <a
 * href="http://tools.ietf.org/html/draft-kewisch-vcard-in-json-04">jCard
 * draft</a>
 */
public class JCardReader implements IParser, Closeable {
	private final Reader reader;
	private final List<String> warnings = new ArrayList<String>();
	private JsonParser jp;
	private final Map<String, Class<? extends VCardType>> extendedTypeClasses = new HashMap<String, Class<? extends VCardType>>();
	private final VCardVersion version = VCardVersion.V4_0;

	private int propertiesRead;
	private boolean versionFound;

	/**
	 * Creates a jCard reader.
	 * @param json the JSON string
	 */
	public JCardReader(String json) {
		this(new StringReader(json));
	}

	/**
	 * Creates a jCard reader.
	 * @param in the input stream to read the vCards from
	 */
	public JCardReader(InputStream in) {
		this(new InputStreamReader(in));
	}

	/**
	 * Creates a jCard reader.
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public JCardReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * Creates a jCard reader.
	 * @param reader the reader to read the vCards from
	 */
	public JCardReader(Reader reader) {
		this.reader = reader;
	}

	//@Override
	public VCard readNext() throws IOException {
		if (jp != null && jp.isClosed()) {
			return null;
		}

		warnings.clear();
		versionFound = false;
		propertiesRead = 0;

		if (jp == null) {
			JsonFactory factory = new JsonFactory();
			jp = factory.createJsonParser(reader);
		}

		//find the next vCard
		JsonToken prev = null;
		JsonToken cur;
		while ((cur = jp.nextToken()) != null) {
			if (prev == JsonToken.START_ARRAY && cur == JsonToken.VALUE_STRING && "vcard".equals(jp.getValueAsString())) {
				break;
			}
			prev = cur;
		}
		if (cur == null) {
			//EOF
			return null;
		}

		//start properties array
		if (jp.nextToken() != JsonToken.START_ARRAY) {
			throw new JCardParseException(JsonToken.START_ARRAY, jp.getCurrentToken());
		}

		VCard vcard = new VCard();
		vcard.setVersion(VCardVersion.V4_0);

		List<String> warningsBuf = new ArrayList<String>();
		while (jp.nextToken() != JsonToken.END_ARRAY) {
			//get property name
			if (jp.nextToken() != JsonToken.VALUE_STRING) {
				throw new JCardParseException(JsonToken.VALUE_STRING, jp.getCurrentToken());
			}
			String propertyName = jp.getValueAsString().toLowerCase();

			//get parameters and group
			String group = null;
			VCardSubTypes subTypes = new VCardSubTypes();
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new JCardParseException(JsonToken.START_OBJECT, jp.getCurrentToken());
			}
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String parameterName = jp.getText();

				List<String> parameterValues = new ArrayList<String>();
				if (jp.nextToken() == JsonToken.START_ARRAY) {
					//multi-valued parameter
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						parameterValues.add(jp.getText());
					}
				} else {
					parameterValues.add(jp.getValueAsString());
				}

				if ("group".equalsIgnoreCase(parameterName)) {
					group = parameterValues.get(0);
				} else {
					subTypes.putAll(parameterName, parameterValues);
				}
			}

			JCardValue jcardValue = new JCardValue();

			//get data type
			if (jp.nextToken() != JsonToken.VALUE_STRING) {
				throw new JCardParseException(JsonToken.VALUE_STRING, jp.getCurrentToken());
			}
			jcardValue.setDataType(JCardDataType.get(jp.getText()));

			//get property value(s)
			boolean structured = false;
			if (jp.nextToken() == JsonToken.START_ARRAY) {
				//structured property value (e.g. ["n", {}, "text", ["Doe", "John", "", "", ["Mr", "Dr"]]])
				structured = true;
				jp.nextToken();
			}
			jcardValue.setStructured(structured);
			while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
				List<Object> curValue = new ArrayList<Object>();
				if (jp.getCurrentToken() == JsonToken.START_ARRAY) {
					//multi-valued component
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						curValue.add(parseValueElement());
					}
				} else {
					curValue.add(parseValueElement());
				}
				if (curValue.isEmpty()) {
					jcardValue.addValues("");
				} else {
					jcardValue.addValues(curValue);
				}
				jp.nextToken();
			}
			if (structured) {
				//consume the extra END_ARRAY character from the structured value array
				if (jp.nextToken() != JsonToken.END_ARRAY) {
					throw new JCardParseException(JsonToken.END_ARRAY, jp.getCurrentToken());
				}
				if (jcardValue.getValues().isEmpty()) {
					jcardValue.addValues("");
				}
			}

			if (propertiesRead == 0 && !"version".equals(propertyName)) {
				addWarning("jCard does not start with the \"version\" property.  Version will be set to " + version);
			}

			propertiesRead++;

			if ("version".equals(propertyName)) {
				Object firstValue = jcardValue.getFirstValue();
				String firstValueStr = (firstValue == null) ? "" : firstValue.toString();
				if (versionFound) {
					addWarning("Additional \"version\" property encountered.  It will be ignored.");
				} else {
					versionFound = true;
					if (!version.getVersion().equals(firstValueStr)) {
						addWarning("Invalid value of \"version\" property: " + firstValueStr);
					}
				}
				continue;
			}

			VCardType type = createTypeObject(propertyName);
			type.setGroup(group);

			//unmarshal the text string into the object
			warningsBuf.clear();
			try {
				type.unmarshalJson(subTypes, jcardValue, version, warningsBuf);
				addToVCard(type, vcard);
			} catch (SkipMeException e) {
				warningsBuf.add("Property has requested that it be skipped: " + e.getMessage());
			} catch (EmbeddedVCardException e) {
				warningsBuf.add("Property will not be unmarshalled because jCard does not supported embedded vCards.");
			} finally {
				for (String warning : warningsBuf) {
					addWarning(warning, type.getTypeName());
				}
			}
		}

		if (!versionFound) {
			addWarning("\"version\" property was missing from the jCard.  The jCard was parsed as version " + version.getVersion() + ".");
		}

		//consume ending of properties array
		if (jp.nextToken() != JsonToken.END_ARRAY) {
			throw new JCardParseException(JsonToken.END_ARRAY, jp.getCurrentToken());
		}

		return vcard;
	}

	private Object parseValueElement() throws JsonParseException, IOException {
		JsonToken curToken = jp.getCurrentToken();
		if (curToken == JsonToken.VALUE_FALSE || curToken == JsonToken.VALUE_TRUE) {
			return jp.getBooleanValue();
		}
		if (curToken == JsonToken.VALUE_NUMBER_FLOAT) {
			return jp.getDoubleValue();
		}
		if (curToken == JsonToken.VALUE_NUMBER_INT) {
			return jp.getLongValue();
		}
		if (curToken == JsonToken.VALUE_NULL) {
			return "";
		}
		return jp.getText();
	}

	/**
	 * Creates the appropriate {@link VCardType} instance, given the type name.
	 * This method does not unmarshal the type, it just creates the type object.
	 * @param name the type name (e.g. "FN")
	 * @return the Type that was created
	 */
	private VCardType createTypeObject(String name) {
		Class<? extends VCardType> clazz = TypeList.getTypeClass(name);
		VCardType t;
		if (clazz != null) {
			try {
				//create a new instance of the class
				t = clazz.newInstance();
			} catch (Exception e) {
				//it is the responsibility of the ez-vcard developer to ensure that this exception is never thrown
				//all type classes defined in the ez-vcard library MUST have public, no-arg constructors
				throw new RuntimeException(e);
			}
		} else {
			Class<? extends VCardType> extendedTypeClass = extendedTypeClasses.get(name);
			if (extendedTypeClass != null) {
				try {
					t = extendedTypeClass.newInstance();
				} catch (Exception e) {
					//this should never happen because the type class is checked to see if it has a public, no-arg constructor in the "registerExtendedType" method
					throw new RuntimeException("Extended property class \"" + extendedTypeClass.getName() + "\" must have a public, no-arg constructor.");
				}
			} else {
				t = new RawType(name); //use RawType instead of TextType because we don't want to unescape any characters that might be meaningful to this type
				if (!name.startsWith("X-")) {
					addWarning("Non-standard property \"" + name + "\" found.  Treating it as an extended property.");
				}
			}
		}
		return t;
	}

	/**
	 * Adds a type object to the vCard.
	 * @param t the type object
	 * @param vcard the vCard
	 */
	private void addToVCard(VCardType t, VCard vcard) {
		Method method = TypeList.getAddMethod(t.getClass());
		if (method != null) {
			try {
				method.invoke(vcard, t);
			} catch (Exception e) {
				//this should NEVER be thrown because the method MUST be public
				throw new RuntimeException(e);
			}
		} else {
			vcard.addExtendedType(t);
		}
	}

	/**
	 * Gets the type name from a type class.
	 * @param clazz the type class
	 * @return the type name
	 */
	private String getTypeNameFromTypeClass(Class<? extends VCardType> clazz) {
		try {
			VCardType t = clazz.newInstance();
			return t.getTypeName().toLowerCase();
		} catch (Exception e) {
			//there is no public, no-arg constructor
			throw new RuntimeException(e);
		}
	}

	//@Override
	public void registerExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.put(getTypeNameFromTypeClass(clazz), clazz);
	}

	//@Override
	public void unregisterExtendedType(Class<? extends VCardType> clazz) {
		extendedTypeClasses.remove(getTypeNameFromTypeClass(clazz));
	}

	//@Override
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	private void addWarning(String message) {
		addWarning(message, null);
	}

	private void addWarning(String message, String propertyName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(jp.getTokenLocation().getLineNr());
		if (propertyName != null) {
			sb.append(" (").append(propertyName).append(" property)");
		}
		sb.append(": ").append(message);

		warnings.add(sb.toString());
	}

	public void close() throws IOException {
		if (jp != null) {
			jp.close();
		}
	}
}
