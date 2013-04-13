package ezvcard.parameters;

/*
 Copyright (c) 2013, Michael Angstadt
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
 * Represents the TYPE parameter of the RELATED type.
 * <p>
 * <b>Supported versions:</b> <code>4.0</code>
 * </p>
 * @author Michael Angstadt
 */
public class RelatedTypeParameter extends TypeParameter {
	public static final RelatedTypeParameter ACQUAINTANCE = new RelatedTypeParameter("acquaintance");
	public static final RelatedTypeParameter AGENT = new RelatedTypeParameter("agent");
	public static final RelatedTypeParameter CHILD = new RelatedTypeParameter("child");
	public static final RelatedTypeParameter CO_RESIDENT = new RelatedTypeParameter("co-resident");
	public static final RelatedTypeParameter CO_WORKER = new RelatedTypeParameter("co-worker");
	public static final RelatedTypeParameter COLLEAGUE = new RelatedTypeParameter("colleague");
	public static final RelatedTypeParameter CONTACT = new RelatedTypeParameter("contact");
	public static final RelatedTypeParameter CRUSH = new RelatedTypeParameter("crush");
	public static final RelatedTypeParameter DATE = new RelatedTypeParameter("date");
	public static final RelatedTypeParameter EMERGENCY = new RelatedTypeParameter("emergency");
	public static final RelatedTypeParameter FRIEND = new RelatedTypeParameter("friend");
	public static final RelatedTypeParameter KIN = new RelatedTypeParameter("kin");
	public static final RelatedTypeParameter ME = new RelatedTypeParameter("me");
	public static final RelatedTypeParameter MET = new RelatedTypeParameter("met");
	public static final RelatedTypeParameter MUSE = new RelatedTypeParameter("muse");
	public static final RelatedTypeParameter NEIGHBOR = new RelatedTypeParameter("neighbor");
	public static final RelatedTypeParameter PARENT = new RelatedTypeParameter("parent");
	public static final RelatedTypeParameter SIBLING = new RelatedTypeParameter("sibling");
	public static final RelatedTypeParameter SPOUSE = new RelatedTypeParameter("spouse");
	public static final RelatedTypeParameter SWEETHEART = new RelatedTypeParameter("sweetheart");

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard TYPEs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "spouse")
	 */
	public RelatedTypeParameter(String value) {
		super(value);
	}

	/**
	 * Searches the static objects in this class for one that has a certain type
	 * value.
	 * @param value the type value to search for (e.g. "spouse")
	 * @return the object or null if not found
	 */
	public static RelatedTypeParameter valueOf(String value) {
		return findByValue(value, RelatedTypeParameter.class);
	}
}
