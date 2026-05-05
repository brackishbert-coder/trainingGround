package TheGame;

import java.awt.Graphics;

import java.util.ArrayList;
import java.util.HashMap;

import Math.*;
import MonopolyActions.Actn;
import MonopolyActions.Buy;
import MonopolyActions.CCSquare;
import MonopolyActions.Chance;
import MonopolyActions.FreeParking;
import MonopolyActions.GTJail;
import MonopolyActions.GoAction;
import MonopolyActions.IncomeTax;
import MonopolyActions.LuxeryTax;
import MonopolyActions.Morgatge;
import MonopolyActions.Move;
import MonopolyActions.RoundAction;
import MonopolyActions.Sell;
import MonopolyActions.Visiting;
import Rules.BoardRules;
import Rules.RuleInterpreter;
import XMLLEVELLOADER.PlayerWrper;

public class Board {
	int width = 1100;
	int height = 1100;
	static ArrayList<BoardSpace> gameSpaces = new ArrayList<>();
	static ArrayList<BoardSpace> freeSpaces = new ArrayList<>();
	static HashMap<PlayerWrper, ArrayList<BoardSpace>> takenSpaces = new HashMap<>();

	ArrayList<SpaceConnections> connections = new ArrayList<>();
	BoardRules rules = new BoardRules();
	RuleInterpreter rI = new RuleInterpreter(rules);

	ArrayList<PlayerWrper> players = new ArrayList<>();
	private HashMap<Integer,PlayerWrper> turnOrderPlayer = new HashMap<>();
	private HashMap<PlayerWrper,Integer> playersTurnOrder = new HashMap<>();
	private Integer currentTurn = 0;

	HashMap<PlayerWrper, PlayerSpaces> portfolios = new HashMap<>();
	HashMap<PlayerWrper, PlayerChance> chance = new HashMap<>();
	HashMap<PlayerWrper, PlayerCommieChest> comunism = new HashMap<>();

	HashMap<PlayerWrper, ArrayList<BoardSpace>> currentPositions = new HashMap<>();
	HashMap<PlayerWrper, Status> statusus = new HashMap<>();
	ArrayList<RoundAction> roundActionHistory = new ArrayList<>();
	ArrayList<Actn> actionsToTakeThisRound = new ArrayList<>();
	ArrayList<HashMap<PlayerWrper, PlayerPositions>> posHist = new ArrayList<>();
	private GameDice instance;

	public Board() {

		instance = GameDice.getInstance();

	}

	public void updatePlayerPositionsBasedOnBoardRules() {

	}

	public void updatePlayerStatus(PlayerWrper p, Status s) {
		if (players.contains(p))
			statusus.replace(p, s);
	}

	public void addToPlayerSpacePortfolio(PlayerWrper p, Space s) {
		if (players.contains(p)) {
			PlayerSpaces playerSpaces = portfolios.get(p);
			playerSpaces.addSpace(s);
			portfolios.replace(p, playerSpaces);
		}
	}

	public void addToPlayerChance(PlayerWrper p, Chance c) {
		if (players.contains(p)) {
			PlayerChance playerChance = chance.get(p);
			playerChance.addChance(c);
			chance.replace(p, playerChance);
		}
	}

	public void addToTheDelinquencyOfTheYouth(PlayerWrper p, CommieChest c) {
		if (players.contains(p)) {
			PlayerCommieChest playerCommieChest = comunism.get(p);
			playerCommieChest.addToNothing(c);
			System.out.println("hey");
			comunism.replace(p, playerCommieChest);
		}

	}

