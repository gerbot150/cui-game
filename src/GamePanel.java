import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

// Chess = 2654 - 265F (KQRBNP W/B)
// Hourglass = 231B

// Boxes: http://unicode-table.com/en/#box-drawing

/* 
 */

public class GamePanel extends JPanel {
	int gameX = 100, gameY = 100, gameWidth = 400, gameHeight = 400;
	int fontSize = 20;
	char[][] squares;
	boolean[][] visible;
	int visionDistance = 10;
	boolean displayRoomNames = true;
	boolean acceptInputs = true;
	boolean mapFromFile = true;
	Player player;
	
	int second = 0;
	int hour = 7;
	int minute = 45;
	boolean am = true;
	int day = 5;
	String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	
	LinkedList<String> textlog;
	LinkedList<String> oldTextlog;
	JTextArea textDisplayArea;
	
	Font f1;
	FontMetrics fm1;
	Font f2;
	FontMetrics fm2;
	Font f3;
	FontMetrics fm3;
	int charWidth, charHeight;
	int numCharsX, numCharsY;

	public GamePanel(){
		super();
		this.setSize(CUIGame.dim);
		
		// Player stuff
		player = new Player(78, 33, (char)0x263A);
		
		// Textlog stuff
		textlog = new LinkedList<String>();
		oldTextlog = new LinkedList<String>();
		
		/*textDisplayArea = new JTextArea("asdfasdfasdfasdf");textDisplayArea.setFont(f2);textDisplayArea.setLineWrap(true);textDisplayArea.setLocation(gameX, gameY + gameHeight);textDisplayArea.setSize(gameWidth, gameHeight/4);textDisplayArea.setEditable(false);textDisplayArea.setVisible(true);this.add(textDisplayArea);textDisplayArea.setEnabled(false);*/

		// Font stuff
		f1 = new Font("Courier", 0, 12);
		fm1 = getFontMetrics(f1);
		f2 = new Font("Courier", 0, fontSize);
		fm2 = getFontMetrics(f2);
		f3 = new Font("Courier", 0, fontSize/3);
		fm3 = getFontMetrics(f3);
		
		// Character display stuff
		charWidth = fm2.charWidth('W');
		charHeight = (fm2.getAscent());
		numCharsX = gameWidth/charWidth;
		numCharsY = gameHeight/charHeight;
		
		// Map stuff
		try {
			constructMap();
		} catch (IOException e) {
			System.out.println("Map could not be found");
		}
		visible = new boolean[squares.length][squares[0].length];
		
		player.addToMap(squares);
		addNPCs();
		addDoors();
		updateVisible();
		//Item.spade.addToMap(squares, 49, 49);
		//Item.heart.addToMap(squares, 47, 49);
		//Item.fitzKey.addToMap(squares, 6, 33);
		Item.masterKey.addToMap(squares, 7, 38);
		
		sendToTextlog("You wake up in the early morning, face down on a book.");
		sendToTextlog("It seems you have fallen asleep studying in the office.");
		sendToTextlog("You can hear the thunder and lightning of a storm outside the school.");
	}
	
