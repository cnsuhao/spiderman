package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.task.Task;


public interface FetchPoint extends Point{

//	void context(Task task) throws Exception;
	
	FetchResult fetch(Task task, FetchResult result) throws Exception;

}
