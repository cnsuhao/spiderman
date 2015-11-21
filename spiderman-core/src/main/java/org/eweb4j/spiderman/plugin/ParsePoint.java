package org.eweb4j.spiderman.plugin;


import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;


public interface ParsePoint extends Point{

//	void context(Task task, Target target, Page page) throws Exception;
	
	FetchResult parse(FetchRequest request,FetchResult result) throws Exception;

}
