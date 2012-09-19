package cma.store.data;

public abstract class RouteAbs implements Route {
	
	private static int ROUTE_NEXT_ID=0;
	
	
	public static synchronized int getNextRouteId(){
		ROUTE_NEXT_ID++;
		return ROUTE_NEXT_ID;
	}


}
