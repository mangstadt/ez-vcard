package ezvcard.io.text;

import ezvcard.VCardVersion;

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
 */

/**
 * Used to pass information about the vCard being written to the property
 * scribes.
 * @author Michael Angstadt
 */
public class WriteContext {
	private final VCardVersion version;
	private final TargetApplication targetApplication;
	private final boolean includeTrailingSemicolons;

	/**
	 * @param version the version that the vCard should adhere to
	 * @param targetApplication the application that the vCard is being targeted
	 * for or null not to target any specific application
	 * @param includeTrailingSemicolons true to include trailing semicolon
	 * delimiters for structured property values whose list of values end with
	 * null or empty values, false not to
	 */
	public WriteContext(VCardVersion version, TargetApplication targetApplication, boolean includeTrailingSemicolons) {
		this.version = version;
		this.targetApplication = targetApplication;
		this.includeTrailingSemicolons = includeTrailingSemicolons;
	}

	/**
	 * Gets the version that the vCard should adhere to.
	 * @return the vCard version
	 */
	public VCardVersion getVersion() {
		return version;
	}

	/**
	 * Gets the application that the vCard is being targeted for.
	 * @return the target application or null not to target any specific
	 * application
	 */
	public TargetApplication getTargetApplication() {
		return targetApplication;
	}

	/**
	 * Gets whether to include trailing semicolon delimiters for structured
	 * property values whose list of values end with null or empty values.
	 * @return true to include the trailing semicolons, false not to
	 */
	public boolean isIncludeTrailingSemicolons() {
		return includeTrailingSemicolons;
	}
}
