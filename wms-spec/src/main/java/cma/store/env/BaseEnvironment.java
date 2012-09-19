package cma.store.env;

public abstract class BaseEnvironment {
	public static long HOUR = 3600*1000;
	public static long DAY = HOUR*24;
	
	public static final long TIME_TO_PICK_UP_PRODUCT = 2000;  //ms
	public static final long TIME_TO_STAY_IN_MVC = 3000;   // TODO 100 is for test only 3000;  //ms
	
	//public static double PIXEL_IN_MM = 1000.0;
	public static double DEFAULT_MAX_BOT_SPEED_MM_PER_MS = 1;
	public static double DEFAULT_MAX_BOT_ACELERATION_MM_PER_MS = 0.01;
	public static long DEFAULT_TIME_UNIT_MS = 100;
	public static long DEFAULT_DIST_UNIT_SIZE_MM = 100;
	
	//when checking accidents we check it in discrete times and this is interval between consecutive checking
	public static long DEFAULT_TIME_ACCURACY = 50; 
	
	public static long DEFAULT_AFTER_ROUTE_ACCURACY = 400;
	
	public static long DEFAULT_MAX_SCHEDULING_TIME = 2*DAY;
	//		1. GENERAL PARAMETERS
	public static final int TRANSFER_DECK_ROWS = 4;		// three transfer rows + one home row
	public static final int HMPC_HOMEROW = 7;//6;	// Pos y coordinate
	public static final int HMPC_ROW1 = 4;//4;
	public static final int HMPC_ROW3 = 10;//8;
	public static final int HMPC_ROW4 = 18;//9;
	public static final int HMPC_BAYS_NUM = 25;
	public static final int HMPC_DISTANCE_BETWEEN_COLS = 10;//4; // 20;
	
	//	SYMBOTIC PARAMETERS
	// 		2. TRAVEL TIMES BETWEEN NEIGHBOR NODES
	// 			A. Times between rows. Vertical movement
	// Times between row 0 and row 1 = 2.17347647790355 s
	public static final int TIME_ROW0_ROW1 = 2173;
	// Times between row 1 and row 2 = 1.95908141739949 s
	public static final int TIME_ROW1_ROW2 = 1959;
	// Times between row 2 and row 3 = 2.17347647790355 s
	public static final int TIME_ROW2_ROW3 = TIME_ROW0_ROW1;
	// Times between row 3 and row 4 = 4.06724632202893 s. Entering storage
	public static final int TIME_ROW3_ROW4 = 4067;
	// Storage time between two neighbor nodes = 0.6755 s
	public static final int TIME_STORAGE = 675;
	
	//	 		B. Times between columns. Horizontal movement
	public static final int TIME_VESTIBULE_UNIT = 2015; 	// 2.01593650693666 s
	public static final int TIME_COL_UNIT = 508; 			// 0.508 s
	
	// 		3. ROTATIONS
	//			Each unit rotation (90 degree) takes 4 seconds
	public static final int ROTATION_90_DEGREE_CLOCKWISE = 4000;	// 4 s
	public static final int ROTATION_90_DEGREE_COUNTER_CLOCKWISE = ROTATION_90_DEGREE_CLOCKWISE;
	public static final int ROTATION_180_DEGREE = 2*ROTATION_90_DEGREE_CLOCKWISE;
}
