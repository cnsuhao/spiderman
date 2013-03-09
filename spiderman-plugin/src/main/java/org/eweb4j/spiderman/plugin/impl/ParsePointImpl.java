package org.eweb4j.spiderman.plugin.impl;

import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.plugin.ParsePoint;
import org.eweb4j.spiderman.plugin.util.ModelParser;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;

public class ParsePointImpl implements ParsePoint{

//	private Task task;
	private SpiderListener listener;
//	private Target target ;
//	private Page page;
	
	public void init(Site site, SpiderListener listener) {
		this.listener = listener;
	}

	public void destroy() {
	}
	
//	public synchronized void context(Task task, Target target, Page page) throws Exception{
//		this.task = task;
//		this.target = target;
//		this.page = page;
//	}
	
	public List<Map<String, Object>> parse(Task task, Target target, Page page, List<Map<String, Object>> models) throws Exception {
		List<Map<String, Object>> result = parseTargetModelByXpathAndRegex(task, target, page);
		
		return result;
	}

	private List<Map<String,Object>> parseTargetModelByXpathAndRegex(Task task, Target target, Page page) throws Exception {
		ModelParser parser = new ModelParser(task, target, listener);
		return parser.parse(page);
	}
}
