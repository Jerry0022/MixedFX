package de.mixedfx.java;

import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;

public class TTSTester {
	public static void main(String[] args) throws Exception {

		MaryInterface marytts = new LocalMaryInterface();
		Set<String> voices = marytts.getAvailableVoices();
		System.out.println(voices);
		marytts.setVoice(voices.iterator().next());
		AudioInputStream audio = marytts.generateAudio("Hallo Welt.");
		AudioPlayer player = new AudioPlayer(audio);
		player.start();
		player.join();
	}
}
