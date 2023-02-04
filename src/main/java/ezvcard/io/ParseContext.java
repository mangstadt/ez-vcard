package ezvcard.io;

import java.util.ArrayList;
import java.util.List;

import ezvcard.VCardVersion;

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
 * Stores information used during the parsing of a vCard.
 * @author Michael Angstadt
 */
public class ParseContext {
	private VCardVersion version;
	private List<ParseWarning> warnings = new ArrayList<>();
	private Integer lineNumber;
	private String propertyName;

	/**
	 * Gets the version of the vCard being parsed.
	 * @return the vCard version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of the vCard being parsed.
	 * @param version the vCard version
	 */
	public void setVersion(VCardVersion version) {
		this.version = version;
	}

	/**
	 * Gets the line number the parser is currently on.
	 * @return the line number or null if not applicable
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/**
	 * Sets the line number the parser is currently on.
	 * @param lineNumber the line number or null if not applicable
	 */
	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * Gets the name of the property that the parser is currently parsing.
	 * @return the property name (e.g. "FN") or null if not applicable
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Sets the name of the property that the parser is currently parsing.
	 * @param propertyName the property name (e.g. "FN") or null if not
	 * applicable
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Adds a parse warning.
	 * @param code the warning code
	 * @param args the warning message arguments
	 */
	public void addWarning(int code, Object... args) {
		//@formatter:off
		warnings.add(new ParseWarning.Builder(this)
			.message(code, args)
		.build());
		//@formatter:on
	}

	/**
	 * Adds a parse warning.
	 * @param message the warning message
	 */
	public void addWarning(String message) {
		//@formatter:off
		warnings.add(new ParseWarning.Builder(this)
			.message(message)
		.build());
		//@formatter:on
	}

	/**
	 * Gets the parse warnings.
	 * @return the parse warnings
	 */
	public List<ParseWarning> getWarnings() {
		return warnings;
	}
}
