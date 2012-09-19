package cma.store.data;

import org.apache.log4j.Logger;

import cma.store.env.BaseEnvironment;
import cms.store.utils.PositionUtils;


/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 23:14:33
autor: Czarek
 */

public class Bot {
	Logger log = Logger.getLogger(getClass());
	
	private static int CAR_ID=0;
	Pos pos;
	Pos nextPos;
	public enum State {GOFORPRODUCT, GOTOMVC};
	State state = State.GOFORPRODUCT;
	
	//milimeters per miliseconds
	private double maxSpeed = BaseEnvironment.DEFAULT_MAX_BOT_SPEED_MM_PER_MS;
	
	//milimeters per miliseconds*miliseconds
	private double maxAcceleration = BaseEnvironment.DEFAULT_MAX_BOT_ACELERATION_MM_PER_MS;
	
	
	public Pos getNextPos() {
		return nextPos;
	}

	public void setNextPos(Pos nextPos) {
		this.nextPos = nextPos;
	}

	private int id;
	private boolean work;
	private boolean available = true;

	public Bot(double x, double y) {
		super();
		pos = new Pos(x,y);
		setId();
		log.debug(
			"Creating Bot("+getId()+ ") at position ("+ PositionUtils.getIntX(x)
			+ ", " + PositionUtils.getIntY(y) + ")"
		);
	}
	
	private synchronized void setId(){
		CAR_ID++;
		id = CAR_ID;
	}
	
	public synchronized int getId(){
		return id;
	}
	
	public Pos getPos(){
		return pos;
	}

	public double getX() {
		return pos.x;
	}

	public void setPos(Pos p) {
		this.pos = p;
	}

	public double getY() {
		return pos.y;
	}

	public void setState (State s) {
		this.state = s;
	}

	public State getState (State s) {
		return state;
	}

	/**
	 * 
	 * @return mm/ms
	 */
	public double getMaxSpeed(){
		return maxSpeed;
	}

	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	public String toString() {
		String s = "Bot(id: " + id + ", pos: " + pos + ")";
		return s;
	}

}
