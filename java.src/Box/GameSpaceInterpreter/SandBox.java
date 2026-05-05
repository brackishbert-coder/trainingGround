package Box.GameSpaceInterpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import Box.Box.Box;
import Box.Box.Observer;
import Box.Grouper.Grouper;
import Box.Interpreter.Interpreter;
import Box.Interpreter.RuntimeError;
import Box.Scanner.Scanner;
import Box.Token.Token;
import Box.Token.TokenType;
import FlatLandStructure.ViewableFlatLand;

import Parser.Declaration;
import Parser.ParserTest;
import audiolizer.InterpreterAudio;
import audiolizer.MidiAudioSink;
import audiolizer.PcmAudioSink;
import flatLand.trainingGround.EventHandler;
import resolver.Resolver;
import theStart.thePeople.FlatLander;
import theStart.thePeople.FlatLanderFaceBook;


public class SandBox extends Box {
	private  final Interpreter interpreter = new Interpreter();
	static boolean hadError = false;
	static boolean hadRuntimeError = false;
	private static ByteArrayOutputStream baos;
	private Observer promptOb;
	private ArrayList<FlatLander> facebook;
	private FlatLanderFaceBook flatLanderFaceBook;
	private ViewableFlatLand viewableFlatLand;
	private EventHandler events;
	private MidiAudioSink audio;
	private InterpreterAudio audio2;

	public SandBox(ByteArrayOutputStream baos2, ArrayList<FlatLander> facebook, FlatLanderFaceBook fb,
			ViewableFlatLand viewableFlatLand, EventHandler events) {
		super(baos2, facebook);
		baos = baos2;
		this.facebook = facebook;
		this.flatLanderFaceBook = fb;
		this.viewableFlatLand = viewableFlatLand;
		this.events = events;

	}






	public void run() {

		try {
			runPrompt();

		} catch (Exception e) {
			// Throwing an exception
			System.out.println("Exception is caught");
		}
	}

	public void runPrompt() throws IOException {

		System.out.flush();
		String string = baos.toString();
		baos.flush();
		baos.reset();
		if (string.length() > 0)
			promptOb.notify(string);
		System.out.flush();

	}

	private void runFile(String string, boolean forward, boolean backward) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(string));
		run(new String(bytes, Charset.defaultCharset()), forward);
		if (hadError)
			System.exit(65);
		if (hadRuntimeError)
			System.exit(70);
	}

	public void runJson(String string, boolean forward) {
		Scanner scanner = new Scanner(string);
		List<Token> tokens = scanner.scanTokensFirstPass();

		Grouper grouper = new Grouper((ArrayList<Token>) tokens);
		ArrayList<Token> toks = grouper.scanTokensSecondPass();

		ParserTest parser = new ParserTest(toks, true, false);
		List<Declaration> statements = parser.parse();

		// interpreter.setForward(!forward);
		interpreter.setForward(forward);
		Resolver resolver = new Resolver(interpreter);
		resolver.resolve(statements);

		if (hadError)
			return;

		if (audio2 == null) {
			try {
				System.out.println("Attempting to use MIDI audio...");
			    audio2 = new MidiAudioSink();
			    System.out.println("MIDI audio enabled.");
			} catch (RuntimeException ex) {
			    System.err.println(ex.getMessage());
			    System.out.println("Falling back to PCM audio...");
			    audio2 = new PcmAudioSink(); // transparent fallback
			    System.out.println("PCM audio enabled.");
			}
			interpreter.setAudio(audio2);
			installShutdownHook(audio2);
		}
		interpreter.interpret(statements);

	}
	static void installShutdownHook(InterpreterAudio c) {
	    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        try { ((AutoCloseable)c).close(); } catch (Exception ignored) {}
	    }, "AudioShutdown"));
	}

	public void run(String string, boolean forward) {
		Scanner scanner = new Scanner(string);
		List<Token> tokens = scanner.scanTokensFirstPass();

		Grouper grouper = new Grouper((ArrayList<Token>) tokens);
		ArrayList<Token> toks = grouper.scanTokensSecondPass();

		ParserTest parser = new ParserTest(toks, true, false);
		List<Declaration> statements = parser.parse();

		ObjectMapper om = new ObjectMapper();
		try {
			String jsonResult = om.writeValueAsString(statements);
			System.out.println(jsonResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void error(int column, int line, String message, boolean report) {
		report(column, line, "", message, report);
	}

	public static void error(Token token, String message, boolean report) {
		if (token != null) {
			if (token.type == TokenType.EOF)
				report(token.column, token.line, " at end", message, report);
			else
				report(token.column, token.line, " at '" + token.lexeme + "'", message, report);
		} else
			report(-1, -1, " at -1", message, report);

	}

	private static void report(int column, int line, String where, String message, boolean report) {
		if (report) {
			System.err.println("[column " + column + ", line " + line + "] Error " + where + ": " + message);
			System.out.println("[column " + column + ", line " + line + "] Error " + where + ": " + message);
			hadError = true;
		}
	}

	public static void resetHadError() {
		hadError = false;
	}

	public static void runtimeError(RuntimeError e) {
		System.err.println(e.getMessage() + " \n[line: " + e.token.line + " column: " + e.token.column + "]");
		System.out.println(e.getMessage() + " \n[line: " + e.token.line + " column: " + e.token.column + "]");
		hadRuntimeError = true;
	}

	public void addObserver(Observer promptOb) {

		this.promptOb = promptOb;

	}

	public void notify(String string) {
		runJson(string, true);
		System.out.flush();
		hadError = false;

	}

	public ArrayList<FlatLander> getFacebook() {
		return facebook;
	}

	public FlatLanderFaceBook getFlatLanderFaceBook() {
		return flatLanderFaceBook;
	}

	public ViewableFlatLand getViewableFlatLand() {
		return viewableFlatLand;
	}

	public EventHandler getEvents() {
		return events;
	}

}
