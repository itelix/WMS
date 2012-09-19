package cma.store.view;

/**
Warehouse optimizer.
creating date: 2012-05-13
creating time: 21:44:40
autor: Czarek
 */

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import cma.store.control.Controller;
import cma.store.data.Bot;
import cma.store.data.BotFleet;
import cma.store.data.LayerModel;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.env.Environment;
import cma.store.env.Timer;
import cma.store.schedule.Schedule;
import cma.store.schedule.ScheduleItem;

public class DisplaySimple extends Thread{
	Logger logger = Logger.getLogger(getClass());
	
	private LayerModel model;
//	private Schedule schedule;
	
	private int US = 10;
	private int US_X= US;
	private int US_Y = 10;
	//private int time = 0;
	private int MARGIN = US;
	private int MARGIN_X = MARGIN;
	private int MARGIN_Y = MARGIN;
	
	PlanType[][] plan;
	Bot[][] botsPlace;
	Environment env;
	BotFleet botFleet;
	private boolean work=true;
	private Timer timer;
	private Controller controller;
	
	public DisplaySimple(Environment env, Timer timer){
		this.env = env;
		this.timer = timer;

	}
	
	public DisplaySimple(Environment env, Timer timer, Controller controller){
		this.env = env;
		this.timer = timer;
		this.controller = controller;
	}
	
	public void stopIt(){
		work=false;
	}
	
	private void init(){
		model = env.getLayerModel();
		botFleet = env.getBotFleet();
//		schedule = env.getSchedule();

		plan = new PlanType[model.getSizeX()][model.getSizeY()];
	}

