package ezvcard.util;

import java.time.Duration;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.junit.rules.ExternalResource;

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
 */

/**
 * Changes the JVM's default timezone for the duration of a test.
 * @author Michael Angstadt
 */
public class DefaultTimezoneRule extends ExternalResource {
	private final TimeZone defaultTz;
	private final TimeZone tz;

	/**
	 * @param hour the hour component of the UTC offset
	 * @param minute the minute component of the UTC offset (if offset is
	 * negative, do not make the minute component negative)
	 */
	public DefaultTimezoneRule(int hour, int minute) {
		defaultTz = TimeZone.getDefault();
		tz = buildSimpleTimezone(hour, minute);
	}

	private TimeZone buildSimpleTimezone(int hour, int minute) {
		Duration hourDuration = Duration.ofHours(hour);

		Duration minuteDuration = Duration.ofMinutes(minute);
		if (hourDuration.isNegative()) {
			minuteDuration = minuteDuration.negated();
		}

		Duration offset = hourDuration.plus(minuteDuration);
		int millis = (int) offset.toMillis();
		return new SimpleTimeZone(millis, "");
	}

	@Override
	protected void before() {
		TimeZone.setDefault(tz);
	}

	@Override
	protected void after() {
		TimeZone.setDefault(defaultTz);
	}
}
