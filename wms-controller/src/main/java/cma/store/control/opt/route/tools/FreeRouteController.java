package cma.store.control.opt.route.tools;

import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.PosTime;
import cma.store.utils.Conflict;

public interface FreeRouteController {
	
	boolean isRoute( Bot bot, Pos pos, Long time);
	
	List<Conflict> isRouteFree( Bot bot, Pos pos, Long time, boolean careStable );
	
//	Conflict isRoutFree( Bot bot, PosTime posTime );
	
	boolean isDestination( Bot bot, Pos pos, Long time);
	
//	boolean isDestination( Bot bot, PosTime posTime );
	
	Double getPriority();
	

}
