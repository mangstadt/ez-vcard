package ezvcard.parameters;

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
 * Represents the TYPE parameter of the SOUND type.
 * <p>
 * vCard versions: 2.1, 3.0, 4.0
 * </p>
 * @author George El-Haddad Mar 10, 2010
 * @author Michael Angstadt Jul 06, 2012
 * 
 */
public class SoundTypeParameter extends MediaTypeParameter {
	/*
	 * IANA Registered Sound Types. Some may not have registered an extension.
	 */
	public static final SoundTypeParameter GPP3 = new SoundTypeParameter("GPP3", "audio/3gpp", "");
	public static final SoundTypeParameter GPP2 = new SoundTypeParameter("GPP2", "audio/3gpp2", "");
	public static final SoundTypeParameter AC3 = new SoundTypeParameter("AC3", "audio/ac3", "ac3");
	public static final SoundTypeParameter AMR = new SoundTypeParameter("AMR", "audio/amr", "amr");
	public static final SoundTypeParameter AMR_WB = new SoundTypeParameter("AMR_WB", "audio/amr-wb", "amr-wb");
	public static final SoundTypeParameter AMR_WB_PLUS = new SoundTypeParameter("AMR_WB_PLUS", "audio/amr-wb+", "amr-wb+");
	public static final SoundTypeParameter ASC = new SoundTypeParameter("ASC", "audio/asc", "");
	public static final SoundTypeParameter ATRAC_ADVANCED_LOSSLESS = new SoundTypeParameter("ATRAC_ADVANCED_LOSSLESS", "audio/atrac-advanced-lossless", "");
	public static final SoundTypeParameter ATRAC_X = new SoundTypeParameter("ATRAC_X", "audio/atrac-x", "");
	public static final SoundTypeParameter ATRAC3 = new SoundTypeParameter("ATRAC3", "audio/atrac3", "");
	public static final SoundTypeParameter BASIC = new SoundTypeParameter("BASIC", "audio/basic", "");
	public static final SoundTypeParameter BV16 = new SoundTypeParameter("BV16", "audio/bv16", "");
	public static final SoundTypeParameter BV32 = new SoundTypeParameter("BV32", "audio/bv32", "");
	public static final SoundTypeParameter CLEARMODE = new SoundTypeParameter("CLEARMODE", "audio/clearmode", "");
	public static final SoundTypeParameter CN = new SoundTypeParameter("CN", "audio/cn", "");
	public static final SoundTypeParameter DAT12 = new SoundTypeParameter("DAT12", "audio/dat12", "");
	public static final SoundTypeParameter DLS = new SoundTypeParameter("DLS", "audio/dls", "");
	public static final SoundTypeParameter DSR_ES201108 = new SoundTypeParameter("DSR_ES201108", "audio/dsr-es201108", "");
	public static final SoundTypeParameter DSR_ES202050 = new SoundTypeParameter("DSR_ES202050", "audio/dsr-es202050", "");
	public static final SoundTypeParameter DSR_ES202211 = new SoundTypeParameter("DSR_ES202211", "audio/dsr-es202211", "");
	public static final SoundTypeParameter DSR_ES202212 = new SoundTypeParameter("DSR_ES202212", "audio/dsr-es202212", "");
	public static final SoundTypeParameter EAC3 = new SoundTypeParameter("EAC3", "audio/eac3", "eac3");
	public static final SoundTypeParameter DVI4 = new SoundTypeParameter("DVI4", "audio/dvi4", "");
	public static final SoundTypeParameter EVRC = new SoundTypeParameter("EVRC", "audio/evrc", "");
	public static final SoundTypeParameter EVRC0 = new SoundTypeParameter("EVRC0", "audio/evrc0", "");
	public static final SoundTypeParameter EVRC1 = new SoundTypeParameter("EVRC1", "audio/evrc1", "");
	public static final SoundTypeParameter EVRCB = new SoundTypeParameter("EVRCB", "audio/evrcb", "");
	public static final SoundTypeParameter EVRCB0 = new SoundTypeParameter("EVRCB0", "audio/evrcb0", "");
	public static final SoundTypeParameter EVRCB1 = new SoundTypeParameter("EVRCB1", "audio/evrcb1", "");
	public static final SoundTypeParameter EVRC_QCP = new SoundTypeParameter("EVRC_QCP", "audio/evrc-qcp", "");
	public static final SoundTypeParameter EVRCWB = new SoundTypeParameter("EVRCWB", "audio/evrcwb", "");
	public static final SoundTypeParameter EVRCWB0 = new SoundTypeParameter("EVRCWB0", "audio/evrcwb0", "");
	public static final SoundTypeParameter EVRCWB1 = new SoundTypeParameter("EVRCWB1", "audio/evrcwb1", "");
	public static final SoundTypeParameter G719 = new SoundTypeParameter("G719", "audio/g719", "");
	public static final SoundTypeParameter G722 = new SoundTypeParameter("G722", "audio/g722", "");
	public static final SoundTypeParameter G7221 = new SoundTypeParameter("G7221", "audio/g7221", "");
	public static final SoundTypeParameter G723 = new SoundTypeParameter("G723", "audio/g723", "");
	public static final SoundTypeParameter G726_16 = new SoundTypeParameter("G726_16", "audio/g726-16", "");
	public static final SoundTypeParameter G726_24 = new SoundTypeParameter("G726_24", "audio/g726-24", "");
	public static final SoundTypeParameter G726_32 = new SoundTypeParameter("G726_32", "audio/g726-32", "");
	public static final SoundTypeParameter G726_40 = new SoundTypeParameter("G726_40", "audio/g726-40", "");
	public static final SoundTypeParameter G728 = new SoundTypeParameter("G728", "audio/g728", "");
	public static final SoundTypeParameter G729 = new SoundTypeParameter("G729", "audio/g729", "");
	public static final SoundTypeParameter G7291 = new SoundTypeParameter("G7291", "audio/g7291", "");
	public static final SoundTypeParameter G729D = new SoundTypeParameter("G729D", "audio/g729d", "");
	public static final SoundTypeParameter G729E = new SoundTypeParameter("G729E", "audio/g729e", "");
	public static final SoundTypeParameter GSM = new SoundTypeParameter("GSM", "audio/gsm", "");
	public static final SoundTypeParameter GSM_EFR = new SoundTypeParameter("GSM_EFR", "audio/gsm-efr", "");
	public static final SoundTypeParameter ILBC = new SoundTypeParameter("ILBC", "audio/ilbc", "");
	public static final SoundTypeParameter L8 = new SoundTypeParameter("L8", "audio/l8", "");
	public static final SoundTypeParameter L16 = new SoundTypeParameter("L16", "audio/l16", "");
	public static final SoundTypeParameter L20 = new SoundTypeParameter("L20", "audio/l20", "");
	public static final SoundTypeParameter L24 = new SoundTypeParameter("L24", "audio/l24", "");
	public static final SoundTypeParameter LPC = new SoundTypeParameter("LPC", "audio/lpc", "");
	public static final SoundTypeParameter MOBILE_XMF = new SoundTypeParameter("MOBILE_XMF", "audio/mobile-xmf", "");
	public static final SoundTypeParameter MPA = new SoundTypeParameter("MPA", "audio/mpa", "mpa");
	public static final SoundTypeParameter MP4 = new SoundTypeParameter("MP4", "audio/mp4", "mp4");
	public static final SoundTypeParameter MP4A_LATM = new SoundTypeParameter("MP$_LATM", "audio/mp4-latm", "");
	public static final SoundTypeParameter MPA_ROBUST = new SoundTypeParameter("MPA_ROBUST", "audio/mpa-robust", "");
	public static final SoundTypeParameter MPEG = new SoundTypeParameter("MPEG", "audio/mpeg", "mpeg");
	public static final SoundTypeParameter MPEG4_GENERIC = new SoundTypeParameter("MPEG4_GENERIC", "audio/mpeg4-generic", "mpeg");
	public static final SoundTypeParameter OGG = new SoundTypeParameter("OGG", "audio/ogg", "ogg");
	public static final SoundTypeParameter PARITYFEC_1D_INT = new SoundTypeParameter("PARITYFEC_1D_INT", "audio/1d-interleaved-parityfec", "");
	public static final SoundTypeParameter PARITYFEC = new SoundTypeParameter("PARITYFEC", "audio/parityfec", "");
	public static final SoundTypeParameter PCMA = new SoundTypeParameter("PCMA", "audio/pcma", "");
	public static final SoundTypeParameter PCMA_WB = new SoundTypeParameter("PCMA_WB", "audio/pcma-wb", "");
	public static final SoundTypeParameter PCMU = new SoundTypeParameter("PCMU", "audio/pcmu", "");
	public static final SoundTypeParameter PCMU_WB = new SoundTypeParameter("PCMU_WB", "audio/pcmu-wb", "");
	public static final SoundTypeParameter PRS_SID = new SoundTypeParameter("PRS_SID", "audio/prs.sid", "sid");
	public static final SoundTypeParameter QCELP = new SoundTypeParameter("QCELP", "audio/qcelp", "");
	public static final SoundTypeParameter RED = new SoundTypeParameter("RED", "audio/red", "");
	public static final SoundTypeParameter RTP_MIDI = new SoundTypeParameter("RTP_MIDI", "audio/rtp-midi", "");
	public static final SoundTypeParameter RTX = new SoundTypeParameter("RTX", "audio/rtx", "");
	public static final SoundTypeParameter SMV = new SoundTypeParameter("SMV", "audio/smv", "");
	public static final SoundTypeParameter SMV0 = new SoundTypeParameter("SMV0", "audio/smv0", "");
	public static final SoundTypeParameter SMV_QCP = new SoundTypeParameter("SMV_QCP", "audio/smv-qcp", "");
	public static final SoundTypeParameter SPEEX = new SoundTypeParameter("SPEEX", "audio/speex", "");
	public static final SoundTypeParameter T140C = new SoundTypeParameter("T140C", "audio/t140c", "");
	public static final SoundTypeParameter T38 = new SoundTypeParameter("T38", "audio/t38", "");
	public static final SoundTypeParameter TELEPHONE_EVENT = new SoundTypeParameter("TELEPHONE_EVENT", "audio/telephone-event", "");
	public static final SoundTypeParameter TONE = new SoundTypeParameter("TONE", "audio/tone", "");
	public static final SoundTypeParameter UEMCLIP = new SoundTypeParameter("UEMCLIP", "audio/uemclip", "");
	public static final SoundTypeParameter ULPFEC = new SoundTypeParameter("ULPFEC", "audio/ulpfec", "");
	public static final SoundTypeParameter VDVI = new SoundTypeParameter("VDVI", "audio/vdvi", "");
	public static final SoundTypeParameter VMR_WB = new SoundTypeParameter("VMR_WB", "audio/vmr-wb", "");
	public static final SoundTypeParameter VORBIS = new SoundTypeParameter("VORBIS", "audio/vorbis", "");
	public static final SoundTypeParameter VORBIS_CONFIG = new SoundTypeParameter("VORBIS_CONFIG", "audio/vorbis-config", "");
	public static final SoundTypeParameter RTP_ENC_AESCM128 = new SoundTypeParameter("RTP_ENC_AESCM128", "audio/rtp-enc-aescm128", "");
	public static final SoundTypeParameter SP_MIDI = new SoundTypeParameter("SP_MIDI", "audio/sp-midi ", "mid");
	public static final SoundTypeParameter GPP3_IUFP = new SoundTypeParameter("GPP3_IUFP", "audio/vnd.3gpp.iufp", "");
	public static final SoundTypeParameter SB4 = new SoundTypeParameter("SB4", "audio/vnd.4sb", "");
	public static final SoundTypeParameter AUDIOKOZ = new SoundTypeParameter("AUDIOKOZ", "audio/vnd.audiokoz", "koz");
	public static final SoundTypeParameter CELP = new SoundTypeParameter("CELP", "audio/vnd.CELP", "");
	public static final SoundTypeParameter NSE = new SoundTypeParameter("NSE", "audio/vnd.cisco.com", "");
	public static final SoundTypeParameter CMLES_RADIO_EVENTS = new SoundTypeParameter("CMLES_RADIO_EVENTS", "audio/vnd.cmles.radio-events", "");
	public static final SoundTypeParameter CNS_ANP1 = new SoundTypeParameter("CNS_ANP1", "audio/vnd.cns.anp1", "");
	public static final SoundTypeParameter CND_INF1 = new SoundTypeParameter("CNS_INF1", "audio/vnd.cns.inf1", "");
	public static final SoundTypeParameter EOL = new SoundTypeParameter("EOL", "audio/vnd.digital-winds", "eol");
	public static final SoundTypeParameter DLNA_ADTS = new SoundTypeParameter("DLNA_ADTS", "audio/vnd.dlna.adts", "");
	public static final SoundTypeParameter HEAAC1 = new SoundTypeParameter("HEAAC1", "audio/vnd.dolby.heaac.1", "");
	public static final SoundTypeParameter HEAAC2 = new SoundTypeParameter("HEAAC2", "audio/vnd.dolby.heaac.2", "");
	public static final SoundTypeParameter MPL = new SoundTypeParameter("MPL", "audio/vnd.dolby.mlp", "mpl");
	public static final SoundTypeParameter MPS = new SoundTypeParameter("MPS", "audio/vnd.dolby.mps", "");
	public static final SoundTypeParameter PL2 = new SoundTypeParameter("PL2", "audio/vnd.dolby.pl2", "");
	public static final SoundTypeParameter PL2X = new SoundTypeParameter("PL2X", "audio/vnd.dolby.pl2x", "");
	public static final SoundTypeParameter PL2Z = new SoundTypeParameter("PL2Z", "audio/vnd.dolby.pl2z", "");
	public static final SoundTypeParameter PULSE_1 = new SoundTypeParameter("PULSE_1", "audio/vnd.dolby.pulse.1", "");
	public static final SoundTypeParameter DRA = new SoundTypeParameter("DRA", "audio/vnd.dra", "");
	public static final SoundTypeParameter DTS = new SoundTypeParameter("DTS", "audio/vnd.dts", "WAV"); //wav, cpt, dts
	public static final SoundTypeParameter DTSHD = new SoundTypeParameter("DTSHD", "audio/vnd.dts.hd", "dtshd");
	public static final SoundTypeParameter PLJ = new SoundTypeParameter("PLJ", "audio/vnd.everad.plj", "plj");
	public static final SoundTypeParameter AUDIO = new SoundTypeParameter("AUDIO", "audio/vnd.hns.audio", "rm");
	public static final SoundTypeParameter VOICE = new SoundTypeParameter("LVP", "audio/vnd.lucent.voice", "lvp");
	public static final SoundTypeParameter PYA = new SoundTypeParameter("PYA", "audio/vnd.ms-playready.media.pya", "pya");
	public static final SoundTypeParameter MXMF = new SoundTypeParameter("MXMF", "audio/vnd.nokia.mobile-xmf", "mxmf");
	public static final SoundTypeParameter VBK = new SoundTypeParameter("VBK", "audio/vnd.nortel.vbk", "vbk");
	public static final SoundTypeParameter ECELP4800 = new SoundTypeParameter("ECELP4800", "audio/vnd.nuera.ecelp4800", "ecelp4800");
	public static final SoundTypeParameter ECELP7470 = new SoundTypeParameter("ECELP7470", "audio/vnd.nuera.ecelp7470", "ecelp7470");
	public static final SoundTypeParameter ECELP9600 = new SoundTypeParameter("ECELP9600", "audio/vnd.nuera.ecelp9600", "ecelp9600");
	public static final SoundTypeParameter SBC = new SoundTypeParameter("SBC", "audio/vnd.octel.sbc", "");
	public static final SoundTypeParameter KADPCM32 = new SoundTypeParameter("KADPCM32", "audio/vnd.rhetorex.32kadpcm", "");
	public static final SoundTypeParameter SMP3 = new SoundTypeParameter("SMP3", "audio/vnd.sealedmedia.softseal.mpeg", "smp3"); //smp3, smp, s1m
	public static final SoundTypeParameter CVSD = new SoundTypeParameter("CVSD", "audio/vnd.vmx.cvsd", "");

	/**
	 * Use of this constructor is discouraged and should only be used for
	 * defining non-standard TYPEs. Please use one of the predefined static
	 * objects.
	 * @param value the type value (e.g. "ogg")
	 * @param mediaType the media type (e.g. "audio/ogg")
	 * @param extension the file extension used for this type (e.g. "ogg")
	 */
	public SoundTypeParameter(String value, String mediaType, String extension) {
		super(value, mediaType, extension);
	}

	/**
	 * Searches the static objects in this class for one that has a certain type
	 * value.
	 * @param value the type value to search for (e.g. "ogg")
	 * @return the object or null if not found
	 */
	public static SoundTypeParameter valueOf(String value) {
		return findByValue(value, SoundTypeParameter.class);
	}

	/**
	 * Searches the static objects in this class for one that has a certain
	 * media type.
	 * @param mediaType the media type to search for (e.g. "audio/ogg")
	 * @return the object or null if not found
	 */
	public static SoundTypeParameter findByMediaType(String mediaType) {
		return findByMediaType(mediaType, SoundTypeParameter.class);
	}
}
