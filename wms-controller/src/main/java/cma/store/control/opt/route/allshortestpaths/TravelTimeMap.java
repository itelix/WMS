/**
 * 
 */
package cma.store.control.opt.route.allshortestpaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import cma.store.data.LayerModel;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.data.PosDirected;
import cma.store.data.PosDirected.Direction;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.serialization.FileSerializer;

/**
 * @author Filip
 *
 */
public class TravelTimeMap extends FloydWarshallAlgorithm {
	private final static String FILE_PREFIX = "TravelTimeMap";
	private static final String DIRECTED_PREFIX = "Directed";
	private final static String FILE_PREFIX_PRED = FILE_PREFIX + "Pred";
	private final static String FILE_PREFIX_D = FILE_PREFIX + "D";
	private static final String SEP = " ";
	private static final String NL = "\n";
	private static TravelTimeMap instance = null;
	
	///
	HashMap<PosDirected,HashMap<PosDirected,PosDirected>> pred;
	HashMap<PosDirected,HashMap<PosDirected,Double>> d;
	///
	
	GraphOfDirectedNodes graph;
	Graph extendedGraph; // graph with all nodes (all positions from layout)
	private Properties properties;
	private PosDirected undefinedPosDirection;
	private String suffix;
	
	public TravelTimeMap(Environment env) {
		super(env);
		this.graph = new GraphOfDirectedNodes(env);
		this.extendedGraph = new Graph<PlanItem>(env);
		this.undefinedPosDirection = new PosDirected(unidentifiedPos, null);
		suffix = "";
		if (env.getLayerModel().isDirectedLayout()) {
			suffix = DIRECTED_PREFIX;
		}
		suffix += env.getLayerModel().getHashString();
	}

	private double distBetweenNeighbors(Pos p1, Pos p2) {
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		if (row1 == row2) {		// columns movement -- horizontal
			if (row1 == 0) { 	// vestibule
				return BaseEnvironment.TIME_VESTIBULE_UNIT;
			}
			else {	// row 1 or row 3
				return BaseEnvironment.TIME_COL_UNIT;
			}
		}
		else if (col1 == col2) {	// rows movement -- vertical
			switch (Math.min(row1, row2)) {
			case 0:
				return BaseEnvironment.TIME_ROW0_ROW1;
			case 1:
				return BaseEnvironment.TIME_ROW1_ROW2;
			case 2:
				return BaseEnvironment.TIME_ROW2_ROW3;
			case 3:
				return BaseEnvironment.TIME_ROW3_ROW4;
			default:
				return BaseEnvironment.TIME_STORAGE;
			}
		}
		return -1;
	}
	
	protected void init() {
		HashMap<PosDirected, PosDirected> predMap = null;
		HashMap<PosDirected, Double> dMap;
		pred = new HashMap<PosDirected, HashMap<PosDirected, PosDirected>>();
		d = new HashMap<PosDirected, HashMap<PosDirected, Double>>();

		GraphDirectedNodeIterator v1 = graph.iterator();
		GraphDirectedNodeIterator v2 = graph.iterator();

		PosDirected pd1;
		PosDirected pd2;
		while ((pd1 = v1.nextDirectedPos()) != null) {
			Pos p1 = pd1.getPos();
			predMap = new HashMap<PosDirected, PosDirected>();
			dMap = new HashMap<PosDirected, Double>();
			while ((pd2 = v2.nextDirectedPos()) != null) {
				Pos p2 = pd2.getPos();
				if (graph.areNeighbours(pd1, pd2)) {
					// Neighbor position
					predMap.put(pd2, pd1);
					double dist;
					if (!p1.equals(p2))
						dist = distBetweenNeighbors(p1, p2);
					else {
						dist = getRotationTime(pd1, pd2);
					}
					dMap.put(pd2, dist);
				} else {
					predMap.put(pd2, undefinedPosDirection);
					dMap.put(pd2, new Double(unidentifiedDistance));
				}
				
				if (p1.equals(p2)) {
					predMap.put(pd1, pd1);
					dMap.put(pd1, new Double(0));
				}
			}
			pred.put(pd1, predMap);
			d.put(pd1, dMap);
			v2 = graph.iterator();
		}

	}
	

