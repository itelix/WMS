package cma.store.schedule;

import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.Route;
import cma.store.input.request.BaseRequest;

/**
Warehouse optimizer.
creating date: 2012-05-17
creating time: 19:30:08
autor: Czarek
 */


/**
 * Schedule for one request for a car
 */
public class ScheduleItem {
	
	private Route route;

	public ScheduleItem( Route route ){
		this.route=route;
	}

	public Bot getCar() {
		return route.getBot();
	}

	public Route getRoute() {
		return route;
	}
	
	public Mvc getMvc(){
		return route.getMvc();
	}
	
	public BaseRequest getBaseRequest(){
		if( route.getRequest()==null ) return null;
		return route.getRequest().getBaseRequest();
	}
	
	public String toString(){
		return ""+route;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((route == null) ? 0 : route.hashCode());
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
		ScheduleItem other = (ScheduleItem) obj;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		return true;
	}
	
	
	

}
