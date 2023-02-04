package ezvcard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import ezvcard.property.VCardProperty;
import ezvcard.util.ListMultimap;
import ezvcard.util.StringUtils;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * </p>
 * 
 * <pre class="brush:java">
 * //validate a vCard object according to the rules of a specific version
 * ValidationWarnings warnings = vcard.validate(VCardVersion.V3_0);
 * 
 * //print all warnings to a string:
 * System.out.println(warnings.toString());
 * //sample output:
 * //W01: A FormattedName property is required for vCard versions 3.0 and 4.0.
 * //[Gender] | W02: Property is not supported in this vCard version.  Supported versions are: [4.0]
 * 
 * //iterate over the warnings
 * for (Map.Entry&lt;VCardProperty, List&lt;Warning&gt;&gt; entry : warnings) {
 *   //the property that caused the warning(s)
 *   VCardProperty property = entry.getKey();
 * 
 *   //the list of warnings that belong to this property
 *   List&lt;Warning&gt; propWarnings = entry.getValue();
 * 
 *   if (property == null) {
 *     //it's a warning about the vCard as a whole
 *   }
 * 
 *   //each warning message has a numeric code
 *   //this allows you to programmatically respond to specific warning messages
 *   List&lt;Warning&gt; propWarnings = entry.getValue();
 *   for (Warning w : propWarnings) {
 *     System.out.println("Code: " + w.getCode());
 *     System.out.printkn("Message: " + w.getMessage());
 *   }
 * }
 * 
 * //you can also get the warnings of specific property classes
 * List&lt;Warnings&gt; telWarnings = warnings.getByProperty(Telephone.class);
 * </pre>
 * 
 * @author Michael Angstadt
 * @see VCard#validate
 */
public class ValidationWarnings implements Iterable<Map.Entry<VCardProperty, List<ValidationWarning>>> {
	private final ListMultimap<VCardProperty, ValidationWarning> warnings = new ListMultimap<>(new IdentityHashMap<>());

	/**
	 * Adds a validation warning.
	 * @param property the property that caused the warning
	 * @param warning the warning
	 */
	public void add(VCardProperty property, ValidationWarning warning) {
		warnings.put(property, warning);
	}

	/**
	 * Adds a property's validation warnings.
	 * @param property the property that caused the warnings
	 * @param warnings the warnings
	 */
	public void add(VCardProperty property, List<ValidationWarning> warnings) {
		this.warnings.putAll(property, warnings);
	}

	/**
	 * Gets all of the validation warnings.
	 * @return the validation warnings
	 */
	public ListMultimap<VCardProperty, ValidationWarning> getWarnings() {
		return warnings;
	}

	/**
	 * Determines whether or not the warnings list is empty.
	 * @return true if there are no warnings, false if there is at least one
	 * warning
	 */
	public boolean isEmpty() {
		return warnings.isEmpty();
	}

	/**
	 * Gets all validation warnings that belong to a property of a specific
	 * class.
	 * @param propertyClass the property class (e.g. {@code Telephone.class}) or
	 * null to get the warnings that apply to the vCard as a whole
	 * @return the validation warnings
	 */
	public List<ValidationWarning> getByProperty(Class<? extends VCardProperty> propertyClass) {
		List<ValidationWarning> propWarnings = new ArrayList<>();
		for (Map.Entry<VCardProperty, List<ValidationWarning>> entry : warnings) {
			VCardProperty property = entry.getKey();

			if ((property == null && propertyClass == null) || (property != null && propertyClass == property.getClass())) {
				List<ValidationWarning> propViolations = entry.getValue();
				propWarnings.addAll(propViolations);
			}
		}
		return propWarnings;
	}

	/**
	 * <p>
	 * Outputs all validation warnings as a newline-delimited string. For
	 * example:
	 * </p>
	 * 
	 * <pre>
	 * W01: A FormattedName property is required for vCard versions 3.0 and 4.0.
	 * [Gender] | W02: Property is not supported in this vCard version.  Supported versions are: [4.0]
	 * </pre>
	 */
	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ROOT);
		nf.setMinimumIntegerDigits(2);

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<VCardProperty, List<ValidationWarning>> entry : warnings) {
			VCardProperty property = entry.getKey();
			List<ValidationWarning> propViolations = entry.getValue();

			for (ValidationWarning propViolation : propViolations) {
				if (property != null) {
					sb.append('[');
					sb.append(property.getClass().getSimpleName());
					sb.append("] | ");
				}

				Integer code = propViolation.getCode();
				if (code != null) {
					sb.append('W');
					sb.append(nf.format(code));
					sb.append(": ");
				}

				sb.append(propViolation.getMessage());
				sb.append(StringUtils.NEWLINE);
			}
		}

		return sb.toString();
	}

	public Iterator<Entry<VCardProperty, List<ValidationWarning>>> iterator() {
		return warnings.iterator();
	}
}