	public void updatePosition(int d){
		if(acceptInputs){
			// Save the current x and y position
			int saveX = player.x, saveY = player.y;
			
			// Replace the player and npcs with what they were standing on
			player.removeFromMap(squares);
			removeNPCs();
			//removeDoors();
			
			// Change the player's position
			switch(d){
				case 37: player.x--; break;
				case 38: player.y--; break;
				case 39: player.x++; break;
				case 40: player.y++; break;
			}
			
			// If there is a wall, revert the player's position
			if(squares[player.x][player.y] >= 0x2500 && squares[player.x][player.y] <= 0x254F){
				player.x = saveX;
				player.y = saveY;
			}
			
			// If there is a door, check to see if the player has the key and change position accordingly
			for(int i = 0; i < Door.doors.size(); i++){
				if(player.x == Door.doors.get(i).x && player.y == Door.doors.get(i).y){
					Door door = Door.doors.get(i);
					Item key = door.key;
					if(player.items.contains(key)){
						//player.items.remove(key);
						squares[player.x][player.y] = 0x002E;
						Door.doors.remove(Door.getDoor(player.x, player.y));
						
						for(int j = 1; j < 5; j++){
							if(squares[player.x+j][player.y] == door.sprite){
								squares[player.x+j][player.y] = 0x002E;
								Door.doors.remove(Door.getDoor(player.x+j, player.y));
							} else {
								break;
							}
						}
						for(int j = 1; j < 5; j++){
							if(squares[player.x-j][player.y] == door.sprite){
								squares[player.x-j][player.y] = 0x002E;
								Door.doors.remove(Door.getDoor(player.x-j, player.y));
							} else {
								break;
							}
						}
						for(int j = 1; j < 3; j++){
							if(squares[player.x][player.y+j] == door.sprite){
								squares[player.x][player.y+j] = 0x002E;
								Door.doors.remove(Door.getDoor(player.x, player.y+j));
							} else {
								break;
							}
						}
						for(int j = 1; j < 3; j++){
							if(squares[player.x][player.y-j] == door.sprite){
								squares[player.x][player.y-j] = 0x002E;
								Door.doors.remove(Door.getDoor(player.x, player.y-j));
							} else {
								break;
							}
						}
						sendToTextlog("You opened the door with " + key.name + ".");
					} else {
						player.x = saveX;
						player.y = saveY;
					}
				}
			}
			
			// If there is an npc, revert the player's position and interact with it
			for(int i = 0; i < NPC.npcs.size(); i++){
				if(player.x == NPC.npcs.get(i).x && player.y == NPC.npcs.get(i).y){
					player.x = saveX;
					player.y = saveY;
					sendToTextlog(NPC.npcs.get(i).getMessage(player));
				}
			}
			
			// Add the Doors back to the map
			//addDoors();
			
			// Add the NPCs back to the map
			addNPCs();
			
			// Add the player back to the map
			player.addToMap(squares);
			
			// Perform any actions based on the tile the player has stepped on
			if(player.x != saveX || player.y != saveY){
				onTileEnter();
				updateVisible();
				updateTime();
			}
			
			repaint();
		}
	}
	
	private void onTileEnter(){
		if(player.standingOn == 0x29C8){
			sendToTextlog("Congratulations you win!");
			acceptInputs = false;
		}
		if(player.standingOn == 0x259F){
			player.removeFromMap(squares);
			player.x += 100;
			player.addToMap(squares);
		}
		if(player.standingOn == 0x2599){
			player.removeFromMap(squares);
			player.x -= 100;
			player.addToMap(squares);
		}
		// this is messy needs some cleaning
		for(int i = 0; i < Item.items.size(); i++){
			if(player.standingOn == Item.items.get(i).sprite){
				player.standingOn = Item.items.get(i).standingOn;
				Item.items.get(i).removeFromMap(squares, player.x, player.y);
				addItem(Item.items.get(i));
				player.addToMap(squares);
				System.out.println(player.items);
			}
		}
	}
	
	private void updateVisible(){
		for(int i = 0; i < visible.length; i++){
			for(int j = 0; j < visible[0].length; j++){
				//visible[i][j] = false;
				//visible[i][j] = true;
			}
		}
		
		getVision(0, player.x, player.y);
		
		int wallVisionDistance = visionDistance + 2;
		for(int i = player.x - wallVisionDistance; i <= player.x + wallVisionDistance; i++){
			for(int j = player.y - wallVisionDistance; j <= player.y + wallVisionDistance; j++){
				if((int)Math.abs(i - player.x) + (int)Math.abs(j - player.y) <= wallVisionDistance){
					if(i >= 0 && i < squares.length && j >= 0 && j < squares[0].length){
						if(squares[i][j] >= 0x2500 && squares[i][j] <= 0x254F){
							visible[i][j] = true;
						}
					}
				}
			}
		}
	}
	
