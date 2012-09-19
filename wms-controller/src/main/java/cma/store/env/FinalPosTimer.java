package cma.store.env;

public class FinalPosTimer implements Timer {

	@Override
	public long getTimeMs() {
		return Long.MAX_VALUE;
	}

}
