package cma.store.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.control.opt.data.Request;
import cma.store.utils.Conflict;
import cms.store.utils.Pair;
import cms.store.utils.PositionUtils;


/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 23:17:36
autor: Czarek
 */

public class RouteImp implements Route{
	private static final String NL = System.getProperty("line.separator");

	
	private long startTime;
	private long endTime;
	private List<? extends Pos> pos;
	private Bot bot;
	private boolean isFinished = false;
	private boolean isActive = false;
	private int currentId=0;
	private int routeGroupId;
	private List<Pair<Speed,Duration>> speedDurList;
	private Pos initPosition;
	private Request request;
	private RouteType type;
	
	private Pos finalPosition;
	private Double priority;
	
	private List<Conflict> blockers;
	private List<Bot> blockerBots;
 	

//	public Route( Bot car, long startTime, List<? extends Pos> pos, int x){
//		this.car = car;
//		this.startTime = startTime;
//		this.maxTime = startTime+pos.size()-1;
//		//this.maxTime = Integer.MAX_VALUE; //test only
//		this.pos = pos;
//	}

	public RouteImp( Bot bot, List<Pair<Speed,Duration>> speedDurList, List<Pos> pp, Pos initPosition, long startTime, Request request){
		this.bot = bot;
		this.speedDurList = speedDurList;
		this.initPosition = initPosition;
		this.request = request;
		this.startTime = startTime;
		this.pos = pp;
		
		if( speedDurList.size() > 0 ){
			startTime = speedDurList.get(0).getT2().getStartTime();
			endTime = speedDurList.get(speedDurList.size()-1).getT2().getEndTime();
			finalPosition = getPos(endTime,false);
		}else{
			endTime = startTime;
			finalPosition = initPosition;
		}
		
	}

	public Bot getBot() {
		return bot;
	}
	
	public double getTotalDistance(){
		double dist = 0;
		for (Pair<Speed,Duration> sd : speedDurList) {
			Speed s = sd.getT1();
			Duration d = sd.getT2();
			dist += (Math.abs(s.getX()) + Math.abs(s.getY()))*(d.getDuration());
		}
		return dist;
	}
	
	public double getTotalTime(){
		return getFinalTime()-getStartTime();
	}
	
	public boolean exists( long time ){
		return time >= startTime && time<=endTime;
	}
	
	public void update( long time ){
		Logger log = Logger.getLogger(getClass());
		if( time>=endTime && isFinished ){
			return;
		}

		if( time < startTime ){
			return;
		}
		
		isActive = true;
		
		Pos x = new Pos(initPosition);
		for(int i=0; i<speedDurList.size(); i++){
			Pair<Speed,Duration> p = speedDurList.get(i);
			Duration d = p.getT2();
			boolean inside = d.getEndTime()>=time;
			final long dur = Math.min( d.getDuration(), time-d.getStartTime() );

			x.x += dur*p.getT1().getX();
			x.y += dur*p.getT1().getY();
			if( inside ) break;
		}
		
		Pos currPos = bot.getPos();
		if (PositionUtils.getIntX(currPos.dist(x)) > 10) // TODO: calibrate constant, remove exact value
			log.error("Error: Bot jumped from prevPos: " + currPos + " to nextPos: " + x);
		bot.setPos( x );
		
		if( time>=endTime ){
			isFinished = true;
			isActive = false;
		}
		
		log.debug(
			"Bot(" + bot.getId() + ") at time " + time + " is at position (" +
			PositionUtils.getIntX(bot.getX()) + ", " + PositionUtils.getIntY(bot.getY()) + ")"
			+" ("+bot.getX() + ", " + bot.getY()+ ")"

		);
	}
	
