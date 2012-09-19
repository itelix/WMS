package cma.store.data;

import java.io.Serializable;

import cms.store.utils.PositionUtils;



/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 22:10:30
autor: Czarek
 */

public class Pos implements Comparable<Pos>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3005859981573281499L;
	public double x;
	public double y;
	
	public Pos( Pos p ){
		this(p.x,p.y);
	}
		
	public Pos( double x, double y ){
		this.x = x;
		this.y = y;
	}
	
	public double dist( Pos p ){
		return Math.abs( x-p.x ) + Math.abs( y-p.y);
	}
	
	public String toString(){
//		return ""+x+":"+y;
		return ""+PositionUtils.getIntX(x)+":"+PositionUtils.getIntY(y);
	}
	
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + (int)x;
        hash = hash * 31 + (int)y;
        return hash;
    }

	@Override
	public boolean equals(Object obj) {
		final Pos other = (Pos) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(Pos other) {
		if (x > other.x || (x == other.x && y > other.y))
			return 1;
		else if (equals(other))
			return 0;
		else
			return -1;
	}

	public Pos max(Pos other) {
		if (compareTo(other) >= 0)
			return this;
		else
			return other;
	}

	public Pos min(Pos other) {
		if (compareTo(other) <= 0)
			return this;
		else
			return other;
	}
	
	
}