	private void getVision(int n, int x, int y){
		if(n <= visionDistance){
			char c = squares[x][y];
			visible[x][y] = true;
			if(!(c >= 0x2500 && c <= 0x257F)){
				getVision(n+1, x+1, y);
				getVision(n+1, x, y+1);
				getVision(n+1, x-1, y);
				getVision(n+1, x, y-1);
			}
		}
	}
	
	private void updateTime(){
		second += 20;
		if(second >= 60){
			second = 0;
			minute++;
			if(minute >= 60){
				minute = 0;
				hour++;
				if(hour == 12){
					am = !am;
					if(am){
						day = (day + 1) % 6;
					}
				}
				if(hour >= 13){
					hour = 1;
				}
			}
		}
		if(day == 0){
			sendToTextlog("You've run out of time to find the keys and get out. You lose.");
			acceptInputs = false;
		}
	}
	
	private void removeNPCs(){
		for(int i = 0; i < NPC.npcs.size(); i++){
			NPC.npcs.get(i).removeFromMap(squares);
		}
	}
	
	private void addNPCs(){
		for(int i = 0; i < NPC.npcs.size(); i++){
			NPC.npcs.get(i).addToMap(squares);
		}
	}
	
	private void removeDoors(){
		for(int i = 0; i < Door.doors.size(); i++){
			Door.doors.get(i).removeFromMap(squares);
		}
	}
	
	private void addDoors(){
		for(int i = 0; i < Door.doors.size(); i++){
			Door.doors.get(i).addToMap(squares);
		}
	}
	
	// picks up an item
	private void addItem(Item item){
		player.items.add(item);
		String s = getAOrAn(item.name);
		sendToTextlog("You have picked up " + s + " " + item.name + ".");
	}
	
	// determine whether to use a or an with the following string
	private String getAOrAn(String s){
		String output = "aeiou".indexOf(s.substring(0,1).toLowerCase()) > -1 ? "an" : "a";
		return output;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		
		drawBackground(g2D);
		drawTitle(g2D);
		drawGameBackground(g2D);
		drawChars(g2D);
		drawTextlog(g2D);
		drawItemList(g2D);
		drawTime(g2D);
	}
	
	private void drawBackground(Graphics2D g2D){
		g2D.setColor(Color.WHITE);
		g2D.fillRect(0,0,this.getWidth(),this.getHeight());
	}
	
	private void drawTitle(Graphics2D g2D){
		g2D.setColor(Color.BLACK);
		g2D.setFont(new Font("SchoolHouse Printed A", 0, 50));
		g2D.drawString("ADVENTURE", gameX, gameY - 25);
	}
	
	private void drawGameBackground(Graphics2D g2D){
		g2D.setColor(Color.BLACK);
		g2D.drawRect(gameX,gameY,gameWidth,gameHeight);
	}
	
	private void drawChars(Graphics2D g2D){
		Color posColor = new Color((int)((player.x/(double)squares.length)*255), 0, (int)((player.y/(double)squares[0].length)*255));
		g2D.setColor(posColor);
		for(int i = 0; i < numCharsX; i++){
			for(int j = 0; j < numCharsY; j++){
				int n = player.x-numCharsX/2+i;
				int m = player.y-numCharsY/2+j;
				if(n < squares.length && m < squares[0].length && n >= 0 && m >= 0){
					String s = "";
					int x = gameX + i*charWidth;
					int y = gameY + j*charHeight + fm2.getAscent();
					if(visible[n][m]){
						char c = squares[n][m];
						
						// adjust for specific characters
						if(c >= 0x2600 && c <= 0x26FF){
							//x -= charWidth/3;
						}
						
						// display room names or not and set colours accordingly
						if(Character.isLetterOrDigit(c)){
							if(displayRoomNames){
								g2D.setColor(new Color(45, 45, 45));
							} else {
								c = 0x002E;
							}
						} else {
							g2D.setColor(posColor);
						}
						
						NPC npc = NPC.getNPC(n, m);
						if(npc != null){
							g2D.setFont(f3);
							g2D.drawString(npc.name, (int) (x + charWidth/2 - fm3.stringWidth(npc.name)*3/8), y + fm3.getAscent());
						}
						
						s = "" + c;
					} else {
						s = "" + ' ';
					}
					g2D.setFont(f2);
					g2D.drawString(s, x, y);
				}
			}
		}
	}
	
