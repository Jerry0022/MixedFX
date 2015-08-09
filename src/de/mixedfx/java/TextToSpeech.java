package de.mixedfx.java;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.modules.synthesis.Voice;
import marytts.util.data.audio.AudioPlayer;

public class TextToSpeech {
	// Download Languages on https://github.com/marytts/marytts/blob/master/download/marytts-components.xml
	// E. g. http://mary.dfki.de/download/5.1/voice-bits1-hsmm-5.1.zip
	static MaryInterface marytts;
	static AudioPlayer ap;

	public static void main(String[] args) {
		try {
			marytts = new LocalMaryInterface();
			marytts.setVoice("dfki-pavoque-neutral-hsmm");
			System.out.println(Voice.getAvailableVoices());
			ap = new AudioPlayer();
		} catch (MaryConfigurationException ex) {
			ex.printStackTrace();
		}
		say("Was willst du eigentlich von mir!");
	}

	public static void say(String input) {
		try {
			AudioInputStream audio = marytts.generateAudio(input);

			ap.setAudio(audio);
			ap.start();
		} catch (SynthesisException ex) {
			System.err.println("Error saying phrase.");
		}
	}
}
