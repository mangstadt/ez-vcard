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

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * Defines the location of the person's death.
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
 * Deathplace deathplace = new Deathplace(&quot;Wilmslow, Cheshire, England&quot;);
 * vcard.setDeathplace(deathplace);
 * 
 * //geo coordinates
 * deathplace = new Deathplace(53.325, -2.239);
 * vcard.setDeathplace(deathplace);
 * 
 * //URI
 * deathplace = new Deathplace();
 * deathplace.setUri(&quot;http://en.wikipedia.org/wiki/Wilmslow&quot;);
 * vcard.setDeathplace(deathplace);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Deathplace deathplace = vcard.getDeathplace();
 * 
 * String text = deathplace.getText();
 * if (text != null){
 *   //property value is plain text
 * }
 * 
 * Double latitude = deathplace.getLatitude();
 * Double longitude = deathplace.getLongitude();
 * if (latitude != null){
 *   //property value is a set of geo coordinates
 * }
 * 
 * String uri = deathplace.getUri();
 * if (uri != null){
 *   //property value is a URI
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code DEATHPLACE}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6474">RFC 6474</a>
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Deathplace extends PlaceProperty {
	/**
	 * Creates a new deathplace property.
	 */
	public Deathplace() {
		super();
	}

	/**
	 * Creates a new deathplace property.
	 * @param latitude the latitude coordinate of the place
	 * @param longitude the longitude coordinate of the place
	 */
	public Deathplace(double latitude, double longitude) {
		super(latitude, longitude);
	}

	/**
	 * Creates a new deathplace property.
	 * @param text a text value representing the place
	 */
	public Deathplace(String text) {
		super(text);
	}
}
