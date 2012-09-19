package cma.store.input.request;

import java.util.ArrayList;
import java.util.List;

import cma.store.env.Environment;
import cms.store.utils.Pair;


/**
Warehouse optimizer.
creating date: 2012-07-20
creating time: 21:53:36
autor: Czarek
 */

public class DefinedBaseRequestCreator implements BaseRequestCreator {
	
	List<Pair<Long,List<BaseRequest>>> requests;
	List<Pair<Long,List<BaseRequest>>> alreadyUsed;
	private Environment env;

	
	public DefinedBaseRequestCreator( Environment env ){
		this.env = env;
		
		requests = new ArrayList<Pair<Long,List<BaseRequest>>>();
		alreadyUsed = new ArrayList<Pair<Long,List<BaseRequest>>>();
	}
	
	public void addBaseRequestList( long time, List<BaseRequest> request ){
		requests.add( new Pair<Long,List<BaseRequest>>(time, request) );
	}
	public void addBaseRequestListAtBeginning( long time, List<BaseRequest> request ){
		List<BaseRequest> newRequestList= new ArrayList<BaseRequest>();
		newRequestList.addAll(request);
		
		for( int i=requests.size()-1; i>=0; i-- ){
			
			Pair<Long,List<BaseRequest>> p = requests.get(i);
			
			if( p.getT1()<=time ){
				requests.remove(i);
				newRequestList.addAll(p.getT2());
			}
		}
		requests.add( new Pair<Long,List<BaseRequest>>(time, newRequestList) );
	}
	
	@Override
	public boolean done() {
		return requests.size()==0; //never finished
	}
	
	

	public synchronized List<Pair<Long, List<BaseRequest>>> getRequestsWithoutDelete(
			List<Pair<Long, List<BaseRequest>>> requests) {
		return this.requests;
	}

	@Override
	public List<BaseRequest> getRequests() {
		long time = env.getTimeMs();
		List<BaseRequest> ret = new ArrayList<BaseRequest>();
		if( done() ) return ret;
		
		for( int i=requests.size()-1; i>=0; i-- ){
			
			Pair<Long,List<BaseRequest>> p = requests.get(i);
			
			if( p.getT1()<=time ){
				requests.remove(i);
				alreadyUsed.add(p);
				ret = p.getT2();
			}
		}
		
		return ret;
	}

	@Override
	public List<Pair<Long, List<BaseRequest>>> getRequestsWithoutDelete() {
		// TODO Auto-generated method stub
		return null;
	}

}
