package cms.store.utils;

/**
Warehouse optimizer.
creating date: 2012-07-20
creating time: 21:57:57
autor: Czarek
 */

public class Pair<T1, T2> {
	T1 t1;
	T2 t2;
	
	public T1 getT1() {
		return t1;
	}

	public T2 getT2() {
		return t2;
	}
	
	public void setT1(T1 t1) {
		this.t1 = t1;
	}

	public void setT2(T2 t2) {
		this.t2 = t2;
	}

	public Pair(T1 t1, T2 t2) {
		super();
		this.t1 = t1;
		this.t2 = t2;
	};
	
	public String toString(){
		return ""+t1+" : "+t2;
	}
	
	
	

}
