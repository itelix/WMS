package cma.store.control.opt.data;

public enum RequestType {
	
	KNOWN_END_AND_BEFORE_END_TIME, //we know where is destination, and bot should be BEFORE a given time
	KNOWN_END_AND_AFTER_END_TIME, //we know where is destination, and bot should be AFTER a given time
	KNOWN_END_AND_MIN_START_TIME, //we know where is destination, and bot start time
	
	KNOWN_START_AND_TIME, //we know where is start position, and start time

	KNOWN_END_AND_AFTER_END_TIME_BACKWARDS // route from storage loc to MVC backwards

}
