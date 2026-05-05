package audiolizer;

//MidiAudioSink.java
import javax.sound.midi.*;

import Box.Token.TokenType;
import audiolizer.InterpreterAudio.AudioEvent;

import java.util.concurrent.*;

public final class MidiAudioSink implements InterpreterAudio, AutoCloseable {
	private final BlockingQueue<AudioEvent> q = new LinkedBlockingQueue<>();
	private final Thread worker;
	private final Synthesizer synth;
	private final MidiChannel ch;
	private volatile boolean running = true;

	public MidiAudioSink() {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open(); // <-- this is where your error occurs
			ch = synth.getChannels()[0];
			ch.programChange(0);
			worker = new Thread(this::loop, "AudioSink");
			worker.start();
			System.out.println("[MIDI] opened " + synth.getDeviceInfo().getName());
		} catch (Exception midiFail) {
			throw new RuntimeException("MIDI unavailable; use PcmAudioSink fallback", midiFail);
		}
	}

	@Override
	public void onEvent(AudioEvent e) {
		q.offer(e);
	}

	private void loop() {
		while (running) {
			try {
				AudioEvent e = q.take();
				handle(e);
			} catch (InterruptedException ie) {
				break;
			}
		}
	}

	private void handle(AudioEvent e) {
		switch (e.type()) {
		case TokenType.PRINT -> playToken(e.lexeme(), e.tNanos());
		case TokenType.TNIRP -> playToken(e.lexeme(), e.tNanos());
		case TokenType.PLUS -> playToken(e.lexeme(), e.tNanos());
		case TokenType.MINUS -> playToken(e.lexeme(), e.tNanos());
		case TokenType.FORWARDSLASH -> playToken(e.lexeme(), e.tNanos());
		case TokenType.TIMES -> playToken(e.lexeme(), e.tNanos());
		default -> throw new IllegalArgumentException("Unexpected value: " + e.type());
		}
	}

	// --- simple mappings you can tweak ---
	private void playToken(String lex, long tNanos) {
		// hash token into a scale degree
		int base = 48; // C3
		int[] scale = { 0, 2, 3, 5, 7, 8, 10 }; // minor
		int deg = Math.floorMod(lex.hashCode(), scale.length);
		int note = base + scale[deg];
		blip(note, 70, 80);
	}

	private void playNumber(double v) {
		// map number roughly 0..1 to notes in C3..C5
		int note = (int) Math.round(48 + (v * 24));
		blip(note, 90, 100);
	}

	private void arp(String ident) {
		int root = 52 + Math.floorMod(ident.hashCode(), 12); // -ish
		blip(root, 60, 90);
		blip(root + 7, 60, 90);
	}

	private void blip(int note, int vel, int lenMs) {
		ch.noteOn(clamp(note, 0, 127), clamp(vel, 0, 127));
		// quick off on a scheduler so we don't block audio thread:
		new Thread(() -> {
			try {
				Thread.sleep(lenMs);
			} catch (InterruptedException ignored) {
			}
			ch.noteOff(clamp(note, 0, 127));
		}, "NoteOff").start();
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	@Override
	public void close() {
		running = false;
		worker.interrupt();
		synth.close();
	}
}
