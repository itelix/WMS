package cma.store.control.opt.route.allshortestpaths;


/**
Warehouse optimizer.
creating date: 30-07-2012
creating time: 05:20:31
autor: Filip
 */

public class ShortestPathsContext {
	private ShortestPathsFinder shortestPathsFinder;

	public ShortestPathsFinder getShortestPathsContext() {
		return shortestPathsFinder;
	}

	public void setShortestPathsContext(ShortestPathsFinder shortestPathsFinder) {
		this.shortestPathsFinder = shortestPathsFinder;
	}
	
	public ShortestPathsContext(ShortestPathsFinder shortestPathsFinder) {
		this.shortestPathsFinder = shortestPathsFinder;
	}
}
