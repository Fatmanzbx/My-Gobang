import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class Start extends JFrame {
	public Start() {
		//创造按钮和背景
	    JButton jbt1 = new JButton("棋手执黑");
	    jbt1.setBounds(70,160,140,30);
	    JButton jbt2 = new JButton("棋手执白");
	    jbt2.setBounds(70,215,140,30);
	    JButton jbt3 = new JButton("双人大战");
	    jbt3.setBounds(70,270,140,30);;
	    BackgroundPanel panel = new BackgroundPanel();
	    panel.add(jbt1);
	    panel.add(jbt2);
	    panel.add(jbt3);
	    add(panel); 
	    // 设定按钮
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
		//开始游戏
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
	//画背景
	public void paintComponent(Graphics g){
		super.paintComponents(g);
		g.drawImage(Img,0,0,this.getWidth(),this.getHeight(),this);
		
	}
}

	


