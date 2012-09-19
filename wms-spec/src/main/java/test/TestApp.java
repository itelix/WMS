package test;

import org.apache.log4j.Logger;

public class TestApp {

	public void start() {
		Logger log = Logger.getLogger(TestApp.class.getName());
    	log.info("test"); 	
	}
}