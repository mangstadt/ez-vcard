package ezvcard.property;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ezvcard.SupportedVersions;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarnings;
import ezvcard.ValidationWarning;

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
 * <p>
 * Defines information about the person's agent.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = new VCard();
 * 
 * //URL
 * Agent agent = new Agent("http://www.linkedin.com/BobSmith");
 * vcard.setAgent(agent);
 * 
 * //vCard
 * VCard agentVCard = new VCard();
 * agentVCard.setFormattedName("Bob Smith");
 * agentVCard.addTelephoneNumber("(555) 123-4566");
 * agentVCard.addUrl("http://www.linkedin.com/BobSmith");
 * agent = new Agent(agentVCard);
 * vcard.setAgent(agent);
 * </pre>
 * 
 * <p>
 * <b>Code sample (retrieving)</b>
 * </p>
 * 
 * <pre class="brush:java">
 * VCard vcard = ...
 * Agent agent = vcard.getAgent();
 * 
 * String url = agent.getUrl();
 * if (url != null) {
 *   //property value is a URL
 * }
 * 
 * VCard agentVCard = agent.getVCard();
 * if (agentVCard != null) {
 *   //property value is a vCard
 * }
 * </pre>
 * 
 * <p>
 * <b>Property name:</b> {@code AGENT}
 * </p>
 * <p>
 * <b>Supported versions:</b> {@code 2.1, 3.0}
 * </p>
 * 
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc2426#page-19">RFC 2426 p.19</a>
 * @see <a href="http://www.imc.org/pdi/vcard-21.doc">vCard 2.1 p.18</a>
 */
@SupportedVersions({ VCardVersion.V2_1, VCardVersion.V3_0 })
public class Agent extends VCardProperty {
	private String url;
	private VCard vcard;

	/**
	 * Creates an empty agent property.
	 */
	public Agent() {
		//empty
	}

	/**
	 * Creates an agent property.
	 * @param url a URL pointing to the agent's information
	 */
	public Agent(String url) {
		setUrl(url);
	}

	/**
	 * Creates an agent property.
	 * @param vcard a vCard containing the agent's information
	 */
	public Agent(VCard vcard) {
		setVCard(vcard);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public Agent(Agent original) {
		super(original);
		url = original.url;
		vcard = (original.vcard == null) ? null : new VCard(original.vcard);
	}

	/**
	 * Gets the URL to the agent's information.
	 * @return the URL or null if not set
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the URL to the agent's information.
	 * @param url the URL
	 */
	public void setUrl(String url) {
		this.url = url;
		vcard = null;
	}

	/**
	 * Gets an embedded vCard with the agent's information.
	 * @return the vCard or null if not set
	 */
	public VCard getVCard() {
		return vcard;
	}

	/**
	 * Sets an embedded vCard with the agent's information.
	 * @param vcard the vCard
	 */
	public void setVCard(VCard vcard) {
		this.vcard = vcard;
		url = null;
	}

	@Override
	protected void _validate(List<ValidationWarning> warnings, VCardVersion version, VCard vcard) {
		if (url == null && this.vcard == null) {
			warnings.add(new ValidationWarning(8));
		}

		if (this.vcard != null) {
			NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ROOT);
			nf.setMinimumIntegerDigits(2);

			ValidationWarnings validationWarnings = this.vcard.validate(version);
			for (Map.Entry<VCardProperty, List<ValidationWarning>> entry : validationWarnings) {
				VCardProperty property = entry.getKey();
				List<ValidationWarning> propViolations = entry.getValue();

				for (ValidationWarning propViolation : propViolations) {
					String className = (property == null) ? "" : property.getClass().getSimpleName();

					int code = propViolation.getCode();
					String codeStr = (code >= 0) ? "W" + nf.format(code) : "";
					String message = propViolation.getMessage();
					warnings.add(new ValidationWarning(10, className, codeStr, message));
				}
			}
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("url", url);
		values.put("vcard", vcard);
		return values;
	}

	@Override
	public Agent copy() {
		return new Agent(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((vcard == null) ? 0 : vcard.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		Agent other = (Agent) obj;
		if (url == null) {
			if (other.url != null) return false;
		} else if (!url.equals(other.url)) return false;
		if (vcard == null) {
			if (other.vcard != null) return false;
		} else if (!vcard.equals(other.vcard)) return false;
		return true;
	}
}
