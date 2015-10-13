package de.mixedfx.texttospeech;

import com.detectlanguage.DetectLanguage;
import com.detectlanguage.errors.APIError;
import de.mixedfx.speechtotext.IncorrectInputLanguage;
import lombok.extern.log4j.Log4j2;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioInputStream;
import java.util.Locale;

@Component
@Log4j2(topic = "TextToSpeech")
@Configuration
public class TextToSpeech
{
	@Autowired
	private AudioPlayer audioPlayer;

	@Bean
	public AudioPlayer produceAudioPlayer() {
		return new AudioPlayer();
	}

	/**
	 *
	 * @param input
	 *            The texttospeech which shall be spoken (not null and not empty).
	 * @param config
	 *            The config for this saying. If null default configuration will be used.
	 * @throws IncorrectInputLanguage
	 *             If the input couldn't be process with the specified language.
	 */
	public void say(final String input, TTSConfig config) throws IncorrectInputLanguage
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
			log.fatal("MaryConfigurationException registered: " + ex);
			return;
		}

		// Set volume
		maryttsServer.setAudioEffects("Volume(Amount=" + config.volume + ")");

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
				log.warn("DetectLanguage API could not detect it! Use default voice now!");
				final Locale locale = new Locale("en");
				config.lang = Language.getVoiceByLocale(locale);
			}
		}

		// Set voice
		maryttsServer.setVoice(config.lang.getVoice(config.male));

		try
		{
			final AudioInputStream audio = maryttsServer.generateAudio(input);
			audioPlayer = new AudioPlayer();
			audioPlayer.setAudio(audio);
			audioPlayer.start();
			if (config.block)
			{
				try
				{
					audioPlayer.join();
				} catch (final InterruptedException e)
				{
				}
			}
		} catch (final SynthesisException ex)
		{
			log.fatal("Error saving input! " + ex);
			throw new IncorrectInputLanguage();
		}
	}
}
