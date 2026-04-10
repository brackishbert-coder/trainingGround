package userInput;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import javax.swing.KeyStroke;

import Player.PlayerState;
import theStart.theView.TheControls.GameScreen;
import flatLand.trainingGround.Sprites.Sprites;
import flatLand.trainingGround.Sprites.TerminalSprite;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PlayerKeybordHandler {

	boolean gameMode = true;
	private static final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
	private static final String MOVE_RIGHT = "move right";
	private static final String MOVE_RIGHT_RELEASE = "move right release";
	private static final String MOVE_LEFT = "move left";
	private static final String MOVE_LEFT_RELEASE = "move left release";
	private static final String MOVE_UP = "move up";
	private static final String MOVE_UP_RELEASE = "move up release";
	private static final String MOVE_DOWN = "move down";
	private static final String MOVE_DOWN_RELEASE = "move down release";
	boolean dropAction = false;

	private static final String FIRE = "move fire";
	private XMLLEVELLOADER.PlayerWrper player;
	private XMLLEVELLOADER.FlatLanderWrper terminal;

	public PlayerKeybordHandler(XMLLEVELLOADER.PlayerWrper player) {
		this.player = player;
		terminal = player.getTerminal();

	}

	public PlayerKeybordHandler(XMLLEVELLOADER.FlatLanderWrper terminal) {
		this.player = new XMLLEVELLOADER.PlayerWrper(Color.blue, -100, -100, "testDummy", 1, false);
		this.terminal = terminal;

	}

	public PlayerKeybordHandler buildKeyBindings(GameScreen panel) {

		buildKeyBoardMap(panel);

		return this;
	}

	private void buildKeyBoardMap(GameScreen panel) {

		// GameControl
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("RIGHT"), MOVE_RIGHT);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), MOVE_RIGHT_RELEASE);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("LEFT"), MOVE_LEFT);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), MOVE_LEFT_RELEASE);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("UP"), MOVE_UP);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), MOVE_UP_RELEASE);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("DOWN"), MOVE_DOWN);
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), MOVE_DOWN_RELEASE);

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke("control CONTROL"), FIRE);

		panel.getActionMap().put(MOVE_RIGHT, new MoveAction(1, 0, player));
		panel.getActionMap().put(MOVE_RIGHT_RELEASE, new StopAction(player));
		panel.getActionMap().put(MOVE_LEFT, new MoveAction(-1, 0, player));
		panel.getActionMap().put(MOVE_LEFT_RELEASE, new StopAction(player));
		panel.getActionMap().put(MOVE_UP, new JumpAction(player));
		panel.getActionMap().put(MOVE_UP_RELEASE, new StopAction(player));
		panel.getActionMap().put(MOVE_DOWN, new MoveDownAction(player));
		panel.getActionMap().put(MOVE_DOWN_RELEASE, new StopAction(player));

		panel.getActionMap().put(FIRE, new AttackAction(player));

