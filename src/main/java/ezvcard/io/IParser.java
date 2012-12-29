package ezvcard.io;

import java.io.IOException;
import java.util.List;

import ezvcard.VCard;
import ezvcard.types.VCardType;

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
 * Common methods that all vCard reader classes must have.
 * @author Michael Angstadt
 */
public interface IParser {
	/**
	 * Registers an extended type class.
	 * @param typeClass the extended type class to register (MUST have a public,
	 * no-arg constructor)
	 */
	void registerExtendedType(Class<? extends VCardType> typeClass);

	/**
	 * Removes an extended type class that was previously registered.
	 * @param clazz the extended type class to remove (MUST have a public,
	 * no-arg constructor)
	 */
	void unregisterExtendedType(Class<? extends VCardType> clazz);

	/**
	 * Gets the warnings from the last vCard that was unmarshalled. This list is
	 * reset every time a new vCard is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	List<String> getWarnings();

	/**
	 * Reads the next vCard.
	 * @return the next vCard or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	VCard readNext() throws IOException;
}
