package ezvcard.types;

import ezvcard.VCardVersion;

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
 * Represents the KIND type.
 * @author Michael Angstadt
 */
public class KindType extends TextType {
	public static final String NAME = "KIND";

	public static final String INDIVIDUAL = "individual";
	public static final String GROUP = "group";
	public static final String ORG = "org";
	public static final String LOCATION = "location";

	public KindType() {
		super(NAME);
	}

	/**
	 * Use of this constructor is discouraged. Please use one of the static
	 * methods to create a new KIND type.
	 * @param kind the kind value
	 */
	public KindType(String kind) {
		super(NAME, kind);
	}

	@Override
	public VCardVersion[] getSupportedVersions() {
		return new VCardVersion[] { VCardVersion.V4_0 };
	}

	public boolean isIndividual() {
		return INDIVIDUAL.equals(value);
	}

	public boolean isGroup() {
		return GROUP.equals(value);
	}

	public boolean isOrg() {
		return ORG.equals(value);
	}

	public boolean isLocation() {
		return LOCATION.equals(value);
	}

	public static KindType individual() {
		return new KindType(INDIVIDUAL);
	}

	public static KindType group() {
		return new KindType(GROUP);
	}

	public static KindType org() {
		return new KindType(ORG);
	}

	public static KindType location() {
		return new KindType(LOCATION);
	}
}
