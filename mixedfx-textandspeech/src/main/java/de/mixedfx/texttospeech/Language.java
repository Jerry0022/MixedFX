package de.mixedfx.texttospeech;

import java.util.Locale;

/**
 * <pre>
 *  German and US English male and female languages.
 * 
 * See Languages on https://github.com/marytts/marytts/blob/master/download/marytts-components.xml 
 * E. g. a download url: http://mary.dfki.de/download/5.1/voice-bits1-hsmm-5.1.zip
 * </pre>
 */
public enum Language
{
	GERMAN("Deutsch", Locale.GERMAN), ENGLISH("English", Locale.ENGLISH);

	public static Language getVoiceByLocale(Locale locale)
	{
		for (Language lang : Language.values())
			if (lang.locale.equals(locale))
				return lang;
		return Language.ENGLISH;
	}

	public String name;
	public Locale locale;

	private Language(String name, Locale locale)
	{
		this.name = name;
		this.locale = locale;
	}

	public String getVoice(boolean male)
	{
		if (this.locale.equals(Locale.GERMAN))
			return male ? "bits3-hsmm" : "bits1-hsmm";
		else
			return male ? "cmu-bdl-hsmm" : "cmu-slt-hsmm";
	}

	public String getListening()
	{
		return locale + "-" + (this.locale.equals(Locale.GERMAN) ? "de" : "us");
	}
}