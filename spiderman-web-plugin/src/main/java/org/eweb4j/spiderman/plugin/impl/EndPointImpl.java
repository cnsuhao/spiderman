package org.eweb4j.spiderman.plugin.impl;


import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.plugin.EndPoint;
import org.eweb4j.spiderman.spider.SpiderListener;

public class EndPointImpl implements EndPoint{
	
	public void init(Component site, SpiderListener listener) {
	}

	public void destroy() {
	}

	public FetchResult complete(FetchRequest request,FetchResult result) throws Exception {
		//List<Map<String,Object>> dataMap = result.getModels();
		/**结束操作*/
		return result;
	}

}
