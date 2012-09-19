package cma.store.control.opt.route.data;

import java.util.List;

import cma.store.data.Pos;
import cma.store.data.PosDirected;
import cma.store.utils.Conflict;

/**
Warehouse optimizer.
creating date: 2012-07-25
creating time: 22:01:35
autor: Czarek
 */

public class ShortRouteScore {
	public double score;
	public PosDirected prior;
	public long time;
	public Double priority;
	public List<Conflict> blockers;
	
	public ShortRouteScore(double score, PosDirected prior, long time, List<Conflict> blockers ) {
		super();
		this.score = score;
		this.prior = prior;
		this.time = time;
		this.blockers = blockers;
	}

	public Double getPriority() {
		return priority;
	}

	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	
	

}
