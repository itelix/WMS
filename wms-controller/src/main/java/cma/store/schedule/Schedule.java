package cma.store.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.print.attribute.standard.SheetCollate;

import org.apache.log4j.Logger;

import cma.store.control.BaseRealization;
import cma.store.control.Realization;
import cma.store.control.utils.RoutUtils;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.MvcController;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.serialization.FileSerializer;
import cma.store.stat.Statistics;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;
import cms.store.utils.PositionUtils;

/**
Warehouse optimizer.
creating date: 2012-05-17
creating time: 20:19:50
autor: Czarek
 */

public class Schedule {
	
	Logger log = Logger.getLogger(getClass());
	
	private static final String fileName = "external.properties";
	private static final String throwOnError = "throw.on.constraint.error";
	private List<ScheduleItem> items;
	private List<BaseRealization> realizations;
	private Environment env;
	private Statistics stat;
	private List<BaseRequest> realizedRequests;
	private Hashtable<Mvc,MvcController> mvc2controller;
	private boolean throwOnConstraintError;
	private Schedule baseSchedule;
	
	public Schedule( Environment env, Schedule baseSchedule ){
		this.env = env;
		this.baseSchedule = baseSchedule;
		
		items = new ArrayList<ScheduleItem>();
		realizations = new ArrayList<BaseRealization>();
		stat = new Statistics(env);
		realizedRequests = new ArrayList<BaseRequest>();
		mvc2controller = new Hashtable<Mvc,MvcController>();
		
		Properties properties = new Properties();
		try {
			URL url =  ClassLoader.getSystemResource(FileSerializer.fileName);
		    properties.load(new FileInputStream(new File(url.getFile())));
		} catch (IOException e) {
			log.error("", e);
		}
		throwOnConstraintError = Boolean.parseBoolean(properties.getProperty(throwOnError, "false"));
	}
	
	private MvcController getMvcController( Mvc mvc ){
		MvcController controller = mvc2controller.get(mvc);
		if( controller==null ){
			controller = new MvcController(mvc);
			mvc2controller.put(mvc, controller);
		}
		return controller;
	}

	
	/**
	 * First Time when MVC is available
	 * @param mvc
	 * @return
	 */
	public long getMvcAvailableTime( Mvc mvc ){
		long t1 = getMvcAvailableTimeSub( mvc );
		
		if( baseSchedule!=null ){
			long t2 = baseSchedule.getMvcAvailableTimeSub( mvc );
			if( t2>t1 ) return t2;
		}
		
		return t1;
	}
	

	private long getMvcAvailableTimeSub( Mvc mvc ){
		
		MvcController controller = mvc2controller.get(mvc);
		
		if( controller==null ){
			return 0;
		}
		
		return controller.getAvailableTime();
	}
	
//	/**
//	 * First Time when MVC is available
//	 * @param mvc
//	 * @return
//	 */
//	public Long getMvcAvailableTime( Mvc mvc ){
//		MvcController controller = getMvcController(mvc);
//		Long time = controller.getFirstAvailableTime(afterTime);
//		log.debug("Next available time = " + time );
//		return time;
//	}


	public void update( Realization rel ){
		env.getSchedule().addItems( rel.getNewItems() );
		for(BaseRealization br: rel.getBaseRealList()){
			addBaseRealization( br );
		}
	}
	
	public List<BaseRealization> getBaseRealizations() {
		return realizations;
	}
	
	public List<BaseRequest> getRealizedRequests() {
		return realizedRequests;
	}

	public void update( Schedule s ){
//		items.addAll( s.items );
		addItems(s.items);
		realizations.addAll( s.realizations );
	}
	
	public void addBaseRealization( BaseRealization br ){
		realizations.add( br );
	}
	
	public void removeBaseRealization( BaseRealization br ){
		realizations.remove( br );
	}
	
	public boolean done(){
		return items.size()==0;
	}
	
