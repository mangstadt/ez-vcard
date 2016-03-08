package ezvcard.parameter;

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
 * Represents a TYPE parameter that also has a media type associated with it.
 * The TYPE parameter value is used in 2.1 and 3.0 vCards, while the media type
 * value is used in 4.0 vCards.
 * @author Michael Angstadt
 */
public class MediaTypeParameter extends VCardParameter {
	protected final String mediaType;
	protected final String extension;

	/**
	 * @param value the TYPE parameter value (e.g. "JPEG")
	 * @param mediaType the media type (e.g. "image/jpeg")
	 * @param extension the file extension (e.g. "jpg")
	 */
	public MediaTypeParameter(String value, String mediaType, String extension) {
		super(value);
		this.mediaType = mediaType;
		this.extension = extension;
	}

	/**
	 * Gets the media type.
	 * @return the media type (e.g. "image/jpeg")
	 */
	public String getMediaType() {
		return mediaType;
	}

	/**
	 * Gets the file extension.
	 * @return the file extension (e.g. "jpg")
	 */
	public String getExtension() {
		return extension;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((mediaType == null) ? 0 : mediaType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaTypeParameter other = (MediaTypeParameter) obj;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (mediaType == null) {
			if (other.mediaType != null)
				return false;
		} else if (!mediaType.equals(other.mediaType))
			return false;
		return true;
	}
}
