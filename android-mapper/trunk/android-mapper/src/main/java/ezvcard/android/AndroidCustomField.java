package ezvcard.android;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.property.VCardProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an "X-ANDROID-CUSTOM" property.
 * @author Michael Angstadt
 */
public class AndroidCustomField extends VCardProperty {
	private String type;
	private boolean dir;
	private List<String> values = new ArrayList<String>();

	/**
	 * Creates an "item" field.
	 * @param type the type
	 * @param value the value
	 * @return the property
	 */
	public static AndroidCustomField item(String type, String value) {
		AndroidCustomField property = new AndroidCustomField();
		property.dir = false;
		property.type = type;
		property.values.add(value);
		return property;
	}

	/**
	 * Creates a "dir" field.
	 * @param type the type
	 * @param values the values
	 * @return the property
	 */
	public static AndroidCustomField dir(String type, String... values) {
		AndroidCustomField property = new AndroidCustomField();
		property.dir = true;
		property.type = type;
        Collections.addAll(property.values, values);
		return property;
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

	public boolean isDir() {
		return dir;
	}

	public void setDir(boolean dir) {
		this.dir = dir;
	}

	public boolean isItem() {
		return !isDir();
	}

	public void setItem(boolean item) {
		setDir(!item);
	}

	public boolean isNickname() {
		return "nickname".equals(type);
	}

	public boolean isContactEvent() {
		return "contact_event".equals(type);
	}

	public boolean isRelation() {
		return "relation".equals(type);
	}
	
	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard){
		if (type == null){
			warnings.add(new Warning("No type specified."));
		}
		if (isItem() && values.size() != 1){
			warnings.add(new Warning("Items must contain exactly one value."));
		}
	}
}
