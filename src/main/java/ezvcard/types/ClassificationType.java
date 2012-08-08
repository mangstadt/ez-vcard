package ezvcard.types;

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
 * Describes the sensitivity of the information in the vCard.
 * 
 * <pre>
 * VCard vcard = new VCard();
 * //sample values: PUBLIC, PRIVATE, CONFIDENTIAL
 * ClassificationType classType = new ClassificationType(&quot;PUBLIC&quot;);
 * vcard.setClassification(classType);
 * </pre>
 * 
 * <p>
 * vCard property name: CLASS
 * </p>
 * <p>
 * vCard versions: 3.0
 * </p>
 * @author Michael Angstadt
 */
public class ClassificationType extends TextType {
	public static final String NAME = "CLASS";

	public ClassificationType() {
		this(null);
	}

	/**
	 * @param classValue the classification (e.g. "PUBLIC", "PRIVATE",
	 * "CONFIDENTIAL")
	 */
	public ClassificationType(String classValue) {
		super(NAME, classValue);
	}
}
