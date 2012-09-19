package cma.store.control;

import cma.store.control.opt.route.RouteCreator;
import cma.store.data.Route;
import cma.store.schedule.ScheduleItem;

/**
Warehouse optimizer.
creating date: 2012-05-17
creating time: 19:42:50
autor: Czarek
 */

public class ScheduleItemCreator {
	RouteCreator rc;

	public ScheduleItemCreator( RouteCreator rc ){ 
		this.rc = rc;
	}
	
	public ScheduleItem createItem( Route rout ){

		ScheduleItem item = new ScheduleItem(rout);
		
		return item;
	}

}
