package cma.store.data;


/**
Warehouse optimizer.
creating date: 2012-05-15
creating time: 22:59:48
autor: Czarek
 */

public class PlanFactory {
	
	private PlanFactory(){
		
	}
	
	public static PlanFactory getPlanFactory1(){
		return new PlanFactory();
		
	}
	
//	public PlanItem[][] getRoads( int sizeX, int sizeY ){
//		
//		PlanItem[][] plan = new PlanItem[sizeX][sizeY];
//		
//		for(int i=0; i<sizeX; i++){
//			for(int j=0; j<sizeY; j++){
//				boolean isRoad = false;
//				
//				if( i%5==0 || j%10==0 ){
//					isRoad = true;
//				}
//				
//				plan[i][j] = new PlanItem(i,j, isRoad);
// 			}			
//		}
//		return plan;
//	}

}
