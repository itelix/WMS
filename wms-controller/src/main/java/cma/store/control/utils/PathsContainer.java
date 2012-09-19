package cma.store.control.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;

public class PathsContainer {

	Environment env;
	double speed = BaseEnvironment.DEFAULT_MAX_BOT_SPEED_MM_PER_MS;
	double deltaTime;
	List<List<Pos>> paths;
	List<Long> startTimes;
	double deltaDist;
	RoutUtils routUtils;
	List<Bot> bots;
	Logger logger = Logger.getLogger(getClass());
	
	public PathsContainer(Environment env) {
		this.env = env;
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
		paths = new ArrayList<List<Pos>>();
		startTimes = new ArrayList<Long>();
		routUtils = new RoutUtils(env);
		bots = new ArrayList<Bot>();
	}
	
	public Colision add(List<Pos> path, long startTime, Bot bot) {
		Colision col = isColiding(path, startTime, bot);
		if (col == null) {
			paths.add(path);
			startTimes.add(startTime);
			bots.add(bot);
		}

		return col;
	}
	
	public void deleteScheduledPaths(long time) {
		for (int i = 0; i < paths.size(); i++) {
			List<Pos> pathI = paths.get(i);
			long pathIEndTime = startTimes.get(i) + pathI.size()*((long) deltaTime);

			if (pathIEndTime < time) {
				paths.remove(i);
				i--;
			}

		}
	}
	
	public Colision isColiding(List<Pos> path, long startTime, Bot bot) {
		long time = startTime;
		for (Pos pos : path) {
			for (int i = 0; i < paths.size(); i++) {
				Bot pathIBot = bots.get(i);
				if (!pathIBot.equals(bot)) {
					List<Pos> pathI = paths.get(i);
					long pathIStartTime = startTimes.get(i);
					long pathIEndTime = startTimes.get(i) + (pathI.size()-1)*((long)deltaTime);
					List<Bot> colidingBots = new ArrayList<Bot>();
					colidingBots.add(bot); colidingBots.add(bots.get(i));

					if (pathIStartTime > time) continue;
					if (pathIEndTime < time) {
						// TODO check collisions with finished routes
						// check also if bot that finished some path doesnt start path path
						//if (pathI.get(pathI.size() - 1).equals(pos));
						//else
						
						
						// We must check the way back of this bot
						int j;
						Pos pathIEndPos = pathI.get(pathI.size() - 1);
						for (j = 0; j < paths.size(); j++) {
							if (pathIBot.equals(bots.get(j)) && pathIEndPos.equals(paths.get(j).get(0))) {
//								logger.debug("pathIBot == botJ && position equal j =" + j);
								break;
							}
						}
						if (j < paths.size()) {
							long pathIBackStartTime = startTimes.get(j);
							if (pathIBackStartTime >= time) {
								// Between pathIEndTime and pathIBackStartTime bot is standing
								// in the position pathIEndPos
//								return new Colision(time, pos, colidingBots);
							}
						}
						
						continue;
					}
					double pPosIndex = (time - pathIStartTime)/deltaTime;
					Pos pPos = pathI.get((int) pPosIndex);
					if (routUtils.colision(pos, pPos))
						return new Colision(time, pos, colidingBots);

				}
				time += deltaTime;
			}
		}
		return null;
	}
	/**
	 * Checikg if bot in time have colision with standing bot
	 * @param pos
	 * @param time
	 * @param bot
	 * @return
	 */
	public StandingBotColision isColiding(Pos pos, long time, Bot bot) {	
		for (int i = 0; i < paths.size(); i++) {
			Bot currentBot = bots.get(i);
			if(!currentBot.equals(bot)) {
				List<Pos> pathI = paths.get(i);
				long pathIStartTime = startTimes.get(i);
				long pathIEndTime = pathIStartTime + (pathI.size()-1)*((long)deltaTime);
				///XXX duplicated code if ok refactor AS
				if(time >= pathIEndTime) {
					// sprawdzamy czy koncowe pozycje sa takie same jak tak to wyjazdek
					Pos lastPos = pathI.get(pathI.size()-1);
					if(lastPos.equals(pos)) {
						List<Bot> bots = new ArrayList<Bot>();
						bots.add(bot);
						bots.add(currentBot);
						return new StandingBotColision(time, pos, bots,pathIEndTime,currentBot);
					}					 
				}
//				if (pathIStartTime > time) continue;
//				if (pathIEndTime < time) continue;
//				double pPosIndex = (time - pathIStartTime)/deltaTime;
//				Pos pPos = pathI.get((int) pPosIndex);
//				if (routUtils.colision(pos, pPos))
//					return new Colision(time, pos, null);
			}
		}
		return null;
	}
}
