package cma.store.data;


public class Mvc {
	
	private static final long TIME_TO_STAY_IN_MVC = 3000;
	private static final long TIME_BETWEEN_SHELLS = 2000;
	
	private long timeToStayInMvc   = TIME_TO_STAY_IN_MVC;
	private long timeBetweenShells = TIME_BETWEEN_SHELLS;
	
	private MvcType type; 
	private Pos pos;
	private long firstAvailableTime = 0;
	
	public Mvc( Pos pos, MvcType type ){
		this.pos = pos;
		this.type = type;
	}

	public String toString(){
		return ""+pos + " type="+type;
	}

	public MvcType getType() {
		return type;
	}

	public Pos getPos() {
		return pos;
	}

	public long getTimeToStayInMvc() {
		return timeToStayInMvc;
	}

	public long getTimeBetweenShells() {
		return timeBetweenShells;
	}

	public long getFirstAvailableTime() {
		return firstAvailableTime;
	}

	

}
