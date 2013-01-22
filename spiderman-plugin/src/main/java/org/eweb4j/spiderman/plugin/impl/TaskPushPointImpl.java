package org.eweb4j.spiderman.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eweb4j.spiderman.plugin.TaskPushPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.util.CommonUtil;

public class TaskPushPointImpl implements TaskPushPoint{
	
	public void init(Site site, SpiderListener listener) {
	}

	public void destroy() {
	}
	
	public Collection<Task> pushTask(Collection<Task> validTasks) throws Exception{
		Collection<Task> newTasks = new ArrayList<Task>();
		for (Task task : validTasks){
			try{
				//如果不是同一个Host
				if (!CommonUtil.isSameHost(task.site.getUrl(), task.url))
					continue;
				
				boolean isOk = task.site.queue.pushTask(task);
				if (isOk)
					newTasks.add(task);
			}catch(Exception e){
				continue;
			}
		}
		
		return newTasks;
	}
	
}
