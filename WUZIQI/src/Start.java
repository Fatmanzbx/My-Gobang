//Start the game
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class Start extends JFrame {
	public Start() {
		//set back ground
	    JButton jbt1 = new JButton("Play black");
	    jbt1.setBounds(70,160,140,30);
	    JButton jbt2 = new JButton("Play White");
	    jbt2.setBounds(70,215,140,30);
	    JButton jbt3 = new JButton("2 Player");
	    jbt3.setBounds(70,270,140,30);;
	    BackgroundPanel panel = new BackgroundPanel();
	    panel.add(jbt1);
	    panel.add(jbt2);
	    panel.add(jbt3);
	    add(panel); 
	    // set buttons
	    jbt1.addActionListener(
	    		new ActionListener() {
	    			public void actionPerformed(ActionEvent e) {
	    	    		initialize("Play Black");
	    			}
	    		}
	    		);
	    jbt2.addActionListener(
	    		new ActionListener() {
	    			public void actionPerformed(ActionEvent e) {
	    	    		initialize("Play White");
	    			}
	    		}
	    		);
	    jbt3.addActionListener(
	    	    new ActionListener() {
	    	    	public void actionPerformed(ActionEvent e) {
	    	    		initialize("Double Player");
	    	   		}
	        	}
	        	);
	}
	public void initialize(String s) {
		//Start game
		Main m = new Main(s);
		m.setSize(560, 700);
		m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m.setVisible(true);
		dispose();
	}
}	
class BackgroundPanel extends JPanel{
	public Image Img = Toolkit.getDefaultToolkit().getImage(JPanel.class.getResource("/qipanstart.jpg"));
	public BackgroundPanel(){
		this.setOpaque(true);
		this.setLayout(null);
	}
	//draw back ground
	public void paintComponent(Graphics g){
		super.paintComponents(g);
		g.drawImage(Img,0,0,this.getWidth(),this.getHeight(),this);
		
	}
}

	


