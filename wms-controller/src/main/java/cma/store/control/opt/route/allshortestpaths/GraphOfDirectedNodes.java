/**
 * 
 */
package cma.store.control.opt.route.allshortestpaths;

import java.util.Iterator;

import cma.store.data.LayerModel;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.data.LayerModel.AVENUE_TYPE;
import cma.store.data.PosDirected;
import cma.store.data.PosDirected.Direction;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;

/**
 * @author Filip
 *
 */
public class GraphOfDirectedNodes extends GraphOfNodes {

	public GraphOfDirectedNodes(Environment env) {
		super(env);
	}
	
	@Override
	public GraphDirectedNodeIterator iterator() {
		return new GraphDirectedNodeIterator(env);
	}
	
	public boolean areNeighbours(PosDirected pd1, PosDirected pd2) {
		Pos p1 = pd1.getPos();
		Pos p2 = pd2.getPos();
		Direction d1 = pd1.getDirection();
		Direction d2 = pd2.getDirection();
		LayerModel m = env.getLayerModel();
		PlanItem pi1 = m.getPlanUnit(p1);
		PlanItem pi2 = m.getPlanUnit(p2);
		
		if (p1.equals(p2) && d1 != d2) {
			// check if p1 is intersection
//			return m.isIntersection(p1);
			return true;
		}
		
		if (!areNeighbours(pi1, pi2))
			return false;
		
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		
		if (row1 == row2) {
			if ((d1 == Direction.WEST && d2 == Direction.WEST && col1 > col2)
				|| (d1 == Direction.EAST && d2 == Direction.EAST && col1 < col2))
				return true;
			return false;
		}
		if (col1 == col2) {
			if ((d1 == Direction.SOUTH && d2 == Direction.SOUTH && row1 > row2)
				|| (d1 == Direction.NORTH && d2 == Direction.NORTH && row1 < row2))
				return true;
			return false;
		}
		return false;
		
	}

}