	public void addToBoardSpace(BoardSpace boardSpace) {
		gameSpaces.add(boardSpace);
		freeSpaces.add(boardSpace);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void draw(Graphics frame) {

		for (BoardSpace boardSpace : gameSpaces) {
			boardSpace.draw(frame);
		}

	}

	public void addPlayers(ArrayList<PlayerWrper> players) {
		this.players = players;
	}

	public void addPlayer(PlayerWrper player) {

		this.players.add(player);

		for (BoardSpace boardSpace : gameSpaces) {
			if (boardSpace.getStatus() == Status.GO) {
				ArrayList<BoardSpace> arrayList = new ArrayList<BoardSpace>();
				arrayList.add(boardSpace);
				currentPositions.put(player, arrayList);
				statusus.remove(player);
				statusus.put(player, boardSpace.getStatus());

			}
		}

	}

	public ArrayList<Actn> getActions() {
		return actionsToTakeThisRound;

	}

	public void collectActions(PlayerWrper player) {

		

			Status status = statusus.get(player);

			if(!isBuyable(status)) {
				if(status == Status.C0 || status == Status.C1 || status == Status.C2|| status == Status.C2UB) {
					actionsToTakeThisRound.add(new Chance(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.CC0 || status == Status.CC1 || status == Status.CC2) {
					actionsToTakeThisRound.add(new CCSquare(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.GOTOJAIL) {
					actionsToTakeThisRound.add(new GTJail(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.JAIL) {
					actionsToTakeThisRound.add(new Visiting(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.GO) {
					actionsToTakeThisRound.add(new GoAction(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.FREEPARKING) {
					actionsToTakeThisRound.add(new FreeParking(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.ST) {
					actionsToTakeThisRound.add(new LuxeryTax(player, status, freeSpaces, takenSpaces));
				}else if(status == Status.INCOMETAX) {
					actionsToTakeThisRound.add(new IncomeTax(player, status, freeSpaces, takenSpaces));
				}
			}
			if (!isSpaceOwned(status) && isBuyable(status)) {
				actionsToTakeThisRound.add(new Buy(player, status, freeSpaces, takenSpaces));
			}
			if (isSpaceOwned(status)&& isBuyable(status)) {
				double dub = Math.random() * 1;
				if (dub > .5)
					actionsToTakeThisRound.add(new Sell(player, status, freeSpaces, takenSpaces));
				else
					actionsToTakeThisRound.add(new Morgatge(player, status, freeSpaces, takenSpaces));

			}

			instance.roll();
			int x = playersnextxpos(player);
			int y = playersnextypos(player);
			BoardSpace findSpaceGivenXY = findSpaceGivenStatus(statusus.get(player));
			if (findSpaceGivenXY != null) {
				statusus.put(player, findSpaceGivenXY.getStatus());
				actionsToTakeThisRound.add(new Move(x, y, player, this));
				GameDice.getInstance().setNextTurn(false);
			}
		

	}

	private boolean isBuyable(Status status) {
		if (status == Status.GO || status == Status.GOTOJAIL || status == Status.FREEPARKING || status == Status.CC0
				|| status == Status.CC1 || status == Status.CC2 || status == Status.ST || status == Status.INCOMETAX
				|| status == Status.C0 || status == Status.C1 || status == Status.C2 | status == Status.C2UB
				|| status == Status.JAIL)
			return false;
		else
			return true;
	}

	public boolean isSpaceOwned(Status status) {
		for (BoardSpace boardSpace : freeSpaces) {
			if (boardSpace.getStatus() == status) {
				return false;
			}
		}
		return true;
	}

	private BoardSpace findSpaceGivenStatus(Status status) {
		int lastRoll = instance.getLastRoll();

		Status nextStatus = Status.getNextStatus(status, lastRoll);

		for (BoardSpace boardSpace : gameSpaces) {
			if (boardSpace.getStatus() == nextStatus)
				return boardSpace;
		}
		return null;
	}

	public int playersnextypos(PlayerWrper player) {

		Status playerStatus = statusus.get(player);

		Status nextStatus = Status.getNextStatus(playerStatus, instance.getLastRoll());
		for (BoardSpace boardSpace : gameSpaces) {
			if (nextStatus == boardSpace.getStatus()) {
				return boardSpace.getyPos();

			}
		}

		return getYPosOfGO();
	}

	private int getYPosOfGO() {

		for (BoardSpace boardSpace : gameSpaces) {
			if (boardSpace.getStatus() == Status.GO) {
				return boardSpace.getyPos();
			}
		}
		System.out.println("no Go found");
		return -1;
	}

	public int playersnextxpos(PlayerWrper player) {

		Status playerStatus = statusus.get(player);

		Status nextStatus = Status.getNextStatus(playerStatus, instance.getLastRoll());

		for (BoardSpace boardSpace : gameSpaces) {
			if (nextStatus == boardSpace.getStatus()) {
				return boardSpace.getxPos();

			}
		}

		return getXPosOfGO();
	}

	private int getXPosOfGO() {

		for (BoardSpace boardSpace : gameSpaces) {
			if (boardSpace.getStatus() == Status.GO) {
				return boardSpace.getxPos();
			}
		}
		System.out.println("no Go thisfound");
		return -1;
	}

	public void executeActions(PlayerWrper player) {
		boolean resol = true;
		if (!previousRoundActionResolved()) {
			for (Actn roundAction : actionsToTakeThisRound) {
				roundAction.execute();
				resol = resol && roundAction.isResolved();
			}
		}

		if (resol) {
			actionsToTakeThisRound.clear();
		}

	}

	public void updatePlayerPositionthis() {
		for (PlayerWrper playerWrper : players) {
			int x = playerWrper.getX();
			int y = playerWrper.getY();
			ArrayList<BoardSpace> spaces = new ArrayList<BoardSpace>();

			BoardSpace bs = findSpaceGivenXY(x, y);
			if (bs != null) {
				spaces.add(bs);
				currentPositions.put(playerWrper, spaces);
				statusus.remove(playerWrper);
				statusus.put(playerWrper, bs.getStatus());
			}
		}

	}

	private BoardSpace findSpaceGivenXY(int x, int y) {

		for (BoardSpace boardSpace : gameSpaces) {
			int getxPos = boardSpace.getxPos();
			int getyPos = boardSpace.getyPos();

			if (x == getxPos && y == getyPos) {
				return boardSpace;
			}

		}
		return null;
	}

	public boolean previousRoundActionResolved() {
		boolean resolved = true;
		for (Actn roundAction : actionsToTakeThisRound) {
			resolved = resolved && roundAction.isResolved();
		}
		return resolved && GameDice.getInstance().nextTurn();
	}

	public int getPlayerCurrentPositionX(PlayerWrper player) {
		ArrayList<BoardSpace> arrayList = currentPositions.get(player);
		return arrayList.get(0).getxPos();

	}

	public int getPlayerCurrentPositionY(PlayerWrper player) {
		ArrayList<BoardSpace> arrayList = currentPositions.get(player);
		return arrayList.get(0).getyPos();
	}

	public boolean hasWinner() {
		return false;
	}

	public ArrayList<PlayerWrper> getPlayers() {
		return players;
	}

	public PlayerWrper currentPlayer() {

		return turnOrderPlayer.get(currentTurn);
	}

	public Integer getCurrentTurn() {
		return currentTurn;
	}

	public void setCurrentTurn(Integer currentTurn) {
		if(currentTurn>=players.size())
			currentTurn = 0;
		this.currentTurn = currentTurn;
	}



	public void setPlayersTurnOrder(Integer turn, PlayerWrper player) {
		
		
		this.playersTurnOrder.put(player, turn);
		this.turnOrderPlayer.put(turn, player);
	}

	public Integer getPlayersTurnOrder(PlayerWrper player) {
		return playersTurnOrder.get(player);
	}

	public void setPlayersTurnOrder(HashMap<PlayerWrper,Integer> playersTurnOrder) {
		this.playersTurnOrder = playersTurnOrder;
	}

	public static ArrayList<BoardSpace> getTakenSpacesByPlayer(PlayerWrper player) {
		return takenSpaces.get(player);
	}

	

	public boolean isSpaceOwnedByPlayer(Status status, PlayerWrper player) {
		ArrayList<BoardSpace> arrayList = takenSpaces.get(player);
		if(arrayList!=null) {
		for (BoardSpace boardSpace : arrayList) {
			if(boardSpace.getStatus() == status) {
				return true;
			}
		}
		}
		
		
		return false;
	}

	public void interpretRules(PlayerWrper player) {
		int playersnextypos = playersnextypos(player);
		int playersnextxpos = playersnextxpos(player);
		BoardSpace findSpaceGivenXY = findSpaceGivenXY(playersnextxpos, playersnextypos);
		rI.checkRules(player, findSpaceGivenXY, this);
	}

}
