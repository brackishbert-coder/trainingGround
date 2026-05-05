package Playable;

import java.awt.Color;
import java.io.IOException;

import FSM.Level;
import FSM.LevelI;
import FlatLandStructure.ViewableFlatLand;
import FlatLander.FlatLandFacebook;
import TheGame.Board;
import XMLLEVELLOADER.PlayerWrper;
import flatLand.trainingGround.Sprites.SkeletonTwo;
import flatLand.trainingGround.Sprites.ZombieBaby;
import theStart.theView.TheControls.GameScreen;

public class LevelZero extends Level implements LevelI {
	PlayerWrper player= null;
	private PlayerWrper player1;
	private PlayerWrper player2;

	
	
	public LevelZero() {
		try {
		player=new PlayerWrper(Color.RED,0,0,"Player0",1.0,true);
			player.setSprite(new SkeletonTwo("/home/wes/git/Monopoly_Island_Index/Monopoly_Island_Index/res/zombie_n_skeleton2.png", 90));
		FlatLandFacebook.getInstance().requestToken(this);
		FlatLandFacebook.getInstance().add(player,this);
		FlatLandFacebook.getInstance().releaseToken(this);
		player1=new PlayerWrper(Color.RED,0,0,"Player1",1.0,true);
		FlatLandFacebook.getInstance().requestToken(this);
		FlatLandFacebook.getInstance().add(player1,this);
		FlatLandFacebook.getInstance().releaseToken(this);
		player1.setSprite(new ZombieBaby("/home/wes/git/Monopoly_Island_Index/Monopoly_Island_Index/res/zombie_n_skeleton2.png", 90));
		player2=new PlayerWrper(Color.RED,0,0,"Player2",1.0,true);
		FlatLandFacebook.getInstance().requestToken(this);
		FlatLandFacebook.getInstance().add(player2,this);
		FlatLandFacebook.getInstance().releaseToken(this);
		player2.setSprite(new ZombieBaby("/home/wes/git/Monopoly_Island_Index/Monopoly_Island_Index/res/zombie_n_skeleton2.png", 90));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	@Override
	public boolean play(ViewableFlatLand flatLandLE, Board board, GameScreen panel) {
		board.addPlayer(player);
		board.setPlayersTurnOrder(0,player);
		board.addPlayer(player1);
		board.setPlayersTurnOrder(1,player1);	
		board.addPlayer(player2);
		board.setPlayersTurnOrder(2,player2);	
		super.play(flatLandLE,board,panel);
	
		
		
		
		return false;
	}

}
