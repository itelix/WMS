package cma.store.control.opt.route.allshortestpaths;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.Environment;
import cma.store.serialization.FileSerializer;
import cma.store.serialization.IFileSerializer;

/**
Warehouse optimizer.
creating date: 30-07-2012
creating time: 03:41:12
Author: Filip
 */

public class FloydWarshallAlgorithm implements ShortestPathsFinder {
	protected final static String FILE_PREFIX = "FloydWarshallAlgorithm";
	private static final String DIRECTED_PREFIX = "Directed";
	private final static String FILE_PREFIX_PRED = FILE_PREFIX + "Pred";
	private final static String FILE_PREFIX_D = FILE_PREFIX + "D";
	private static FloydWarshallAlgorithm instance = null;
	Logger logger = Logger.getLogger(FILE_PREFIX);
	Environment env;
	List<Pos> excludedPosSet;

	protected HashMap<Pos,HashMap<Pos,Pos>> pred;
	protected HashMap<Pos,HashMap<Pos,Double>> d;
	Graph graph;
	Pos unidentifiedPos;
	Double unidentifiedDistance;
	protected String fileNamePred, fileNameD;
	
	public FloydWarshallAlgorithm(Environment env) {
		this(env, null);

//		init();// we don't need this. Done in computePaths -- TODO
//		runTest();
	}
	
	public String getExcludedPosHash() {
		String ret = "";
		for (Pos pos : excludedPosSet) {
			ret += "__" + pos.x + "_" + pos.y;
		}
		return ret;
	}
	
	public FloydWarshallAlgorithm(Environment env, List<Pos> excludedPosSet) {
		this.env = env;
		this.excludedPosSet = excludedPosSet;
		if (excludedPosSet == null)
			this.excludedPosSet = new ArrayList<Pos>();
		this.graph = new Graph(env);
		String suffix = env.getLayerModel().getHashString() + getExcludedPosHash();
		this.fileNamePred = FILE_PREFIX_PRED + suffix;
		this.fileNameD = FILE_PREFIX_D + suffix;
		if (env.getLayerModel().isDirectedLayout()) {
			fileNamePred = DIRECTED_PREFIX + fileNamePred;
			fileNameD = DIRECTED_PREFIX + fileNameD;
		}
		
		unidentifiedDistance = new Double(Double.MAX_VALUE);
		unidentifiedPos = env.createPos(env.getLayerModel().getSizeX() + 1,
				env.getLayerModel().getSizeY() + 1);

//		runTest();
	}
	
	 public static FloydWarshallAlgorithm getInstance(Environment env) {
	      if(instance  == null) {
	         instance = new FloydWarshallAlgorithm(env);
	         instance.computePaths();
	      }
	      return instance;
	}

	protected void init() {
		HashMap<Pos, Pos> predMap = null;
		HashMap<Pos, Double> dMap;
		pred = new HashMap<Pos, HashMap<Pos, Pos>>();
		d = new HashMap<Pos, HashMap<Pos, Double>>();
			
		Iterator<PlanItem> v1 = graph.iterator();
		Iterator<PlanItem> v2 = graph.iterator();
			
		PlanItem pi1;
		while ((pi1 = v1.next()) != null) {
			Pos p1 = pi1.getPos();
			predMap = new HashMap<Pos, Pos>();
			dMap = new HashMap<Pos, Double>();
			PlanItem pi2;
			while ((pi2 = v2.next()) != null) {
				Pos p2 = pi2.getPos();
				if (graph.areNeighbours(pi1, pi2)) {
					// Neighbor position
					if (excludedPosSet.size() > 0 &&
							(excludedPosSet.contains(p1) || excludedPosSet.contains(p2))) {
						predMap.put(p2, unidentifiedPos);
						dMap.put(p2, unidentifiedDistance);
					} else {
						predMap.put(p2, p1);
						dMap.put(p2, new Double(1));
					}
				} else {
					predMap.put(p2, unidentifiedPos);
					dMap.put(p2, new Double(unidentifiedDistance));
				}
				
				if (p1.equals(p2)) {
					dMap.put(p1, new Double(0));
					if (excludedPosSet.size() > 0 && excludedPosSet.contains(p1)) {
						dMap.put(p1, unidentifiedDistance);
					}
				}
			}
			pred.put(p1, predMap);
			d.put(p1, dMap);
			v2 = graph.iterator();
		}
			
	}
	
	private void printPaths() {
		HashMap<Pos, Pos> predMap;
		HashMap<Pos, Double> dMap = null;
		
		Iterator<PlanItem> v1 = graph.iterator();
		Iterator<PlanItem> v2 = graph.iterator();
		
		while (v1.hasNext()) {
			PlanItem pi1 = v1.next();
			while (v2.hasNext()) {
				PlanItem pi2 = v2.next();
				Pos p1 = pi1.getPos();
				Pos p2 = pi2.getPos();

				dMap = d.get(p1);
				predMap = pred.get(p1);
				
				logger.debug("(" + p1 + ", " + p2 + ") = " + predMap);
				
			}
			v2 = graph.iterator();
		}
		
	}
	
	@Override
	public String printPath(Pos p1, Pos p2) {
		return "" + getPath(p1, p2);
	}
	