	public double get90DegreeRotationTime(PosDirected pd1, PosDirected pd2) {
		double rot = getRotationTime(pd1, pd2);
		if (rot == BaseEnvironment.ROTATION_180_DEGREE)
			return 0;
		return rot;
	}

	public double getRotationTime(PosDirected pd1, PosDirected pd2) {	
		if (pd1.equals(undefinedPosDirection) || pd2.equals(undefinedPosDirection))
			return 0;
		if (pd1.getDirection() == null || pd2.getDirection() == null)
			return 0;

		int angle = pd1.getDirection().getValue() - pd2.getDirection().getValue();
		switch (angle) {
		case 90: case -270:
			return BaseEnvironment.ROTATION_90_DEGREE_CLOCKWISE;
		case -90: case 270:
			return BaseEnvironment.ROTATION_90_DEGREE_COUNTER_CLOCKWISE;
		case 180: case -180:
			return BaseEnvironment.ROTATION_180_DEGREE;
		default:
			return 0;
		}
	}
	
	private PosDirected nextPosOnPath(PosDirected startPos, PosDirected endPos) {
		PosDirected pos = endPos;
		HashMap<PosDirected, PosDirected> predMap = pred.get(startPos);
		while (!(predMap.get(pos)).equals(startPos)) {
			pos = predMap.get(pos);
			if (pos.equals(unidentifiedPos))
				return endPos;
		}
		return pos;
	}
	
	@Override
	public void computePaths() {
		logger.info("Computing all shortest paths for plain graph");

		// try to read paths from file
		if (readPathsFromFile()) {
			logger.debug("Paths read from file. Computing skipped");
			return;
		}

		init();
		HashMap<PosDirected, PosDirected> predV1Map;
		HashMap<PosDirected, Double> dV1Map;
		HashMap<PosDirected, PosDirected> predUMap;
		HashMap<PosDirected, Double> dUMap;
		
		double dist_p1_p2;
		double dist_p1_pu;
		double dist_pu_p2;

		GraphDirectedNodeIterator v1 = graph.iterator();
		GraphDirectedNodeIterator v2 = graph.iterator();
		GraphDirectedNodeIterator u = graph.iterator();
		
		PosDirected pdu;
		while ((pdu = u.nextDirectedPos()) != null) {
			predUMap = pred.get(pdu);
			dUMap = d.get(pdu);
			
			PosDirected pd1;
			while ((pd1 = v1.nextDirectedPos()) != null) {
				predV1Map = pred.get(pd1);
				dV1Map = d.get(pd1);
				PosDirected pd2;
				while ((pd2 = v2.nextDirectedPos()) != null) {					
					dist_p1_p2 = dV1Map.get(pd2);
					dist_p1_pu = dV1Map.get(pdu);
					dist_pu_p2 = dUMap.get(pd2);

					if (dist_p1_p2 > dist_p1_pu + dist_pu_p2) {
						dV1Map.put(pd2, dist_p1_pu + dist_pu_p2);
						predV1Map.put(pd2, predUMap.get(pd2));
					}
				}
				pred.put(pd1, predV1Map);
				d.put(pd1, dV1Map);
				
				v2 = graph.iterator();
			}
			v1 = graph.iterator();
		}
		
		// save results to file
		writePathsToFile();
	}

	private PosDirected parseNode(String nodeString) {
		int rIdx = nodeString.indexOf("R");
		int cIdx = nodeString.indexOf("C");
		String[] split = nodeString.split("[0-9]");
		String dStr = split[split.length - 1];
		int cIdxEnd = nodeString.indexOf(dStr);
		int row = Integer.parseInt(nodeString.substring(rIdx + 1, cIdx));
		int col = Integer.parseInt(nodeString.substring(cIdx + 1, cIdxEnd));
		Direction d = Direction.fromString(dStr);
		return new PosDirected(env.getLayerModel().createPosFromNode(row, col), d);
	}
	
