package cma.store.control.opt.route.allshortestpaths;

import java.util.Iterator;

import org.apache.log4j.Logger;

import cma.store.data.PlanItem;
import cma.store.env.Environment;

/**
Warehouse optimizer.
creating date: 30-07-2012
creating time: 06:09:55
autor: Filip
 * @param <T>
 */


public class GraphIterator<T> implements Iterator<T> {

	Environment env;
	int i;
	int j;
	int sizeX;
	int sizeY;
	Logger logger;
	
	public GraphIterator(Environment env) {
		i = 0;
		j = -1;
		this.env = env;
		sizeX = env.getLayerModel().getSizeX();
		sizeY = env.getLayerModel().getSizeY();
		logger = Logger.getLogger(getClass());
	}
	
	@Override
	public boolean hasNext() {
		//Out of date; it isn't used now
		return false;
	}
	
	protected boolean allItemsProcessed() {
		if (j + 1 < sizeY || i + 1 < sizeX)
			return true;
		return false;
	}

	@Override
	public T next() {
		PlanItem pi = null;
		if (allItemsProcessed()) {
			if (j == sizeY - 1) {
				i = i + 1;
			}
			j = (j + 1) % sizeY;
			pi = env.getLayerModel().getPlanUnitWithValidPos(i, j);
			if (!pi.isRoad()) return next();
			//if (excludedPos != null && pi.getPos().equals(excludedPos)) return next();
		}
		//logger.debug("Next -> i: " + i + " j: " + j);
		return (T) pi;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
}




