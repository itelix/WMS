/**
 * 
 */
package cma.store.control.opt.route.allshortestpaths;

import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.data.PosDirected;
import cma.store.data.PosDirected.Direction;
import cma.store.env.Environment;

/**
 * @author Filip
 *
 */
public class GraphDirectedNodeIterator extends GraphNodeIterator {
	PlanItem currentPi = null;
	int dirIdx;
	private Direction[] dirOrder = Direction.getDirections();
	
	public GraphDirectedNodeIterator(Environment env) {
		super(env);
		dirIdx = 0;
	}

	public PosDirected nextDirectedPos() {
		Pos pos = null;
		Direction direction;
		if (dirIdx == 0) {
			currentPi = next();
		}
		direction = dirOrder[dirIdx];
		dirIdx = (dirIdx + 1) % dirOrder.length;
		
		if (currentPi == null)
			return null;
		
		pos = currentPi.getPos();
		
		return new PosDirected(pos, direction); 
	}
	
	@Override
	public PlanItem next() {
		return super.next();
	}
}
