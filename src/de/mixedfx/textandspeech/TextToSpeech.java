package de.mixedfx.textandspeech;

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
	private static AudioPlayer	audioPlayer;
	private static Language		currentLang;

	/**
	 *
	 * @param input
	 *            The text which shall be spoken (not null and not empty).
	 * @param config
	 *            The config for this saying. If null default configuration will be used.
	 * @throws IncorrectInputLanguage
	 *             If the input couldn't be process with the specified language.
	 */
	public static void say(final String input, TTSConfig config) throws IncorrectInputLanguage
	{
		// Input mustn't be null
		if ((input == null) || input.isEmpty())
			throw new NullPointerException("Text to speek can't be null!");

		// Initialize default configuration
		if (config == null)
			config = new TTSConfig();

		final MaryInterface maryttsServer;
		try
		{
			maryttsServer = new LocalMaryInterface();
		} catch (final MaryConfigurationException ex)
		{
			Log.textAndSpeech.fatal("MaryConfigurationException registered: " + ex);
			return;
		}

		// Set volume
		maryttsServer.setAudioEffects("Volume(Amount=" + config.volume + ")");

		System.out.println("JUHU");
		System.out.println(config.lang);
		System.out.println(config.male);
		System.out.println(config.lang.getVoice(config.male));

		// Autodetect language if no Language was chosen!
		if (config.lang == null)
		{
			try
			{
				DetectLanguage.apiKey = config.DETECTLANGUAGE_API_KEY;
				final Locale locale = new Locale(DetectLanguage.simpleDetect(input));
				config.lang = Language.getVoiceByLocale(locale);
			} catch (final APIError e)
			{
				Log.textAndSpeech.warn("DetectLanguage API could not detect it! Use default voice now!");
				final Locale locale = new Locale("en");
				config.lang = Language.getVoiceByLocale(locale);
			}
		}

		// Set voice
		maryttsServer.setVoice(config.lang.getVoice(config.male));

		try
		{
			final AudioInputStream audio = maryttsServer.generateAudio(input);
			TextToSpeech.audioPlayer = new AudioPlayer();
			TextToSpeech.audioPlayer.setAudio(audio);
			TextToSpeech.audioPlayer.start();
			if (config.block)
			{
				try
				{
					TextToSpeech.audioPlayer.join();
				} catch (final InterruptedException e)
				{
				}
			}
		} catch (final SynthesisException ex)
		{
			Log.textAndSpeech.fatal("Error saving input! " + ex);
			throw new IncorrectInputLanguage();
		}
	}
}