	public void run() {
		
		init();
		
		try {
		
		Display.setDisplayMode(new DisplayMode(
				US_X*(model.getSizeX()+1),
				US_Y*(model.getSizeY()+1)));
		Display.create();
		
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);	
		}
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(MARGIN_X, MARGIN_X+model.getSizeX()*US_X, MARGIN_Y, MARGIN_Y+model.getSizeY()*US_Y, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		// Clear the screen and depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);	
				
		// set the color of the quad (R,G,B,A)
		GL11.glColor3f(0.5f,0.5f,1.0f);
		
		//env.setTime(0);
		
		// init OpenGL here
		while (!Display.isCloseRequested() && work ) {
			//model.setTime( model.getTime()+1 );
			
			updatePlan();
			print();
			printDestination();
			
			//printBotsLocations();
			
			try {
				
				while (Keyboard.next()) {
					int key = Keyboard.getEventKey();
					boolean down = Keyboard.getEventKeyState();
					boolean repeat = Keyboard.isRepeatEvent();
					char c = Keyboard.getEventCharacter();
					new Integer(key).toString();
					
					if(key== Keyboard.KEY_UP && !down) {
						// up arrow
						controller.increaseSpeed();
					}
					if(key== Keyboard.KEY_DOWN && !down) {
						// down arrow
						controller.reduceSpeed();
					}
					if(key== Keyboard.KEY_SPACE && !down) {
						controller.pausePlay();
					}
				}
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// render OpenGL here
			Display.update();
		}
	
		Display.destroy();
	}


	private void printBotsLocations() {
		for(Bot bot: env.getBotFleet().getBots() ){
			System.out.println("Bot:" + bot);
		}
	}

	private void printDestination() {
		Hashtable<Bot,Route> htTest = new Hashtable<Bot, Route>();
		
		for( ScheduleItem item: env.getSchedule().getItems() ){
			
			Pos currPos = item.getRoute().getPos( timer.getTimeMs(), true );
			if( currPos==null ) continue;

			Pos endPos = item.getRoute().getFinalPos();
			//XX sprawdzic dlaczego tak musi byc 
			//endPos.x -= 0.5;
			//currPos.x -= 0.5;
			Route r1 = htTest.get(item.getRoute().getBot());
			if( r1!=null ){
//				System.out.println("rout1 = " + r1);
//				System.out.println("rout2 = " + item.getRoute());
			}
			htTest.put(item.getRoute().getBot(), item.getRoute());
			
			if( currPos!=null && endPos!=null ){
				printArrow( currPos, endPos);
			}
		}
		
		// TODO Auto-generated method stub
	}
	
	private void printArrow(Pos p1, Pos p2){
		GL11.glColor3f(0.0f,0.0f,0.0f); 
		
		GL11.glBegin(GL11.GL_QUADS);
			float x1 = env.getLayerModel().convX(p1.x)+0.5f;
			float y1 = env.getLayerModel().convX(p1.y)+0.5f;
			float x2 = env.getLayerModel().convX(p2.x)+0.5f;
			float y2 = env.getLayerModel().convX(p2.y)+0.5f;
		
		    GL11.glVertex2f(MARGIN_X+US_X*x1,MARGIN_Y+US_Y*y1);
		    GL11.glVertex2f(MARGIN_X+US_X*x1+2,MARGIN_Y+US_Y*y1+2);
		    GL11.glVertex2f(MARGIN_X+US_X*x2+2,MARGIN_Y+US_Y*y2+2);
		    GL11.glVertex2f(MARGIN_X+US_X*x2,MARGIN_Y+US_Y*y2);

	    GL11.glEnd();
	}

	public void updatePlan(){
		
		botsPlace = new Bot[model.getSizeX()][model.getSizeY()];
		for(int i=0; i<model.getSizeX(); i++){
			for(int j=0; j<model.getSizeY(); j++){
				if( model.getPlanUnit(i,j).isMvcIn()){
					plan[i][j] = PlanType.MVC_IN;
					continue;
				}		
				if( model.getPlanUnit(i,j).isMvcOut()){
					plan[i][j] = PlanType.MVC_OUT;
					continue;
				}	
				
				if ( model.getPlanUnit(i,j).isNode() ){
					plan[i][j] = PlanType.NODE;
					continue;
				}
				
				if( model.getPlanUnit(i,j).isRoad() ){
					plan[i][j] = PlanType.ROAD;
				}
				else if( model.getPlanUnit(i,j).isWall()){
					plan[i][j] = PlanType.WALL;
				}else{
					plan[i][j] = PlanType.NOTHING;
				}
				
			}			
		}
		
		for( Bot b: botFleet.getBots() ){
			
			Pos p = env.getSchedule().getBotPosition(b, timer.getTimeMs(), false);
			
			int x = env.getLayerModel().convX(p.x);
			int y = env.getLayerModel().convX(p.y);
			
			plan[x][y] = PlanType.CAR;
			botsPlace[x][y] = b;
		}
		
		//logger.debug("Plan updated, elements count=" + this.env.getSchedule().getItems().size() );
		
	}
	

//	@Override
//	public void update(ScheduleItem item) {
//		Pos p = item.getRout().getPos( timer.getTimeMs() );
//		env
//		
//		if( p==null ) return;
//		
//		for(int ii=p.x-1; ii<=p.x+1; ii++){
//			for(int jj=p.y-1; jj<=p.y+1; jj++){
//				if( ii>=0 && jj>=0 && ii<model.getSizeX() && jj<model.getSizeY() ){
//					plan[p.x][p.y] = PlanColor.CAR;
//				}
//			}
//		}
//	}
	
	private void print(){
		for(int i=0; i<model.getSizeX(); i++){
			for(int j=0; j<model.getSizeY(); j++){
				print(i,j);
			}			
		}		
	}

	public void print(int i, int j){
		
		switch( plan[i][j] ){
			case CAR:{
//				GL11.glColor3f(0.0f,1.0f,0.0f); break;
				
				Bot bot = botsPlace[i][j];
				if(bot != null) {
					float color = bot.getId()*100;
					color = color/255;
					while(color > 1) {
						color = color/2;
					}
					GL11.glColor3f(0.0f,color,0.0f); 
				} else {
					GL11.glColor3f(0.0f,1.0f,0.0f); 
				}
				break;
			}
			case WALL: GL11.glColor3f(0.5f,0.5f,0.0f); break;
//			case NODE: GL11.glColor3f(1.0f,1.0f,1.0f); break;
			case NODE: GL11.glColor3f(0.5f,0.5f,1.0f); break;
			case ROAD: GL11.glColor3f(0.5f,0.5f,1.0f);break;
			case MVC_IN:  GL11.glColor3f(1f,0.5f,0.0f); break;
			case MVC_OUT:  GL11.glColor3f(1f,0.8f,0.5f); break;

			default: GL11.glColor3f(0.0f,0.0f,0.0f);
		}

		// draw quad
		GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(MARGIN_X+US_X*i,MARGIN_Y+US_Y*j);
		    GL11.glVertex2f(MARGIN_X+US_X*(i+1),MARGIN_Y+US_Y*j);
		    GL11.glVertex2f(MARGIN_X+US_X*(i+1),MARGIN_Y+US_Y*(j+1));
		    GL11.glVertex2f(MARGIN_X+US_X*i,MARGIN_Y+US_Y*(j+1));
		GL11.glEnd();

	}

}