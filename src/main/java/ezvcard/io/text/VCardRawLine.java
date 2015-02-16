package ezvcard.io.text;

import java.util.Arrays;

import ezvcard.parameter.VCardParameters;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Represents a parsed line from a vCard file.
 * @author Michael Angstadt
 */
public class VCardRawLine {
	private final String group, name, value;
	private final VCardParameters parameters;

	/**
	 * @param group the group
	 * @param name the property name
	 * @param parameters the parameters
	 * @param value the property value
	 */
	public VCardRawLine(String group, String name, VCardParameters parameters, String value) {
		this.group = group;
		this.name = name;
		this.value = value;
		this.parameters = parameters;
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
			return new VCardRawLine(group, name, parameters, value);
		}
	}
}