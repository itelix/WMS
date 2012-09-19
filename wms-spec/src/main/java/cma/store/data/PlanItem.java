package cma.store.data;


/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 22:10:30
autor: Czarek
 */

public class PlanItem {
	
	private int x;
	private int y;
	private Pos pos;
	//private boolean isRoad;
	PlanItemType type;
	double unitSize;

	public PlanItem( int x, int y, PlanItemType type, double unitSize ){
		this.x = x;
		this.y = y;
		this.pos = new Pos((x+0.5)*unitSize,(y+0.5)*unitSize);
		this.type = type;
	}

	
	public Pos getPos(){
		return pos;
	}

//	public void setPos(Pos pos){
//		this.pos = pos;
//	}
	
	public boolean isAisle(){
		return type==PlanItemType.AISLE;
	}
	
	public boolean isRoad(){
		return type.isRoute();
	}
	
	public boolean isMvcIn(){
		return type == PlanItemType.MVC_IN;
	}
	
	public boolean isMvcOut(){
		return type == PlanItemType.MVC_OUT;
	}
	
	public boolean isMvc(){
		return type == PlanItemType.MVC_IN || type == PlanItemType.MVC_OUT;
	}
	
	public boolean isWall(){
		return type == PlanItemType.WALL;
	}

	public boolean isNode(){
		return type == PlanItemType.NODE || isMvc() || type == PlanItemType.AISLE;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlanItem other = (PlanItem) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	
	
}
