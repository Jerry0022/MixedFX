package de.mixedfx.java;

import java.util.Locale;

import javax.sound.sampled.AudioInputStream;

import com.detectlanguage.DetectLanguage;
import com.detectlanguage.errors.APIError;

import de.mixedfx.logging.Log;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;

public class TextToSpeech
{
	/**
	 * <pre>
	 * German and US English male and female languages.
	 * 
	 * See Languages on https://github.com/marytts/marytts/blob/master/download/marytts-components.xml 
	 * E. g. a download url: http://mary.dfki.de/download/5.1/voice-bits1-hsmm-5.1.zip
	 * </pre>
	 * 
	 * @author Jerry
	 */
	public enum Language
	{
		GERMAN_FEMALE(Locale.GERMAN, false, "bits1-hsmm"), GERMAN_MALE(Locale.GERMAN, true, "bits3-hsmm"), ENGLISH_US_FEMALE(Locale.ENGLISH, false, "cmu-slt-hsmm"), ENGLISH_US_MALE(Locale.ENGLISH,
				true, "cmu-bdl-hsmm");

		public static Language getBy(Locale locale, boolean male)
		{
			if (locale != Locale.GERMAN)
				locale = Locale.ENGLISH;

			for (Language lang : Language.values())
				if (lang.locale == locale && lang.male == male)
					return lang;
			return Language.values()[0];
		}

		public Locale locale;
		public boolean male;
		public String name;

		private Language(Locale locale, boolean male, String name)
		{
			this.locale = locale;
			this.male = male;
			this.name = name;
		}

		@Override
		public String toString()
		{
			return this.name;
		}
	}

	private static MaryInterface maryttsServer;
	private static AudioPlayer audioPlayer;
	private static Language currentLang;

	/**
	 * May need some seconds!
	 */
	public static void init()
	{
		try
		{
			maryttsServer = new LocalMaryInterface();
			audioPlayer = new AudioPlayer();
		} catch (MaryConfigurationException ex)
		{
			Log.textAndSpeech.fatal("MaryConfigurationException registered: " + ex);
		}
	}

	/**
	 * Is much faster if {@link #init()} was called once before!
	 * 
	 * @param input
	 *            The text which shall be spoken (not null and not empty).
	 * @param config
	 *            The config for this saying. If null default configuration will be used.
	 */
	public static void say(String input, TTSConfig config)
	{
		// Input can't be null
		if (input == null || input.isEmpty())
			throw new NullPointerException("Text to speek can't be null!");

		// Initialize default configuration
		if (config == null)
			config = new TTSConfig();

		// If not initialized, do it! (Needs some seconds)
		if (audioPlayer == null)
			init();

		// Set volume
		maryttsServer.setAudioEffects("Volume(Amount=" + config.volume + ")");

		// Autodetect language if no Language was chosen!
		if (currentLang == null)
		{
			try
			{
				DetectLanguage.apiKey = config.DETECTLANGUAGE_API_KEY;
				Locale locale = new Locale(DetectLanguage.simpleDetect(input));
				config.lang = Language.getBy(locale, config.male);
			} catch (APIError e)
			{
				Log.textAndSpeech.warn("DetectLanguage API could not detect it! Use default voice now!");
				Locale locale = new Locale("en");
				config.lang = Language.getBy(locale, config.male);
			}
		}

		// Set voice
		maryttsServer.setVoice(config.lang.toString());

		try
		{
			AudioInputStream audio = maryttsServer.generateAudio(input);
			audioPlayer.setAudio(audio);
			audioPlayer.start();
			if (config.block)
			{
				try
				{
					audioPlayer.join();
				} catch (InterruptedException e)
				{
				}
			}
		} catch (SynthesisException ex)
		{
			Log.textAndSpeech.fatal("Error saving input! " + ex);
		}
	}
}
