package com.ezvcard.android;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
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

		List<Object> out = new ArrayList<Object>(values.size() + 1);
		out.add("vnd.android.cursor/" + type); //TODO what's the full URI?
		out.addAll(values);

		return structured(out.toArray());
	}

	@Override
	protected AndroidCustomField _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		VCardPropertyScribe.SemiStructuredIterator it = semistructured(value);

		String uriStr = it.next();
		if (uriStr == null || !uriStr.contains("vnd.android.cursor")) {
			throw new SkipMeException("Property URI is invalid: " + uriStr);
		}

		Uri uri = Uri.parse(uriStr);
		List<String> pathSegments = uri.getPathSegments();
		String type = null;
		if (pathSegments != null && pathSegments.size() >= 2) {
			type = pathSegments.get(1);
		}

		List<String> values = new ArrayList<String>();
		while (it.hasNext()) {
			values.add(it.next());
		}

		return new AndroidCustomField(type, values);
	}
}
