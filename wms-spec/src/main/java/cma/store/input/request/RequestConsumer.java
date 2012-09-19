package cma.store.input.request;

import cma.store.control.opt.data.Request;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 16:18:39
autor: Czarek
 */

public interface RequestConsumer {
	
	void addRequest( Request r );

}
