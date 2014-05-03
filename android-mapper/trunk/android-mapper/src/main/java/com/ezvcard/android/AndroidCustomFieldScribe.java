package com.ezvcard.android;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.VCardParameters;

/**
 * Marshals {@link AndroidCustomField} properties.
 * @author Michael Angstadt
 */
public class AndroidCustomFieldScribe extends VCardPropertyScribe<AndroidCustomField> {
	private final Pattern uriRegex = Pattern.compile("^vnd\\.android\\.cursor\\.(dir|item)/(.*)");
	
	public AndroidCustomFieldScribe() {
		super(AndroidCustomField.class, "X-ANDROID-CUSTOM");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return null;
	}

	@Override
	protected String _writeText(AndroidCustomField property, VCardVersion version) {
		String type = property.getType();
		List<String> values = property.getValues();

		List<Object> out = new ArrayList<Object>();
		String uri = "vnd.android.cursor." + (property.isDir() ? "dir" : "item") + "/" + (type == null ? "" : type);
		out.add(uri);
		if (property.isItem()) {
			out.add(values.isEmpty() ? "" : values.get(0));
		} else {
			out.addAll(values);
		}
		return structured(out.toArray());
	}

	@Override
	protected AndroidCustomField _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		VCardPropertyScribe.SemiStructuredIterator it = semistructured(value);

		String uri = it.next();
		if (uri == null){
			throw new SkipMeException("Property value is blank.");
		}
		
		Matcher matcher = uriRegex.matcher(uri);
		if (!matcher.find()){
			throw new SkipMeException("Property URI is invalid: " + uri);
		}
		
		AndroidCustomField property = new AndroidCustomField();
		property.setDir(matcher.group(1).equals("dir"));
		property.setType(matcher.group(2));
		while (it.hasNext()) {
			property.getValues().add(it.next());
		}
		return property;
	}
}
