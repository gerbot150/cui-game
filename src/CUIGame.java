import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

public class CUIGame extends JFrame implements KeyListener, MouseListener{
	static Dimension dim = new Dimension(600,600);
	private GamePanel gPanel;
	
	public CUIGame(){
		super("Adventure");
		gPanel = new GamePanel();
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.add(gPanel);
		this.setLocation(340, 100);
		this.setVisible(true);
	}

	public static void main(String[] args){
		JFrame game = new CUIGame();
		game.setDefaultCloseOperation(EXIT_ON_CLOSE);
		game.setSize(dim);
	}
	
	public void mouseClicked(MouseEvent arg0) {
		gPanel.acceptInputs = true;
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void keyPressed(KeyEvent e) {
		int ek = e.getKeyCode();
		if(ek == KeyEvent.VK_LEFT || ek == KeyEvent.VK_RIGHT || ek == KeyEvent.VK_UP || ek == KeyEvent.VK_DOWN){
			gPanel.updatePosition(ek);
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
