package de.mixedfx.texttospeech;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;

import javax.sound.sampled.AudioInputStream;
import javax.xml.crypto.Data;

import com.detectlanguage.DetectLanguage;
import com.detectlanguage.errors.APIError;

import de.mixedfx.file.DataHandler;
import de.mixedfx.file.FileObject;
import de.mixedfx.logging.Log;
import de.mixedfx.speechtotext.IncorrectInputLanguage;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import org.apache.commons.io.FileUtils;

public class TextToSpeech
{
	private static AudioPlayer	audioPlayer;

    public static void loadVoices() throws ZipException, IOException {
        for(Language lang : Language.values())
            for(int male=0; male<2; male++)
            {
                // Built-in language
                if(lang.equals(Language.ENGLISH) && male==0)
                    continue;
                // Already available?
                if(new File("voice-"+lang.getVoice(male==0?false:true)+".jar").exists())
                    continue;
                else
                    System.out.println("DO Lang: " + lang);

                String filename = "voice-"+lang.getVoice(male==0?false:true)+"-5.1.zip";
                File file = new File(filename);
                FileUtils.copyURLToFile(new URL("http://mary.dfki.de/download/5.1/"+filename), file);
                ZipFile zipFile = new ZipFile(filename);
                zipFile.extractAll("./voices");
            }
            FileObject zipDirectory = FileObject.create().setPath("voices\\lib\\");
            Collection<File> files = DataHandler.listFiles(zipDirectory);
            for(File f : files)
            {
                FileObject endFile = FileObject.create(f).setPath(".");
                FileUtils.copyFile(f, endFile.toFile());
                DataHandler.deleteFile(endFile.setExtension("zip"));
                FileUtils.moveFile(endFile.toFile(), endFile.setName(endFile.getName().substring(0, endFile.getName().length()-4)).toFile());
            }
            DataHandler.deleteFile(FileObject.create().setPath("voices"));
            // TODO Jerry In which folder? MaryTTS don't recognize them...
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
