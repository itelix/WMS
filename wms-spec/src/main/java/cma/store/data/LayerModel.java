package cma.store.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.env.BaseEnvironment;
import cms.store.utils.PositionUtils;



/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 22:12:25
autor: Czarek
 */

public class LayerModel {
	int sizeX;
	int sizeY;
	String name;
	
	PlanItem[][] units;
	
	List<PlanItem> roads;
	List<PlanItem> mvcs;
	List<PlanItem> aisles;
	
	int time;
	double unitSize;
	int numberOfCols;
	int distanceBetweenCols;
	// out of date -- do not use. To remove in future
	int numberOfAvenues;
	// out of date -- do not use. To remove in future
	int distanceBetweenAvenues;
	
	int homeRow;
	boolean directedLayout;
	boolean HMPCType;

	int row1, row3, row4;

	public int getRow1() {
		return row1;
	}

	public void setRow1(int row1) {
		this.row1 = row1;
	}

	public int getRow3() {
		return row3;
	}

	public void setRow3(int row3) {
		this.row3 = row3;
	}

	public int getRow4() {
		return row4;
	}

	public void setRow4(int row4) {
		this.row4 = row4;
	}

	public int getHomeRow() {
		return homeRow;
	}
	
	public void setHomeRow(int homeRow) {
		this.homeRow = homeRow;
	}
	
	// out of date -- do not use. To remove in future
	public int getNumberOfAvenues() {
		return numberOfAvenues;
	}

	// out of date -- do not use. To remove in future
	public int getDistanceBetweenAvenues() {
		return distanceBetweenAvenues;
	}
	
	public LayerModel( PlanItem[][] units, double unitSize ){
		this.units = units;
		this.unitSize = unitSize;
		this.sizeX = units.length;
		this.sizeY = units[0].length;
		this.numberOfCols = 2;
		this.distanceBetweenCols = 29;
		this.distanceBetweenAvenues = 4;
		this.numberOfAvenues = 3; 
		this.directedLayout = false; 
		
		this.roads = null;
		
		this.name = "Basic";
		
		this.row1 = LayerModelFactory.DEFAULT_ROW1;
		this.row3 = LayerModelFactory.DEFAULT_ROW3;
		this.row4 = LayerModelFactory.DEFAULT_ROW4;
		this.homeRow = LayerModelFactory.DEFAULT_HOMEROW;
		
		init();
	}
	
	private void init(){
//		roads = new ArrayList<PlanItem>();
//		mvcs = new ArrayList<PlanItem>();
		
//		for(int i=0; i<sizeX; i++){
//			for(int j=0; j<sizeY; j++){
//				if( units[i][j].isMvc() ){
//					mvcs.add( units[i][j] );
//				}
//				if( units[i][j].isRoad() ){
//					roads.add( units[i][j] );
//				}	
//				if( units[i][j].isAisle() ){
//					aisles.add( units[i][j] );
//				}
//			}			
//		}
		
	}
	
	public LayerModel( String name, PlanItem[][] units, double unitSize,
			int numberOfCols, int numberOfAvenues,
			int distanceBetweenCols, int distanceBetweenAvenues,
			boolean HMPCType){
		this(units, unitSize);
		this.name = name;
		this.numberOfCols = numberOfCols;
		this.distanceBetweenCols = distanceBetweenCols;
		this.distanceBetweenAvenues = distanceBetweenAvenues;
		this.numberOfAvenues = numberOfAvenues;
		this.homeRow = BaseEnvironment.HMPC_HOMEROW;
		this.HMPCType = HMPCType;
	}
	
	public LayerModel( String name, PlanItem[][] units, double unitSize,
			int numberOfCols, int numberOfAvenues,
			int distanceBetweenCols, int distanceBetweenAvenues,
			boolean directedLayout, boolean HMPCType){
		this(name, units, unitSize, numberOfCols, numberOfAvenues,
				distanceBetweenCols, distanceBetweenAvenues, HMPCType);
		this.directedLayout = directedLayout;
	}
	
	public boolean isDirectedLayout() {
		return directedLayout;
	}

	public void setDirectedLayout(boolean directedLayout) {
		this.directedLayout = directedLayout;
	}

	public int getColsNum() {
		return numberOfCols;
	}

