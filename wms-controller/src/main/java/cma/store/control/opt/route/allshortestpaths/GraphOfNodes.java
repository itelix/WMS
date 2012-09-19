/**
 * 
 */
package cma.store.control.opt.route.allshortestpaths;

import java.util.Iterator;

import org.lwjgl.Sys;

import cma.store.data.LayerModel;
import cma.store.data.LayerModelFactory;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.data.LayerModel.AVENUE_TYPE;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;

/**
 * @author Filip
 *
 */
public class GraphOfNodes extends Graph<PlanItem> {

	public GraphOfNodes(Environment env) {
		super(env);
	}

	@Override
	public Iterator<PlanItem> iterator() {
		return new GraphNodeIterator(env);
	}
	
	public boolean areNeighbours(PlanItem pi1, PlanItem pi2) {
		Pos p1 = pi1.getPos();
		Pos p2 = pi2.getPos();
		
		if (!pi1.isNode() || !pi2.isNode() || p1.equals(p2)) {
			return false;
		}
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		
		if (Math.abs(row1 - row2) + Math.abs(col1 - col2) > 1)
			return false;
		
		if (col1 != col2) {
			if (row1 == m.getHomeRow())
				return false;
			if (row1 >= BaseEnvironment.TRANSFER_DECK_ROWS)
				return false;
			if (m.getAvenueType(p1) != AVENUE_TYPE.BOTH) {
				// horizontal: street 0 -- EAST; street 1 -- EAST; street 2 -- WEST
				if (row1 < m.getHomeRow()) { // EAST
					if (col1 < col2)
						return true;
				}
				else {	// WEST
					if (col1 > col2)
						return true;
				}
				return false;
			}
			return true;
		}
		
		if (row1 != row2 && env.getLayerModel().isHMPCType()) {
			int street = row1;
			// vertical: exclude segment between two lower rows and two MVCs
			int colsNum = env.getLayerModel().getColsNum();
			if ((col1 % 4 == 1 || col1 % 4 == 2) && colsNum > 2) {
				if (row2 < row1)
					street = row2;
				if (street == 0)
					return false;
				else
					return true;
			}
		}
		
		return true;
	}
}
