//draw chessboard and chessman
import java.util.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JPanel;
public class DrawChessBoard extends JPanel implements MouseListener, MouseMotionListener{
	final static int BLACK=-1;
	final static int WHITE=1;
	protected AI ai;
	private int Gao;
	private int Kuan;
	private int edge1;
	private int edge2;
	private int stepback=0;
	protected int bushu = 0;
	protected int end=0;
	protected int mode=2;
	protected Qipan qipan=new Qipan();
	protected Qizi[] qizi=new Qizi[226];
	public Image boardImg;
	final private int ROWS = 15;
	final private int COLS = 15;
	protected Chessman[][] chessStatus=new Chessman[COLS][ROWS];
	//Constructor of a game		
	public DrawChessBoard() {
		boardImg = Toolkit.getDefaultToolkit().getImage(JPanel.class.getResource("/qipan.jpg"));
		if(boardImg == null)
			System.err.println("image do not exist");
		addMouseListener(this);
	}
	//Constructor to record a game
	public DrawChessBoard(Qizi[] qizi) {
		boardImg = Toolkit.getDefaultToolkit().getImage(JPanel.class.getResource("/qipan.jpg"));
		if(boardImg == null)
			System.err.println("image do not exist");
		addMouseListener(this);
		this.qizi=qizi;
		for(int i=1;i<qizi.length;i++) {
			if(qizi[i]==null) {
				this.bushu=i-1;
				break;
			}
			chessStatus[qizi[i].getHang()][qizi[i].getLie()]=new Chessman(qizi[i].getColor(),true);
			qipan.setQipan(qizi[i].getHang(), qizi[i].getLie(), qizi[i].getColor());
		}
		setLayout(null);
	}
	//withdraw
	public void withDraw(int k) {
		if(bushu>=k&&end!=mode) {
			for(int i=0;i<k;i++) {
			qipan.setQipan(qizi[bushu-i].getHang(),qizi[bushu-i].getLie(),0);
			chessStatus[qizi[bushu-i].getHang()][qizi[bushu-i].getLie()]=null;
			qizi[bushu-i]=null;
			}
			bushu-=k;
			end=0;
			boardImg = Toolkit.getDefaultToolkit().getImage(JPanel.class.getResource("/qipan.jpg"));
			repaint();
		}
	}
	//last step in record
	public void goBack() {
		if(bushu>stepback) {
		qipan.setQipan(qizi[bushu-stepback].getHang(),qizi[bushu-stepback].getLie(),0);
		chessStatus[qizi[bushu-stepback].getHang()][qizi[bushu-stepback].getLie()]=null;
		stepback++;
		repaint();
		}
	}
	//next step in record
	public void goForward() {
		if(stepback>0) {
		qipan.setQipan(qizi[bushu-stepback+1].getHang(),qizi[bushu-stepback+1].getLie(),qizi[bushu-stepback+1].getColor());
		chessStatus[qizi[bushu-stepback+1].getHang()][qizi[bushu-stepback+1].getLie()]=new Chessman(qizi[bushu-stepback+1].getColor(),true);
		stepback--;
		repaint();
		}
	}
	// initialize a function to play chess. Will be reloaded latter
	public void xiaqi(int hang,int lie) {} 
	//Judgment for win and loss
	public void judge(){
		this.end=qipan.pan();
		if(end==-1) 
		boardImg = Toolkit.getDefaultToolkit().getImage(JPanel.class.getResource("/Blackwin.jpg"));
		if(end==1)
		boardImg = Toolkit.getDefaultToolkit().getImage(JPanel.class.getResource("/Whitewin.jpg"));	
	}
	//Use AI
	public void ai(Qipan a,int color) {
		int trycb[][]=new int[15][15];
		for(int i=0; i<15; i++) {
			for(int j=0; j<15; j++) {
				trycb[i][j]=qipan.getQipan(i,j);
			}
		}
		ai=new AI(trycb,color);
		int[] result=new int[2];
		result=ai.getResult();
		this.chessStatus[result[0]][result[1]]=new Chessman(-color, true);
		qipan.setQipan(result[0], result[1],-color);
		qizi[bushu]=new Qizi(-color,result[0],result[1]);
	}
	//Return chessmen of the game to save
	public Qizi getQizi(int number) {
		return qizi[number];
	}
	//Get the number of steps
	public int getBushu() {
		return bushu;
	}
	//draw picture
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int imgWidth = boardImg.getWidth(this)-140;
		int imgHeight = boardImg.getHeight(this)-280;
		int FWidth = getWidth();
		int FHeight= getHeight();
		
		int x=(FWidth-imgWidth)/2;
		this.edge1=x;
		int y=(FHeight-imgHeight)/2;
		this.edge2=y;
		g.drawImage(boardImg, 0, 0, null);
		
		int span_x=imgWidth/(COLS-1);
		this.Kuan=span_x;
		int span_y=imgHeight/(ROWS-1);
		this.Gao=span_y;
		int margin_x = (imgWidth%COLS-1)/2+x;
		int margin_y = (imgHeight%ROWS-1)/2+y;
		for(int i=0;i<ROWS;i++)
		{
			g.drawLine(margin_x, margin_y+i*span_y, FWidth-margin_x, margin_y+i*span_y);
		}

		for(int i=0;i<COLS;i++)
		{
			g.drawLine(margin_x+i*span_x, margin_y, margin_x+i*span_x, FHeight-margin_y);
		}

		for(int i=0;i<COLS;i++)
		{
			for(int j=0;j<ROWS;j++)
			{
				if(chessStatus[i][j]!=null&&chessStatus[i][j].getPlaced()==true)
				{
					int pos_x=x+i*span_x;
					int pos_y=y+j*span_y;
					int chessman_width=20;
					float radius_b=20;
					float radius_w=50;
					float[] fractions = new float[]{0f,1f};
					Color[] colors_b = new Color[]{Color.BLUE,Color.BLACK};
					Color[] colors_w = new Color[]{Color.WHITE,Color.YELLOW};
					RadialGradientPaint paint;
					if(chessStatus[i][j].getColor()==1)
					{
						paint = new RadialGradientPaint(pos_x, pos_y, radius_w, fractions, colors_w);
						((Graphics2D)g).setPaint(paint);
						
						((Graphics2D)g).fillOval(pos_x-chessman_width/2,pos_y-chessman_width/2,chessman_width,chessman_width);
					}
					if(chessStatus[i][j].getColor()==-1){
						paint = new RadialGradientPaint(pos_x-chessman_width/2f, pos_y-chessman_width/2f, radius_b*2, fractions, colors_b);
					
					((Graphics2D)g).setPaint(paint);
					
					((Graphics2D)g).fillOval(pos_x-chessman_width/2,pos_y-chessman_width/2,chessman_width,chessman_width);
					}
				}
			}
		}
	}
	//Mose reaction
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		int x=e.getX();
		int y=e.getY();
		if(end==0) {
			int hang = 0;
			int lie = 0;
			int valid=0;;
			for(int i=0;i<15;i++) {
				for(int j=0; j<15; j++) {
					if((x-(i*Kuan+edge1))*(x-(i*Kuan+edge1))+(y-(j*Gao+edge2))*(y-(j*Gao+edge2))<(Kuan+Gao)*(Kuan+Gao)/25){
						hang=i;lie=j;
						valid++;
						break;
					}
				}
			}
			if(valid==1&&qipan.getQipan(hang,lie)==0){
				xiaqi(hang,lie);
			}	
		}	
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}
}