package cma.store.control.opt.route.matrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cma.store.control.PenaltyInfo;
import cma.store.control.PenaltyInfoIfc;
import cma.store.data.Pos;
import cma.store.data.PosTime;
import cma.store.env.Environment;

/**
Warehouse optimizer.
creating date: 2012-05-23
creating time: 20:41:25
autor: Czarek
 */

public class RouteScorring {
	//private static final double NOT_DEF = -1.0;
	
	PenaltyInfoIfc penalty;
	public MatrixScore[][][] score;
	
	public int[][] min_time;
	int maxXdisc;
	int maxYdisc;
	double maxX;
	double maxY;
	int maxT;
	PosTime to;
	long timeAdjust;
	long minTime;
	long maxTime;
	Environment env;
	long layerDifferenceMS; //time difference between layers;
	//long timeLayer0;
	int numberOfIterationToStayInAPossition = 1;  //it is a little bet questionable
	PosTime[] neighbours;
	
	public RouteScorring( Environment env, int maxX, int maxY ){
		this.env = env;
		this.maxXdisc = maxX;
		this.maxYdisc = maxY;
		
		this.maxX = env.getLayerModel().convX( maxX );
		this.maxY = env.getLayerModel().convY( maxY );
		
		layerDifferenceMS = 100;
		neighbours = Neighbours.getClosestNeighboursBefore();
	}
	
	private PenaltyInfoIfc getPenalty(){
		if( penalty==null ) penalty=env.getPenaltyInfo();
		return penalty;
	}
	
	public int getMaxXdisc(){
		return maxXdisc;
	}
	
	public int getMaxYdisc(){
		return maxYdisc;
	}
	
	public long getMaxTime(){
		return maxTime;
	}
	
	public MatrixScore[][][] getScore( PosTime to, int maxT ){
		this.to = to;
		this.maxT = maxT;
		
		initMatrixScore();
		return score;
	}
	
	private long layerToTime( int timeId ){
		return maxTime-timeId*layerDifferenceMS;
	}
	
	private int timeToLayer( long time ){
		return (int)((time-maxTime) / layerDifferenceMS);
	}
	
	private double getPenalty( int x, int y, int timeId ){
		return getPenalty().getPenalty(x, y, timeId);
	}
	
	private double getPenalty(PosTime p){
		if(p.x>2 && p.x<7 && p.y==5 ) {
			return 100;
		}
		return 1.0;
	}
	
//	private int getMinTime1( int x, int y ){
//		return Math.abs(to.x-x) + Math.abs(to.y-y); //simplest way to reach point x,y 		
//	}
	
	private boolean inRange( Pos p ){
		return p.x>=0 && p.y>=0 && p.x<maxX && p.y<=maxY;
	}
	
	public MatrixScore getMatrixScore( PosTime ps ){
		int x = env.getLayerModel().convX(ps.x);
		int y = env.getLayerModel().convX(ps.y);
		
		return score[x][y][(int)(ps.time+timeAdjust)];
	}

	public MatrixScore getMatrixScoreLayer( PosTime ps ){
		int x = env.getLayerModel().convX(ps.x);
		int y = env.getLayerModel().convX(ps.y);
		
		return score[x][y][(int)(ps.time)];
	}	
	
	public void setMatrixScore( PosTime ps, MatrixScore ms){
		int x = env.getLayerModel().convX(ps.x);
		int y = env.getLayerModel().convX(ps.y);
		
		score[x][y][(int)(ps.time+timeAdjust)]=ms;
	}	
	
	private List<PosTime> getNeighbours( PosTime actual ){
		List<PosTime> nn = new ArrayList<PosTime>();
		
		for( PosTime n: neighbours){
			PosTime x = new PosTime(actual.x+n.x, actual.y+n.y, actual.time+n.time);
			if( x.time<minTime || x.x<0 || x.y<0 || x.x>maxX || x.y>maxY) continue;

			nn.add( x );
		}
		
		return nn;
	}
	
	private void initMatrixScore( ){
		score = new MatrixScore[maxXdisc+1][maxYdisc+1][maxT+1];

		Set<PosTime> change = new HashSet<PosTime>();
		
		//MatrixScore s = new MatrixScore( 0, minEndTime );
		//int minEndTime = to.getTime();
		
		maxTime = to.getTime();  //0 layer
		minTime = Math.max(0, maxTime-maxT); //max layer
		timeAdjust = -to.getTime() + maxT;
		
		MatrixScore ms1 = new MatrixScore(to, getPenalty( to ), to.getTime(), null);
		setMatrixScore(to,ms1);
		
		change.add( to );
		
		while(change.size()>0){
			Set<PosTime> next = new HashSet<PosTime>();
			
			for(PosTime p: change){
				MatrixScore from = getMatrixScore(p);
				
				List<PosTime> neighbours = getNeighbours(p);
				
				for( PosTime x: neighbours){
					
					double newCost = getPenalty().getPenalty( x ) + from.cost;
					if( newCost >= PenaltyInfo.PENALTY_FORBIDDEN_WAY ) continue;

					MatrixScore ms = getMatrixScore(x);
					
					if( ms==null || ms.cost > newCost ) {
						ms = new MatrixScore(x,newCost,x.time,from);
						setMatrixScore(x,ms);
						next.add( x );
					}

				}
//				System.out.println("Pos=" + p );
//				Utils.printLayer(this, p.getTime());

			}
			change = next;
			//System.out.println("change=" + change.size() );
		}

	}

	
	
	public static void main(String[] args) {
//		Schedule schedule = new Schedule();
//		Model model = new Model(10,10,null);
//		PenaltyInfo penaltyInfo = new PenaltyInfo(model,schedule);
//		
//		MatrixRouteCreator f = new MatrixRouteCreator(model,schedule,penaltyInfo);
//		
//		Pos to = new Pos(3,6);
//		MatrixScore[][][] d= f.getMatrixScore();
//		
//		for(int i=0; i<d.length; i++){
//			for(int j=0; j<d[i].length; j++){
//				System.out.print(""+d[i][j] + ": ");
//			}	
//			System.out.println("");
//		}
	}


}
