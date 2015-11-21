package org.eweb4j.spiderman.plugin;

import java.util.Collection;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.task.Task;


public interface DupRemovalPoint extends Point{

//	void context(Task task, Collection<String> newUrls);
	
	Collection<Task> removeDuplicateTask(FetchRequest request,FetchResult result);
}
