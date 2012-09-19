package cma.store.control;

import cma.store.data.PosTime;

/**
Warehouse optimizer.
creating date: 2012-05-27
creating time: 13:35:14
autor: Czarek
 */

public interface PenaltyInfoIfc {
	double getPenalty( double x, double y, long time );
	double getPenalty( PosTime pt );


}
