package org.eweb4j.spiderman.plugin.impl;

import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;

import org.eweb4j.spiderman.plugin.util.Util;

public class TargetPointImpl implements TargetPoint{

	private Task task = null;
	
	public void init(Site site, SpiderListener listener) {
	}

	public void destroy() {
	}
	
	public void context(Task task) throws Exception {
		this.task = task;
	}
	
	public Target confirmTarget(Target target) throws Exception {
		return Util.isTargetUrl(task);
	}

}