	public int getDistanceBetweenCols() {
		return distanceBetweenCols;
	}

	public enum AVENUE_TYPE {EAST, WEST, BOTH};
	
	public AVENUE_TYPE getAvenueType(Pos p) {
		if (!directedLayout)
			return AVENUE_TYPE.BOTH;
		int y = PositionUtils.getIntY(p.y);
		if ((y / distanceBetweenAvenues)  < 2)
			return AVENUE_TYPE.EAST;
		else
			return AVENUE_TYPE.WEST;
	}
	
	/**
	 * @param x
	 * @return
	 */
	public final int convX(double x){
		return (int)(x/unitSize);
	}
	
	/**
	 * @param x
	 * @return
	 */
	public final int convY(double y){
		return (int)(y/unitSize);
	}
	
	public List<PlanItem> getMvcLocations(){
		mvcs = new ArrayList<PlanItem>();
		for(int i=0; i<getSizeX(); i++){
			for(int j=0; j<getSizeY(); j++){
				if( getPlanUnit(i, j).isMvc() ){
					// Filip fix: getPlanUnit changed to getPlanUnitWithValidPos
					// No side effects in other code
					mvcs.add( getPlanUnitWithValidPos(i, j) );
				}
			}			
		}
		return mvcs;
	}
	
	public List<Pos> getMvcPositions(){
		ArrayList<Pos> mvcsPos = new ArrayList<Pos>();
		for(int i=0; i<getSizeX(); i++){
			for(int j=0; j<getSizeY(); j++){
				if( getPlanUnit(i, j).isMvc() ){
					// Filip fix: getPlanUnit changed to getPlanUnitWithValidPos
					// No side effects in other code
					mvcsPos.add(createPos(i, j) );
				}
			}			
		}
		return mvcsPos;
	}


	public List<PlanItem> getRoads(){
		if( roads==null ){
			collectRoads();
		}
		
		return roads;
	}
	
	public List<PlanItem> getAisles(){
		if( aisles==null){
			collectRoads();
		}
		
		return aisles;
	}
	
	public int getSizeX(){
		return sizeX;
	}
	
	public int getSizeY(){
		return sizeY;
	}
	
	public double getRealSizeX(){
		return sizeX*unitSize;
	}
	
	public double getRealSizeY(){
		return sizeY*unitSize;
	}
	
	public boolean isValidPos(Pos p) {
		if (/*PositionUtils.getIntX(p.x) >= getSizeX()
				|| PositionUtils.getIntX(p.y) >= getSizeY() ||*/
				!isRoad(p))
			return false;
		return true;
	}
	
	public String getHashString() {
		return "__" + name + "__" + sizeX + "_" + sizeY + "__" + getRow1() + "_" + getRow3() + "_" + getRow4();
	}
	
	private void collectRoads(){
		roads = new ArrayList<PlanItem>();
		mvcs = new ArrayList<PlanItem>();
		aisles = new ArrayList<PlanItem>();
		
		
		for(int i=0; i<getSizeX(); i++){
			for(int j=0; j<getSizeY(); j++){
				if( getPlanUnit(i, j).isRoad() ){
					roads.add( getPlanUnit(i, j) );
				}
				if( getPlanUnit(i, j).isMvc() ){
					mvcs.add( getPlanUnit(i, j) );
				}
				if( getPlanUnit(i, j).isAisle() ){
					aisles.add( getPlanUnit(i, j) );
				}
			}			
		}
	}
	
	public boolean isRoad(Pos p){
		return isRoad( p.x, p.y );
	}
	
	public boolean isRoadGood(Pos p) {
		return isRoadGood(p.x,p.y);
	}

	
	public boolean isRoad(double i, double j){
		if( i<0 || j<0 || i>=sizeX*unitSize || j>=sizeY*unitSize ){
			return false;
		}
		
		int x = convX(i);
		int y = convX(j);
		
//		if( getPlanUnit(x,y)==null ){
//			return false;
//		}
		
		
		return getPlanUnit(x,y).isRoad();
	}
	
	public boolean isRoadGood(double i, double j){
		int x = PositionUtils.getIntX(i);
		int y = PositionUtils.getIntX(j);
		
		if( x<0 || y<0 || x>=sizeX || y>=sizeY ){
			return false;
		}
		
//		if( getPlanUnit(x,y)==null ){
//			return false;
//		}
		
		
		return getPlanUnit(x,y).isRoad();
	}

