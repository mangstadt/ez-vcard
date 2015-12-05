package ezvcard.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.Warning;
import ezvcard.parameter.MediaTypeParameter;
import ezvcard.util.IOUtils;
import lombok.*;

/*
 Copyright (c) 2012-2015, Michael Angstadt
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
 * Represents a property whose value contains binary data.
 * @author Michael Angstadt
 * @param <T> the class used for representing the content type of the resource
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class BinaryProperty<T extends MediaTypeParameter> extends VCardProperty implements HasAltId {
	/**
	 * The decoded data.
	 */
	protected byte[] data;

	/**
	 * The URL to the resource.
	 */
	protected String url;

	/**
	 * The content type of the resource (for example, a JPEG image).
	 */
	protected T contentType;

	public BinaryProperty() {
		//empty
	}

	/**
	 * Creates a binary property.
	 * @param url the URL to the resource
	 * @param type the content type
	 */
	public BinaryProperty(String url, T type) {
		setUrl(url, type);
	}

	/**
	 * Creates a binary property.
	 * @param data the binary data
	 * @param type the content type
	 */
	public BinaryProperty(byte[] data, T type) {
		setData(data, type);
	}

	/**
	 * Creates a binary property.
	 * @param in an input stream to the binary data (will be closed)
	 * @param type the content type
	 * @throws IOException if there is a problem reading from the input stream
	 */
	public BinaryProperty(InputStream in, T type) throws IOException {
		this(IOUtils.toByteArray(in, true), type);
	}

	/**
	 * Creates a binary property.
	 * @param file the file containing the binary data
	 * @param type the content type
	 * @throws IOException if there is a problem reading from the file
	 */
	public BinaryProperty(File file, T type) throws IOException {
		this(new FileInputStream(file), type);
	}

	/**
	 * Gets the binary data of the resource.
	 * @return the binary data or null if there is none
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the binary data of the resource.
	 * @param data the binary data
	 * @param type the content type (e.g. "JPEG image")
	 */
	public void setData(byte[] data, T type) {
		this.url = null;
		this.data = data;
		setContentType(type);
	}

	/**
	 * Gets the URL to the resource
	 * @return the URL or null if there is none
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the URL to the resource.
	 * @param url the URL
	 * @param type the content type (e.g. "JPEG image")
	 */
	public void setUrl(String url, T type) {
		this.url = url;
		this.data = null;
		setContentType(type);
	}

	/**
	 * Gets the content type of the resource.
	 * @return the content type (e.g. "JPEG image")
	 */
	public T getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the resource.
	 * @param contentType the content type (e.g. "JPEG image")
	 */
	public void setContentType(T contentType) {
		this.contentType = contentType;
	}

	/**
	 * Gets the vCard 4.0 TYPE parameter. This should NOT be used to get the
	 * TYPE parameter for 2.1/3.0 vCards. Use {@link #getContentType} instead.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @return the TYPE value (typically, this will be either "work" or "home")
	 * or null if it doesn't exist
	 */
	public String getType() {
		return parameters.getType();
	}

	/**
	 * Sets the vCard 4.0 TYPE parameter. This should NOT be used to set the
	 * TYPE parameter for 2.1/3.0 vCards. Use {@link #setContentType} instead.
	 * <p>
	 * <b>Supported versions:</b> {@code 4.0}
	 * </p>
	 * @param type the TYPE value (should be either "work" or "home") or null to
	 * remove
	 */
	public void setType(String type) {
		parameters.setType(type);
	}

	@Override
	public List<Integer[]> getPids() {
		return super.getPids();
	}

	@Override
	public void addPid(int localId, int clientPidMapRef) {
		super.addPid(localId, clientPidMapRef);
	}

	@Override
	public void removePids() {
		super.removePids();
	}

	@Override
	public Integer getPref() {
		return super.getPref();
	}

	@Override
	public void setPref(Integer pref) {
		super.setPref(pref);
	}

	//@Override
	public String getAltId() {
		return parameters.getAltId();
	}

	//@Override
	public void setAltId(String altId) {
		parameters.setAltId(altId);
	}

	@Override
	protected void _validate(List<Warning> warnings, VCardVersion version, VCard vcard) {
		if (url == null && data == null) {
			warnings.add(new Warning(8));
		}
	}
}