//		game control

		KeyStroke keyTStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK);
		panel.getInputMap(IFW).put(keyTStroke, "Terminal Mode");
		panel.getActionMap().put("Terminal Mode", new ChangeModeAction(player, terminal));

		// terminal control
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('q'), "q");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('w'), "w");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('e'), "e");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('r'), "r");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('t'), "t");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('y'), "y");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('u'), "u");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('i'), "i");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('o'), "o");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('p'), "p");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('['), "[");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(']'), "]");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('\\'), "\\");
		panel.getActionMap().put("q", new UpdateTerminalAction(terminal, "q"));
		panel.getActionMap().put("w", new UpdateTerminalAction(terminal, "w"));
		panel.getActionMap().put("e", new UpdateTerminalAction(terminal, "e"));
		panel.getActionMap().put("r", new UpdateTerminalAction(terminal, "r"));
		panel.getActionMap().put("t", new UpdateTerminalAction(terminal, "t"));
		panel.getActionMap().put("y", new UpdateTerminalAction(terminal, "y"));
		panel.getActionMap().put("u", new UpdateTerminalAction(terminal, "u"));
		panel.getActionMap().put("i", new UpdateTerminalAction(terminal, "i"));
		panel.getActionMap().put("o", new UpdateTerminalAction(terminal, "o"));
		panel.getActionMap().put("p", new UpdateTerminalAction(terminal, "p"));
		panel.getActionMap().put("[", new UpdateTerminalAction(terminal, "["));
		panel.getActionMap().put("]", new UpdateTerminalAction(terminal, "]"));
		panel.getActionMap().put("\\", new UpdateTerminalAction(terminal, "\\"));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('Q'), "Q");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('W'), "W");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('E'), "E");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('R'), "R");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('T'), "T");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('Y'), "Y");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('U'), "U");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('I'), "I");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('O'), "O");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('P'), "P");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('{'), "{");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('}'), "}");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('|'), "|");

		panel.getActionMap().put("Q", new UpdateTerminalAction(terminal, "Q"));
		panel.getActionMap().put("W", new UpdateTerminalAction(terminal, "w"));
		panel.getActionMap().put("E", new UpdateTerminalAction(terminal, "E"));
		panel.getActionMap().put("R", new UpdateTerminalAction(terminal, "R"));
		panel.getActionMap().put("T", new UpdateTerminalAction(terminal, "T"));
		panel.getActionMap().put("Y", new UpdateTerminalAction(terminal, "Y"));
		panel.getActionMap().put("U", new UpdateTerminalAction(terminal, "U"));
		panel.getActionMap().put("I", new UpdateTerminalAction(terminal, "I"));
		panel.getActionMap().put("O", new UpdateTerminalAction(terminal, "O"));
		panel.getActionMap().put("P", new UpdateTerminalAction(terminal, "P"));
		panel.getActionMap().put("{", new UpdateTerminalAction(terminal, "{"));
		panel.getActionMap().put("}", new UpdateTerminalAction(terminal, "}"));
		panel.getActionMap().put("|", new UpdateTerminalAction(terminal, "|"));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('a'), "a");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('s'), "s");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('d'), "d");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('f'), "f");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('g'), "g");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('h'), "h");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('j'), "j");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('k'), "k");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('l'), "l");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(';'), ";");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('\''), "\'");

		panel.getActionMap().put("a", new UpdateTerminalAction(terminal, "a"));
		panel.getActionMap().put("s", new UpdateTerminalAction(terminal, "s"));
		panel.getActionMap().put("d", new UpdateTerminalAction(terminal, "d"));
		panel.getActionMap().put("f", new UpdateTerminalAction(terminal, "f"));
		panel.getActionMap().put("g", new UpdateTerminalAction(terminal, "g"));
		panel.getActionMap().put("h", new UpdateTerminalAction(terminal, "h"));
		panel.getActionMap().put("j", new UpdateTerminalAction(terminal, "j"));
		panel.getActionMap().put("k", new UpdateTerminalAction(terminal, "k"));
		panel.getActionMap().put("l", new UpdateTerminalAction(terminal, "l"));
		panel.getActionMap().put(";", new UpdateTerminalAction(terminal, ";"));
		panel.getActionMap().put("\'", new UpdateTerminalAction(terminal, "\'"));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('A'), "A");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('S'), "S");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('D'), "D");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('F'), "F");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('G'), "G");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('H'), "H");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('J'), "J");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('K'), "K");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('L'), "L");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(':'), ":");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('\"'), "\"");

		panel.getActionMap().put("A", new UpdateTerminalAction(terminal, "A"));
		panel.getActionMap().put("S", new UpdateTerminalAction(terminal, "S"));
		panel.getActionMap().put("D", new UpdateTerminalAction(terminal, "D"));
		panel.getActionMap().put("F", new UpdateTerminalAction(terminal, "F"));
		panel.getActionMap().put("G", new UpdateTerminalAction(terminal, "G"));
		panel.getActionMap().put("H", new UpdateTerminalAction(terminal, "H"));
		panel.getActionMap().put("J", new UpdateTerminalAction(terminal, "J"));
		panel.getActionMap().put("K", new UpdateTerminalAction(terminal, "K"));
		panel.getActionMap().put("L", new UpdateTerminalAction(terminal, "L"));
		panel.getActionMap().put(":", new UpdateTerminalAction(terminal, ":"));
		panel.getActionMap().put("\"", new UpdateTerminalAction(terminal, "\""));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('z'), "z");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('x'), "x");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('c'), "c");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('v'), "v");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('b'), "b");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('n'), "n");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('m'), "m");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(','), ",");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('.'), ".");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('/'), "/");

		panel.getActionMap().put("z", new UpdateTerminalAction(terminal, "z"));
		panel.getActionMap().put("x", new UpdateTerminalAction(terminal, "x"));
		panel.getActionMap().put("c", new UpdateTerminalAction(terminal, "c"));
		panel.getActionMap().put("v", new UpdateTerminalAction(terminal, "v"));
		panel.getActionMap().put("b", new UpdateTerminalAction(terminal, "b"));
		panel.getActionMap().put("n", new UpdateTerminalAction(terminal, "n"));
		panel.getActionMap().put("m", new UpdateTerminalAction(terminal, "m"));
		panel.getActionMap().put(",", new UpdateTerminalAction(terminal, ","));
		panel.getActionMap().put(".", new UpdateTerminalAction(terminal, "."));
		panel.getActionMap().put("/", new UpdateTerminalAction(terminal, "/"));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('Z'), "Z");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('X'), "X");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('C'), "C");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('V'), "V");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('B'), "B");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('N'), "N");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('M'), "M");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('<'), "<");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('>'), ">");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('?'), "?");

		panel.getActionMap().put("Z", new UpdateTerminalAction(terminal, "Z"));
		panel.getActionMap().put("X", new UpdateTerminalAction(terminal, "X"));
		panel.getActionMap().put("C", new UpdateTerminalAction(terminal, "C"));
		panel.getActionMap().put("V", new UpdateTerminalAction(terminal, "V"));
		panel.getActionMap().put("B", new UpdateTerminalAction(terminal, "B"));
		panel.getActionMap().put("N", new UpdateTerminalAction(terminal, "N"));
		panel.getActionMap().put("M", new UpdateTerminalAction(terminal, "M"));
		panel.getActionMap().put("<", new UpdateTerminalAction(terminal, "<"));
		panel.getActionMap().put(">", new UpdateTerminalAction(terminal, ">"));
		panel.getActionMap().put("?", new UpdateTerminalAction(terminal, "?"));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('`'), "`");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('1'), "1");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('2'), "2");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('3'), "3");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('4'), "4");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('5'), "5");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('6'), "6");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('7'), "7");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('8'), "8");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('9'), "9");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('0'), "0");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('-'), "-");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('='), "=");

		panel.getActionMap().put("`", new UpdateTerminalAction(terminal, "`"));
		panel.getActionMap().put("1", new UpdateTerminalAction(terminal, "1"));
		panel.getActionMap().put("2", new UpdateTerminalAction(terminal, "2"));
		panel.getActionMap().put("3", new UpdateTerminalAction(terminal, "3"));
		panel.getActionMap().put("4", new UpdateTerminalAction(terminal, "4"));
		panel.getActionMap().put("5", new UpdateTerminalAction(terminal, "5"));
		panel.getActionMap().put("6", new UpdateTerminalAction(terminal, "6"));
		panel.getActionMap().put("7", new UpdateTerminalAction(terminal, "7"));
		panel.getActionMap().put("8", new UpdateTerminalAction(terminal, "8"));
		panel.getActionMap().put("9", new UpdateTerminalAction(terminal, "9"));
		panel.getActionMap().put("0", new UpdateTerminalAction(terminal, "0"));
		panel.getActionMap().put("-", new UpdateTerminalAction(terminal, "-"));
		panel.getActionMap().put("=", new UpdateTerminalAction(terminal, "="));

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('~'), "~");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('!'), "!");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('@'), "@");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('#'), "#");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('$'), "$");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('%'), "%");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('^'), "^");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('&'), "&");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('*'), "*");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('('), "(");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(')'), ")");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('_'), "_");
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('+'), "+");

		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke(' '), " ");

		panel.getActionMap().put(" ", new UpdateTerminalAction(terminal, " "));
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('\n'), "\n");

		panel.getActionMap().put("\n", new UpdateTerminalAction(terminal, "\n"));

		panel.getActionMap().put("\r", new UpdateTerminalAction(terminal, "\r"));
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('\r'), "\r");

		panel.getActionMap().put("\b", new UpdateTerminalAction(terminal, "\b"));
		panel.getInputMap(IFW).put(KeyStroke.getKeyStroke('\b'), "\b");


		panel.getActionMap().put("~", new UpdateTerminalAction(terminal, "~"));
		panel.getActionMap().put("!", new UpdateTerminalAction(terminal, "!"));
		panel.getActionMap().put("@", new UpdateTerminalAction(terminal, "@"));
		panel.getActionMap().put("#", new UpdateTerminalAction(terminal, "#"));
		panel.getActionMap().put("$", new UpdateTerminalAction(terminal, "$"));
		panel.getActionMap().put("%", new UpdateTerminalAction(terminal, "%"));
		panel.getActionMap().put("^", new UpdateTerminalAction(terminal, "^"));
		panel.getActionMap().put("&", new UpdateTerminalAction(terminal, "&"));
		panel.getActionMap().put("*", new UpdateTerminalAction(terminal, "*"));
		panel.getActionMap().put("(", new UpdateTerminalAction(terminal, "("));
		panel.getActionMap().put(")", new UpdateTerminalAction(terminal, ")"));
		panel.getActionMap().put("_", new UpdateTerminalAction(terminal, "_"));
		panel.getActionMap().put("+", new UpdateTerminalAction(terminal, "+"));

	}

	private class ChangeModeAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private XMLLEVELLOADER.FlatLanderWrper terminal;

		ChangeModeAction(XMLLEVELLOADER.PlayerWrper play, XMLLEVELLOADER.FlatLanderWrper terminal) {

			this.terminal = terminal;
		}

		public void actionPerformed(ActionEvent e) {
			gameMode = !gameMode;
		}
	}

	private class UpdateTerminalAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private XMLLEVELLOADER.FlatLanderWrper terminal;

		public XMLLEVELLOADER.FlatLanderWrper getTerminal() {
			return terminal;
		}

		private String key;

		UpdateTerminalAction(XMLLEVELLOADER.FlatLanderWrper terminal, String key) {

			this.terminal = terminal;
			this.key = key;
		}

		public void actionPerformed(ActionEvent e) {
			terminal.update(key, gameMode);
		}
	}

	private class StopAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private XMLLEVELLOADER.PlayerWrper player;

		StopAction(XMLLEVELLOADER.PlayerWrper player) {

			this.player = player;
		}

		public void actionPerformed(ActionEvent e) {
			if (gameMode) {
				player.setState(PlayerState.STILL);

				player.getSprite().updateState();
			}
		}
	}

	private class MoveDownAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private XMLLEVELLOADER.PlayerWrper player;

		MoveDownAction(XMLLEVELLOADER.PlayerWrper player) {

			this.player = player;
		}

		public void actionPerformed(ActionEvent e) {
			if (gameMode) {
				int moveY = 1;
				player.setMoveY(moveY);

				player.getSprite().updateState();
			}
		}
	}

	private class MoveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int xDirection;
		private XMLLEVELLOADER.PlayerWrper player;
		private int yDirection;

		MoveAction(int xDirection, int yDirection, XMLLEVELLOADER.PlayerWrper player) {

			this.xDirection = xDirection;
			this.yDirection = yDirection;
			this.player = player;
		}

		public void actionPerformed(ActionEvent e) {
			if (gameMode) {
				player.setState(PlayerState.MOVE);

				int moveX = xDirection * 5;
				player.direction = xDirection;
				player.setMoveX(moveX);
			
				player.getSprite().updateState();
			}
		}
	}

	private class AttackAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		XMLLEVELLOADER.PlayerWrper player;

		AttackAction(XMLLEVELLOADER.PlayerWrper player) {

			this.player = player;
		}

		public void actionPerformed(ActionEvent e) {
			if (gameMode)
				player.setState(PlayerState.ATTACK);

		}
	}

	private class JumpAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		XMLLEVELLOADER.PlayerWrper player;
		private int yDirection = -1;

		JumpAction(XMLLEVELLOADER.PlayerWrper player) {

			this.player = player;
		}

		public void actionPerformed(ActionEvent e) {
			if (gameMode) {
				if (!dropAction) {
					int moveY = yDirection * 60;

					player.setMoveY(moveY);

					player.getSprite().updateState();
					dropAction = true;
				} else {
					dropAction = false;
				}
			}
		}

	}

	private class FallAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		XMLLEVELLOADER.PlayerWrper player;
		private int yDirection = -1;

		FallAction(XMLLEVELLOADER.PlayerWrper player) {

			this.player = player;
		}

		public void actionPerformed(ActionEvent e) {
			if (gameMode) {
				if (player.state() == PlayerState.JUMP) {
					player.setState(PlayerState.FALL);

				}
			}
		}

	}

}
