package cma.store.control.opt.route.tools;

import java.util.ArrayList;
import java.util.List;

import cma.store.control.opt.data.Request;
import cma.store.data.Bot;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.data.PosTime;
import cma.store.env.Environment;
import cma.store.input.request.LocPriority;
import cma.store.schedule.Schedule;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;

public class ConfigurableFreeRouteController implements FreeRouteController {
	Environment env;
	Request request;
	double accuracy;
	boolean careStableBots=false;
	boolean careBots=false;
	Schedule tmpSchedule;
	LocPriority destination = null;
	
	
	
	public ConfigurableFreeRouteController( Environment env, Request request, 
											boolean careStableBots, boolean careBots,
											Schedule tmpSchedule) {
		this.env = env;
		this.request = request;
		this.careStableBots = careStableBots;
		this.careBots = careBots;
		this.tmpSchedule = tmpSchedule;
		accuracy = env.getAccuracy();
	}	
	
	public Schedule getTmpSchedule() {
		return tmpSchedule;
	}

	public ConfigurableFreeRouteController(Environment env, Request request, 
			boolean careStableBots, boolean careBots) {
		this(env,request,careStableBots, careBots, null);
	}
	
	
	@Override
	public boolean isRoute( Bot bot, Pos pos, Long time){
		PlanItem pi = env.getLayerModel().getPlanUnit(pos);
		return  pi!=null && pi.isRoad();
	}

	@Override
	public List<Conflict> isRouteFree(Bot bot, Pos pos, Long time, boolean careStable ) {

		if( !careBots ){
			return null;
		}
		
		return ConflictFinder.getConflictsAtTime( bot, pos, time, tmpSchedule, env, careStable );
	}
	
	

//	@Override
//	public Conflict isRoutFree(Bot bot, PosTime posTime) {
//		return isRoutFree(bot,posTime,posTime.getTime());
//	}
	
	private boolean isDestination( Bot bot, Pos pos, Long time, List<LocPriority> dest ){
		for(LocPriority d: dest){
			if( isDestination( bot, pos, time, d.getPos()) ){
				destination = d;
				return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public Double getPriority(){
		if( destination!=null ){
			return destination.getPriority();
		}else{
			return null;
		}
	}
	
	private boolean isDestination( Bot bot, Pos pos, Long time, Pos dest ){
		
		if( Math.abs(pos.x-dest.x)>accuracy ){
			return false;
		}
		if( Math.abs(pos.y-dest.y)>accuracy ){
			return false;
		}
		
		for(long t=time; t<time+request.getStayAtFinal(); t+=env.DEFAULT_TIME_ACCURACY ){
			if( isRouteFree(bot, pos, t, false)!=null ){
				return false;
			}
		}
		
		if( isRouteFree(bot, pos, time+request.getStayAtFinal(), false)!=null ){
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isDestination(Bot bot, Pos pos, Long time) {
		
		switch(request.getType()){
			case KNOWN_END_AND_BEFORE_END_TIME:
			case KNOWN_END_AND_AFTER_END_TIME:
			case KNOWN_END_AND_MIN_START_TIME:
//				if(request.getTo() == null && request.getBaseRequest() != null && request.getBaseRequest().bestLocPriority != null ) {
//					return isDestination( bot, pos, time, request.getBaseRequest().bestLocPriority.getPos() );
//				}else 
				if( request.getProdLocPtiority()!=null ){
					return isDestination( bot, pos, time, request.getProdLocPtiority() );
				}else{
					return isDestination( bot, pos, time, request.getTo() );
				}
	
			case KNOWN_START_AND_TIME:
//				if( !env.getLayerModel().getPlanUnit(pos).isAisle() ){ //this has to modified
//					return false;
//				}
				return !isBlockingPlace(bot, pos, time);
				
			case KNOWN_END_AND_AFTER_END_TIME_BACKWARDS:
				if( request.getProdLocPtiority()!=null ){
					return isDestination( bot, pos, time, request.getProdLocPtiority() );
				}else{
					return isDestination( bot, pos, time, request.getFrom() );
				}
			default:
				throw new RuntimeException("Type:" + request.getType() + " is not implemented yet");
			}
		
	}
	
	private boolean isBlockingPlace( Bot bot, Pos pos, long time){
		
		if( tmpSchedule==null ){
			tmpSchedule = env.getSchedule();
		}
		
		return ConflictFinder.getFirstConflictAfterTime(bot, pos,time, tmpSchedule, env)!=null;
		
	}


//	@Override
//	public boolean isDestination(Bot bot, PosTime posTime) {
//		return isDestination(bot,posTime,posTime.getTime());
//	}



	public double getDestinationPriority() {
		if( destination==null ) return 0;
		return destination.getPriority();
	}

	
	

}
