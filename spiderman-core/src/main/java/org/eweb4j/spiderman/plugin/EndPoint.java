package org.eweb4j.spiderman.plugin;


import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;


public interface EndPoint extends Point{

//	void context(Task task, List<Map<String, Object>> models) throws Exception;
	
	FetchResult complete(FetchRequest request,FetchResult result) throws Exception;

}
