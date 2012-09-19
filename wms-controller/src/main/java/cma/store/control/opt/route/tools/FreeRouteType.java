package cma.store.control.opt.route.tools;

public enum FreeRouteType {
	FREE(true),
	COLISON(false);
	
	private boolean free;
	
	private FreeRouteType( boolean free ){
		this.free = free;
	}
	
	public boolean isFree(){
		return free;
	}
	
	public boolean isColision(){
		return !free;
	}

}
