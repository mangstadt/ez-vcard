package ezvcard;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ezvcard.parameter.VCardParameter;
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
 */

/**
 * <p>
 * Defines the vCard version(s) that support a property ({@link VCardProperty}
 * class), parameter value ({@link VCardParameter} instance), or data type (
 * {@link VCardDataType} instance). If this annotation is not present, then all
 * versions are presumed to be supported.
 * </p>
 * <p>
 * <b>Examples</b>
 * </p>
 * 
 * <pre class="brush:java">
 * {@literal @}SupportedVersions({ VCardVersion.V2_1, VCardVersion.V3_0 })
 * public class Agent extends VCardProperty{
 *   ...
 * }
 * 
 * public class AddressType extends VCardParameter{
 *   {@literal @}SupportedVersions({ VCardVersion.V2_1, VCardVersion.V3_0 })
 *   public static final AddressType DOM = new AddressType("dom");
 *   ...
 * }
 * 
 * public class VCardDataType{
 *   {@literal @}SupportedVersions(VCardVersion.V2_1)
 *   public static final VCardDataType CONTENT_ID = new VCardDataType("content-id");
 *   ...
 * }
 * </pre>
 * @author Michael Angstadt
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedVersions {
	VCardVersion[] value();
}
