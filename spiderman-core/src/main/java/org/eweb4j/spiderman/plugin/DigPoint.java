package org.eweb4j.spiderman.plugin;

import java.util.Collection;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;


public interface DigPoint extends Point{

//	void context(FetchResult result, Task task) throws Exception;
	
	Collection<Object> digNewUrls(FetchRequest request,FetchResult result) throws Exception;

}
