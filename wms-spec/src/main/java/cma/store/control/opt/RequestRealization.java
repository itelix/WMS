package cma.store.control.opt;

import java.util.List;

import cma.store.data.Route;

/**
Warehouse optimizer.
creating date: 2012-07-18
creating time: 21:49:11
autor: Czarek
 */

public class RequestRealization {
	List<Route> routes;

	public RequestRealization(List<Route> routes) {
		super();
		this.routes = routes;
	}

	public List<Route> getRoutes() {
		return routes;
	}
	

	
}