	private String posDirectionToString(PosDirected pd) {
		LayerModel m = env.getLayerModel();
		Pos p = pd.getPos();
		int col = m.getCol(p);
		int row = m.getRow(p);
		Direction d = pd.getDirection();
		return "R" + (row < 10 ? "0" : "") + row + "C" + (col < 10 ? "0" : "") + col + d;
	}
	
	private BufferedWriter openToWrite(String pathName) throws IOException {
		File file = new File(pathName);
		FileWriter outputFile = new FileWriter(file);
		return new BufferedWriter(outputFile);
	}
	
	private BufferedReader openToRead(String pathName) throws IOException {
		File file = new File(pathName);
		FileReader inputFile = new FileReader(file);
		return new BufferedReader(inputFile);
	}
	
	protected void writePathsToFile() {
		initProperties();
		
		try {
			BufferedWriter outD = openToWrite(properties.getProperty(FILE_PREFIX_D) + suffix);
			BufferedWriter outPred = openToWrite(properties.getProperty(FILE_PREFIX_PRED) + suffix);
		
			PosDirected pdu = null;
			PosDirected pdv = null;
			GraphDirectedNodeIterator u = graph.iterator();
			GraphDirectedNodeIterator v = graph.iterator();
			while ((pdu = u.nextDirectedPos()) != null) {
				while ((pdv = v.nextDirectedPos()) != null) {
					PosDirected pdPred = pred.get(pdu).get(pdv);
					double dist = d.get(pdu).get(pdv);
					String appendStr = 	posDirectionToString(pdu) + SEP + posDirectionToString(pdv) + SEP;
					outD.append(appendStr + dist + NL);
					outPred.append(appendStr + posDirectionToString(pdPred) + NL);
				}
				v = graph.iterator();
			}
			outD.close();
			outPred.close();
		} catch (IOException e) {
			logger.error("", e);
		}
		
	}

	private void initProperties() {
		properties = new Properties();
		try {
			URL url =  ClassLoader.getSystemResource(FileSerializer.fileName);
		    properties.load(new FileInputStream(new File(url.getFile())));
		} catch (IOException e) {
			logger.error("", e);
		}
	}
	
	protected boolean readPathsFromFile() {
		d = new HashMap<PosDirected, HashMap<PosDirected, Double>>();
		pred = new HashMap<PosDirected, HashMap<PosDirected, PosDirected>>();
		initProperties();
		try {
			BufferedReader inD = openToRead(properties.getProperty(FILE_PREFIX_D) + suffix);
			BufferedReader inPred = openToRead(properties.getProperty(FILE_PREFIX_PRED) + suffix);

			String sD = null, sPred = null;
			while ((sD = inD.readLine()) != null && (sPred = inPred.readLine()) != null) {
				String lineElementsD[] = sD.split(SEP);
				String lineElementsPred[] = sPred.split(SEP);
				if (lineElementsD.length < 3 || lineElementsPred.length < 3) {
					inD.close();
					inPred.close();
					return false;
				}
				PosDirected pd1 = parseNode(lineElementsD[0]);
				PosDirected pd2 = parseNode(lineElementsD[1]);
				double dist = Double.parseDouble(lineElementsD[2]);
				PosDirected pd3 = parseNode(lineElementsPred[2]);
//				logger.debug("parsed: pd1 " + pd1 + " pd2: " + pd2 + " dist: " + dist + " pd3: " + pd3);
				HashMap<PosDirected, Double> p1DMap;
				HashMap<PosDirected, PosDirected> p1PredMap;
				p1DMap = d.get(pd1);
				if (p1DMap == null)
					p1DMap = new HashMap<PosDirected, Double>();
				p1DMap.put(pd2, dist);
				d.put(pd1, p1DMap);
				
				p1PredMap = pred.get(pd1);
				if (p1PredMap == null)
					p1PredMap = new HashMap<PosDirected, PosDirected>();
				p1PredMap.put(pd2, pd3);
				pred.put(pd1, p1PredMap);
			}
			inD.close();
			inPred.close();
			return true;
		} catch (IOException e) {
		}

		return false;
	}
	
