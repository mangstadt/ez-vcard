package ezvcard.parameters;

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
 Copyright (c) 2012, Michael Angstadt
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
 * Represents the TYPE parameter of the SOUND type.
 * <p>
 * <b>Supported versions:</b> <code>2.1, 3.0, 4.0</code>
 * </p>
 * @author George El-Haddad Mar 10, 2010
 * @author Michael Angstadt
 */
public class SoundTypeParameter extends MediaTypeParameter {
	public static final SoundTypeParameter AAC = new SoundTypeParameter("AAC", "audio/aac", "aac");
	public static final SoundTypeParameter MIDI = new SoundTypeParameter("MIDI", "audio/midi", "mid");
	public static final SoundTypeParameter MP3 = new SoundTypeParameter("MP3", "audio/mp3", "mp3");
	public static final SoundTypeParameter MPEG = new SoundTypeParameter("MPEG", "audio/mpeg", "mpeg");
	public static final SoundTypeParameter OGG = new SoundTypeParameter("OGG", "audio/ogg", "ogg");
	public static final SoundTypeParameter WAV = new SoundTypeParameter("WAV", "audio/wav", "wav");

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard TYPEs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "ogg")
	 * @param mediaType the media type (e.g. "audio/ogg")
	 * @param extension the file extension used for this type (e.g. "ogg")
	 */
	public SoundTypeParameter(String value, String mediaType, String extension) {
		super(value, mediaType, extension);
	}

	/**
	 * Searches the static objects in this class for one that has a certain type
	 * value.
	 * @param value the type value to search for (e.g. "ogg")
	 * @return the object or null if not found
	 */
	public static SoundTypeParameter valueOf(String value) {
		return findByValue(value, SoundTypeParameter.class);
	}

	/**
	 * Searches the static objects in this class for one that has a certain
	 * media type.
	 * @param mediaType the media type to search for (e.g. "audio/ogg")
	 * @return the object or null if not found
	 */
	public static SoundTypeParameter findByMediaType(String mediaType) {
		return findByMediaType(mediaType, SoundTypeParameter.class);
	}
}
