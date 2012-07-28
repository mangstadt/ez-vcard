package ezvcard.parameters;


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
 * Represents a CALSCALE parameter.
 * <p>
 * vCard versions: 4.0
 * </p>
 * @author Michael Angstadt
 */
public class CalscaleParameter extends VCardParameter {
	public static final String NAME = "CALSCALE";

	public static final CalscaleParameter GREGORIAN = new CalscaleParameter("gregorian");

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard values. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "gregorian")
	 */
	public CalscaleParameter(String value) {
		super(NAME, value);
	}

	/**
	 * Searches the static objects in this class for one that has a certain type
	 * value.
	 * @param value the type value to search for (e.g. "work")
	 * @return the object or null if not found
	 */
	public static CalscaleParameter valueOf(String value) {
		return findByValue(value, CalscaleParameter.class);
	}
}
