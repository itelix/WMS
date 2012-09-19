package cma.store.stat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.schedule.ScheduleItem;
import cms.store.utils.Pair;
import cms.store.utils.PositionUtils;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 17:18:16
autor: Czarek
 */

public class Statistics {
	private static final String NL = System.getProperty("line.separator");

	private static final String SEP = ";";
	
	Environment env;
	Logger logger = Logger.getLogger(getClass());
	
	int count;
	double distance;
	double time;
	double priority;
	long minTime;
	long maxTime;
	int priorityCount;
	
	int toMVCCount;
	int toStorageCount;
	int escapeCount;
	double toMVCDistance;
	double toStorageDistance;
	double escapeDistance;
	double toMVCTime;
	double toStorageTime;
	double escapeTime;
	List<Double> times;
	int requestCount;
	
	Map<String,String> paramsHT;
	List<String> paramNames;
	
	public class routeSummary {
		double time;
		double startTime;
		double endTime;
		int bot;
		int route;
		RouteType routeType;
		
		public routeSummary() {
		}
		public routeSummary(double startTime, double endTime, int bot, int route, RouteType routeType) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.bot = bot;
			this.route = route;
			this.routeType = routeType;
			this.time = endTime - startTime;
		}
		
		public String getTableHeader() {
			return "route;bot;routeType;startTime;endTime;time" + NL;
		}
		
