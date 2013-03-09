package org.eweb4j.spiderman.plugin.impl;

import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.plugin.EndPoint;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;

public class EndPointImpl implements EndPoint{
	
	public void init(Site site, SpiderListener listener) {
	}

	public void destroy() {
	}

	public List<Map<String, Object>> complete(Task task, List<Map<String, Object>> dataMap) throws Exception {
		return dataMap;
	}

}
