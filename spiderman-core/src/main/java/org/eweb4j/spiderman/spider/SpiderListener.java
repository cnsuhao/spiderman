package org.eweb4j.spiderman.spider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.task.Task;



public interface SpiderListener {

    void onNewUrls(Thread thread, Task task, Collection<String> newUrls);
	
    void onFetch(Thread thread, Task task, FetchResult result);
    
    void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks);
    
	void onNewTasks(Thread thread, Task task, Collection<Task> newTasks);
	
	void onTargetPage(Thread thread, Task task, Page page);
	
	void onParse(Thread thread, Task task, List<Map<String, Object>> models);
	
	void onPojo(Thread thread, Task task, List<Object> pojos);

	void onInfo(Thread thread, Task task, String info);
	
	void onError(Thread thread, Task task, String err, Exception e);
	
}
