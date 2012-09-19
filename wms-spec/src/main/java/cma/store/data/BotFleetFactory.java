package cma.store.data;

import java.util.List;
import java.util.Random;

/**
Warehouse optimizer.
creating date: 2012-07-20
creating time: 19:31:17
autor: Czarek
 */

public class BotFleetFactory {
	LayerModel model;
	Random rnd;
	
	public BotFleetFactory(LayerModel model, long seed){
		this.model = model;
		rnd = new Random(seed);
	}
	
	public BotFleet createRandomLocatetFleet( int carsCount ) {
		BotFleet fleet = new BotFleet();
		List<PlanItem> roads = model.getRoads();
		
		for(int i=0; i<carsCount; i++){
			if( roads.size()<=0 ) {
				System.out.println("To low number of rouads to set all required cars");
				break;
			}
			
			PlanItem pi = roads.remove(rnd.nextInt(roads.size()));
			Pos p = pi.getPos();
			
			Bot c = new Bot( (p.x+0.5)*model.getUnitSize(), (p.y+0.5)*model.getUnitSize());
			fleet.addBot(c);
		}
		
		return fleet;
	}

}
