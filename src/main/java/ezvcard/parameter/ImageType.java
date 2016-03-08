package ezvcard.parameter;

import java.util.Collection;

import ezvcard.property.Logo;
import ezvcard.property.Photo;

/**
 * Copyright 2011 George El-Haddad. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of George El-Haddad.
 */

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Represents an image media type used in the TYPE parameter, MEDIATYPE
 * parameter, and data URIs of the {@link Photo} and {@link Logo}
 * properties.
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author George El-Haddadt Mar 10, 2010
 * @author Michael Angstadt
 */
public class ImageType extends MediaTypeParameter {
	private static final MediaTypeCaseClasses<ImageType> enums = new MediaTypeCaseClasses<ImageType>(ImageType.class);

	public static final ImageType GIF = new ImageType("GIF", "image/gif", "gif");
	public static final ImageType JPEG = new ImageType("JPEG", "image/jpeg", "jpg");
	public static final ImageType PNG = new ImageType("PNG", "image/png", "png");

	private ImageType(String value, String mediaType, String extension) {
		super(value, mediaType, extension);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param type the TYPE parameter value to search for (e.g. "JPEG") or null
	 * to not search by this value
	 * @param mediaType the media type to search for (e.g. "image/png") or null
	 * to not search by this value
	 * @param extension the file extension to search for (excluding the ".",
	 * e.g. "jpg") or null to not search by this value
	 * @return the object or null if not found
	 */
	public static ImageType find(String type, String mediaType, String extension) {
		return enums.find(new String[] { type, mediaType, extension });
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param type the TYPE parameter value to search for (e.g. "JPEG") or null
	 * to not search by this value
	 * @param mediaType the media type to search for (e.g. "image/png") or null
	 * to not search by this value
	 * @param extension the file extension to search for (excluding the ".",
	 * e.g. "jpg") or null to not search by this value
	 * @return the object
	 */
	public static ImageType get(String type, String mediaType, String extension) {
		return enums.get(new String[] { type, mediaType, extension });
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<ImageType> all() {
		return enums.all();
	}
}
