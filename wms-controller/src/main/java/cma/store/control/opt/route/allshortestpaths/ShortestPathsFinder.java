package cma.store.control.opt.route.allshortestpaths;


import java.util.List;

import cma.store.data.Pos;

/**
Warehouse optimizer.
creating date: 30-07-2012
creating time: 03:44:32
autor: Filip
 */

public interface ShortestPathsFinder {
	public void computePaths();
	public List<Pos> getPath(Pos p1, Pos p2);
	// Alternative path
	public List<Pos> getCompressedPath(Pos p1, Pos p2);
	public double getPathLength(Pos p1, Pos p2);
	public String printPath(Pos p1, Pos p2);
}