	public double getTravelTimeBetweenNodes(Pos p1, Pos p2) {
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);

		return getTravelTime(row1, col1, row2, col2);
	}
	
	// TODO: Not working for directed layout 59:4 -> 60:4.
	// If we plug non-direcrted map it will work, but with incorrect times
	// Need changes in floyd-warshall for directed layouts
	public double getTravelTime(Pos p1, Pos p2) {
		LayerModel m = env.getLayerModel();
		Pos p1Nearest = m.getNearestNode(p1);
		Pos p1SecondNearest = m.getSecondNearestNode(p1);
		Pos p2Nearest = m.getNearestNode(p2);
		Pos p2SecondNearest = m.getSecondNearestNode(p2);
		double p1UnitTime = 0;
		double p2UnitTime = 0;
		double p1NearestDist = 0;
		double p2NearestDist = 0;
		if (p1SecondNearest != null) {
			p1UnitTime = getTravelTimeBetweenNodes(p1Nearest, p1SecondNearest);
			p1NearestDist = p1Nearest.dist(p1SecondNearest);
		}
		if (p2SecondNearest != null) {
			p2UnitTime = getTravelTimeBetweenNodes(p2Nearest, p2SecondNearest);
			p2NearestDist = p2Nearest.dist(p2SecondNearest);
		}
		
		Pos[] p1Array = {p1Nearest, p1SecondNearest};
		Pos[] p2Array = {p2Nearest, p2SecondNearest};
		
		Set<Pos> p1Set = new HashSet<Pos>(Arrays.asList(p1Array));
		Set<Pos> p2Set = new HashSet<Pos>(Arrays.asList(p2Array));
		if (p1Set.equals(p2Set)) { // p1 and p2 are in the same segment
			return p1UnitTime * (p2.dist(p1) / p1NearestDist);
		}
		
		double minTime = unidentifiedDistance;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				if (p1Array[i] == null || p2Array[j] == null)
					continue;
				double time1 = 0;
				double time2 = 0;
				if (p1NearestDist > 0) {
					time1 = p1UnitTime * (p1.dist(p1Array[i]) / p1NearestDist);
				}
				if (p2NearestDist > 0) {
					time2 = p2UnitTime * (p2.dist(p2Array[j]) / p2NearestDist);
				}
				
				if (minTime > time1 + time2 + getTravelTimeBetweenNodes(p1Array[i], p2Array[j])) {
					minTime = time1 + time2 + getTravelTimeBetweenNodes(p1Array[i], p2Array[j]);
				}
				
			}
		}
		
		return minTime;
