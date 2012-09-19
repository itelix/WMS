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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import cma.store.data.LayerModel;
import cma.store.data.PlanItem;
import cma.store.data.Pos;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.serialization.FileSerializer;

/**
 * @author Filip
 *
 */
public class TravelTimeMapNaive extends FloydWarshallAlgorithm {
	private final static String FILE_PREFIX = "TravelTimeMapNaive";
	private static final String DIRECTED_PREFIX = "Directed";
	private final static String FILE_PREFIX_PRED = FILE_PREFIX + "Pred";
	private final static String FILE_PREFIX_D = FILE_PREFIX + "D";
	private static final String SEP = " ";
	private static final String NL = "\n";
	private static TravelTimeMapNaive instance = null;
	
	Graph graph;
	private Properties properties;
	
	public TravelTimeMapNaive(Environment env) {
		super(env);
		this.graph = new GraphOfNodes(env);
		String suffix = env.getLayerModel().getHashString();
		this.fileNamePred = FILE_PREFIX_PRED + suffix;
		this.fileNameD = FILE_PREFIX_D + suffix;
		this.fileNamePred = FILE_PREFIX_PRED;
		this.fileNameD = FILE_PREFIX_D;
		if (env.getLayerModel().isDirectedLayout()) {
			fileNamePred = DIRECTED_PREFIX + fileNamePred;
			fileNameD = DIRECTED_PREFIX + fileNameD;
		}
	}

