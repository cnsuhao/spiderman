package org.eweb4j.spiderman.plugin.impl;

import org.eweb4j.spiderman.plugin.TaskPollPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;

public class TaskPollPointImpl implements TaskPollPoint{

	private Site site = null;
	
	public void init(Site site, SpiderListener listener) {
		this.site = site;
	}

	public void destroy() {
	}
	
	public Task pollTask() throws Exception{
		return site.queue.pollTask();
	}
}