//		return getTravelTime(row1, col1, row2, col2);
	}
	
	public double getPosDirectedTravelTime(Pos p1, Pos p2) {
		double min = unidentifiedDistance;

		for (Direction d1 : Direction.getDirections()) {
			for (Direction d2 : Direction.getDirections()) {
				PosDirected pd1 = new PosDirected(p1, d1);
				PosDirected pd2 = new PosDirected(p2, d2);
				min = Math.min(min, d.get(pd1).get(pd2));
			}
		}
		return min;
	}
	
	public double getTravelTime(int row1, int col1, int row2, int col2) {
		LayerModel m = env.getLayerModel();
		Pos p1 = m.createPosFromNode(row1, col1);
		Pos p2 = m.createPosFromNode(row2, col2);
		if (p1 == null || p2 == null)
			return unidentifiedDistance;
		return getPosDirectedTravelTime(p1, p2);
	}
	
	public static TravelTimeMap getInstance(Environment env) {
	     if(instance  == null) {
	        instance = new TravelTimeMap(env);
	        instance.computePaths();
	     }
	     return instance;
	}

	public List<Pos> removeRepetitions(List<Pos> posList) {
		List<Pos> ret = new ArrayList<Pos>();
		
		if (posList == null || posList.size() == 0)
			return posList;
		Pos prev = posList.get(0);
		for (int i = 1; i < posList.size(); i++) {
			Pos pos = posList.get(i);
			if (!pos.equals(prev))
				ret.add(prev);
			prev = pos;
		}
		ret.add(posList.get(posList.size() - 1));
		
		Collections.reverse(ret);
		return ret;
	}
	
	private double getTravelTime(PosDirected pd1, PosDirected pd2) {
		return d.get(pd1).get(pd2);
	}
	
	public double getNeighbourTravelTimeNoRatations(PosDirected pd1, PosDirected pd2) {
		Pos p1 = pd1.getPos();
		Pos p2 = pd2.getPos();
		PlanItem pi1 = env.getLayerModel().getPlanUnit(p1);
		PlanItem pi2 = env.getLayerModel().getPlanUnit(p2);
		if (!extendedGraph.areNeighbours(pi1, pi2))
			return unidentifiedDistance;
		return getTravelTime(p1, p2);
	}
	
	@Override
	public double getPathLength(Pos p1, Pos p2) {
		return getTravelTime(p1, p2);
	}
	
	public List<PosDirected> getPath(PosDirected pd1, PosDirected pd2) {
		List<PosDirected> ret = new ArrayList<PosDirected>();

		ret.add(pd2);
		if (pd1.equals(pd2))
			return ret;
		
		HashMap<PosDirected, PosDirected> predMap = pred.get(pd1);
		PosDirected pd3 = predMap.get(pd2);
		if (pd3.getPos().equals(unidentifiedPos))
			return null;
		while (!pd3.equals(pd1)) {
			ret.add(pd3);
			pd3 = predMap.get(pd3);
			if (pd3.getPos().equals(unidentifiedPos))
				return null;
		}
		
		ret.add(pd1);
		Collections.reverse(ret);
		return ret;
	}
	
	public List<PosDirected> getPathWithOrientation(int row1, int col1, int row2, int col2) {
		LayerModel m = env.getLayerModel();
		Pos p1 = m.createPosFromNode(row1, col1);
		Pos p2 = m.createPosFromNode(row2, col2);
		if (p1 == null || p2 == null)
			return null;
		
		double min = unidentifiedDistance;
		PosDirected minPd1 = null, minPd2 = null;
		for (Direction d1 : Direction.getDirections()) {
			for (Direction d2 : Direction.getDirections()) {
				PosDirected pd1 = new PosDirected(p1, d1);
				PosDirected pd2 = new PosDirected(p2, d2);
				if (d.get(pd1).get(pd2) < min) {
					min = d.get(pd1).get(pd2);
					minPd1 = pd1;
					minPd2 = pd2;
				}
			}
		}
		
		return getPath(minPd1, minPd2);
	}
	
	public List<Pos> getPath(int row1, int col1, int row2, int col2) {
		List<PosDirected> path = getPathWithOrientation(row1, col1, row2, col2);
		return removeRepetitions(posDirectedsListToPosList(path));
	}
	
	public List<Pos> posDirectedsListToPosList(List<PosDirected> pdList) {
		List<Pos> ret = new ArrayList<Pos>();
		for (PosDirected pd : pdList) {
			ret.add(pd.getPos());
		}
		Collections.reverse(ret);
		return ret;
	}
	

	public List<PosDirected> getPathWithOrientation(Pos p1, Pos p2) {
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		return getPathWithOrientation(row1, col1, row2, col2);
	}
	
	@Override
	public List<Pos> getPath(Pos p1, Pos p2) {
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		return getPath(row1, col1, row2, col2);
	}
}