	public Pos getPos( long time, boolean nullWhenAfterRoute ){
//		if( !exists(time) ){
//			return null;
//		}
		
		if( nullWhenAfterRoute ){
			if( time>endTime ){
				return null;
			}
		}
		
		if( time < startTime ){
			return null;
		}
		
		Pos x = new Pos(initPosition);
		
		for(int i=0; i<speedDurList.size(); i++){
			Pair<Speed,Duration> p = speedDurList.get(i);
			Duration d = p.getT2();
			boolean inside = d.getEndTime()>=time;
			final long dur = Math.min( d.getDuration(), time-d.getStartTime() );

			x.x += dur*p.getT1().getX();
			x.y += dur*p.getT1().getY();
			if( inside ) break;
		}
		return x;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getFinalTime() {
		return endTime;
	}
	

	public boolean isFinished() {
		return isFinished;
	}

	public boolean isActive() {
		return isActive;
	}

	public int getRouteGroupId() {
		return routeGroupId;
	}

	
	public void setRouteGroupId(int routeGroupId) {
		this.routeGroupId = routeGroupId;
		
	}
	public String toString(){
		return toStringLong(false);
	}

	public String toStringLong( boolean fullInfo ){
		String x = "groupId=" + routeGroupId
				+"  type=" + type
				+"  StartPos=" + initPosition
				+"  EndPos=" + finalPosition
				+"  Start[ms]=" + startTime
				+"  End[ms]=" + endTime
				+"  car=" + bot;
		
		if(fullInfo){
		for( Pair<Speed,Duration> sd: speedDurList ){
			x +=  NL + " speed=" + sd.getT1() + " duration=" + sd.getT2();
		}}
		
		return x;
	}
	
	public String printRoute() {
		String ret = "Bot:" + bot.getId() + " [";
		long time;
		for (time = startTime; time < endTime; time += 100) {
			ret += getPos(time,false) + ", ";
		}
		ret += getPos(time,false) + "]"; 
		return ret;
	}
	
	public String printRouteWithTime() {
		String ret = "Bot:" + bot.getId() +" Start time "+startTime+" [";
		long time;
		for (time = startTime; time < endTime; time += 100) {
			ret += "("+getPos(time,false) + ", "+time+")";
		}
		ret += getPos(time,false) +", "+time+"]"; 
		return ret;
	}

	public Pos getFinalPos() {
		if( finalPosition==null ){
			finalPosition = getPos( Long.MAX_VALUE, false );
		}
		return finalPosition;
	}
	
	public Request getRequest() {
		return request;
	}

	public RouteImp( Pos from, long startTime, double deltaTime, List<Pos> pp, Bot bot ){
		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed,Duration>>();
		
		if( pp.size()>1){
			double priorTime = startTime;
			Pos prior = pp.get(0);
			for(int i=1; i<pp.size(); i++){
				Pos next = pp.get(i);
				Speed s = new Speed( (next.x-prior.x)/deltaTime, (next.y-prior.y)/deltaTime );
				double nextTime = priorTime+deltaTime;
				Duration d = new Duration( (long)priorTime, (long)nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s,d);
				speedDurList.add(pair);
				prior=next;
				priorTime=nextTime;
			}
		}
		
		this.bot = bot;
		this.speedDurList = speedDurList;
		this.initPosition = from;
		this.pos = pp;
		
		if( speedDurList.size() > 0 ){
			startTime = speedDurList.get(0).getT2().getStartTime();
			endTime = speedDurList.get(speedDurList.size()-1).getT2().getEndTime();
		}
	}

//	public Mvc getMVC() {
//		if( request==null ) return null;
//		return request.getMvc();
//	}

	public List<? extends Pos> getPos() {
		return pos;
	}

	public void setPos(List<? extends Pos> pos) {
		this.pos = pos;
	}
	
	public List<Pair<Speed, Duration>> getSpeedDurList() {
		return speedDurList;
	}

	public void setSpeedDurList(List<Pair<Speed, Duration>> speedDurList) {
		this.speedDurList = speedDurList;
	}

	public Pos getInitPosition() {
		return initPosition;
	}

	public void setInitPosition(Pos initPosition) {
		this.initPosition = initPosition;
	}

	public RouteType getType() {
		return type;
	}

	public void setType(RouteType type) {
		this.type = type;
	}

	public Double getPriority() {
		return priority;
	}

	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public List<Conflict> getBlockers() {
		return blockers;
	}

	public void addBlockers( Conflict blocker ) {
		if( blockers==null ){
			blockers = new ArrayList<Conflict>();
		}
		blockers.add( blocker );
	}

	public void addBlockers( List<Conflict> add ) {
		if( blockers==null ){
			blockers = new ArrayList<Conflict>();
			blockerBots = new ArrayList<Bot>();
		}
		
		for( Conflict c: add ){
			Bot blockBot=null;
			
			if( c.b1==bot ){
				blockBot = c.b2;
			}else{
				blockBot = c.b1;
			}
			
			if( blockerBots.contains(blockBot) ){
				continue; //alrady has it
			}
			
			blockerBots.add( blockBot );
			blockers.add( c );
		}
		
		
	}


	@Override
	public boolean isMvcRoute() {
		if( request==null || request.getMvc()==null ) return false;
		
		if( request.getMvc().getType()==MvcType.INPUT ){
			if( type == RouteType.ROUTE_2 ){
				return true;
			}
		}
		
		if( request.getMvc().getType()==MvcType.OUTPUT ){
			if( type == RouteType.ROUTE_1 ){
				return true;
			}
		}		

		return false;
	}

	@Override
	public Mvc getMvc() {
		if( request==null ) return null;
		return request.getMvc();
	}

}
