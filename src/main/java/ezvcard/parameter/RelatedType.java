package ezvcard.parameter;

import java.util.Collection;

import ezvcard.property.Related;

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
 * Represents the TYPE parameter of the {@link Related} property.
 * <p>
 * <b>Supported versions:</b> {@code 4.0}
 * </p>
 * @author Michael Angstadt
 */
public class RelatedType extends VCardParameter {
	private static final VCardParameterCaseClasses<RelatedType> enums = new VCardParameterCaseClasses<RelatedType>(RelatedType.class);

	public static final RelatedType ACQUAINTANCE = new RelatedType("acquaintance");
	public static final RelatedType AGENT = new RelatedType("agent");
	public static final RelatedType CHILD = new RelatedType("child");
	public static final RelatedType CO_RESIDENT = new RelatedType("co-resident");
	public static final RelatedType CO_WORKER = new RelatedType("co-worker");
	public static final RelatedType COLLEAGUE = new RelatedType("colleague");
	public static final RelatedType CONTACT = new RelatedType("contact");
	public static final RelatedType CRUSH = new RelatedType("crush");
	public static final RelatedType DATE = new RelatedType("date");
	public static final RelatedType EMERGENCY = new RelatedType("emergency");
	public static final RelatedType FRIEND = new RelatedType("friend");
	public static final RelatedType KIN = new RelatedType("kin");
	public static final RelatedType ME = new RelatedType("me");
	public static final RelatedType MET = new RelatedType("met");
	public static final RelatedType MUSE = new RelatedType("muse");
	public static final RelatedType NEIGHBOR = new RelatedType("neighbor");
	public static final RelatedType PARENT = new RelatedType("parent");
	public static final RelatedType SIBLING = new RelatedType("sibling");
	public static final RelatedType SPOUSE = new RelatedType("spouse");
	public static final RelatedType SWEETHEART = new RelatedType("sweetheart");

	private RelatedType(String value) {
		super(value);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static RelatedType find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * {@code ==} equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static RelatedType get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<RelatedType> all() {
		return enums.all();
	}
}