	// Check if one bot has exactly one item at time
	private boolean verifyItem(ScheduleItem item) {
		long itemStartTime = item.getRoute().getStartTime();
		long itemEndTime = item.getRoute().getFinalTime();
		Bot itemBot = item.getRoute().getBot();
		for (ScheduleItem otherItem : items) {
			Route otherRoute = otherItem.getRoute();
			long otherStartTime = otherRoute.getStartTime();
			long otherEndTime = otherRoute.getFinalTime();
			// Check all items with the same bot, and if they have
			// startTime or endTime between itemStartTime and itemEndTime
			if (otherRoute.getBot().equals(itemBot)
				&& ((otherStartTime > itemStartTime && otherStartTime < itemEndTime)
					|| otherEndTime > itemStartTime && otherEndTime < itemEndTime)) {
				return false;
			}
		}
		
		Route lastRoute = getLastRoute(itemBot);
		if(lastRoute != null && !lastRoute.getFinalPos().equals(item.getRoute().getInitPosition())) {
			log.error("Try to add Route with Jump position");
		}
		
		return true;
	}
	
	public synchronized void add( ScheduleItem item ){
		// Check if one bot has exactly one item at time
		if (!verifyItem(item)) {
			String error = "Error: invalid/redundant schedule item added! (item: " + item + ")";
			log.debug(error);
			if (throwOnConstraintError) {
				throw new RuntimeException(error);
			}
		}
		items.add( item );
		
		if( item.getRoute().isMvcRoute() ){
			getMvcController( item.getMvc() ).reserveSlots( item.getRoute() );
		}
	}
	
	public synchronized void addItems( List<ScheduleItem> list ){
		for (ScheduleItem item : list) {
			add(item);
		}
//		items.addAll( list );
	}
	
	public synchronized void addRoutes( List<Route> routes ){

		for( Route r: routes ){
			add( new ScheduleItem(r) );
		}
	}
	

	public long getAvailableMinTime( Bot bot ){
		 Route r = getLastRoute( bot );
		 if( r==null ) return 0;
		 return r.getFinalTime();
	}
	
	
	public Pos getLastPos( Bot bot ){
		 Route r = getLastRoute( bot );
		 if( r==null ) {
			 return bot.getPos();
		 }
		 return r.getFinalPos();
	}
	
	public Route getLastRoute( Bot bot, long tillTime ){
		Route r = null;
		
		for(ScheduleItem item: items){
			if( item.getCar()==bot && item.getRoute().getStartTime()<=tillTime ){
				r = item.getRoute();
			}
		}
		
		return r;
	}
	
	public Route getLastRoute( Bot bot ){
		Route r = null;
		
		for(ScheduleItem item: items){
			if( item.getCar()==bot ){
				r = item.getRoute();
			}
		}
		
		return r;
	}
	
	public synchronized Pos getBotPosition( Bot bot, long time, boolean nullWhenAfterRoute ){

		Route r = getLastRoute(bot);
		
		if( nullWhenAfterRoute ){
			if( r==null || time > r.getFinalTime()+BaseEnvironment.DEFAULT_AFTER_ROUTE_ACCURACY){
				return null;
			}
		}
		
		r = getLastRoute(bot, time);
		
		if( r==null ){
			return bot.getPos();
		}
		
		return r.getPos(time, false);
	}
	
	public Route getRoute( Bot car, long time, boolean nullWhenAfterRoute ){
		Route r = null;
		
		for(ScheduleItem item: items){
			if( item.getCar()==car ){
				
				if( time < item.getRoute().getStartTime() ) continue;
				
				if( nullWhenAfterRoute ){
					if( time <= item.getRoute().getFinalTime() ){
						r=item.getRoute();
					}
					
				}else{
					r=item.getRoute();
				}
			}
		}

		return r;
	}
	
	public long getMvcUseEndTime( Mvc mvc, int location ){
		long time = -mvc.getTimeToStayInMvc();
		
		for(ScheduleItem item: items){
			if( item.getMvc()==mvc ){
				if( time < item.getRoute().getFinalTime()) {
					 time = item.getRoute().getFinalTime();
				}
			}
		}
		
		return time+mvc.getTimeToStayInMvc();
	}
	
//	public Car getAnyAvailableCar(){
//		if( cars.size()<=0 ) return null;
//		Car c = cars.get(0);
//		cars.remove(c);
//		return c;
//	}
	
//	public void allocateCar( Car car ){
//		cars.remove(car);
//	}
	
