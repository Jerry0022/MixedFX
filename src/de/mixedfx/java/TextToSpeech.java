package de.mixedfx.java;

import java.util.Locale;

import javax.sound.sampled.AudioInputStream;

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

	private static MaryInterface maryttsServer;
	private static AudioPlayer audioPlayer;

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
		maryttsServer.setVoice(lang.voice);
	}

	public static Languages[] getLangs()
	{
		return Languages.values();
	}

	public static void main(String[] args)
	{
		say("Was denkst du was am Besten für dich ist?");
	}

	/**
	 * Speaks a text in the given language {@link Languages#GERMAN_FEMALE}! Is much faster if {@link #init()} was called before!
	 * 
	 * @param input
	 *            The text to speak!
	 */
	public static void say(String input)
	{
		if (audioPlayer == null)
			init();
		try
		{
			AudioInputStream audio = maryttsServer.generateAudio(input);
			audioPlayer.setAudio(audio);
			audioPlayer.start();
		} catch (SynthesisException ex)
		{
			System.err.println("Error saying phrase.");
		}
	}
}
