package cma.store.data;

import java.util.ArrayList;
import java.util.List;

public class MvcFleet {
	
	private List<Mvc> mvcs;


	public MvcFleet(){
		mvcs = new ArrayList<Mvc>();
	}
	
	public void addMvc( Mvc c ){
		mvcs.add( c );
	}
	
	public void addMvcs( List<Mvc> mvcList ){
		mvcs.addAll( mvcList );
	}
	
	public List<Mvc> getMvcs(){
		return mvcs;
	}

	public List<Mvc> getOutputMvcs(){
		List<Mvc> ret = new ArrayList<Mvc>();
		for (Mvc m : mvcs) {
			if (m.getType() == MvcType.OUTPUT) {
				ret.add(m);
			}
		}
		return ret;
	}

	public List<Mvc> getInputMvcs(){
		List<Mvc> ret = new ArrayList<Mvc>();
		for (Mvc m : mvcs) {
			if (m.getType() == MvcType.INPUT) {
				ret.add(m);
			}
		}
		return ret;
	}
	
	public Mvc getMvc( MvcType type, int nr ){
		
		int count = 0;
		for( Mvc m: mvcs ){
			if( m.getType() == MvcType.INPUT ){
				if( count == nr ) return m;
				count++;
			}
		}
		return null;
	}


}
