package cma.store.control;

import cma.store.data.Route;

public class RealizationScoreCalculator {
	
	private static final double TIME_DEL_TO_SCORE = -0.001;  //1 seccond == 1 point
		
	public void updateScore( Realization realization ){
		realization.setScore( calcScore(realization) ) ;
	}

	private double calcScore(Realization realization) {
		
		double score = 0;
		long endTime = 0;
		
		for( BaseRealization br: realization.getBaseRealList() ){
			for( Route r: br.getBaseRoutes() ){
				if( r.getFinalTime() > endTime ){
					endTime = r.getFinalTime();
				}
			}
		}
		
		
		score += endTime * TIME_DEL_TO_SCORE;
		
		return score;
	}

}