	private double distBetweenNeighbours(Pos p1, Pos p2) {
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
						dMap.put(p2, distBetweenNeighbours(p1, p2));
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
	
	private double getRotationTime(Pos p1, Pos p2, Pos p3) {		
		if (p1.equals(unidentifiedPos) || p2.equals(unidentifiedPos) || p3.equals(unidentifiedPos))
			return 0;
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		int row3 = m.getRow(p3);
		int col3 = m.getCol(p3);
		
		// Orientation of cross product:
		int P = (col2 - col1)*(row3 - row1) - (row2 - row1)*(col3 - col1);
		if (P > 0) { // counter clockwise
			return BaseEnvironment.ROTATION_90_DEGREE_COUNTER_CLOCKWISE;
		}
		else if (P < 0) { // clockwise
			return BaseEnvironment.ROTATION_90_DEGREE_CLOCKWISE;
		}
		else if (row1 == row3 && col1 == col3 && (row1 != row2 || col1 != col2))
			return BaseEnvironment.ROTATION_180_DEGREE;
		return 0;
	}
	
	private Pos nextPosOnPath(Pos startPos, Pos endPos) {
		Pos pos = endPos;
		HashMap<Pos, Pos> predMap = pred.get(startPos);
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
		HashMap<Pos, Pos> predV1Map;
		HashMap<Pos, Double> dV1Map;
		HashMap<Pos, Pos> predUMap;
		HashMap<Pos, Double> dUMap;
		
		double dist_p1_p2;
		double dist_p1_pu;
		double dist_pu_p2;
		double rotation;
		
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
					rotation = 0;
					Pos nextOnPath = null;
					Pos prevOnPath = null;
					if (dist_pu_p2 != unidentifiedDistance) {
						nextOnPath = nextPosOnPath(pu, p2);
						prevOnPath = predV1Map.get(pu);
						rotation = getRotationTime(prevOnPath, pu, nextOnPath);
					}

					if (dist_p1_p2 > dist_p1_pu + dist_pu_p2 + rotation) {
						
						dV1Map.put(p2, dist_p1_pu + dist_pu_p2 + rotation);

						Pos pos = p2; //predUMap.get(p2);
						while (pos != null && !pos.equals(pu) && !pos.equals(unidentifiedPos) && !pos.equals(predUMap.get(pos))) {
							predV1Map.put(pos, predUMap.get(pos));
							pos = predUMap.get(pos);
						}
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

	private Pos parseNode(String nodeString) {
		int rIdx = nodeString.indexOf("R");
		int cIdx = nodeString.indexOf("C");
		int row = Integer.parseInt(nodeString.substring(rIdx + 1, cIdx));
		int col = Integer.parseInt(nodeString.substring(cIdx + 1));
		return env.getLayerModel().createPosFromNode(row, col);
	}
	private String posToString(int row, int col) {
		return "R" + (row < 10 ? "0" : "") + row + "C" + (col < 10 ? "0" : "") + col;
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
			BufferedWriter outD = openToWrite(properties.getProperty(fileNameD));
			BufferedWriter outPred = openToWrite(properties.getProperty(fileNamePred));
		
			PlanItem piu = null;
			PlanItem piv = null;
			Iterator<PlanItem> u = graph.iterator();
			Iterator<PlanItem> v = graph.iterator();
			LayerModel m = env.getLayerModel();
			while ((piu = u.next()) != null) {
				Pos pu = piu.getPos();
				while ((piv = v.next()) != null) {
					Pos pv = piv.getPos();
					int colu = m.getCol(pu);
					int rowu = m.getRow(pu);
					int colv = m.getCol(pv);
					int rowv = m.getRow(pv);
					int colPred = m.getCol(pred.get(pu).get(pv));
					int rowPred = m.getRow(pred.get(pu).get(pv));
					String appendStr = 	posToString(rowu, colu) + SEP + posToString(rowv, colv) + SEP;
					outD.append(appendStr + d.get(pu).get(pv) + NL);
					outPred.append(appendStr + posToString(rowPred, colPred) + NL);
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
		d = new HashMap<Pos, HashMap<Pos, Double>>();
		pred = new HashMap<Pos, HashMap<Pos, Pos>>();
		initProperties();
		try {
			BufferedReader inD = openToRead(properties.getProperty(fileNameD));
			BufferedReader inPred = openToRead(properties.getProperty(fileNamePred));

			String sD = null, sPred = null;
			while ((sD = inD.readLine()) != null && (sPred = inPred.readLine()) != null) {
				String lineElementsD[] = sD.split(SEP);
				String lineElementsPred[] = sPred.split(SEP);
				if (lineElementsD.length < 3 || lineElementsPred.length < 3) {
					inD.close();
					inPred.close();
					return false;
				}
				Pos p1 = parseNode(lineElementsD[0]);
				Pos p2 = parseNode(lineElementsD[1]);
				double dist = Double.parseDouble(lineElementsD[2]);
				Pos p3 = parseNode(lineElementsPred[2]);
				logger.debug("parsed: p1 " + p1 + " p2: " + p2 + " dist: " + dist + " p3: " + p3);
				HashMap<Pos, Double> p1DMap;
				HashMap<Pos, Pos> p1PredMap;
				p1DMap = d.get(p1);
				if (p1DMap == null)
					p1DMap = new HashMap<Pos, Double>();
				p1DMap.put(p2, dist);
				d.put(p1, p1DMap);
				
				p1PredMap = pred.get(p1);
				if (p1PredMap == null)
					p1PredMap = new HashMap<Pos, Pos>();
				p1PredMap.put(p2, p3);
				pred.put(p1, p1PredMap);
			}
			inD.close();
			inPred.close();
			return true;
		} catch (IOException e) {
		}

		return false;
	}
	
	public double getTravelTime(Pos p1, Pos p2) {
		LayerModel m = env.getLayerModel();
		int row1 = m.getRow(p1);
		int col1 = m.getCol(p1);
		int row2 = m.getRow(p2);
		int col2 = m.getCol(p2);
		return getTravelTime(row1, col1, row2, col2);
	}
	
	public double getTravelTime(int row1, int col1, int row2, int col2) {
		LayerModel m = env.getLayerModel();
		Pos p1 = m.createPosFromNode(row1, col1);
		Pos p2 = m.createPosFromNode(row2, col2);
		if (p1 == null || p2 == null)
			return unidentifiedDistance;
		return d.get(p1).get(p2);
	}
	
	public static TravelTimeMapNaive getInstance(Environment env) {
	     if(instance  == null) {
	        instance = new TravelTimeMapNaive(env);
	        instance.computePaths();
	     }
	     return instance;
	}

	public List<Pos> getPath(int row1, int col1, int row2, int col2) {
//		List<Pos> ret = new ArrayList<Pos>();
		LayerModel m = env.getLayerModel();
		Pos p1 = m.createPosFromNode(row1, col1);
		Pos p2 = m.createPosFromNode(row2, col2);
		if (p1 == null || p2 == null)
			return null;
//		HashMap<Pos, Pos> predMap = pred.get(p1);
//		Pos p3 = predMap.get(p2);
//		if (p3.equals(unidentifiedPos))
//			return null;
//		while (!p3.equals(p1)) {
//			ret.add(p3);
//			p3 = predMap.get(p3);
//			if (p3.equals(unidentifiedPos))
//				return null;
//		}
//		return ret;
		return super.getPath(p1, p2);
	}
}