	@Override
	public List<Pos> getPath(Pos p1, Pos p2) {
		List<Pos> path = new ArrayList<Pos>();
		if (excludedPosSet.size() > 0 &&
				(excludedPosSet.contains(p1) || excludedPosSet.contains(p2))) {
			// can't happen in algorithm, only if you insert excludedPos manually
			logger.debug("From or To position is in excludedPosSet -- from: " + p1 + " to: " + p2 + " excludePosSet: " + excludedPosSet);
			return null;
		}
		if (!env.getLayerModel().isValidPos(p1) || !env.getLayerModel().isValidPos(p2)) {
			logger.error("Can't find route. Position out of bounds from: " + p1 + " to: " + p2);
			return null;
		}
			
		HashMap<Pos, Double> dMap = d.get(p1);

		if (/*dMap.get(p2) == null || */dMap.get(p2) == Double.MAX_VALUE) {
			logger.debug("NULL Thrown in getPath() for p1: " + p1 + " p2: " + p2);
			return null;
		}
		if (p1.equals(p2)) {
			// empty path
			path.add(p2);
			return path;
		}
		Pos intermediate = pred.get(p1).get(p2);
		if (intermediate.equals(unidentifiedPos)) {
			return path;
		}
		else {
			path = getPath(p1, intermediate);
			if (path != null) path.add(p2);
			return path;
		}
	}
	
	@Override
	public List<Pos> getCompressedPath(Pos p1, Pos p2) {
		List<Pos> wholePath = getPath(p1, p2);
		List<Pos> compressedPath = new ArrayList<Pos>();
		if (wholePath == null) return null;
		compressedPath.add(wholePath.get(0));
		
		if (wholePath.size() > 1) {
			Pos current = wholePath.get(1);
			Pos endSegment = wholePath.get(0);
			boolean horizontalSegment = true;
			if (current.x == endSegment.x)
				horizontalSegment = false;
			boolean directionChanged = false;
			for (int i = 1; i < wholePath.size(); i++) {
				current = wholePath.get(i);
				if (current.x == endSegment.x) { // vertical segment
					if (horizontalSegment == true)
						directionChanged = true;
					horizontalSegment = false;
				} else { // horizontal
					if (horizontalSegment == false)
						directionChanged = true;
					horizontalSegment = true;
				}
				if (directionChanged) {
					compressedPath.add(endSegment);
					directionChanged = false;
				}
				endSegment = current;
			}
			if (!compressedPath.get(compressedPath.size() - 1).equals(endSegment))
				compressedPath.add(endSegment);
		}
		return compressedPath;
	}
	
	
	protected void writePathsToFile() {
		IFileSerializer<HashMap<Pos,HashMap<Pos,Pos>>> predSerializer =
				new FileSerializer<HashMap<Pos,HashMap<Pos,Pos>>>();
		IFileSerializer<HashMap<Pos,HashMap<Pos,Double>>> dSerializer =
				new FileSerializer<HashMap<Pos,HashMap<Pos,Double>>>();
		try {
			predSerializer.serialize(pred, fileNamePred);
			dSerializer.serialize(d, fileNameD);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("writePathsToFile error");
			e.printStackTrace();
		}
	}
	
	protected boolean readPathsFromFile() {
		boolean readSucceed = true;
		IFileSerializer<HashMap<Pos,HashMap<Pos,Pos>>> predSerializer =
				new FileSerializer<HashMap<Pos,HashMap<Pos,Pos>>>();
		IFileSerializer<HashMap<Pos,HashMap<Pos,Double>>> dSerializer =
				new FileSerializer<HashMap<Pos,HashMap<Pos,Double>>>();
		try {
			pred = predSerializer.deserialize(fileNamePred);
			d = dSerializer.deserialize(fileNameD);
		} catch (IOException e) {
			// e.printStackTrace();
			logger.error("readPathsFromFile error: IOException (probably there is no file with serialized paths)");
			readSucceed = false;
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
			logger.error("readPathsFromFile error: ClassNotFoundException");
			readSucceed = false;
		}
		return readSucceed;
	}

	
	@Override
	public void computePaths() {
		if (excludedPosSet.isEmpty()) {
			logger.info("Computing all shortest paths for plain graph");
		} else {
			logger.info("Computing all shortest paths for graph with excluded position set " + excludedPosSet);
		}

		// try to read paths from file
		if (readPathsFromFile()) {
			logger.debug("Paths read from file. Computing skipped");
			return;
		}

		init();
		HashMap<Pos, Pos> predV1Map;
		HashMap<Pos, Double> dV1Map;
		HashMap<Pos, Pos> predUMap;
		HashMap<Pos, Double> dUMap;
		
		double dist_p1_p2;
		double dist_p1_pu;
		double dist_pu_p2;
		
		Iterator<PlanItem> v1 = graph.iterator();
		Iterator<PlanItem> v2 = graph.iterator();
		Iterator<PlanItem> u = graph.iterator();
		
		PlanItem piu;
		while ((piu = u.next()) != null) {
			Pos pu = piu.getPos();
			predUMap = pred.get(pu);
			dUMap = d.get(pu);
			
			PlanItem pi1;
			while ((pi1 = v1.next()) != null) {
				Pos p1 = pi1.getPos();
				predV1Map = pred.get(p1);
				dV1Map = d.get(p1);
				PlanItem pi2;
				while ((pi2 = v2.next()) != null) {
					Pos p2 = pi2.getPos();

					dist_p1_p2 = dV1Map.get(p2);
					dist_p1_pu = dV1Map.get(pu);
					dist_pu_p2 = dUMap.get(p2);
					
					if (dist_p1_p2 > dist_p1_pu + dist_pu_p2) {
						dV1Map.put(p2, dist_p1_pu + dist_pu_p2);
						predV1Map.put(p2, predUMap.get(p2));
					}

				}
				pred.put(p1, predV1Map);
				d.put(p1, dV1Map);
				
				v2 = graph.iterator();
			}
			v1 = graph.iterator();
		}
		// save results to file
		writePathsToFile();
	}

	@Override
	public double getPathLength(Pos p1, Pos p2) {
		if (d.get(p1) == null || d.get(p1).get(p2) == null)
			return unidentifiedDistance;
		return d.get(p1).get(p2);
	}

}
