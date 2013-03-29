package ezvcard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
 * Represents the value of a jCard property.
 * @author Michael Angstadt
 */
public class JCardValue {
	private JCardDataType dataType;
	private final List<List<Object>> values = new ArrayList<List<Object>>();
	private boolean structured;

	private JCardValue(JCardDataType dataType, boolean structured) {
		this.dataType = dataType;
		this.structured = structured;
	}

	public static JCardValue single(JCardDataType dataType, Object value) {
		JCardValue v = new JCardValue(dataType, false);
		v.setValue(value);
		return v;
	}

	public static JCardValue multi(JCardDataType dataType, List<Object> values) {
		JCardValue v = new JCardValue(dataType, false);
		v.addAllValues(values);
		return v;
	}

	public static JCardValue structured(JCardDataType dataType, List<Object> values) {
		JCardValue v = new JCardValue(dataType, true);
		v.addAllValues(values);
		return v;
	}

	public JCardDataType getDataType() {
		return dataType;
	}

	public void setDataType(JCardDataType dataType) {
		this.dataType = dataType;
	}

	public boolean isStructured() {
		return structured;
	}

	public void setStructured(boolean structured) {
		this.structured = structured;
	}

	public List<List<Object>> getValues() {
		return values;
	}

	public void setValue(Object value) {
		values.clear();
		addValue(value);
	}

	public void addValue(Object value) {
		addArrayValue(Arrays.asList(value));
	}

	public void addAllValues(List<Object> values) {
		for (Object value : values) {
			addValue(value);
		}
	}

	public void addArrayValue(List<Object> values) {
		this.values.add(values);
	}
}
