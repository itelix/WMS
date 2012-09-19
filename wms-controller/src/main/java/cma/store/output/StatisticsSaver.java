package cma.store.output;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import cma.store.serialization.FileSerializer;
import cma.store.stat.Statistics;
import cma.store.stat.Statistics.dirMap;
import cma.store.stat.Statistics.heatMap;
import cma.store.stat.Statistics.routeSummary;

public class StatisticsSaver {
	
	Logger log = Logger.getLogger(getClass());
	
	public static final String OUT_FOLDER_NAME = "results";
	public static final String OUT_FILE_NAME = "statistics.csv";
	public static final String OUT_FILE_NAME_ROUTES = "routes.csv";
	private static final String OUT_FILE_NAME_HEATMAP = "heatmap.csv";
	private static final String OUT_FILE_NAME_DIRMAP_HORIZ = "dirmap_horizontal.csv";
	private static final String OUT_FILE_NAME_DIRMAP_VERT = "dirmap_vertical.csv";
	public static final String SEP = ";";
	Properties properties;
	
	public final static String instalationFolder = "r.instalation.folder";	
	public final static String scriptFolder = "statistics.script.folder";
	
	public StatisticsSaver(){
		properties = new Properties();
		try {
			URL url =  ClassLoader.getSystemResource(FileSerializer.fileName);
		    properties.load(new FileInputStream(new File(url.getFile())));
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void saveHeatmap(Statistics stat) throws IOException {
		heatMap hmap = stat.getHeatmap();
		if (hmap == null) return;
		File out = new File(OUT_FOLDER_NAME, OUT_FILE_NAME_HEATMAP);
		FileWriter writer = new FileWriter(out);

		writer.append(hmap.toString());

		writer.flush();
		writer.close();
	}
	
	public void saveDirections(Statistics stat) throws IOException {
		saveDirectionsImpl(stat, true);
		saveDirectionsImpl(stat, false);
	}
	
	public void saveDirectionsImpl(Statistics stat, boolean horizontal) throws IOException {
		dirMap dmap = stat.getDirmap();
		if (dmap == null) return;
		File out = new File(OUT_FOLDER_NAME,
				horizontal ? OUT_FILE_NAME_DIRMAP_HORIZ : OUT_FILE_NAME_DIRMAP_VERT);
		FileWriter writer = new FileWriter(out);

		writer.append(dmap.toString(horizontal));

		writer.flush();
		writer.close();
	}
	
	public void saveRoutes(Statistics stat) throws IOException {
		File out = new File(OUT_FOLDER_NAME, OUT_FILE_NAME_ROUTES);
		FileWriter writer = new FileWriter(out);

		Statistics.routeSummary rs = stat.new routeSummary();
		writer.append(rs.getTableHeader());
			for (routeSummary row : stat.getRouteSummaries()) {
		   writer.append(row.toString());
		}
		writer.flush();
		writer.close();
	}
	
	public void saveStatistics( Statistics stat, String algorithmName, String testCase, long seed ) throws IOException{
		
		File out = new File( OUT_FOLDER_NAME, testCase + "_" + OUT_FILE_NAME );
		boolean exists = out.exists();
		if( !exists ){
			 (new File( OUT_FOLDER_NAME )).mkdirs();
		}
		
		BufferedWriter bw = new BufferedWriter( new FileWriter(out, exists));
		StringBuffer sb;
		
		if( !exists ){
			sb = new StringBuffer();
			sb.append( "Timestamp");
			sb.append( SEP+"TestCase");
			sb.append( SEP+"Algorithm");
			sb.append( SEP+"Seed");
			sb.append( SEP+"Unused");
			
			List<String> params = stat.getParamiters();
			for( String x: params){
				sb.append( SEP + x);
			}
			bw.write(sb.toString());
		}
		bw.newLine();
		
		sb = new StringBuffer();
		sb.append( "" + (new Date(System.currentTimeMillis()).toString())  );
		sb.append( SEP +testCase);
		sb.append( SEP+algorithmName);
		sb.append( SEP+seed);
		sb.append( SEP);
		
		List<String> params = stat.getParamiters();
		for( String x: params){
			sb.append( SEP + stat.getValue(x) );
		}
		bw.write(sb.toString());

		
		
		bw.close();
		
		saveRoutes(stat);
		saveHeatmap(stat);
		saveDirections(stat);
		
	}

	public void generateRPdfFile() {
		try {
			 Runtime rt = Runtime.getRuntime();
			 
			 String rFolder = properties.getProperty(instalationFolder);
			 String rScriptFolder = properties.getProperty(scriptFolder);
			 
             //Process pr = rt.exec("cmd /c dir");
             Process pr = rt.exec(rFolder+"R.exe  CMD BATCH "+rScriptFolder+"generate_report.R ");

             BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

             String line=null;

             while((line=input.readLine()) != null) {
                 log.info(line);
             }

             int exitVal = pr.waitFor();
             log.info("Exited with error code "+exitVal);
		} catch (Exception e) {
			log.error("", e);
		}
	}

}
