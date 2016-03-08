package ezvcard.parameter;

import java.lang.reflect.Constructor;

import ezvcard.util.CaseClasses;

/*
 Copyright (c) 2012-2016, Michael Angstadt
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
 * Manages the list of pre-defined values for a media type parameter.
 * @author Michael Angstadt
 * @param <T> the parameter class
 */
public class MediaTypeCaseClasses<T extends MediaTypeParameter> extends CaseClasses<T, String[]> {
	public MediaTypeCaseClasses(Class<T> clazz) {
		super(clazz);
	}

	@Override
	protected T create(String[] value) {
		try {
			//reflection: return new ClassName(value, mediaType, extension);
			Constructor<T> constructor = clazz.getDeclaredConstructor(String.class, String.class, String.class);
			constructor.setAccessible(true);
			return constructor.newInstance(value[0], value[1], value[2]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean matches(T object, String[] value) {
		String objectValues[] = new String[] { object.getValue(), object.getMediaType(), object.getExtension() };
		for (int i = 0; i < value.length; i++) {
			String v = value[i];
			if (v != null && !v.equalsIgnoreCase(objectValues[i])) {
				return false;
			}
		}
		return true;
	}
}
