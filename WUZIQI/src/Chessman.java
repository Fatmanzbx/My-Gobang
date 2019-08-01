//辅助画棋子的功能
public class Chessman {
	private int color;//1-white  -1-black
	private boolean placed = false;
	
	public Chessman(int color,boolean placed){
		this.color=color;
		this.placed=placed;
	}
 
	public boolean getPlaced() {
		return placed;
	}
 
	public void setPlaced(boolean placed) {
		this.placed = placed;
	}
 
	public int getColor() {
		return color;
	}
 
	public void setColor(int color) {
		this.color = color;
	}	
}
