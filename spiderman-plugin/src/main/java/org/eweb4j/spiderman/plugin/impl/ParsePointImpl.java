package org.eweb4j.spiderman.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.plugin.ParsePoint;
import org.eweb4j.spiderman.plugin.util.ModelParser;
import org.eweb4j.spiderman.plugin.util.UrlUtils;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Field;
import org.eweb4j.spiderman.xml.Model;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Site;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;

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
		List<Map<String, Object>> results = new ModelParser(task, target, listener).parse(page);
		//用来记录分页里已经解析的url
		Set<String> visitedUrls = new HashSet<String>();
		visitedUrls.add(task.url);
		
		List<Rule> rules = target.getUrlRules().getRule();
		for (Rule rule : rules) {
			//顺序递归解析下一页的内容
			Map<String, Object> finalFields = new HashMap<String, Object>();
			parseNextPage(rule, target, task, page, results, visitedUrls, finalFields);
		}
		
		return results;
	}

	//递归的关键是 Page
	public void parseNextPage(Rule rule, Target target, Task task, Page page, List<Map<String, Object>> results, Set<String> visitedUrls, Map<String,Object> finalFields) throws Exception{
//		System.out.println("parse.next->"+page.getUrl());
		Model mdl = rule.getNextPage();
		if (mdl == null)
			return ;

		Target tgt = new Target();
		tgt.setName(target.getName());
		tgt.setModel(mdl);

		//解析Model获得next URL
//		System.out.println("page--!!!!!!----->"+page.getUrl());
		Collection<String> nextUrls = UrlUtils.digUrls(page, task, rule, tgt, listener, finalFields);
//		System.out.println("visitedUrls-->>>>>>>>>>>>!!!!!!!!!!!!!!" + visitedUrls);
		System.out.println("\ttarget digNextUrl->" + nextUrls + " from->" + page.getUrl());
		if (nextUrls == null || nextUrls.isEmpty())
			return ;
		String nextUrl = new ArrayList<String>(nextUrls).get(0);
		if (nextUrl == null || nextUrl.trim().length() == 0)
			return ;

		if (visitedUrls.contains(nextUrl)){
			return ;
		}

		FetchRequest req = new FetchRequest();
		req.setUrl(nextUrl);
		req.setHttpMethod(rule.getHttpMethod());
		FetchResult fr = task.site.fetcher.fetch(req);
		if (fr == null || fr.getPage() == null)
			return ;

		//记录已经访问过该url，下次不要重复访问它
		visitedUrls.add(nextUrl);

		//解析nextPage
		List<Field> isAlsoParseInNextPageFields = target.getModel().getIsAlsoParseInNextPageFields();
		if (isAlsoParseInNextPageFields == null || isAlsoParseInNextPageFields.isEmpty())
			return ;

		Task nextTask = new Task(nextUrl, rule.getHttpMethod(), task.url, task.site, 0);
		//构造一个model
		Model nextModel = new Model();
		nextModel.getField().addAll(isAlsoParseInNextPageFields);
		tgt.setModel(nextModel);

		ModelParser parser = new ModelParser(nextTask, tgt, listener);
		Page nextPageResult = fr.getPage();
		List<Map<String, Object>> nextMaps = parser.parse(nextPageResult);
		if (nextMaps == null)
			return ;

//		System.out.println("\n\tfuck!!!!!->" + CommonUtil.toJson(nextMaps.get(0)) + " \n\tfrom->" + fr.getPage().getUrl());
		
		for (Map<String, Object> nextMap : nextMaps){
			for (Iterator<Entry<String, Object>> it = nextMap.entrySet().iterator(); it.hasNext();){
				Entry<String, Object> e = it.next();
				String key = e.getKey();
				Object value = e.getValue();
				for (Map<String, Object> result : results){
					if (nextModel.isArrayField(key)){
						List<Object> list = (List<Object>) result.get(key);
						list.addAll((List<Object>)value);
					}else{
						StringBuilder sb = new StringBuilder();
						sb.append(result.get(key)).append("_##_").append(value);
						result.put(key, sb.toString());
					}
				}
			}
		}

		parseNextPage(rule, target, nextTask, nextPageResult, results, visitedUrls, finalFields);
	}
}
