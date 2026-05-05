package TheGame;


import java.awt.Color;
import java.awt.Graphics;


public class BoardSpace implements Space {
	private int position;
	private int side;
	private String name;
	private int boardWidth;
	private int boardHeight;
	private int xPos = 0;
	private int yPos = 0;
	private Status status;
    int topBottemX = 15;
    int topBottemY = -65;
    int rightX = -40;
    int rightY = -15;
	private boolean morgataged;
    
    
    public BoardSpace(int position, int side, String name, int width, int height,Status status) {
		this.position = position;
		this.side = side;
		this.name = name;
		this.boardWidth = width;
		this.boardHeight = height;
		this.status = status;
	}				

	public void	 draw(Graphics graphics) {

		if (side ==1) {
			graphics.setColor(Color.white);
			for (int i = 0; i < (boardHeight/20); i++) {
				graphics.drawLine(position *  (boardWidth/20), i, position *  (boardWidth/20)+ (boardWidth/20), i);
				xPos=position *  (boardWidth/20)+topBottemX;
				yPos = i+topBottemY;
			}
			graphics.setColor(Color.BLACK);
			if(name.contains("0I")) {
				graphics.setColor(Color.RED);
				for (int i = 25; i < (boardHeight/20); i++) {
					graphics.drawLine(position *  (boardWidth/20), i, position *  (boardWidth/20)+ (boardWidth/20), i);
					
				}
				graphics.setColor(Color.BLACK);
			}
			if(name.contains("1I")) {
				graphics.setColor(Color.CYAN);
				for (int i = 25; i < (boardHeight/20); i++) {
					graphics.drawLine(position *  (boardWidth/20), i, position *  (boardWidth/20)+ (boardWidth/20), i);
					
				}
				graphics.setColor(Color.BLACK);
			}
			
			
			
			if(name.contains("Triplet"))
				graphics.drawLine(position *  (boardWidth/20), 25, position *  (boardWidth/20)+ (boardWidth/20), 25);
			graphics.drawRect(position * (boardWidth/20), 0, (boardWidth/20), (boardHeight/20));
			
		} else if (side ==2) {
			
			
			graphics.setColor(Color.white);
			for (int i = 0; i < (boardHeight/20); i++) {
				graphics.drawLine(10*(boardWidth/20)+i,(position-10)*(boardHeight/20),10*(boardWidth/20)+i,(position-10)*(boardHeight/20)+(boardHeight/20));
				xPos=10*(boardWidth/20)+i+rightX;
				yPos = (position-10)*(boardHeight/20)+rightY;
			}
			graphics.setColor(Color.BLACK);
			
			if(name.contains("2I")) {
				graphics.setColor(Color.MAGENTA);
				for (int i = 10*(boardWidth/20); i < 10*(boardWidth/20)+25; i++) {
					graphics.drawLine(i,(position-10)*(boardHeight/20),i,(position-10)*(boardHeight/20)+(boardHeight/20));
					
				}
				
			}
			if(name.contains("3I")) {
				graphics.setColor(Color.PINK);
				for (int i = 10*(boardWidth/20); i < 10*(boardWidth/20)+25; i++) {
					graphics.drawLine(i,(position-10)*(boardHeight/20),i,(position-10)*(boardHeight/20)+(boardHeight/20));
					
				}
				
			}
			
			
			graphics.setColor(Color.BLACK);
			
			
			
			
			
			if(name.contains("Triplet")) {
				graphics.drawLine(10*(boardWidth/20)+25,(position-10)*(boardHeight/20),10*(boardWidth/20)+25,(position-10)*(boardHeight/20)+(boardHeight/20));
			}
			graphics.drawRect(10*(boardWidth/20),(position-10)*(boardHeight/20), (boardWidth/20), (boardHeight/20));
			
		}else if (side ==3) {
			
			int pos = position -20;
			pos = Math.abs(pos-10);
			
			graphics.setColor(Color.white);
			for (int i = 0; i < (boardHeight/20); i++) {
				graphics.drawLine(pos*(boardWidth/20),10*(boardHeight/20)+i,pos*(boardWidth/20)+(boardWidth/20),10*(boardHeight/20)+i);
				xPos=pos*(boardWidth/20)+topBottemX;
				yPos = 10*(boardHeight/20)+i+topBottemY;
			}
			graphics.setColor(Color.BLACK);
			
			
			
			
			if(name.contains("4I")) {
				graphics.setColor(Color.YELLOW);
				for (int i = 10*(boardHeight/20); i < 10*(boardHeight/20)+25; i++) {
					graphics.drawLine(pos*(boardWidth/20),i,pos*(boardWidth/20)+(boardWidth/20),i);
					
				}
				
			}
			if(name.contains("5I")) {
				graphics.setColor(Color.ORANGE);
				for (int i = 10*(boardHeight/20); i < 10*(boardHeight/20)+25; i++) {
					graphics.drawLine(pos*(boardWidth/20),i,pos*(boardWidth/20)+(boardWidth/20),i);
					
				}
				
			}
			graphics.setColor(Color.BLACK);
			if(name.contains("Triplet"))
				graphics.drawLine(pos*(boardWidth/20),10*(boardHeight/20)+25,pos*(boardWidth/20)+(boardWidth/20),10*(boardHeight/20)+25);
			graphics.drawRect(pos*(boardWidth/20),10*(boardHeight/20), (boardWidth/20), (boardHeight/20));
			
		}else if (side ==4) {

			
			
			int pos = position-30;
			pos = Math.abs(pos-10);
			
			graphics.setColor(Color.white);
			for (int i = 0; i < (boardHeight/20); i++) {
				graphics.drawLine(i,pos*(boardHeight/20),i,pos*(boardHeight/20)+(boardHeight/20));
				xPos=i+rightX;
				yPos = pos*(boardHeight/20)+rightY;
			}
			graphics.setColor(Color.BLACK);
			
			
			if(name.contains("6I")) {
				graphics.setColor(Color.PINK);
				for (int i = 25; i < boardWidth/20; i++) {
					graphics.drawLine(i,pos*(boardHeight/20),i,pos*(boardHeight/20)+(boardHeight/20));
					
				}
				
			}
			
			if( name.contains("FIN") || name.contains("Legal") ) {
				graphics.setColor(Color.LIGHT_GRAY);
				for (int i = 25; i < boardWidth/20; i++) {
					graphics.drawLine(i,pos*(boardHeight/20),i,pos*(boardHeight/20)+(boardHeight/20));
					
				}
				
			}
			
			
			graphics.setColor(Color.BLACK);
			if(name.contains("Triplet") || name.contains("FIN") || name.contains("Legal")    )
				graphics.drawLine(25,pos*(boardHeight/20),25,pos*(boardHeight/20)+(boardHeight/20));
			graphics.drawRect(0,pos*(boardHeight/20), (boardWidth/20), (boardHeight/20));
			
		}
	}

	public int getPosition() {
		// TODO Auto-generated method stub
		return position;
	}

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public Status getStatus() {
		return status;
	}

	public boolean morgataged() {
		return morgataged;
	}
}
