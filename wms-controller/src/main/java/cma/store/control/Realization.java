package cma.store.control;

import java.util.ArrayList;
import java.util.List;

import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.schedule.Schedule;
import cma.store.schedule.ScheduleItem;

/**
Warehouse optimizer.
creating date: 2012-07-18
creating time: 20:40:20
autor: Czarek
 */

public class Realization {
	
	private List<BaseRealization> baseRealList;
	private Double score = null;
	Schedule localSchedule;
	private List<ScheduleItem> newItems; 
	

	public Realization( List<BaseRealization> baseRealList, Environment env) {
		this.baseRealList = baseRealList;
		localSchedule = new Schedule(env, env.getSchedule());
		
		newItems = new ArrayList<ScheduleItem>();
		localSchedule.update( env.getSchedule() );
	}
	
	public void addScheduleItem( ScheduleItem si ){
		
		Route lastRoute = localSchedule.getLastRoute( si.getCar() );
		if( lastRoute!=null ){
			Pos p1 = lastRoute.getFinalPos();
			Pos p2 = si.getRoute().getInitPosition();
			
			if( Math.abs(p1.x-p2.x)>0.001 || Math.abs(p1.y-p2.y)>0.001 ){
				throw new RuntimeException( "Wrong starting poit of new route!");
			}
			
			if( lastRoute.getFinalTime() > si.getRoute().getStartTime() ){
				throw new RuntimeException( "Wrong starting time of new route!");
			}
		}
		
		newItems.add(si);
		localSchedule.add(si);

	}
	
	public void removeScheduleItem( ScheduleItem si ){
		newItems.remove(si);
		localSchedule.remove(si);
	}
	

	public void removeScheduleItems(List<ScheduleItem> list) {
		newItems.removeAll(list);
		localSchedule.removeAll(list);
	}
	
	
	
	
	public Schedule getLocalSchedule() {
		return localSchedule;
	}

	public static final Realization createRealization( List<BaseRequest> requests, Environment env ){
		List<BaseRealization> list = new ArrayList<BaseRealization>();
		
		for( BaseRequest br: requests ){
			list.add( new BaseRealization(br) );
		}
		
		return new Realization( list, env );
	}
	

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public List<BaseRealization> getBaseRealList() {
		return baseRealList;
	}

	public List<ScheduleItem> getNewItems() {
		return newItems;
	}


	

}