		public String toString() {
			return "" + route + SEP + bot + SEP + routeType + SEP + startTime + SEP + endTime + SEP + time + NL;
		}
	}
	
	List<routeSummary> routeSummaries;
	
	public class heatMap {
		int sizeX;
		int sizeY;
		int[][] heatmap;
		
		public heatMap() {
			sizeX = env.getLayerModel().getSizeX();
			sizeY = env.getLayerModel().getSizeY();
			heatmap = new int[sizeX][sizeY];
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					heatmap[i][j] = 0;
				}
			}
		}
		
		private void update(int i, int j) {
			heatmap[i][j] = heatmap[i][j] + 1;
		}

		public void update(Route route) {
			List<Pos> posList = (List<Pos>) route.getPos();
//			for (Pos p : posList) {
//				update(PositionUtils.getIntX(p.x), PositionUtils.getIntY(p.y));
//			}
		}
		
		public String toString() {
			String ret = "";
//			String ret = "1";
//			for (int j = 1; j < sizeY; j++) {
//				ret += SEP + j; 
//			}
			for (int i = 0; i < sizeX; i++) {
				ret += "" + heatmap[i][0];
				for (int j = 1; j < sizeY; j++) {
					ret += SEP + heatmap[i][j];
//					ret += SEP + heatmap.get(i).get(j);
				}
				ret += NL;
			}
			return ret;
		}
	}
	
	public class DirElem {
		int east, west, north, south;
		public DirElem(int east, int west, int north, int south) {
			this.east = east;
			this.west = west;
			this.north = north;
			this.south = south;
		}
		
		public void inc(DirElem de) {
			this.east += de.east;
			this.west += de.west;
			this.north += de.north;
			this.south += de.south;
		}
		
		public String toString(boolean horizontal) {
			String ret = "";
			
			if (horizontal) {
				if (north + south > 0)
					ret += 0;
				else
					ret += east - west;
			}
			else {
				if (east + west > 0)
					ret += 0;
				else
					ret += north - south;				
			}
			return ret;			
		}
		
		public String toString() {
			return toString(true);
		}
	}
	
	public class dirMap {
		int sizeX;
		int sizeY;
		DirElem[][] dirmap;
		
		public dirMap() {
			sizeX = env.getLayerModel().getSizeX();
			sizeY = env.getLayerModel().getSizeY();
			dirmap = new DirElem[sizeX][sizeY];
			for (int i = 0; i < sizeX; i++) {
				for (int j = 0; j < sizeY; j++) {
					DirElem p = new DirElem(0, 0, 0, 0);
					dirmap[i][j] = p;
				}
			}
		}
		
		private void update(Pos p, Pos prev) {
			int i = PositionUtils.getIntX(p.x);
			int j = PositionUtils.getIntY(p.y);
			if (prev == null)
				return;
			int east = 0;
			int west = 0;
			int north = 0;
			int south = 0;
			int xDiff = PositionUtils.getIntX(p.x) - PositionUtils.getIntX(prev.x);
			int yDiff = PositionUtils.getIntY(p.y) - PositionUtils.getIntY(prev.y);
			if (xDiff > 0)
				east = 1;
			else if (xDiff < 0)
				west = 1;
			if (yDiff > 0)
				north = 1;
			else if (yDiff < 0)
				south = 1;
			dirmap[i][j].inc(new DirElem(east, west, north, south));
		}

		public void update(Route route) {
			List<Pos> posList = (List<Pos>) route.getPos();
			Pos prev = null;
//			for (Pos p : posList) {
//				update(p, prev);
//				prev = p;
//			}
		}
		
		public String toString() {
			return toString(true);
		}
		
		public String toString(boolean horizontal) {
			String ret = "";
//			String ret = "1";
//			for (int j = 1; j < sizeY; j++) {
//				ret += SEP + j; 
//			}
			for (int i = 0; i < sizeX; i++) {
				ret += "" + dirmap[i][0].toString(horizontal);
				for (int j = 1; j < sizeY; j++) {
					ret += SEP + dirmap[i][j].toString(horizontal);
//					ret += SEP + heatmap.get(i).get(j);
				}
				ret += NL;
			}
			return ret;
		}
	}
	
	heatMap heatmap = null;
	dirMap dirmap = null;
	int wrongOrder;
	
	public Statistics(Environment env){
		this(env, false);
	}
	
	public Statistics(Environment env, boolean createHMap) {
		this.env = env;
		priority = 0;
		maxTime = 0;
		minTime = 0;
		priorityCount = 0;
		toMVCCount = 0;
		toStorageCount = 0;
		escapeCount = 0;
		toMVCDistance = 0;
		toStorageDistance = 0;
		escapeDistance = 0;
		toMVCTime = 0;
		toStorageTime = 0;
		escapeTime = 0;
		times = new ArrayList<Double>();
		
		routeSummaries = new ArrayList<Statistics.routeSummary>();
		requestCount = 0;
		
		if (createHMap) {
			heatmap = new heatMap();
			dirmap = new dirMap();
		}
	}

	public heatMap getHeatmap() {
		return heatmap;
	}
	
	public void update( ScheduleItem si ){
		
		logger.debug("update() " + si.getRoute().getRouteGroupId());
		count++;
		distance += si.getRoute().getTotalDistance();
		time += si.getRoute().getTotalTime();
		
		if( maxTime < si.getRoute().getFinalTime() ){
			maxTime = si.getRoute().getFinalTime();
		}
		
		if( si.getRoute().getPriority()!=null ){
			priority += si.getRoute().getPriority();
			priorityCount++;
		}	
		
		switch (si.getRoute().getType()) {
		case ROUTE_1:
			toStorageCount++;
			toStorageDistance += si.getRoute().getTotalDistance();
			toStorageTime += si.getRoute().getTotalTime();
			break;
		case ROUTE_2:
			toMVCCount++;
			toMVCDistance += si.getRoute().getTotalDistance();
			toMVCTime += si.getRoute().getTotalTime();
			break;
		case BLOCKER:
			escapeCount++;
			escapeDistance += si.getRoute().getTotalDistance();
			escapeTime += si.getRoute().getTotalTime();
			break;
		default:
			break;
		}
		
		times.add(si.getRoute().getTotalTime());
		routeSummary rs = new routeSummary(
				si.getRoute().getStartTime(),
				si.getRoute().getFinalTime(),
				si.getRoute().getBot().getId(),
				si.getRoute().getRouteGroupId(),
				si.getRoute().getType()
		);
		routeSummaries.add(rs);
		if (rs.routeType == RouteType.ROUTE_1) {
			requestCount++;
		}
		
		if (heatmap != null) {
			heatmap.update(si.getRoute());
			dirmap.update(si.getRoute());
		}
	}
	
	private void add( String par, String val ){
		paramNames.add( par );
		paramsHT.put( par, val );
	}
	
	public void recalulate(){
		paramsHT = new Hashtable<String,String>();
		paramNames = new ArrayList<String>();
		
		int c = Math.max(1, count);
		int cp = Math.max(1, priorityCount);
		int cMVC = Math.max(1, toMVCCount);
		int cStorage = Math.max(1, toStorageCount);
		int cEscape = Math.max(1, escapeCount);
		
		double throughtput = (requestCount*1000.0/(maxTime-minTime));
					
		add( "Total Duration[ms]",""+(maxTime-minTime) );
		add( "Avg priority",""+(priority/cp) );
		add( "Roads Count",""+count );
		add( "Total requests",""+requestCount );
		add( "Throughput",""+throughtput );
		add( "Avg distance",""+(distance/c) );
		add( "Avg time",""+((toMVCTime + toStorageTime)/cMVC) );
		add( "Avg time (with blockers)",""+(time/c) );
		add( "Roads to MVC Count",""+toMVCCount );
		add( "Roads to storage Count",""+toStorageCount );
		add( "Roads escape Count",""+escapeCount );
		add( "Avg to MVC distance",""+(toMVCDistance/cMVC) );
		add( "Avg to storage distance",""+(toStorageDistance/cStorage) );
		add( "Avg escape distance",""+(escapeDistance/cEscape) );
		add( "Avg to MVC time",""+(toMVCTime/cMVC) );
		add( "Avg to storage time",""+(toStorageTime/cStorage) );
		add( "Avg escape time",""+(escapeTime/cEscape) );
		add( "Num of wrong orders",""+wrongOrder );
	}
	
	public List<String> getParamiters(){
		if( paramNames==null ) recalulate();
		return paramNames;
	}
	
	public String getValue( String paramName ){
		return paramsHT.get(paramName);
	}

	public String toString(){
		recalulate();
		StringBuffer sb = new StringBuffer();
		for(String key: paramNames){
			sb.append( key + "=" + paramsHT.get(key) + NL);
		}
		
		return sb.toString();
	}

	public List<routeSummary> getRouteSummaries() {
		return routeSummaries;
	}

	public dirMap getDirmap() {
		return dirmap;
	}

	public void notifyWrongOrder() {
		wrongOrder++;
	}

}
