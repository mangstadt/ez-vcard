package com.ezvcard.android;

import java.util.List;

import ezvcard.property.VCardProperty;

/**
 * Represents an "X-ANDROID-CUSTOM" property.
 * @author Michael Angstadt
 */
public class AndroidCustomField extends VCardProperty {
	private String type;
	private List<String> values;

	public AndroidCustomField(String type, List<String> values) {
		this.type = type;
		this.values = values;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public boolean isNickname() {
		return (type == null) ? false : "nickname".equals(type);
	}

	public boolean isContactEvent() {
		return (type == null) ? false : "contact_event".equals(type);
	}

	public boolean isRelation() {
		return (type == null) ? false : "relation".equals(type);
	}
}
