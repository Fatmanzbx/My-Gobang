//Player play white
public class PlayWhite extends DrawChessBoard {
	public PlayWhite() {
		mode=1;
		chessStatus[7][7]=new Chessman(BLACK, true);
		qipan.setQipan(7, 7, BLACK);
		bushu++;
		qizi[bushu]=new Qizi(BLACK ,7,7);
	}
	public void xiaqi(int hang,int lie) {
		bushu++;
		chessStatus[hang][lie]=new Chessman(WHITE, true);
		qipan.setQipan(hang, lie, WHITE);
		qizi[bushu]=new Qizi(WHITE,hang,lie);
		judge();
		repaint();
		if(end==0) {
			bushu++;
			ai(qipan,WHITE);
			judge();
			repaint();
		}
	} 
}
