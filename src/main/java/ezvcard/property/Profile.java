package ezvcard.property;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
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
 * Simply identifies the vCard as a "vCard".
 * </p>
 * 
 * <p>
 * <b>Code sample</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * Profile profile = new Profile();
 * vcard.setProfile(profile);
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code PROFILE}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 3.0}
 * </p>
 * @author Michael Angstadt
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Profile extends TextProperty {
	public Profile() {
		super("VCARD");
	}

	@Override
	public Set<VCardVersion> _supportedVersions() {
		return EnumSet.of(VCardVersion.V3_0);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (!"VCARD".equalsIgnoreCase(value)) {
			//see RFC 2426 p.5
			warnings.add(new Warning(18, value));
		}
	}
}