	public boolean isIntersection(Pos p) {
		int x = convX(p.x);
		int y = convY(p.y);
		int roadNeighboursX = 0;
		int roadNeighboursY = 0;
		for (int i = -1; i < 2; i += 2) {
			if (isRoad(createPos(x + i, y)))
				roadNeighboursX++;
			if (isRoad(createPos(x, y + i)))
				roadNeighboursY++;
		}
		return (roadNeighboursX > 0 && roadNeighboursY > 0);
	}
	
	public PlanItem getPlanUnit( Pos p ){
		return getPlanUnit( convX(p.x), convY(p.y) );
	}


	public PlanItem getPlanUnit( int i, int j ){
		if( i<0 || j<0 || i>=sizeX || j>=sizeY ){
			return null;
		}
		
		return units[i][j];
	}

	public PlanItem getPlanUnitWithValidPos( int i, int j ){
		if( i<0 || j<0 || i>=sizeX || j>=sizeY ){
			return null;
		}
		
		//units[i][j].setPos(createPos(i, j));
		
		return units[i][j];
	}

	public double getUnitSize() {
		return unitSize;
	}

	public Bot createBot( int x, int y ){
		double xx = (x+0.5)*unitSize;
		double yy = (y+0.5)*unitSize;
		return new Bot( xx, yy);
	}
	
	public Bot createBotDouble( double x, double y ){
		return new Bot( x, y);
	}
	
	public Pos createPos( int x, int y ){
		double xx = (x+0.5)*unitSize;
		double yy = (y+0.5)*unitSize;
		return new Pos( xx, yy);
	}

	public int getRow(Pos p) {
		int y = PositionUtils.getIntX(p.y);
		if (y < row1)
			return 0;
		if (y < homeRow && y >= row1)
			return 1;
		if (y < row3 && y >= homeRow)
			return 2;
		if (y < row4 && y >= row3)
			return 3;
		return BaseEnvironment.TRANSFER_DECK_ROWS + y - row4;
		
//		if (y == homeRow)
//			return 2;
//		int row = y / distanceBetweenAvenues;
//		if ((y - 1) / distanceBetweenAvenues < numberOfAvenues - 1) {// Transfer deck
//			if (y > homeRow)
//				return row + 1;
//			else
//				return row;
//		}
//		else
//			return (BaseEnvironment.TRANSFER_DECK_ROWS - 1) + y - distanceBetweenAvenues*(numberOfAvenues - 1);
	}

	public int getCol(Pos p) {
		return PositionUtils.getIntX(p.x) / distanceBetweenCols;
	}

	public Pos createPosFromNode(int row, int col) {
		List<PlanItem> roads = getRoads();
		for (PlanItem pi : roads) {
			Pos p = pi.getPos();
			if (getRow(p) == row && getCol(p) == col)
				return p;
		}
		return null;
	}

	public Pos getNearestNode(Pos p) {
		int row = getRow(p);
		int col = getCol(p);
		Pos nearest = createPosFromNode(row, col);
		double minDist = p.dist(nearest);
		if (nearest.equals(p))
			return p;
		Pos tmp;
		if (nearest.x == p.x) {
			tmp = createPosFromNode(row + 1, col);
		}
		else /* if (nearest.y == p.y) */ {
			tmp = createPosFromNode(row, col + 1);	
		}
		if (p.dist(tmp) < minDist)
			nearest = tmp;
		return nearest;
	}

	public Pos getSecondNearestNode(Pos p) {
		Pos nearest = getNearestNode(p);
		int nearestRow = getRow(nearest);
		int nearestCol = getCol(nearest);
		if (nearest.equals(p))
			return null;
		if (nearest.x == p.x) {
			if (nearest.y < p.y)
				return createPosFromNode(nearestRow + 1, nearestCol);
			else
				return createPosFromNode(nearestRow - 1, nearestCol);
		}
		else /* if (nearest.y == p.y) */ {
			if (nearest.x < p.x)
				return createPosFromNode(nearestRow, nearestCol + 1);
			else
				return createPosFromNode(nearestRow, nearestCol - 1);
		}
	}
	
	public boolean isHMPCType() {
		return HMPCType;
	}

}