	private void drawItemList(Graphics2D g2D){
		g2D.setColor(Color.BLACK);
		g2D.setFont(f1);
		g2D.drawString("Items:", 10, gameY + fm1.getAscent());
		for(int i = 0; i < player.items.size(); i++){
			g2D.drawString(player.items.get(i).name, 10, gameY + (i+1)*20 + fm1.getAscent());
		}
	}
	
	private void drawTextlog(Graphics2D g2D){
		g2D.setColor(Color.BLACK);
		g2D.setFont(f1);
		for(int i = 0; i < textlog.size(); i++){
			g2D.drawString(textlog.get(i), gameX/* + gameWidth + 25*/, gameY + gameHeight + i*charHeight + fm1.getAscent() + 10);
		}
	}
	
	private void drawTime(Graphics2D g2D){
		g2D.setColor(Color.BLACK);
		g2D.setFont(f1);
		g2D.drawString("Day: " + days[day], gameX + gameWidth + 5, gameY + fm1.getAscent());
		g2D.drawString("Time: " + hour + ":" + (minute < 10 ? "0" : "") + minute + " " + (am ? "AM" : "PM"), gameX + gameWidth + 5, gameY + fm1.getAscent() + 20);
	}
	
	private void sendToTextlog(String s){
		textlog.add(s);
		while(textlog.size() > 3){
			oldTextlog.add(textlog.remove());
		}
	}
	
	private void constructMap() throws IOException{
		if(mapFromFile){
			FileReader fr = new FileReader("map.txt");
			BufferedReader br = new BufferedReader(fr);
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while((line = br.readLine()) != null){
				lines.add(line);
				//System.out.println(line);
			}
			squares = new char[lines.get(0).length()][lines.size()];
			for(int i = 0; i < lines.get(0).length(); i++){
				for(int j = 0; j < lines.size(); j++){
					squares[i][j] = lines.get(j).charAt(i);
				}
			}
			br.close();
			fr.close();
			//addRoom(50,50,20,10);
			//addDoor(50,55);
			//addDoor(51,55);
		} else {
			makeBlankMap(100,100);
			addRoom(50,50,20,10);
			addDoor(50,55);
		}
	}
	
	private void makeBlankMap(int x, int y){
		squares = new char[x][y];
		for(int i = 0; i < squares.length; i++){
			for(int j = 0; j < squares[0].length; j++){
				squares[i][j] = 0x0020;
			}
		}
	}
	
	
	// Backup methods for in case the file doesn't load
	
	private void addRoom(int x, int y, int w, int h){
		for(int i = 1; i < w; i++){
			squares[x+i][y] = 0x2500;
			squares[x+i][y+h] = 0x2500;
		}
		for(int i = 1; i < h; i++){
			squares[x][y+i] = 0x2502;
			squares[x+w][y+i] = 0x2502;
		}
		squares[x][y] = 0x250C;
		squares[x][y+h] = 0x2514;
		squares[x+w][y] = 0x2510;
		squares[x+w][y+h] = 0x2518;
		for(int i = 1; i < w; i++){
			for(int j = 1; j < h; j++){
				squares[x+i][y+j] = 0x002E;
			}
		}
	}
	
	private void addDoor(int x, int y){
		squares[x][y] = 0x2341;
	}
}
