package ezvcard.io.xml;

import java.util.HashMap;
import java.util.Map;

import ezvcard.VCardDataType;
import ezvcard.VCardVersion;
import ezvcard.io.StreamWriter;
import ezvcard.parameter.VCardParameters;

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
 * Base class for xCard writers.
 * @author Michael Angstadt
 */
abstract class XCardWriterBase extends StreamWriter {
	protected final VCardVersion targetVersion = VCardVersion.V4_0; //xCard only supports 4.0

	/**
	 * Defines the names (data types) of the XML elements that are used to hold
	 * each parameter's value.
	 */
	protected final Map<String, VCardDataType> parameterDataTypes = new HashMap<String, VCardDataType>();
	{
		registerParameterDataType(VCardParameters.ALTID, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.CALSCALE, VCardDataType.TEXT);
		//registerParameterDataType(VCardParameters.CHARSET, VCardDataType.TEXT); //not 4.0
		//registerParameterDataType(VCardParameters.ENCODING, VCardDataType.TEXT); //not 4.0
		registerParameterDataType(VCardParameters.GEO, VCardDataType.URI);
		registerParameterDataType(VCardParameters.INDEX, VCardDataType.INTEGER);
		registerParameterDataType(VCardParameters.LABEL, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.LANGUAGE, VCardDataType.LANGUAGE_TAG);
		registerParameterDataType(VCardParameters.LEVEL, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.MEDIATYPE, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.PID, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.PREF, VCardDataType.INTEGER);
		registerParameterDataType(VCardParameters.SORT_AS, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.TYPE, VCardDataType.TEXT);
		registerParameterDataType(VCardParameters.TZ, VCardDataType.URI);
		//registerParameterDataType(VCardParameters.VALUE, VCardDataType.TEXT); //not used by xCard
	}

	@Override
	protected VCardVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Registers the data type of an experimental parameter. Experimental
	 * parameters use the "unknown" data type by default.
	 * @param parameterName the parameter name (e.g. "x-foo")
	 * @param dataType the data type or null to remove
	 */
	public void registerParameterDataType(String parameterName, VCardDataType dataType) {
		parameterName = parameterName.toLowerCase();
		if (dataType == null) {
			parameterDataTypes.remove(parameterName);
		} else {
			parameterDataTypes.put(parameterName, dataType);
		}
	}
}
