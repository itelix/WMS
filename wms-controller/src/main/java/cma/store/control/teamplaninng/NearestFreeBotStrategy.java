package cma.store.control.teamplaninng;

import java.util.List;

import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.data.Bot;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.Utils;

/**
Warehouse optimizer.
creating date: 24-07-2012
creating time: 09:46:30
autor: adam
 */

public class NearestFreeBotStrategy implements TeamPlaninngStrategy {

	Environment env;
	
	public NearestFreeBotStrategy(){}
	
	public NearestFreeBotStrategy(Environment env){
		this.env = env;
	}

	@Override
	public BotProdLocation getCarProdLocation(List<Bot> bots,
		List<LocPriority> prodLocations, boolean[][] used) {
		
//		this.testBotShedule();
		
		double bestDist = Double.MAX_VALUE;
		int bestI=-1;
		int bestJ=-1;
		
		for(int i=0;i<bots.size();i++){
			if(!bots.get(i).isWork()) {
				for(int j=0;j<prodLocations.size();j++){
					if( prodLocations.get(j).isAssigned() ) continue;
					double d = Utils.distance( prodLocations.get(j), bots.get(i));
					if( d<bestDist ){
						bestDist = d;
						bestI = i;
						bestJ = j;
					}
				}
			}
		}
		
		if( bestI == -1 ) return null;
		//used[bestI][bestJ] = true;
		//productLocations.remove(bestJ);
		bots.get(bestI).setWork(true);
		prodLocations.get(bestJ).setAssigned(true);
		return new BotProdLocation( bots.get(bestI), prodLocations.get(bestJ).getPos(), null );
	}
/*
	@Override
	public CarProdLocation getCarProdLocation(List<Bot> bots,
			List<LocPriority> prodLocations, boolean[] used) {
		double bestDist = Double.MAX_VALUE;
		int bestI=-1;
		int bestJ=-1;
		
		for(int i=0;i<bots.size();i++){
			for(int j=0;j<prodLocations.size();j++){
				if( used[i][j] ) continue;
				double d = Utils.distance( prodLocations.get(j), bots.get(i));
				if( d<bestDist ){
					bestDist = d;
					bestI = i;
					bestJ = j;
				}
			}
		}
		
		if( bestI == -1 ) return null;
		used[bestI][bestJ] = true;
		//productLocations.remove(bestJ);
		return new CarProdLocation( bots.get(bestI), prodLocations.get(bestJ).getPos() );
	}
*/

	private void testBotShedule() {
		if( this.env.getSchedule() != null && this.env.getSchedule().getItems() !=null) {
			for (ScheduleItem item : this.env.getSchedule().getItems()) {
				item.toString();
			}
		}
	}

	@Override
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMVCController(IMVCController mvcController) {
		// TODO Auto-generated method stub
		
	}
}
