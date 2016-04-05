package ezvcard.util;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import ezvcard.VCardVersion;
import ezvcard.property.VCardProperty;

public class JCardPrettyPrinter extends DefaultPrettyPrinter {
	private static final long serialVersionUID = 1L;

	/**
	 * Alias for {@link DefaultIndenter#SYSTEM_LINEFEED_INSTANCE}
	 */
	public static final Indenter NEWLINE_INDENTER = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
	/**
	 * Instance of {@link DefaultPrettyPrinter.FixedSpaceIndenter}
	 */
	public static final Indenter INLINE_INDENTER = new DefaultPrettyPrinter.FixedSpaceIndenter();

	private Indenter propertyIndenter = INLINE_INDENTER;
	private Indenter arrayIndenter = NEWLINE_INDENTER;
	private Indenter objectIndenter = NEWLINE_INDENTER;

	public JCardPrettyPrinter() {
		super.indentArraysWith(arrayIndenter);
		super.indentObjectsWith(objectIndenter);
	}
	
	public JCardPrettyPrinter(JCardPrettyPrinter base) {
		super(base);
		this.propertyIndenter = base.propertyIndenter;
		this.arrayIndenter = base.arrayIndenter;
		this.objectIndenter = base.objectIndenter;
		super.indentArraysWith(arrayIndenter);
		super.indentObjectsWith(objectIndenter);
	}
	
	@Override
	public DefaultPrettyPrinter createInstance() {
		return new JCardPrettyPrinter(this);
	}

	@Override
	public void indentArraysWith(Indenter i) {
		arrayIndenter = i;
		super.indentArraysWith(i);
	}

	@Override
	public void indentObjectsWith(Indenter i) {
		objectIndenter = i;
		super.indentObjectsWith(i);
	}

	public void indentVCardPropertiesWith(Indenter i) {
		propertyIndenter = i;
	}

	protected static boolean isInVCardProperty(JsonStreamContext context) {
		if (context == null) {
			return false;
		} else if (context.getCurrentValue() instanceof VCardProperty
				|| context.getCurrentValue() instanceof VCardVersion) {
			return true;
		} else {
			return isInVCardProperty(context.getParent());
		}
	}

	private void updateIndenter(JsonStreamContext context) {
		if (isInVCardProperty(context)) {
			super.indentArraysWith(propertyIndenter);
			super.indentObjectsWith(propertyIndenter);
		} else {
			super.indentArraysWith(arrayIndenter);
			super.indentObjectsWith(objectIndenter);
		}
	}

	@Override
	public void writeStartArray(JsonGenerator gen) throws IOException, JsonGenerationException {
		updateIndenter(gen.getOutputContext().getParent());
		super.writeStartArray(gen);
	}

	@Override
	public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException, JsonGenerationException {
		updateIndenter(gen.getOutputContext().getParent());
		super.writeEndArray(gen, nrOfValues);
	}

	@Override
	public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
		updateIndenter(gen.getOutputContext().getParent());
		super.writeArrayValueSeparator(gen);
	}
}
