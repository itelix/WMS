package cma.store.input.request;

import java.util.List;

import cms.store.utils.Pair;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 17:59:16
autor: Czarek
 */

public interface BaseRequestCreator {
	
	List<BaseRequest> getRequests();
	List<Pair<Long, List<BaseRequest>>> getRequestsWithoutDelete();
	boolean done();
}
