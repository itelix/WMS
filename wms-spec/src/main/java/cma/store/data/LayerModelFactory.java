package cma.store.data;

import cma.store.env.BaseEnvironment;



/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 22:23:48
autor: Czarek
 */

public class LayerModelFactory {
	public static final int DEFAULT_ROW1 = 4;
	public static final int DEFAULT_ROW3 = 8;
	public static final int DEFAULT_HOMEROW = 6;
	public static final int DEFAULT_ROW4 = 12;
	PlanItem[][] units;
	double unitSize = BaseEnvironment.DEFAULT_DIST_UNIT_SIZE_MM;
	
	private LayerModelFactory(){
	}

	public static final LayerModelFactory getModelFactory(){
		return new LayerModelFactory();
	}
	
	public final LayerModel getSimpleModel(){
		int size = 51;
		int sizeX = size;
		int sizeY = size;
		
		units = new PlanItem[sizeX][sizeY];
		//horizontal
		for(int i=0; i<sizeX; i++){
			for(int j=0; j<sizeY; j++){
				PlanItemType type = PlanItemType.WALL;
				
				if( i%5==0 || j%10==0 ){
					type = PlanItemType.ROAD;
				}
				if( i==0 && (j+5)%10==0 ){
					type = PlanItemType.MVC_IN;
				}
				
				set(i,j,type);
 			}			
		}
		LayerModel model = new LayerModel( units, unitSize );
		return model;
	}
	
	public final LayerModel getNewBurgModel(){
		int sizeX = 100;
		int sizeY = 200;
		
		units = new PlanItem[sizeX][sizeY];
		//horizontal
		for(int i=0; i<sizeX; i++){
			for(int j=0; j<sizeY; j++){
				PlanItemType type = PlanItemType.WALL;
				
				if( i%5==0 || j%10==0 ){
					type = PlanItemType.ROAD;
				}
				
				if( i==0 && (j+5)%10==0 ){
					type = PlanItemType.MVC_IN;
				}
				
				set(i,j,type);
 			}			
		}
				
		LayerModel model = new LayerModel( units, unitSize );
		return model;
	}
	
	public final LayerModel getTestCaseModel_1(){
		int sizeX = 30;
		int sizeY = 60;
		
		units = new PlanItem[sizeX][sizeY];
		//horizontal
		
		for(int i=0; i<sizeX; i++){
			for(int j=0; j<sizeY; j++){
				set(i,j, PlanItemType.WALL);
 			}			
		}

		for(int i=0; i<sizeX; i++){
				set(i,0, PlanItemType.ROAD);
				set(i,DEFAULT_ROW1, PlanItemType.ROAD);
				set(i,DEFAULT_ROW3, PlanItemType.ROAD);
		}

		for(int j=0; j<sizeY; j++){
			if( j < DEFAULT_ROW4 ){
				set(0,j, PlanItemType.ROAD);
				set(sizeX-1,j, PlanItemType.ROAD);
			}else{
				set(0,j, PlanItemType.AISLE);
				set(sizeX-1,j, PlanItemType.AISLE);
			}
		}		
		
		// Nodes:
		for (int i=0; i<sizeX; i+=29) {
			for(int j=0; j<sizeY; j+=4){
				set(i, j, PlanItemType.NODE);
			}
			set(i, DEFAULT_HOMEROW, PlanItemType.NODE);
		}
		
		set(10,0, PlanItemType.MVC_IN);
		set(20,0, PlanItemType.MVC_IN);
		set(15,0, PlanItemType.MVC_OUT);
		
		LayerModel model = new LayerModel( units, unitSize );
		model.setRow1(DEFAULT_ROW1);
		model.setRow3(DEFAULT_ROW3);
		model.setRow4(DEFAULT_ROW4);
		return model;
	}
	
