package ezvcard.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ezvcard.VCard;
import ezvcard.io.scribe.ScribeIndex;
import ezvcard.io.scribe.VCardPropertyScribe;
import ezvcard.parameter.AddressType;
import ezvcard.property.Address;
import ezvcard.property.Label;
import ezvcard.property.VCardProperty;

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
 * Parses vCards from a data stream.
 * @author Michael Angstadt
 */
public abstract class StreamReader implements Closeable {
	protected final ParseWarnings warnings = new ParseWarnings();
	protected ScribeIndex index = new ScribeIndex();

	/**
	 * Reads all vCards from the data stream.
	 * @return the vCards
	 * @throws IOException if there's a problem reading from the stream
	 */
	public List<VCard> readAll() throws IOException {
		List<VCard> vcards = new ArrayList<VCard>();
		VCard vcard = null;
		while ((vcard = readNext()) != null) {
			vcards.add(vcard);
		}
		return vcards;
	}

	/**
	 * Reads the next vCard from the data stream.
	 * @return the next vCard or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public VCard readNext() throws IOException {
		warnings.clear();
		return _readNext();
	}

	/**
	 * Reads the next vCard from the data stream.
	 * @return the next vCard or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	protected abstract VCard _readNext() throws IOException;

	/**
	 * Matches up a list of {@link Label} properties with their corresponding
	 * {@link Address} properties. If no match can be found, then the LABEL
	 * property itself is assigned to the vCard.
	 * @param vcard the vCard that the properties belong to
	 * @param labels the LABEL properties
	 */
	protected void assignLabels(VCard vcard, List<Label> labels) {
		List<Address> adrs = vcard.getAddresses();
		for (Label label : labels) {
			boolean orphaned = true;
			List<AddressType> labelTypes = label.getTypes();
			for (Address adr : adrs) {
				if (adr.getLabel() != null) {
					//a label has already been assigned to it
					continue;
				}

				if (adr.getTypes().equals(labelTypes)) {
					adr.setLabel(label.getValue());
					orphaned = false;
					break;
				}
			}
			if (orphaned) {
				vcard.addOrphanedLabel(label);
			}
		}
	}

	/**
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(VCardPropertyScribe<? extends VCardProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return warnings.copy();
	}
}
