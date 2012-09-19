package cma.store.control.opt.route.allshortestpaths;

import java.util.Iterator;

import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.data.LayerModel.AVENUE_TYPE;
import cma.store.env.Environment;
import cms.store.utils.PositionUtils;

/**
Warehouse optimizer.
creating date: 30-07-2012
creating time: 06:13:51
autor: Filip
 * @param <T>
 * @param <T>
 */

public class Graph<T> implements Iterable<T> {
	
	Environment env;
	Pos excludedPos;
	boolean isDirected;
	
	public Graph(Environment env) {
		this(env, false);
	}
	
	public Graph(Environment env, boolean isDirected) {
		this.env = env;
		this.isDirected = isDirected;
	}
	
	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) new GraphIterator(env);
	}

	protected boolean areNeighbours(PlanItem pi1, PlanItem pi2) {
		Pos p1 = pi1.getPos();
		Pos p2 = pi2.getPos();

		if (Math.abs(PositionUtils.getIntX(p2.x) - PositionUtils.getIntX(p1.x)) +
				Math.abs(PositionUtils.getIntY(p2.y) - PositionUtils.getIntY(p1.y)) < 2 &&
				pi1.isRoad() && pi2.isRoad() && !p1.equals(p2)) {
//			if (isDirected) {
			if (env.getLayerModel().getAvenueType(p1) != AVENUE_TYPE.BOTH) {
				int street = env.getLayerModel().getRow(p1); //PositionUtils.getIntY(p1.y)/streetsDist;
				if (PositionUtils.getIntY(p1.y) == PositionUtils.getIntY(p2.y)) {
					// horizontal: street 0 -- EAST; street 1 -- EAST; street 2 -- WEST
					if (street < 2) { // EAST
						if (PositionUtils.getIntY(p1.x) < PositionUtils.getIntY(p2.x))
							return true;
					}
					else {	// WEST
						if (PositionUtils.getIntY(p1.x) > PositionUtils.getIntY(p2.x))
							return true;
					}
					return false;
				}
				if (PositionUtils.getIntY(p1.x) == PositionUtils.getIntY(p2.x)) {
					// vertical: exclude segment between two lower streets and two MVCs
//					int alleysDist = env.getLayerModel().getDistanceBetweenCols();
					int alleysNum = env.getLayerModel().getColsNum();
					int alley = env.getLayerModel().getCol(p1); //PositionUtils.getIntY(p1.x)/alleysDist;
					if ((alley % 4 == 1 || alley % 4 == 2) && alleysNum > 2) {
						if (PositionUtils.getIntY(p2.y) < PositionUtils.getIntY(p1.y))
							street = env.getLayerModel().getRow(p2); //PositionUtils.getIntY(p2.y)/streetsDist;
						if (street == 0)
							return false;
						else
							return true;
					}
				}
			}
			return true;
		}
		return false;
	}
	
}
