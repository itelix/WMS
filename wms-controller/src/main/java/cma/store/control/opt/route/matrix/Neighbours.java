package cma.store.control.opt.route.matrix;

import cma.store.data.Pos;
import cma.store.data.PosTime;

/**
Warehouse optimizer.
creating date: 2012-05-27
creating time: 13:28:33
autor: Czarek
 */

public class Neighbours {
	private static PosTime[] neighboursBefore=null;
	private static Pos[] neighbours=null;
	
	public static PosTime[] getClosestNeighboursBefore(){
		if( neighboursBefore==null ){
			neighboursBefore = new PosTime[5];
			neighboursBefore[0] = new PosTime(-1,0,-1);
			neighboursBefore[1] = new PosTime(1,0,-1);
			neighboursBefore[2] = new PosTime(0,-1,-1);
			neighboursBefore[3] = new PosTime(0,1,-1);
			neighboursBefore[4] = new PosTime(0,0,-1);
		}
		return neighboursBefore;
	}
	
	public static Pos[] getClosestNeighbours(){
		if( neighbours==null ){
			neighbours = new PosTime[4];
			neighbours[0] = new Pos(-1,0);
			neighbours[1] = new Pos(1,0);
			neighbours[2] = new Pos(0,-1);
			neighbours[3] = new Pos(0,1);
		}
		return neighbours;
	}

}
