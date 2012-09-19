package cma.store.input.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.data.LayerModel;
import cma.store.data.Mvc;
import cma.store.data.MvcType;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cms.store.utils.Pair;

/**
Warehouse optimizer.
creating date: 2012-05-19
creating time: 15:15:50
autor: Czarek
 */

public class RandomBaseRequestCreator implements BaseRequestCreator{
	
	private LayerModel model;
	
	//Number of requests per second
	private double avgCreationRatio;
	private Random rnd;
	private long seed;
	Environment env;
	private Long priorTime = null;
	private long MINUTE = 60*1000;
	
	
	public RandomBaseRequestCreator( 
			long seed,
			double avgCreationRatioPerSecond,
			Environment env){

		this.env=env;
		this.avgCreationRatio = avgCreationRatioPerSecond;
		this.seed=seed;
		this.model=env.getLayerModel();
		rnd=new Random(this.seed);
	}



	@Override
	public boolean done() {
		return false; //never finished
	}

	@Override
	public List<BaseRequest> getRequests() {
		
		if( priorTime==null ){
			priorTime = env.getTimeMs();
			return null;
		}
		
		long actTime = env.getTimeMs();
		double timeDifS = (actTime - priorTime)/1000.;
		priorTime = actTime;
		
		int acc = 1000;
		int r = (int)(avgCreationRatio * timeDifS*acc);
		int count = rnd.nextInt( r*2 ) % acc;

		List<BaseRequest> brList = new ArrayList<BaseRequest>();
		for(int i=0; i<count; i++){
			int prdLocationCount = 1 + rnd.nextInt(5);
			//long destTime = actTime + MINUTE * 2 + 1000*i;
			
			//Pos pos = getRandomMvcPos();
			Mvc mvc = env.getMvcFleet().getMvc(MvcType.INPUT, 0);
			
			BaseRequest br = new BaseRequest( 
									-1,
									mvc,
									getRandomProductLocations( prdLocationCount )
							);
			
			brList.add( br );
			
			
		}
		
		return brList;
		
//		
//		List<PlanItem> roads = model.getRoads();
//		
//		Pos start = roads.get( rnd.nextInt( (roads.size() ) )).getPos();
//		Pos end = roads.get( rnd.nextInt( (roads.size() ) )).getPos();
//		
//		int diff = Math.abs(start.x-end.x) + Math.abs(start.y-end.y);
//		
//		return new Request(null, start, startTime+diff, end );
	}
	
	private List<LocPriority> getRandomProductLocations( int count ){
		List<LocPriority> lpList = new ArrayList<LocPriority>();
		List<PlanItem> x = model.getRoads();
		
		for(int i=0; i<count; i++){
			
			PlanItem pi = x.get( rnd.nextInt( x.size() ) );
			
			LocPriority lp = new LocPriority(pi.getPos(),1);
			lpList.add( lp );
			
		}
		
		return lpList;
	}
	
	private Pos getRandomMvcPos(){
		List<PlanItem> x = model.getMvcLocations();
		if( x.size()<=0 ) {
			throw new RuntimeException( "No MVCs");
		}
		PlanItem pi = x.get( rnd.nextInt( x.size() ) );
		return pi.getPos();
	}



	@Override
	public List<Pair<Long, List<BaseRequest>>> getRequestsWithoutDelete() {
		// TODO Auto-generated method stub
		return null;
	}


	
//	public void run(){
//		
//		createRandomRequests( 50 );
		
//		int sec = (int)(System.currentTimeMillis()/1000);
//		
//		while(work){
//			
//			int sec1 = (int)(System.currentTimeMillis()/1000);
//			
//			if( sec1>sec ){
//				int count = rnd.nextInt(avCountInOneSecond*(sec1-sec)*2);
//				createRandomRequests( count );
//				sec = sec1;
//				
//			}
//			
//			try {
//				this.wait(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
//	private void getAccidentOpositDir() {
//		List<PlanItem> roads = model.getRoads();
//		
//		PlanItem pu = roads.get(0);
//		Pos p = pu.getPos();
//		
//		Pos start = new Pos( 0, p.y );
//		Pos end = new Pos( Math.min(20, model.getSizeX()), p.y );
//		
//		Request r1 = new Request(0, start, null, end );
//		Request r2 = new Request(0, end, null, start );
//		
//		ScheduleItem item1 = sheduleCreator.createItem(r1);
//		schedule.add(item1);
//		
//		ScheduleItem item2 = sheduleCreator.createItem(r2);
//		schedule.add(item2);
//	}
//	
//	private void getAccidentTheSameDir() {
//		List<PlanItem> roads = model.getRoads();
//		
//		PlanItem pu = roads.get(0);
//		Pos p = pu.getPos();
//		
//		Pos start = new Pos( 0, p.y );
//		Pos end = new Pos( Math.min(20, model.getSizeX()), p.y );
//		
//		Request r1 = new Request(0, start, null, end );
//		Request r2 = new Request(0, start, null, end );
//		
//		ScheduleItem item1 = sheduleCreator.createItem(r1);
//		schedule.add(item1);
//		
//		ScheduleItem item2 = sheduleCreator.createItem(r2);
//		schedule.add(item2);
//	}
//	


//	private void createRandomRequests( int count ) {
//		for(int i=0; i<count; i++){
//			
//			Request r = createRandomRequest( i );
//			
//			ScheduleItem item = sheduleCreator.createItem(r);
//			
//			if( item!=null ){
//				schedule.add(item);
//			}else{
//				System.out.println("Can't find road");
//			}
//		}
//	}

}
