import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
//Creat Chess board panel
public class Main extends JFrame{
	File file = new File("record.txt");
	JButton jbt1 = new JButton("go back");
    JButton jbt2 = new JButton("save");
    JButton jbt3 = new JButton("view record");
    JButton jbt4 = new JButton("menue");
    JButton jbt5 = new JButton("<<<");
    JButton jbt6 = new JButton(">>>");
	private DrawChessBoard drawChessBoard;
	//Method to record a game
	public Main(Qizi[] qizi) {
		drawChessBoard=new DrawChessBoard(qizi);
		drawChessBoard.add(jbt4);
		jbt4.setBounds(180,30,200, 30);
		drawChessBoard.add(jbt5);
		jbt5.setBounds(10,300,50,100);
		drawChessBoard.add(jbt6);
		jbt6.setBounds(500,300,50,100);
		setTitle("Gobang");	
        add(drawChessBoard);
        // go back to menue
        jbt4.addActionListener(
    	    	new ActionListener() {
    	    		public void actionPerformed(ActionEvent e) {
    	    			Start frame = new Start();
    	    			frame.setTitle("Gobang: Select mode");
    	    			frame.setSize(280,350);
    	    			frame.setLocation(280, 350);
    	    		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	    		    frame.setVisible(true);
    	    			dispose();
    	    		}
    	    	}
    	    	);
        //See last step of record
        jbt5.addActionListener(
    	    	new ActionListener() {
    	    		public void actionPerformed(ActionEvent e) {
    	    			drawChessBoard.goBack();
    	    		}
    	    	}
    	    	);
        //see next step of record
        jbt6.addActionListener(
    	    	new ActionListener() {
    	    		public void actionPerformed(ActionEvent e) {
    	    			drawChessBoard.goForward();
    	    		}
    	    	}
    	    	);
	}
	//Play game
	public Main(String SET) {
		if(SET=="Play Black") 
		drawChessBoard = new PlayBlack();
		if(SET=="Play White")
		drawChessBoard = new PlayWhite();
		if(SET=="Double Player")
		drawChessBoard = new DoublePlayer();
		drawChessBoard.add(jbt1);
		jbt1.setPreferredSize(new Dimension(200, 30));
		drawChessBoard.add(jbt2);
		jbt2.setPreferredSize(new Dimension(200, 30));
		drawChessBoard.add(jbt3);
		jbt3.setPreferredSize(new Dimension(200, 30));
		drawChessBoard.add(jbt4);
		jbt4.setPreferredSize(new Dimension(200, 30));
		//withdraw
		jbt1.addActionListener(
	    	new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			drawChessBoard.withDraw(2);
	    		}
	    	}
	    	);
		//save
		jbt2.addActionListener(
	    	new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			try {
	    	            file.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
	    	            try (PrintWriter writer = new PrintWriter(file);
	    	            ) {
	    	            	writer.println(drawChessBoard.mode);
	    	            	for(int i=1;i<=drawChessBoard.getBushu();i++) {
	    	            		Qizi qizi=drawChessBoard.getQizi(i);
	    	            		writer.println(qizi.getColor());
	    	            		writer.println(qizi.getHang());
	    	            		writer.println(qizi.getLie());
	    	            	}
	    	            }
	    	        } catch (IOException e1) {
	    	            e1.printStackTrace();
	    	        }
	    		}
	    	}
	    	);
		//read
		jbt3.addActionListener(
		    	new ActionListener() {
		    		public void actionPerformed(ActionEvent e) {
		    			int i=0;
		    			Qizi[] qizi=new Qizi[226];
		    			int[] a=new int[700];
		    	        try (Scanner input=new Scanner(file);
		    	        ){
		    	        	a[0]=input.nextInt();
		    	            while (input.hasNext()==true) {
		    	            	i++;
		    	            	a[i]=input.nextInt();
		    	            }
		    	            if(input.hasNext()==false)input.close();
		    	        } catch (IOException e1) {
		    	            e1.printStackTrace();
		    	        }
		    	        for(int j=1; j<=225; j++) {
		    	        	if(a[3*j-2]==0) break;
		    	        	qizi[j]=new Qizi(a[3*j-2],a[3*j-1],a[3*j]);
		    	        }
		    	        Main m=new Main(qizi);
		    	        m.setSize(560, 700);
		    			m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    			m.setVisible(true);
		    	        dispose();
		    		}
		    	}
		    	);
		//return to menue
		jbt4.addActionListener(
	    	new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			Start frame = new Start();
	    			frame.setTitle("Gobang: Select mode");
	    			frame.setSize(280,350);
	    			frame.setLocation(280, 350);
	    		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    		    frame.setVisible(true);
	    			dispose();
	    		}
	    	}
	    	);
		setTitle("Gobang");	
		add(drawChessBoard);
	}
	public static void main(String[] args) {
		//initialize a menue panel
		Start frame = new Start();
		frame.setTitle("Gobang: Select mode");
		frame.setSize(280,350);
		frame.setLocation(280, 350);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}
}