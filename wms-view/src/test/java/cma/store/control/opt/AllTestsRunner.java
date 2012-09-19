package cma.store.control.opt;

public class AllTestsRunner {
	
	
	public AllTestsRunner(){
		
	}
	
	protected String[] getTestsNames(){
		
		return new String[]{
				"RouteChooseTest_1"
				,"RouteChooseTest_2"
				,"RouteChooseTest_3"
				,"RouteChooseTest_4"
				,"RouteChooseTest_4a"
				,"RouteChooseTest_5"
				,"RouteChooseTest_6"
				,"RouteChooseTest_7"
				,"RouteChooseTest_8"
				,"RouteChooseTest_9"
				,"RouteChooseTest_10"
				,"RouteChooseTest_11"
				,"RouteChooseTest_12"
		};
	}
	
	public void runTests() {
		for(String x: getTestsNames()){
			try {
				RouteChooseAbs test = (RouteChooseAbs) (Class.forName( "cma.store.control.opt."+ x ).newInstance());
				
				test.setSeed(123);
				test.setTestSeed(234);
				test.switchViewOn( false );
				
				test.runTest();
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
		}
		
	}
	
	
	public static void main(String[] args) {
		(new AllTestsRunner()).runTests();
		
	}

}
