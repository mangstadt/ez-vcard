package ezvcard.property;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ezvcard.parameter.ImageType;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * <p>
 * Defines a photo, such as the person's portrait.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //URL
 * Photo photo = new Photo("http://www.mywebsite.com/my-photo.jpg", ImageType.JPEG);
 * vcard.addPhoto(photo);
 * 
 * //binary data
 * byte data[] = ...
 * photo = new Photo(data, ImageType.JPEG);
 * vcard.addPhoto(photo);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * for (Photo photo : vcard.getPhotos()){
 *   PhotoType contentType = photo.getContentType(); //e.g. "image/jpeg"
 * 
 *   String url = photo.getUrl();
 *   if (url != null){
 *     //property value is a URL
 *     continue;
 *   }
 *   
 *   byte[] data = photo.getData();
 *   if (data != null){
 *     //property value is binary data
 *     continue;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code PHOTO}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Photo extends ImageProperty {
	/**
	 * Creates a photo property.
	 * @param url the URL to the photo
	 * @param type the content type (e.g. JPEG)
	 */
	public Photo(String url, ImageType type) {
		super(url, type);
	}

	/**
	 * Creates a photo property.
	 * @param data the binary data of the photo
	 * @param type the content type (e.g. JPEG)
	 */
	public Photo(byte[] data, ImageType type) {
		super(data, type);
	}

	/**
	 * Creates a photo property.
	 * @param in an input stream to the binary data (will be closed)
	 * @param type the content type (e.g. JPEG)
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public Photo(InputStream in, ImageType type) throws IOException {
		super(in, type);
	}

	/**
	 * Creates a photo property.
	 * @param file the image file
	 * @param type the content type (e.g. JPEG)
	 * @throws IOException if there's a problem reading from the file
	 */
	public Photo(File file, ImageType type) throws IOException {
		super(file, type);
	}
}
