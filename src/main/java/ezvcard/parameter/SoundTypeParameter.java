package ezvcard.parameter;

import java.util.Collection;

import ezvcard.property.Sound;

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
 Copyright (c) 2013, Michael Angstadt
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
 * Represents the TYPE parameter of the {@link Sound} type.
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author George El-Haddad Mar 10, 2010
 * @author Michael Angstadt
 */
public class SoundTypeParameter extends MediaTypeParameter {
	private static final MediaTypeCaseClasses<SoundTypeParameter> enums = new MediaTypeCaseClasses<SoundTypeParameter>(SoundTypeParameter.class);

	public static final SoundTypeParameter AAC = new SoundTypeParameter("AAC", "audio/aac", "aac");
	public static final SoundTypeParameter MIDI = new SoundTypeParameter("MIDI", "audio/midi", "mid");
	public static final SoundTypeParameter MP3 = new SoundTypeParameter("MP3", "audio/mp3", "mp3");
	public static final SoundTypeParameter MPEG = new SoundTypeParameter("MPEG", "audio/mpeg", "mpeg");
	public static final SoundTypeParameter OGG = new SoundTypeParameter("OGG", "audio/ogg", "ogg");
	public static final SoundTypeParameter WAV = new SoundTypeParameter("WAV", "audio/wav", "wav");

	private SoundTypeParameter(String value, String mediaType, String extension) {
		super(value, mediaType, extension);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param type the TYPE parameter value to search for (e.g. "MP3") or null
	 * to not search by this value
	 * @param mediaType the media type to search for (e.g. "audio/mp3") or null
	 * to not search by this value
	 * @param extension the file extension to search for (excluding the ".",
	 * e.g. "mp3") or null to not search by this value
	 * @return the object or null if not found
	 */
	public static SoundTypeParameter find(String type, String mediaType, String extension) {
		return enums.find(new String[] { type, mediaType, extension });
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param type the TYPE parameter value to search for (e.g. "MP3") or null
	 * to not search by this value
	 * @param mediaType the media type to search for (e.g. "audio/mp3") or null
	 * to not search by this value
	 * @param extension the file extension to search for (excluding the ".",
	 * e.g. "mp3") or null to not search by this value
	 * @return the object
	 */
	public static SoundTypeParameter get(String type, String mediaType, String extension) {
		return enums.get(new String[] { type, mediaType, extension });
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<SoundTypeParameter> all() {
		return enums.all();
	}
}
