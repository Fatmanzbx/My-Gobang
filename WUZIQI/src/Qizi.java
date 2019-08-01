//辅助复盘以及悔棋功能
public class Qizi {
	int color;
	int hang;
	int lie;
	public Qizi(int color,int hang,int lie) {
		this.color=color;
		this.hang=hang;
		this.lie=lie;
	}
	public int getHang() {
		return hang;
	}
	public int getLie() {
		return lie;
	}
	public int getColor() {
		return color;
	}
}
