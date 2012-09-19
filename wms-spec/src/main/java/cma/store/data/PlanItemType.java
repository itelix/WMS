package cma.store.data;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 19:34:02
autor: Czarek
 */

public enum PlanItemType {
	WALL(false),
	NODE(true),
	ROAD(true),
	AISLE(true),
	MVC_IN(true),
	MVC_OUT(true);
	
	private boolean route;
	
	private PlanItemType( boolean route ){
		this.route = route;
	}

	public boolean isRoute() {
		return route;
	}
	
	

}
