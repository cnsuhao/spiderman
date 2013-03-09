package org.eweb4j.spiderman.plugin;

import java.util.Collection;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.task.Task;


public interface DigPoint extends Point{

//	void context(FetchResult result, Task task) throws Exception;
	
	Collection<String> digNewUrls(FetchResult result, Task task, Collection<String> urls) throws Exception;

}
