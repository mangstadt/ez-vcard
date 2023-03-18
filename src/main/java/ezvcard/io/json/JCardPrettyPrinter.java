package ezvcard.io.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

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
 * A JSON pretty-printer for jCards.
 * @author Buddy Gorven
 * @author Michael Angstadt
 */
public class JCardPrettyPrinter extends DefaultPrettyPrinter {
	private static final long serialVersionUID = 1L;

	/**
	 * The value that is assigned to {@link JsonGenerator#setCurrentValue} to
	 * let the pretty-printer know that a vCard property is currently being
	 * written.
	 */
	public static final Object PROPERTY_VALUE = "vcard-property";

	/**
	 * Alias for {@link DefaultIndenter#SYSTEM_LINEFEED_INSTANCE}
	 */
	private static final Indenter NEWLINE_INDENTER = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
	/**
	 * Instance of {@link DefaultPrettyPrinter.FixedSpaceIndenter}
	 */
	private static final Indenter INLINE_INDENTER = new DefaultPrettyPrinter.FixedSpaceIndenter();

	private Indenter propertyIndenter, arrayIndenter, objectIndenter;

	public JCardPrettyPrinter() {
		propertyIndenter = INLINE_INDENTER;
		indentArraysWith(NEWLINE_INDENTER);
		indentObjectsWith(NEWLINE_INDENTER);
	}

	public JCardPrettyPrinter(JCardPrettyPrinter base) {
		super(base);
		propertyIndenter = base.propertyIndenter;
		indentArraysWith(base.arrayIndenter);
		indentObjectsWith(base.objectIndenter);
	}

	@Override
	public JCardPrettyPrinter createInstance() {
		return new JCardPrettyPrinter(this);
	}

	@Override
	public void indentArraysWith(Indenter indenter) {
		arrayIndenter = indenter;
		super.indentArraysWith(indenter);
	}

	@Override
	public void indentObjectsWith(Indenter indenter) {
		objectIndenter = indenter;
		super.indentObjectsWith(indenter);
	}

	public void indentVCardPropertiesWith(Indenter indenter) {
		propertyIndenter = indenter;
	}

	protected static boolean isInVCardProperty(JsonStreamContext context) {
		if (context == null) {
			return false;
		}

		Object currentValue = context.getCurrentValue();
		if (currentValue == PROPERTY_VALUE) {
			return true;
		}

		return isInVCardProperty(context.getParent());
	}

	private void updateIndenter(JsonStreamContext context) {
		boolean inVCardProperty = isInVCardProperty(context);
		super.indentArraysWith(inVCardProperty ? propertyIndenter : arrayIndenter);
		super.indentObjectsWith(inVCardProperty ? propertyIndenter : objectIndenter);
	}

	@Override
	public void writeStartArray(JsonGenerator gen) throws IOException, JsonGenerationException {
		updateIndenter(gen.getOutputContext().getParent());
		super.writeStartArray(gen);
	}

	@Override
	public void writeEndArray(JsonGenerator gen, int numValues) throws IOException, JsonGenerationException {
		updateIndenter(gen.getOutputContext().getParent());
		super.writeEndArray(gen, numValues);
	}

	@Override
	public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
		updateIndenter(gen.getOutputContext().getParent());
		super.writeArrayValueSeparator(gen);
	}
}