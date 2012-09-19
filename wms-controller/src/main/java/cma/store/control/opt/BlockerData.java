package cma.store.control.opt;

import java.util.LinkedList;

import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.schedule.Schedule;

public class BlockerData {
	
	Schedule schedule;
	Bot blocker;
	Duration startTimeRange;
	LinkedList<BlockerData> blockers;

	private BlockerData( Bot blocker, Duration startTimeRange, Schedule schedule ) {
		this.blocker = blocker;
		this.startTimeRange = startTimeRange;
		this.schedule = schedule;
		blockers = new LinkedList<BlockerData>();
	}
	
	public static BlockerData createBlockerData( Bot blocker, long blockingTime, Schedule schedule ){
		
		long minStartTime = schedule.getAvailableMinTime( blocker );
		if( minStartTime >= blockingTime ) return null; //can't remove
		
		Duration d = new Duration(minStartTime,blockingTime);
		return new BlockerData(blocker,d,schedule);
	}

	
	public void addSubBlocker( BlockerData subBlocker ){
		blockers.add(subBlocker);
	}

	public LinkedList<BlockerData> getBlockers() {
		return blockers;
	}
	
	
	
}
