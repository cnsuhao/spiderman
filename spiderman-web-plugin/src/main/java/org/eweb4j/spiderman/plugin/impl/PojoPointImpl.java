/**
 * 
 */
package org.eweb4j.spiderman.plugin.impl;

import org.eweb4j.spiderman.container.Component;
import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
import org.eweb4j.spiderman.plugin.PojoPoint;
import org.eweb4j.spiderman.spider.SpiderListener;

/**
 * 一个Host一个FetchPointImpl对象
 * @author weiwei l.weiwei@163.com
 * @author wchao wchaojava@163.com
 * @date 2015-1-7 下午06:40:05
 */
public class PojoPointImpl implements PojoPoint {
	@SuppressWarnings("unused")
	private SpiderListener listener;
	
	@Override
	public void init(Component site, SpiderListener listener) {
		this.listener = listener;
	}

	@Override
	public void destroy() {

	}

	@Override
	public FetchResult mapping(FetchRequest request , FetchResult result,Class<?> mappingClass) {
		
		return null;
	}

}