	public synchronized List<Bot> getAvailableCars(){
		List<Bot> available = new ArrayList<Bot>( env.getBotFleet().getBots() );
		
		for( ScheduleItem item: items ){
			
			available.remove( item.getRoute().getBot() );
			
		}
		
		return available;
	}
	
	private long minTimeToTrevel( Bot bot, Pos pos1, Pos pos2 ){
		//TODO calculation is to raw
		double dist = Math.abs(pos1.x-pos2.x) + Math.abs(pos1.y-pos2.y);
		return (long)(dist / bot.getMaxSpeed());
	}
	
	public synchronized List<Bot> getAvailableCars(long availableAfterTime, Pos availableAtPosition, List<Bot> bots ){
		List<Bot> available = new LinkedList<Bot>( bots );
		
		for( ScheduleItem item: items ){
			Pos end = item.getRoute().getFinalPos();
			long timeToTrevel = minTimeToTrevel( item.getCar(), availableAtPosition, end);
			
			if( item.getRoute().getFinalTime() > availableAfterTime-timeToTrevel ){
				//bot is not available
				available.remove( item.getRoute().getBot() );
				if( available.size()==0 ) return available;
			}
			
		}
		
		return available;
	}

	
	
//	public synchronized void update( int time, SchedulObservator o ){
//		for(ScheduleItem item: items){
//			o.update( item );
//		}
//	}
	
	private Route getNextRoute( Bot bot, long afterTime ){
		Route next = null;

		for( ScheduleItem si: items ){
			if( si.getRoute().getBot()==bot ){
				if( si.getRoute().getStartTime() > afterTime  ){
					if( next==null || (next.getStartTime()>si.getRoute().getStartTime()) ){
						next = si.getRoute();
					}
				}
				
			}
		}
		return next;
	}
	

	/**
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @param time
	 * @param careStableCars
	 * @return true at give position and time there is Bot
	 * if careStableCars is false - Algorithm will don't care Bots which are after work and have no other task to do  - stable blockers
	 * 
	 */
	public synchronized boolean isBotConflit1( Bot bot, double xMin, double xMax, double yMin, double yMax, long time, boolean careStableCars ) {
		
		for( Bot bot2: env.getBotFleet().getBots() ){
			if( bot==bot2) continue;
			
			 Pos p = getBotPosition( bot2, time, false );
			 
			 if( p.x<xMin || p.x>xMax || p.y<=yMin || p.y>yMax ){
				continue; //couldn't be in colision
			 }
			 
			 if( getBotPosition( bot2, time, true )!=null ){
				 getBotPosition( bot2, time, false ); //TODO for debug only
//				 log.info("Collision with Bot("+bot2.getId()+" and " +bot.getId()+") at time" +time);
//				 dumpAllRoutes(time-100);
				 return true; //in move colision
			 }	
			 
			 if( careStableCars ){
				 getBotPosition( bot2, time, false ); //TODO for debug only
				 getLastRoute(bot2);
				 return true;
			 }else{
				 if( getNextRoute( bot2, time ) !=null ){
					 return true;
				 }				 
			 }
				
		}
		return false;
	}
	
	private void dumpAllRoutes(long startTime) {
		for (ScheduleItem it : items) {
			Route route =it.getRoute();
			if(route.getStartTime() >= startTime){
				log.info(route.printRouteWithTime());
			}		
		}
		
	}
	
	public synchronized Route getConflitedRoute( Bot bot, double xMin, double xMax, double yMin, double yMax, long time, boolean careStableCars ) {
		
		for( Bot bot2: env.getBotFleet().getBots() ){
			if( bot==bot2) continue;
			
			 Pos p = getBotPosition( bot2, time, false );
			 
			 if( p.x<xMin || p.x>xMax || p.y<yMin || p.y>yMax ){
				continue; //couldn't be in colision
			 }
			 
			 Route r = getRoute( bot2, time, !careStableCars );
			 if( r!=null ) return r;
		}
		return null;
	}
	
