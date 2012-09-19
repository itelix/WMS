package cma.store.control.opt.route;

import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.tools.FreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Route;

/**
Warehouse optimizer.
creating date: 2012-07-15
creating time: 18:18:51
autor: Czarek
 */

public interface RouteCreator {
	Route createRout( Request r, Bot car, FreeRouteController freeRouteController, boolean forward );

}
