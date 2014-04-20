package ezvcard.io.text;

import java.util.Arrays;

import ezvcard.parameter.VCardParameters;

/**
 * Represents a parsed line from a vCard file.
 * @author Michael Angstadt
 */
public class VCardRawLine {
	private final String group, name, value;
	private final VCardParameters parameters;

	public VCardRawLine(String group, VCardParameters parameters, String name, String value) {
		this.group = group;
		this.name = name;
		this.value = value;
		this.parameters = parameters;
	}

	/**
	 * Determines if this line is a "BEGIN" property.
	 * @return true if it is, false if not
	 */
	public boolean isBegin() {
		return "BEGIN".equalsIgnoreCase(name);
	}

	/**
	 * Determines if this line is an "END" property.
	 * @return true if it is, false if not
	 */
	public boolean isEnd() {
		return "END".equalsIgnoreCase(name);
	}

	/**
	 * Determines if this line is a "VERSION" property.
	 * @return true if it is, false if not
	 */
	public boolean isVersion() {
		return "VERSION".equalsIgnoreCase(name);
	}

	/**
	 * Gets the property's group.
	 * @return the group or null if there is no group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Gets the property name.
	 * @return the property name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the property value.
	 * @return the property value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the property's parameters.
	 * @return the parameters
	 */
	public VCardParameters getParameters() {
		return parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VCardRawLine other = (VCardRawLine) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public static class Builder {
		private String group, name, value;
		private VCardParameters parameters = new VCardParameters();

		public Builder group(String group) {
			this.group = group;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder value(String value) {
			this.value = value;
			return this;
		}

		public Builder param(String name, String... values) {
			this.parameters.putAll(name, Arrays.asList(values));
			return this;
		}

		public VCardRawLine build() {
			if (name == null) {
				throw new IllegalArgumentException("Property name required.");
			}
			return new VCardRawLine(group, parameters, name, value);
		}
	}
}