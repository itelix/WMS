/**
 * 
 */
package cma.store.data;

/**
 * @author Filip
 *
 */
public class PosDirected {

	public enum Direction {
	    NORTH(90), SOUTH(270), EAST(0), WEST(180);
	    int direction;
	    
	    Direction(int direction) {
	    	this.direction = direction;
	    }
	    
	    public int getValue() {
	    	return direction;
	    }

/*
 *      Not used at the moment
		public String toString() {
			return "" + getValue();
		}
 *
 */
	    public static Direction fromString(String s) {
	    	if (s.startsWith("NORTH"))
				return NORTH;
	    	if (s.startsWith("SOUTH"))
				return SOUTH;
	    	if (s.startsWith("EAST"))
				return EAST;
	    	if (s.startsWith("WEST"))
				return WEST;
			return null;
	    }

		public static Direction[] getDirections() {
			Direction[] ret = {NORTH, SOUTH, EAST, WEST};
			return ret;
		}
	};

	Pos pos;
	Direction direction;

	public PosDirected(Pos pos, Direction direction) {
		this.pos = pos;
		this.direction = direction;
	}

	public Pos getPos() {
		return pos;
	}

	public Direction getDirection() {
		return direction;
	}

	public String toString() {
		return pos.toString() + "|" + direction.toString();
	}
	
    @Override
    public int hashCode() {
        int hash = pos.hashCode();
        hash = hash * 43 + direction.getValue();
        return hash;
    }
	
	@Override
	public boolean equals(Object obj) {
		final PosDirected other = (PosDirected) obj;
		return pos.equals(other.getPos()) && direction == other.getDirection();
	}

}