	public synchronized List<ScheduleItem> getItems() {
		return new ArrayList<ScheduleItem>( items );
	}
//	public synchronized void deleteItem(int index) {
//		
//		if( item.getRoute().isMvcRoute() ){
//			
//		}
//		this.items.remove(index);
//	}
	
	public void updateFleet() {
		long time = env.getTimeMs();

		for( int i=items.size()-1; i>=0; i-- ){
			ScheduleItem item = items.get(i);
			item.getRoute().update(time);
			if( item.getRoute().isFinished() ){
//				item.getCar().setWork(false);
				if (item.getRoute().getType() == RouteType.ROUTE_2) {
					BaseRequest br = item.getRoute().getRequest().getBaseRequest();
					log.debug(
						"Request realized. Order(" + br.getOrderId() + ")"
						+ "\n\twith StorageLoc list: " + br.getStorageLocations()
						+ "\n\tand MVC: " + br.getMvc()
					);
					realizedRequests.add(br);
					
					br.getOrderId();
					br.getQueueId();
					br.getMvc();
					
				}
				remove( item );
			}			
		}
	}

	public synchronized void remove(ScheduleItem si) {
		items.remove(si);
		stat.update( si );

		if( si.getRoute().isMvcRoute() ){
			getMvcController( si.getMvc() ).freeSlots( si.getRoute() );
		}
	}

	public void removeAll(List<ScheduleItem> list) {
		for( ScheduleItem si: list){
			remove( si );
		}
	}

	public Statistics getStat() {
		return stat;
	}

	public void initStats() {
		stat = new Statistics(env, true);
	}
	
	

	/**
	 * Return bots that have less routs then 2. For this bot we should start planning request
	 * @return
	 */
	public List<Bot> getBotsReadyToRambo() {
		Map<Bot, Integer> countingMap = new HashMap<Bot, Integer>();
		for( int i=items.size()-1; i>=0; i-- ){
			ScheduleItem item = items.get(i);
			Bot bot = item.getCar();
			Integer numberOfRoutes = countingMap.get(bot);
			if(numberOfRoutes == null) {
				numberOfRoutes = 1;
			} else {
				numberOfRoutes++;
			}
			countingMap.put(bot, numberOfRoutes);
		}
		List<Bot> result = new ArrayList<Bot>(env.getBotFleet().getBots());
		for (Map.Entry<Bot, Integer> entry : countingMap.entrySet()) {
			if(entry.getValue() >2) {
				result.remove(entry.getKey());
//				result.add(entry.getKey());
			}
		}
		
		return result;		
	}

	/**
	 * Delete all routes after request from argument
	 * @param baseRequest
	 * @return success or fail
	 */
	public void deleteRouteForRequest(BaseRequest baseRequest) {
		// ger firts route for this request
		Route routeToDelete = null;
		for (ScheduleItem item : this.getItems()) {
			if (item.getBaseRequest().getOrderId() == baseRequest.getOrderId()) {
				routeToDelete = item.getRoute();
			}
		}
		// delete routes
		deleteRoutesAfter(routeToDelete);
	}

	/**
	 * Delete all routes after Route from argument including bot move routes
	 * @param routeToDelete
	 * @return
	 */
	private boolean deleteRoutesAfter(Route routeToDelete) {
		boolean result = false;
		BaseRequest requestToDelete = routeToDelete.getRequest().getBaseRequest();
		
		Iterator<ScheduleItem> itr = items.iterator();
		while (itr.hasNext()) {
			ScheduleItem item = itr.next();
			Route route = item.getRoute();
			if (route.getStartTime() >= routeToDelete.getStartTime()
			|| route.getRouteGroupId() == routeToDelete
					.getRouteGroupId()) {
				itr.remove();
				result = true;
			} else {
				BaseRequest request = route.getRequest().getBaseRequest();
				
				if(request.getOrderId() >= requestToDelete.getOrderId()) {
					itr.remove();
					result = true;
				}
			}
		}
		
		return result;
	}
}
