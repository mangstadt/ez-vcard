package ezvcard.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

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
 * Represents the various ISO8601 date/time formats that vCard dates can be
 * represented as.
 * @author George El-Haddad
 * @author Michael Angstadt
 */
public enum ISOFormat {
	//@formatter:off
	/**
	 * Example: 20120701
	 */
	DATE_BASIC("\\d{8}","yyyyMMdd"),
	
	/**
	 * Example: 2012-07-01
	 */
	DATE_EXTENDED("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd"),
	
	/**
	 * Example: 20120701T142110-0500
	 */
	TIME_BASIC("\\d{8}T\\d{6}[-\\+]\\d{4}", "yyyyMMdd'T'HHmmssZ"),
	
	/**
	 * Example: 2012-07-01T14:21:10-05:00
	 */
	TIME_EXTENDED("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[-\\+]\\d{2}:\\d{2}", "yyyy-MM-dd'T'HH:mm:ssZ"),
	
	/**
	 * Example: 20120701T192110Z
	 */
	UTC_TIME_BASIC("\\d{8}T\\d{6}Z", "yyyyMMdd'T'HHmmssZ", "yyyyMMdd'T'HHmmss'Z'"),
	
	/**
	 * Example: 2012-07-01T19:21:10Z
	 */
	UTC_TIME_EXTENDED("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss'Z'");
	//@formatter:on

	/**
	 * The regular expression pattern for the date format.
	 */
	private final Pattern pattern;

	/**
	 * The {@link SimpleDateFormat} format string used for parsing dates.
	 */
	private final String parseFormat;

	/**
	 * The {@link SimpleDateFormat} format string used for formatting dates.
	 */
	private final String formatFormat;

	/**
	 * @param regex the regular expression for the date format
	 * @param format the {@link SimpleDateFormat} format string used for parsing
	 * and formatting dates.
	 */
	private ISOFormat(String regex, String format) {
		this(regex, format, format);
	}

	/**
	 * @param regex the regular expression for the date format
	 * @param parseFormat the {@link SimpleDateFormat} format string used for
	 * parsing dates.
	 * @param formatFormat the {@link SimpleDateFormat} format string used for
	 * formatting dates.
	 */
	private ISOFormat(String regex, String parseFormat, String formatFormat) {
		pattern = Pattern.compile(regex);
		this.parseFormat = parseFormat;
		this.formatFormat = formatFormat;
	}

	/**
	 * Determines whether a date string is in this ISO format.
	 * @param dateStr the date string
	 * @return true if it matches the date format, false if not
	 */
	public boolean matches(String dateStr) {
		return pattern.matcher(dateStr).matches();
	}

	/**
	 * Builds a {@link DateFormat} object for parsing dates in this ISO format.
	 * @return the {@link DateFormat} object
	 */
	public DateFormat getParseDateFormat() {
		return new SimpleDateFormat(parseFormat);
	}

	/**
	 * Builds a {@link DateFormat} object for formatting dates in this ISO
	 * format.
	 * @return the {@link DateFormat} object
	 */
	public DateFormat getFormatDateFormat() {
		return new SimpleDateFormat(formatFormat);
	}
}
