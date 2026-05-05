package Rules;

import java.util.ArrayList;

import TheGame.Board;
import TheGame.BoardSpace;
import XMLLEVELLOADER.PlayerWrper;

public class BoardRules {
	ArrayList<TheRules> rules = new ArrayList<>();
	public BoardRules(){
		rules.add(new PropertyRule());
	}
	/*
	players each start with $Y
	players collect $X when they Pass go
	when a player lands on a property they may
		if unowned
			buy
		if owned by other player
			pay
		if owned by player
			morgatge
	when a player lands on a rail road they may
		if unowned
			buy
			limited ride
		if owned by other player
			pay
			full ride -1
		if owned by player
			pay
			full ride +1
	when a player lands on a utility they may
		if unowned
			buy
		if owned by other player
			pay
		if owned by player
			attack houses/hotels
	
	when a player Lands on a chance they may
		draw and take the effect of the card
	
	when a player lands on a community chest they may
		draw and take the effect of the card.
		
	when a player lands on free parking the may
		collect the pot
		
	when a player lands on go to jail they must
		go directly to jail do not collecct $X
		
	when a player lands on jail there status is updated to just visiting
	
	
	
	player may build houses/hotels to increase rent
	(build 4 houses first then replace them with hotels.)
	
	the game ends when all but one player have gone bankrupt
	*/
	public void check(PlayerWrper player, BoardSpace boardSpace, Board board) {

		for (TheRules rule : rules) {
			rule.check(player, boardSpace, board);
		}
		
		
	}
	
	
	

}