	public final LayerModel get3AlleysModel(){
		int sizeX = BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS * 3;
		int sizeY = BaseEnvironment.HMPC_ROW4 + BaseEnvironment.HMPC_BAYS_NUM + 1;
		
		sizeX += 1; // last column
		
		units = new PlanItem[sizeX][sizeY];
		//horizontal
		
		for(int i=0; i<sizeX; i++){
			for(int j=0; j<sizeY; j++){
				set(i,j, PlanItemType.WALL);
 			}			
		}

		for(int i=0; i<sizeX; i++){
			set(i,0, PlanItemType.ROAD);
			set(i,BaseEnvironment.HMPC_ROW1, PlanItemType.ROAD);
			set(i,BaseEnvironment.HMPC_ROW3, PlanItemType.ROAD);
		}
		
		for (int i=0; i<sizeX; i+=BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS) {
			for(int j=0; j<sizeY; j++){
				if( j < BaseEnvironment.HMPC_ROW4 ){
					set(i,j, PlanItemType.ROAD);	
				}else{
					set(i,j, PlanItemType.AISLE);
				}
			}
		}
//
//		for(int j=0; j<sizeY; j++){
//			if( j < BaseEnvironment.HMPC_ROW4 ){
//				set(sizeX-1,j, PlanItemType.ROAD);	
//			}else{
//				set(sizeX-1,j, PlanItemType.AISLE); 
//			}
//		}
		
		// Nodes:
		for (int i=0; i<sizeX; i+=BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS) {
			set(i,0, PlanItemType.NODE);
			set(i,BaseEnvironment.HMPC_ROW1, PlanItemType.NODE);
			set(i,BaseEnvironment.HMPC_ROW3, PlanItemType.NODE);
			set(i, BaseEnvironment.HMPC_HOMEROW, PlanItemType.NODE);
		}
		
		for (int i=0; i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS<sizeX; i++) {
			if (i % 4 == 0 || i % 4 == 3)
				continue;
			set(i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS,0, PlanItemType.MVC_IN);
		}
		
		LayerModel model = new LayerModel("FourAlleysModel", units, unitSize, 4, 3, BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS, 4, false, false);
		model.setRow1(BaseEnvironment.HMPC_ROW1);
		model.setRow3(BaseEnvironment.HMPC_ROW3);
		model.setRow4(BaseEnvironment.HMPC_ROW4);
		return model;
	}
	
	public final LayerModel getBasicHMPCModelNonDirected(){
		LayerModel m = getBasicHMPCModel();
		m.setDirectedLayout(false);
		return m;
	}
	
	public final LayerModel getBasicHMPCModel(){
		int sizeX = BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS * 3;
//		int sizeY = 35;
		int sizeY = BaseEnvironment.HMPC_ROW4 + BaseEnvironment.HMPC_BAYS_NUM + 1;
		
		sizeX += 1; // last column
		
		units = new PlanItem[sizeX][sizeY];
		
		for(int i=0; i<sizeX; i++){
			for(int j=0; j<sizeY; j++){
				set(i,j, PlanItemType.WALL);
 			}			
		}

		for(int i=0; i<sizeX; i++){
			set(i,0, PlanItemType.ROAD);
			set(i,BaseEnvironment.HMPC_ROW1, PlanItemType.ROAD);
			set(i,BaseEnvironment.HMPC_ROW3, PlanItemType.ROAD);
		}
		
		for (int i=0; i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS<sizeX; i++) {
			for(int j=0; j<sizeY; j++){
				if ((j >= BaseEnvironment.HMPC_ROW1) || (i % 4 == 0 || i % 4 == 3)) {
					if( j < BaseEnvironment.HMPC_ROW4 ){
						set(i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS,j, PlanItemType.ROAD);	
					}else{
						set(i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS,j, PlanItemType.AISLE);
					}
				}
			}
		}
		
		// Nodes:
		for (int i=0; i<sizeX; i+=BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS) {
			set(i,0, PlanItemType.NODE);
			set(i,BaseEnvironment.HMPC_ROW1, PlanItemType.NODE);
			set(i,BaseEnvironment.HMPC_ROW3, PlanItemType.NODE);
			set(i, BaseEnvironment.HMPC_HOMEROW, PlanItemType.NODE);
		}

		for (int i=0; i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS<sizeX; i++) {
			if (i % 4 == 0 || i % 4 == 3)
				continue;
			set(i*BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS,0, PlanItemType.MVC_IN);
		}
		
		LayerModel model = new LayerModel("BasicHMPC", units, unitSize, 4, 3, BaseEnvironment.HMPC_DISTANCE_BETWEEN_COLS, 4, true, true);
		model.setRow1(BaseEnvironment.HMPC_ROW1);
		model.setRow3(BaseEnvironment.HMPC_ROW3);
		model.setRow4(BaseEnvironment.HMPC_ROW4);
		return model;
	}
	
	private void set(int x, int y, PlanItemType type){
		if( units[x][y]==null ){
			units[x][y] = new PlanItem(x,y,type,unitSize);
		}else{
			units[x][y].type = type;
		}
	}



}
