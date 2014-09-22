package org.eweb4j.spiderman.spider;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;



public interface SpiderListener {

	void onDigUrls(Thread thread, Task task, String fieldName, Collection<String> urls);
	
    void onNewUrls(Thread thread, Task task, Collection<String> newUrls);
	
    void onFetch(Thread thread, Task task, FetchResult result);
    
    void onDupRemoval(Thread currentThread, Task task, Collection<Task> validTasks);
    
    void onTaskSort(Thread currentThread, Task task, Collection<Task> afterSortTasks);
    
	void onNewTasks(Thread thread, Task task, Collection<Task> newTasks);
	
	void onTargetPage(Thread thread, Task task, Page page);
	
	void onParseField(Thread thread, Task task, Object selector, String field, Object value);
	
	void onParseOne(Thread thread, Task task, int size, int index, Map<String, Object> model);
	
	void onParse(Thread thread, Task task, List<Map<String, Object>> models);
	
	void onPojo(Thread thread, Task task, List<Object> pojos);

	void onInfo(Thread thread, Task task, String info);
	
	void onStartup(Site site);
	
	void onError(Thread thread, Task task, String err, Throwable e);
	
	void onInitError(Site site, String err, Throwable e);
	
	/**
	 * 调度结束后回调此方法
	 * @date 2013-4-1 下午03:17:23
	 */
	void onAfterScheduleCancel();
	
	/**
	 * 每次调度执行前回调此方法
	 * @date 2013-4-1 下午03:33:11
	 * @param theLastTimeScheduledAt 上一次调度时间
	 */
	void onBeforeEveryScheduleExecute(Date theLastTimeScheduledAt);
	
	/**
	 * 
	 * Spiderman.shutdown()被调用之前回调此方法
	 * @date 2013-6-3 下午05:00:43
	 */
	void onBeforeShutdown(Object... args);
	
	/**
	 * Spiderman.shutdown()被调用之后回调此方法
	 * @date 2013-6-3 下午05:01:02
	 */
	void onAfterShutdown(Object... args);
	
	/**
	 * 
	 * Spiderman.shutdown()被调用之前回调此方法
	 * @date 2013-6-3 下午05:00:43
	 */
	void onBeforeShutdown(Site site, Object... args);
	
	/**
	 * Spiderman.shutdown()被调用之后回调此方法
	 * @date 2013-6-3 下午05:01:02
	 */
	void onAfterShutdown(Site site, Object... args);
}
