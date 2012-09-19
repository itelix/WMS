package cma.store.data;

import java.util.List;

import cma.store.control.opt.data.Request;
import cma.store.utils.Conflict;
import cms.store.utils.Pair;


/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 23:17:36
autor: Czarek
 */

public interface Route {

	 Bot getBot();

	 double getTotalDistance();

	 double getTotalTime();
	
	 boolean exists( long time );
	
	 void update( long time );
	
	 Pos getPos( long time, boolean nullWhenAfterRoute );
	
	 long getStartTime();
	
	 long getFinalTime();

	 boolean isFinished();

	 boolean isActive();

	 int getRouteGroupId();
	
	 void setRouteGroupId(int routeGroupId);

	 Pos getFinalPos();
	
	 Request getRequest();
	 
	 boolean isMvcRoute();

	 List<? extends Pos> getPos();

	 void setPos(List<? extends Pos> pos);
	
	 List<Pair<Speed, Duration>> getSpeedDurList();

	 Pos getInitPosition();

	 RouteType getType();
	 
	 void setType(RouteType type);

	 Double getPriority();
	
	 List<Conflict> getBlockers();

	 String printRouteWithTime();
	 
	 Mvc getMvc();

}
