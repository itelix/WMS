package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.control.BaseRealization;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.schedule.ScheduleItem;

public class RealizerVer4 extends RealizerVer3 {
	
	private static final long NEXT_ROUTE_TIME_DELTA = 200;
	private static final long MAX_ITER = 50;


	public RealizerVer4(Environment env) {
		super(env);
	}
	
	protected boolean isNewBetter( Route oldR, Route newR ){
		 if( oldR==null ) return true;
		 
		 if( oldR.getFinalTime() > newR.getFinalTime() + 10 ) return true;
		 
		 if( oldR.getFinalTime() < newR.getFinalTime() - 10 ) return false;
		 
		 //almoust equals
		 
		 //TODO should be improved (if similar distance - check number of blocker, total time ...
		 return oldR.getTotalDistance() > newR.getTotalDistance();
	}
	
	@Override
	protected final Route findRoutes2( BaseRealization br, long startTime )  throws RealizationException{
		
		long delta = NEXT_ROUTE_TIME_DELTA;
		
		Route route = null;
		Route best = null;
		List<ScheduleItem> bestRouteItems=null;
		long st = startTime;
		
		Route shortest = shortestRouteSecond( br, Environment.TIME_TO_STAY_IN_MVC );
		long bestStartTime = 0;
		
		int countBefore = getLocSchedule().getItems().size();
		
		for( int i=0; i<MAX_ITER; i++ ){
			
			List<ScheduleItem> toRemove = getLocSchedule().getItems().subList(countBefore, getLocSchedule().getItems().size());
			for( ScheduleItem si: toRemove ){
				removeRoute(si, br, si.getRoute().getType() == RouteType.ROUTE_2 );
			}
			
			if( i>0 ){
				if( route.getStartTime()>st ){
					st = route.getStartTime();
				}
				
				st += delta;
			}
			
			route = super.findRoutes2( br, st );		
			
			if( route==null ){
				break;
			}
			
			if( isNewBetter(best, route) ){
				best = route;
				bestRouteItems = getLocSchedule().getItems().subList(countBefore, getLocSchedule().getItems().size());
				bestStartTime = st;
			}
			
			if( shortest!=null ){
				if( best.getTotalTime() <= shortest.getTotalTime() + 10 ){ //close to optimal (10mls difference only)
					break;
				}
			}
		}
		
		List<ScheduleItem> toRemove = getLocSchedule().getItems().subList(countBefore, getLocSchedule().getItems().size());
		for( ScheduleItem si: toRemove ){
			removeRoute(si, br, si.getRoute().getType() == RouteType.ROUTE_2 );
		}

		int countAfter = getLocSchedule().getItems().size();
		
		if( countBefore!=countAfter ){
			throw new RuntimeException("Wrong number of elements after testing some variants of possible solutions");
		}
		
		if( best!=null ){
			
			//List<ScheduleItem> tmp = new ArrayList<ScheduleItem>( bestRouteItems );
			
			for( ScheduleItem si: bestRouteItems){
				try{
					addRoute(si.getRoute(), br, si.getRoute().getType() == RouteType.ROUTE_2 );
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		return best;
	}


}
