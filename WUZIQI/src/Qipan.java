
//A class for chessboard
public class Qipan {
	private int[][] cb = new int[15][15];//record game
	public int getQipan(int i, int j) {
		return cb[i][j];
	}
	public void setQipan(int i, int j, int color) {
		this.cb[i][j]=color;
	}
	//Judege for result
	public int pan (){
		int caipan =0;
		for(int i=0;i<15;i++) {
			for(int j=0;j<15;j++) {
				int sum1 = 0;
				try {
					for(int k=-2;k<=2;k++){
						sum1+=cb[i+k][j+k];
					}
				}catch(ArrayIndexOutOfBoundsException e){}
				int sum2 = 0;
				try {
					for(int k=-2;k<=2;k++){
						sum2+=cb[i-k][j+k];
					}
				}catch(ArrayIndexOutOfBoundsException e){}
				int sum3 = 0;
				int edge3=Math.max(-j,-2);
				for(int k=edge3;k<=2&&j+k<15;k++){
					sum3+=cb[i][j+k];
				}
				int sum4 = 0;
				int edge4=Math.max(-i,-2);
				for(int k=edge4;k<=2&&i+k<15;k++){
					sum4+=cb[i+k][j];
				}
				if(sum1==-5||sum2==-5||sum3==-5||sum4==-5){
					caipan--;
					i+=20;
					j+=20;
				}
				if(sum1==5||sum2==5||sum3==5||sum4==5){
					caipan++;
					i+=20;
					j+=20;
				}
			}
		}
		return caipan;
	}
}

