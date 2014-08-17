package ezvcard.android;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.SkipMeException;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.VCardParameters;

/*
 Copyright (c) 2014, Michael Angstadt
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
