package ezvcard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ezvcard.ValidationWarnings.WarningsGroup;
import ezvcard.types.VCardType;
import ezvcard.util.VCardStringUtils;
import ezvcard.util.VCardStringUtils.JoinCallback;

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
 */

/**
 * <p>
 * Holds the validation warnings of a vCard.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * //validate a vCard object according to the rules of a specific version
 * ValidationWarnings warnings = vcard.validate(VCardVersion.V3_0);
 * 
 * //print all warnings to a string:
 * System.out.println(warnings.toString());
 * //sample output:
 * //FormattedNameType is not set (it is a required property).
 * //[GenderType]: Not supported by version 3.0.  Supported versions are: [4.0]
 * 
 * //iterate over each warnings group
 * //this gives you access to the property object that threw each warning
 * for (WarningsGroup group : warnings) {
 * 	//get the property instance
 * 	ICalProperty prop = group.getProperty();
 * 	if (prop == null) {
 * 		//it's a general warning about the vCard
 * 	}
 * 
 * 	//get warning messages
 * 	List&lt;String&gt; messages = group.getMessages();
 * }
 * 
 * //you can also get the warnings of specific properties
 * List&lt;WarningsGroup&gt; telWarnings = warnings.getByProperty(TelephoneType.class);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see VCard#validate
 */
public class ValidationWarnings implements Iterable<WarningsGroup> {
	private final List<WarningsGroup> warnings;
	private final VCardVersion version;

	/**
	 * Creates a new validation warnings list.
	 * @param warnings the validation warnings
	 * @param version the vCard version that the validation was performed
	 * against
	 */
	public ValidationWarnings(List<WarningsGroup> warnings, VCardVersion version) {
		this.warnings = warnings;
		this.version = version;
	}

	/**
	 * Gets all validation warnings of a given property.
	 * @param propertyClass the property (e.g. <code>TelephoneType.class</code>)
	 * or null to get the generic vCard warnings
	 * @return the validation warnings
	 */
	public List<WarningsGroup> getByProperty(Class<? extends VCardType> propertyClass) {
		List<WarningsGroup> warnings = new ArrayList<WarningsGroup>();
		for (WarningsGroup group : this.warnings) {
			VCardType property = group.getProperty();
			if (property == null) {
				if (propertyClass == null) {
					warnings.add(group);
				}
			} else if (propertyClass == property.getClass()) {
				warnings.add(group);
			}
		}
		return warnings;
	}

	/**
	 * Gets all the validation warnings.
	 * @return the validation warnings
	 */
	public List<WarningsGroup> getWarnings() {
		return warnings;
	}

	/**
	 * Gets the vCard version that the validation was performed against.
	 * @return the version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Determines whether there are any validation warnings.
	 * @return true if there are none, false if there are one or more
	 */
	public boolean isEmpty() {
		return warnings.isEmpty();
	}

	/**
	 * <p>
	 * Outputs all validation warnings as a newline-delimited string. For
	 * example:
	 * </p>
	 * 
	 * <pre>
	 * FormattedNameType is not set (it is a required property).
	 * [GenderType]: Not supported by version 3.0.  Supported versions are: [4.0]
	 * </pre>
	 */
	@Override
	public String toString() {
		return VCardStringUtils.join(warnings, VCardStringUtils.NEWLINE);
	}

	/**
	 * Iterates over each warning group (same as calling
	 * <code>getWarnings().iterator()</code>).
	 * @return the iterator
	 */
	public Iterator<WarningsGroup> iterator() {
		return warnings.iterator();
	}

	/**
	 * Holds the validation warnings of a property or component.
	 * @author Michael Angstadt
	 */
	public static class WarningsGroup {
		private final VCardType property;
		private final List<String> messages;

		/**
		 * Creates a new set of validation warnings for a property.
		 * @param property the property that caused the warnings
		 * @param messages the warning messages
		 */
		public WarningsGroup(VCardType property, List<String> messages) {
			this.property = property;
			this.messages = messages;
		}

		/**
		 * Gets the property object that caused the validation warnings.
		 * @return the property object or null if a component caused the
		 * warnings.
		 */
		public VCardType getProperty() {
			return property;
		}

		/**
		 * Gets the warning messages.
		 * @return the warning messages
		 */
		public List<String> getMessages() {
			return messages;
		}

		/**
		 * <p>
		 * Outputs each message in this warnings group as a newline-delimited
		 * string. Each line includes the name of the property. For example:
		 * </p>
		 * 
		 * <pre>
		 * [GenderType]: Not supported by version 3.0.  Supported versions are: [4.0]
		 * </pre>
		 */
		@Override
		public String toString() {
			final String prefix = (property == null) ? "" : "[" + property.getClass().getSimpleName() + "]: ";
			return VCardStringUtils.join(messages, VCardStringUtils.NEWLINE, new JoinCallback<String>() {
				public void handle(StringBuilder sb, String message) {
					sb.append(prefix).append(message);
				}
			});
		}
	}
}
