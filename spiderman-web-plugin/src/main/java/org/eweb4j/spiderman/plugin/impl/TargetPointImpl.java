package org.eweb4j.spiderman.plugin.impl;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.plugin.TargetPoint;
import org.eweb4j.spiderman.plugin.util.Util;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.xml.Target;

public class TargetPointImpl implements TargetPoint{

	public void init(Component site, SpiderListener listener) {
	}

	public void destroy() {
	}
	
	public FetchResult confirmTarget(FetchRequest request,FetchResult result) throws Exception {
		Target tgt = Util.matchTarget(request.task);
		if ((tgt != null && "1".equals(tgt.getIsSkip())) | tgt == null)
		    return null;
		request.task.target = tgt;
		result.setTarget(tgt);
		return result;
	}

}
