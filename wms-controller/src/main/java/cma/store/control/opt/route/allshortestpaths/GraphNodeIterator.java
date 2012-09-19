/**
 * 
 */
package cma.store.control.opt.route.allshortestpaths;

import cma.store.data.PlanItem;
import cma.store.env.Environment;

/**
 * @author Filip
 *
 */
public class GraphNodeIterator extends GraphIterator<PlanItem> {

	public GraphNodeIterator(Environment env) {
		super(env);
	}

	@Override
	public PlanItem next() {
		PlanItem pi = null;
		if (allItemsProcessed()) {
			if (j == sizeY - 1) {
				i = i + 1;
			}
			j = (j + 1) % sizeY;
			pi = env.getLayerModel().getPlanUnitWithValidPos(i, j);
			if (!pi.isNode()) return next();
		}
		//logger.debug("Next -> i: " + i + " j: " + j);
		return pi;
	}
}
