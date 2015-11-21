package org.eweb4j.spiderman.plugin;


import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;

public interface TaskPushPoint extends Point{
	
	public FetchResult pushTask(FetchRequest request,FetchResult result) throws Exception;
	
}
