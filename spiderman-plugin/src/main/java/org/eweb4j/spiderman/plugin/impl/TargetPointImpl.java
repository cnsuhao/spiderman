package org.eweb4j.spiderman.plugin.impl;

import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;

public class TargetPointImpl implements TargetPoint{

	public void init(Site site, SpiderListener listener) {
	}

	public void destroy() {
	}
	
	public synchronized Target confirmTarget(Task task, Target target) throws Exception {
		Target tgt = Util.matchTarget(task);
		if (tgt != null && "1".equals(tgt.getIsSkip()))
		    return null;
		
		return tgt;
	}

}
