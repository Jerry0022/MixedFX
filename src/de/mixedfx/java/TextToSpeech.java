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
	public enum Languages
	{
		GERMAN_FEMALE(Locale.GERMAN, false, "bits1-hsmm"), GERMAN_MALE(Locale.GERMAN, true, "bits3-hsmm"), ENGLISH_US_FEMALE(Locale.ENGLISH, false, "cmu-slt-hsmm"), ENGLISH_US_MALE(Locale.ENGLISH,
				true, "cmu-bdl-hsmm");

		public Locale language;
		public boolean male;
		public String voice;

		private Languages(Locale language, boolean male, String voice)
		{
			this.male = male;
			this.voice = voice;
		}
	}

	public static String DETECTLANGUAGE_API_KEY = "d4bb31a242aba2eb925ae4cada947095";

	private static MaryInterface maryttsServer;
	private static AudioPlayer audioPlayer;
	private static Languages currentLang;

	/**
	 * May need some some seconds! Uses {@link Languages#GERMAN_FEMALE} as default.
	 */
	public static void init()
	{
		init(Languages.GERMAN_FEMALE);
	}

	/**
	 * May need some seconds!
	 * 
	 * @param lang
	 */
	public static void init(Languages lang)
	{
		try
		{
			maryttsServer = new LocalMaryInterface();
			audioPlayer = new AudioPlayer();
		} catch (MaryConfigurationException ex)
		{
			Log.textAndSpeech.fatal("MaryConfigurationException registered: " + ex);
		}
		applyLang(lang);
	}

	public static void applyLang(Languages lang)
	{
		currentLang = lang;
		validate();
		maryttsServer.setVoice(currentLang.voice);
	}

	public static Languages[] getLangs()
	{
		return Languages.values();
	}

	/**
	 * Speaks a text auto detecting the language (German or if other language it uses english)! Is much faster if {@link #init()} was called once before! Restriction to 1MB per day and 5000
	 * requests/day with the API Key {@link TextToSpeech#DETECTLANGUAGE_API_KEY}! Just before calling this method you may easily change the API key!
	 */
	public static void sayAuto(String input, boolean waitFor, boolean male)
	{
		validate();
		try
		{
			DetectLanguage.apiKey = DETECTLANGUAGE_API_KEY;
			String language = DetectLanguage.simpleDetect(input);
			if (language.equalsIgnoreCase("de"))
				applyLang(male ? Languages.GERMAN_MALE : Languages.GERMAN_FEMALE);
			else
				applyLang(male ? Languages.ENGLISH_US_MALE : Languages.ENGLISH_US_FEMALE);
		} catch (APIError e)
		{
			Log.textAndSpeech.warn("DetectLanguage API could not detect it! Use default voice now!");
			if (male)
				applyLang(Languages.ENGLISH_US_MALE);
			else
				applyLang(Languages.ENGLISH_US_FEMALE);
		}
		say(input, waitFor);
	}

	/**
	 * Speaks a text in the given language, may change with {@link #applyLang(Languages)}! Is much faster if {@link #init()} was called once before!
	 * 
	 * @param input
	 *            The text to speak!
	 */
	public static void say(String input, boolean waitFor, Languages lang)
	{
		applyLang(lang);
		say(input, waitFor);
	}

	/**
	 * Speaks a text in the current language, default is {@link Languages#GERMAN_FEMALE}! Is much faster if {@link #init()} was called once before!
	 * 
	 * @param input
	 *            The text to speak!
	 */
	public static void say(String input, boolean waitFor)
	{
		validate();
		try
		{
			AudioInputStream audio = maryttsServer.generateAudio(input);
			audioPlayer.setAudio(audio);
			audioPlayer.start();
			if (waitFor)
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
			ex.printStackTrace();
			System.err.println("Error saying phrase.");
		}
	}

	private static void validate()
	{
		if (audioPlayer == null)
			init();
		if (currentLang == null)
			currentLang = Languages.GERMAN_FEMALE;
	}
}
