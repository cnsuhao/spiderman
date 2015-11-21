package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;


public interface FetchPoint extends Point{

//	void context(Task task) throws Exception;
	
	FetchResult fetch(FetchRequest request, FetchResult result) throws Exception;

}
