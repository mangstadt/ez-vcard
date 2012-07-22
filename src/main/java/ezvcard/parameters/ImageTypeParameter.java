package ezvcard.parameters;

import ezvcard.util.ParameterUtils;

/**
 * Copyright 2011 George El-Haddad. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY GEORGE EL-HADDAD ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GEORGE EL-HADDAD OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of George El-Haddad.
 */

/**
 * Represents the TYPE parameter of the PHOTO and LOGO types.
 * @author George El-Haddadt Mar 10, 2010
 * @author Michael Angstadt Jul 06, 2012
 */
public class ImageTypeParameter extends TypeParameter {
	/*
	 * IANA Registered Media Types. Some may not have registered an extension.
	 */

	public static final ImageTypeParameter CGM = new ImageTypeParameter("CGM", "image/cgm", "cgm");
	public static final ImageTypeParameter JP2 = new ImageTypeParameter("JP2", "image/jp2", "jp2");
	public static final ImageTypeParameter JPM = new ImageTypeParameter("JPM", "image/jpm", "jpm");
	public static final ImageTypeParameter JPX = new ImageTypeParameter("JPX", "image/jpx", "jpf");
	public static final ImageTypeParameter NAPLPS = new ImageTypeParameter("NAPLPS", "image/naplps", "");
	public static final ImageTypeParameter PNG = new ImageTypeParameter("PNG", "image/png", "png");
	public static final ImageTypeParameter BTIF = new ImageTypeParameter("BTIF", ".image/prs.btif", "btif");
	public static final ImageTypeParameter PTI = new ImageTypeParameter("PTI", "image/prs.pti", "pti");
	public static final ImageTypeParameter DJVU = new ImageTypeParameter("DJVU", "image/vnd.djvu", "djvu");
	public static final ImageTypeParameter SVF = new ImageTypeParameter("SVF", "image/vnd.svf", "svf");
	public static final ImageTypeParameter WBMP = new ImageTypeParameter("WBMP", "image/vnd.wap.wbmp", "wbmp");
	public static final ImageTypeParameter PSD = new ImageTypeParameter("PSD", "image/vnd.adobe.photoshop", "psd");
	public static final ImageTypeParameter INF2 = new ImageTypeParameter("INF2", "image/vnd.cns.inf2", "");
	public static final ImageTypeParameter DWG = new ImageTypeParameter("DWG", "image/vnd.dwg", "dwg");
	public static final ImageTypeParameter DXF = new ImageTypeParameter("DXF", "image/vnd.dxf", "dxf");
	public static final ImageTypeParameter FBS = new ImageTypeParameter("FBS", "image/vnd.fastbidsheet", "fbs");
	public static final ImageTypeParameter FPX = new ImageTypeParameter("FPX", "image/vnd.fpx", "fpx");
	public static final ImageTypeParameter FST = new ImageTypeParameter("FST", "image/vnd.fst", "fst");
	public static final ImageTypeParameter MMR = new ImageTypeParameter("MMR", "image/vnd.fujixerox.edmics-mmr", "mmr");
	public static final ImageTypeParameter RLC = new ImageTypeParameter("RLC", "image/vnd.fujixerox.edmics-rlc", "rlc");
	public static final ImageTypeParameter PGB = new ImageTypeParameter("PGB", "image/vnd.globalgraphics.pgb", "pgb");
	public static final ImageTypeParameter ICO = new ImageTypeParameter("ICO", "image/vnd.microsoft.icon", "ico");
	public static final ImageTypeParameter MIX = new ImageTypeParameter("MIX", "image/vnd.mix", "");
	public static final ImageTypeParameter MDI = new ImageTypeParameter("MDI", "image/vnd.ms-modi", "mdi");
	public static final ImageTypeParameter PIC = new ImageTypeParameter("PIC", "image/vnd.radiance", "pic");
	public static final ImageTypeParameter SPNG = new ImageTypeParameter("SPNG", "image/vnd.sealed.png", "spng");
	public static final ImageTypeParameter SGIF = new ImageTypeParameter("SGIF", "image/vnd.sealedmedia.softseal.gif", "sgif");
	public static final ImageTypeParameter SJPG = new ImageTypeParameter("SJPG", "image/vnd.sealedmedia.softseal.jpg", "sjpg");
	public static final ImageTypeParameter XIF = new ImageTypeParameter("XIF", "image/vnd.xiff", "xif");
	public static final ImageTypeParameter JPEG = new ImageTypeParameter("JPEG", "image/jpeg", "jpg");

	private final String ianaRegisteredName;
	private final String extension;

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard TYPEs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "JPEG")
	 */
	public ImageTypeParameter(String value) {
		this(value, null, null);
	}

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard TYPEs. Please use one of the predefined static
	 * objects.
	 * @param value the type name (e.g. "JPEG")
	 * @param _ianaRegisteredName the IANA registered name (e.g. "image/jpeg")
	 * @param _extension the file extension used for this type (e.g. "jpg")
	 */
	public ImageTypeParameter(String value, String _ianaRegisteredName, String _extension) {
		super(value);
		ianaRegisteredName = _ianaRegisteredName;
		extension = _extension;
	}

	/**
	 * Gets the content-type.
	 * @return the content-type (e.g. "image/jpeg")
	 */
	public String getIanaRegisteredName() {
		return ianaRegisteredName;
	}

	/**
	 * Gets the file extension used for this type.
	 * @return the file extension (e.g. "jpg")
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Retrieves one of the static objects in this class by name.
	 * @param typeName the type name (e.g. "PNG")
	 * @return the object associated with the given type name or null if none
	 * was found
	 */
	public static ImageTypeParameter valueOf(String typeName) {
		return ParameterUtils.valueOf(ImageTypeParameter.class, typeName);
	}
}
