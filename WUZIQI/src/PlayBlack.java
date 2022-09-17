//Player play balck
public class PlayBlack extends DrawChessBoard {
	public PlayBlack() {
		mode=-1;
	}
	public void xiaqi(int hang,int lie) {
		bushu++;
		this.chessStatus[hang][lie]=new Chessman(BLACK, true);
		qipan.setQipan(hang, lie,BLACK);
		qizi[bushu]=new Qizi(BLACK,hang,lie);
		judge();
		if(end==0) {
			if(bushu>1) {
				bushu++;
				ai(qipan,BLACK);
				judge();
				
			}else {
				int hang1=8;
				int lie1=8;
				int[] x={1,1,-1,-1,0,0,1,-1};
				int[] y={1,-1,-1,1,1,-1,0,0};
				for(int i=0;i<8;i++) {
					if(Math.abs(hang+x[i]-7)<=Math.abs(hang-7)&&Math.abs(lie+y[i]-7)<=Math.abs(lie-7)) {
						hang1=hang+x[i];
						lie1=lie+y[i];
						break;
					}	
				}
				bushu++;
				this.chessStatus[hang1][lie1]=new Chessman(WHITE, true);
				qipan.setQipan(hang1, lie1,WHITE);
				qizi[bushu]=new Qizi(WHITE,hang1,lie1);
				judge();
			}
		}
		repaint();
	} 
}