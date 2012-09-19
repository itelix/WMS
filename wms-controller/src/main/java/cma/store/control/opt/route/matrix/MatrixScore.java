package cma.store.control.opt.route.matrix;

import cma.store.data.PosTime;

/**
Warehouse optimizer.
creating date: 2012-05-23
creating time: 23:28:00
autor: Czarek
 */

public class MatrixScore {
	public double cost;
	public long time;
	public MatrixScore from;
	public PosTime pos;
	
	public MatrixScore( PosTime pos, double cost, long time, MatrixScore from ){
		this.pos = pos;
		this.cost = cost;
		this.time = time;
		this.from = from;
	}

}
