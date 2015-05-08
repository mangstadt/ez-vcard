package ezvcard.property;

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

import lombok.*;

/**
 * <p>
 * Defines the location of the person's birth.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //text
 * Birthplace birthplace = new Birthplace(&quot;Maida Vale, London, United Kingdom&quot;);
 * vcard.setBirthplace(birthplace);
 * 
 * //geo coordinates
 * birthplace = new Birthplace(51.5274, -0.1899);
 * vcard.setBirthplace(birthplace);
 * 
 * //URI
 * birthplace = new Birthplace();
 * birthplace.setUri(&quot;http://en.wikipedia.org/wiki/Maida_Vale&quot;);
 * vcard.setBirthplace(birthplace);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Birthplace birthplace = vcard.getBirthplace();
 * 
 * String text = birthplace.getText();
 * if (text != null){
 *   //property value is plain text
 * }
 * 
 * Double latitude = birthplace.getLatitude();
 * Double longitude = birthplace.getLongitude();
 * if (latitude != null){
 *   //property value is a set of geo coordinates
 * }
 * 
 * String uri = birthplace.getUri();
 * if (uri != null){
 *   //property value is a URI
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code BIRTHPLACE}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Birthplace extends PlaceProperty {
	/**
	 * Creates a new birthplace property.
	 */
	public Birthplace() {
		super();
	}

	/**
	 * Creates a new birthplace property.
	 * @param latitude the latitude coordinate of the place
	 * @param longitude the longitude coordinate of the place
	 */
	public Birthplace(double latitude, double longitude) {
		super(latitude, longitude);
	}

	/**
	 * Creates a new birthplace property.
	 * @param text a text value representing the place
	 */
	public Birthplace(String text) {
		super(text);
	}
}
