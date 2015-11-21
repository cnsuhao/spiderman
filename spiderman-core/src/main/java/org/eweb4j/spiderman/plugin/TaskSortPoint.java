package org.eweb4j.spiderman.plugin;


import org.eweb4j.spiderman.fetcher.FetchResult;

public interface TaskSortPoint extends Point{

	FetchResult sortTasks(FetchResult result) throws Exception;
	
}
