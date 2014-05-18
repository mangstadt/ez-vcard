package ezvcard.io.xml;

import org.xml.sax.SAXException;

/**
 * Thrown from {@link XCardListener#vcardRead(ezvcard.VCard, java.util.List)
 * XCardListener.vcardRead()} to signal that the xCard reader should stop
 * parsing vCards.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class StopReadingException extends SAXException {
	//empty
}
