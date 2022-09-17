//Input data, which is a 2 dimensional array to get reaction of AI
public class AI{
	private int mode;
	private int countscore;
	private int score[][]=new int[8][20];
	private int trycb[][];
	private int[] result=new int[2];
	private static int[] x={0,1,1,1,0,-1,-1,-1};
	private static int[] y={1,1,0,-1,-1,-1,0,1};
	public AI(int[][] trycb, int mode) {
		this.trycb=trycb;
		this.mode=mode;
		for(int i=0; i<8;i++) {
			for(int j=0;j<20;j++) {
				score[i][j]=-150000;
			}
		}
	}
	//determine whether there are other chess piece around one certain chess piece
	public boolean neighbor(int distance, int hang, int lie) {
		boolean neighbor=false;
		int edge1=Math.min(hang, distance);
		int edge2=Math.min(lie, distance);
		int edge3=Math.max(hang, 14-distance);
		int edge4=Math.max(lie, 14-distance);
		for(int i=-edge1;i<=14-edge3;i++) {
			for(int j=-edge2;j<=14-edge4;j++) {
				if(trycb[hang+i][lie+j]!=0) {
					neighbor=true;
					break;
				}		
			}
		}
		return neighbor;
	}
	//Calculate 2,  3 ,  and 4 consecutive pieces
	public int[] H432(int color) {
		int score=0;
		int block=0;//to record whether need to block immediately
		for(int hang=0;hang<=14;hang++) {
			for(int lie=0;lie<=14;lie++) {
				if(trycb[hang][lie]==0) {
					for(int k=0;k<8;k++) {
						int space=0;//to record how many spaces in the line;more than two is meaning less;
						int i=0;//to record the length of the line
						while(i<5&&(hang+i*x[k])<15&&lie+i*y[k]<15&&hang+i*x[k]>=0&&lie+i*y[k]>=0){
							i++;
							try {
								if(trycb[hang+i*x[k]][lie+i*y[k]]==0)space++;
								if(space==2)break;
								if(trycb[hang+i*x[k]][lie+i*y[k]]==-color) {
									i--;
									break;
								}
							}catch(ArrayIndexOutOfBoundsException e) {
								i--;
								break;
							}
						}
						int state=10*space+i;
						switch(state){
							case 15: 
								score+=(1000*color);
								if(trycb[hang+5*x[k]][lie+5*y[k]]==0)score+=(45000*color);
								break;
							case 25:
								if(trycb[hang+1*x[k]][lie+1*y[k]]!=0) {
									score+=(1500*color);
									block++;
								}
								break;
							case 4:
								score+=(2500*color);
								break;
							case 14: 
								score+=(500*color);
								if(trycb[hang+4*x[k]][lie+4*y[k]]==0){
									score+=(1000*color);
									block++;
								}
								break;
							case 24: 
								if(trycb[hang+1*x[k]][lie+1*y[k]]!=0) {
									score+=(200*color);
								}
								break;
							case 3: score+=(200*color);break;
							case 13: 
								score+=(40*color);
								if(trycb[hang+3*x[k]][lie+3*y[k]]==0) {
									score+=200*color;
								}
								break;
						}
					}
				}
			}
		}
		if (block>4)score+=10000*color;
		int[] a={block,score};
		return a;
	}
	//Count 4 and 5 consecutive pieces
	public int[] H5(int color) {
		int block=0;
		int addscore=0;
		for(int i=0;i<15;i++) {
			for(int j=0;j<15;j++) {
				int sum1 = 0;
				boolean s1=true;
				for(int k=-2;k<=2;k++){
					try {
					sum1+=trycb[i+k][j+k];
					}catch(ArrayIndexOutOfBoundsException e){
						s1=false;
					}
				}
				boolean s2=true;
				int sum2 = 0;
				for(int k=-2;k<=2;k++){
					try {
					sum2+=trycb[i-k][j+k];
					}catch(ArrayIndexOutOfBoundsException e){
						s2=false;
					}
				}
				boolean s3=true;
				int sum3 = 0;
				for(int k=-2;k<=2;k++){
					try {
					sum3+=trycb[i][j+k];
					}catch(ArrayIndexOutOfBoundsException e) {
						s3=false;
					}
				}
				boolean s4=true;
				int sum4 = 0;
				for(int k=-2;k<=2;k++){
					try {
					sum4+=trycb[i+k][j];
					}catch(ArrayIndexOutOfBoundsException e) {
						s4=false;
					}
				}
				if(sum1==5*color||sum2==5*color||sum3==5*color||sum4==5*color){
					addscore+=180000*color;
					i+=20;
					j+=20;
				}
				if((sum1==4*color&&s1==true)||(sum2==4*color&&s2==true)||(sum3==4*color&&s3==true)||(sum4==4*color&&s4==true)){
					addscore+=2500*color;
					block+=100;
				}

			}	
		}
		int a[]= {block,addscore};
		return a;
	}
	//Use the score above to make decision. Use induction. 
	public void countScore(int color,int deep,int num) {
		for(int hang=0;hang<=14;hang++) {
			for(int lie=0;lie<=14;lie++) {
				if(neighbor(2,hang,lie)==true&&trycb[hang][lie]==0) {
					trycb[hang][lie]=color;
					countscore=0;
					int[] a1=H5(-color);
					int[] a2=H5(color);
					int[] a3=H432(-color);
					int[] a4=H432(color);
					if((a2[1]/color>=120000)||(a2[1]+a4[1])/color>=40000&&a1[0]<100) {
						if(deep==0) {
						result[0]=hang;
						result[1]=lie;
						}else {
							score[deep-1][num]=(-140000);
						}
						return;
					}
					trycb[hang][lie]=0;
				}
			}
		}
		if(deep==1) {
			score[deep][0]=-150000;
			for(int hang=0;hang<=14;hang++) {
				for(int lie=0;lie<=14;lie++) {
					if(neighbor(2,hang,lie)==true&&trycb[hang][lie]==0) {
						trycb[hang][lie]=color;
						countscore=0;
						int[] a1=H5(-color);
						int[] a2=H5(color);
						int[] a3=H432(-color);
						int[] a4=H432(color);
						countscore+=(a1[1]+a2[1]+1.1*a3[1]+a4[1]);
						if ((countscore/color)>score[deep][0]&&(a3[0]+a1[0]==0||(a1[0]<100&&a2[0]+a4[0]>100))) {
							score[deep][0]=countscore/color;
						}
						trycb[hang][lie]=0;
					}
				}
			}
			score[deep-1][num]=-score[deep][0];
		}
		if(deep<1) {
			int range=12-2*deep;
			int[] tryhang=new int[range];
			int[] trylie=new int[range];
			int[] tryscore=new int[30];
			for(int j=0; j<20;j++) {
				tryscore[j]=-150000;
			}
			for(int hang=0;hang<=14;hang++) {
				for(int lie=0;lie<=14;lie++) {
					if(neighbor(2,hang,lie)==true&&trycb[hang][lie]==0) {
						trycb[hang][lie]=color;
						countscore=0;
						int[] a1=H5(-color);
						int[] a2=H5(color);
						int[] a3=H432(-color);
						int[] a4=H432(color);
						countscore+=(a1[1]+a2[1]+1.1*a3[1]+a4[1]);
						for(int j=0;j<range;j++) {
							if ((countscore/color)>tryscore[j]&&(a3[0]+a1[0]==0||(a1[0]<100&&a2[0]>=100))) {
								if(j==0) {
									result[0]=hang;
									result[1]=lie;
								}
								for(int t=range-1;t>j;t--) {
									tryhang[t]=tryhang[t-1];
									trylie[t]=trylie[t-1];
									tryscore[t]=tryscore[t-1];
								}
								tryhang[j]=hang;
								trylie[j]=lie;
								tryscore[j]=countscore/color;
								break;
							}
						}
						trycb[hang][lie]=0;
					}
				}
			}
			for(int j=0;j<range&&tryhang[j]+trylie[j]>0;j++) {
				trycb[tryhang[j]][trylie[j]]=color;
				countScore(-color,deep+1,j);
				trycb[tryhang[j]][trylie[j]]=0;
			}
			int max=0;
			for(int j=1;j<range;j++) {
				if(score[deep][j]>score[deep][max])max=j;
			}
			if(deep==0) {
				result[0]=tryhang[max];
				result[1]=trylie[max];
			}else {
				score[deep-1][num]=-score[deep][max];
			}
			System.out.println(score[deep][max]);
		}
	}
	//return result
	public int[] getResult(){
		countScore(-mode,0,0);
		return result;
	}
}
