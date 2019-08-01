

public class DoublePlayer extends DrawChessBoard {
	public void xiaqi(int hang,int lie) {
		bushu++;
		chessStatus[hang][lie]=new Chessman(((1+bushu)%2)*2-1, true);
		qipan.setQipan(hang, lie, ((1+bushu)%2)*2-1);
		qizi[bushu]=new Qizi(((1+bushu)%2)*2-1,hang,lie);
		judge();
		repaint();
	} 
}

