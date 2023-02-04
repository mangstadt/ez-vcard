package ezvcard.property;

import java.util.UUID;

/*
 Copyright (c) 2012-2023, Michael Angstadt
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
 * Defines a globally unique identifier for this vCard.
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Uid uid = new Uid("urn:uuid:b8767877-b4a1-4c70-9acc-505d3819e519");
 * vcard.setUid(uid);
 * 
 * //or, generate a random UID
 * uid = Uid.random();
 * vcard.setUid(uid);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code UID}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0, 4.0}
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6350#page-46">RFC 6350 p.46</a>
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-24">RFC 2426 p.24</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.21</a>
 */
public class Uid extends UriProperty {
	/**
	 * Creates a UID property.
	 * @param uid the UID
	 */
	public Uid(String uid) {
		super(uid);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Uid(Uid original) {
		super(original);
	}

	/**
	 * Creates a UID property that contains a random UUID URI.
	 * @return the property
	 */
	public static Uid random() {
		String uuid = UUID.randomUUID().toString();
		return new Uid("urn:uuid:" + uuid);
	}

	@Override
	public Uid copy() {
		return new Uid(this);
	}
}
