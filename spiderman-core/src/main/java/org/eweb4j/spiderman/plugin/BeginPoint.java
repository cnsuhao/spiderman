package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;

/**
 * 扩展点：爬虫开始时
 * @author weiwei
 * @author wchao
 *
 */
public interface BeginPoint extends Point{

	FetchResult preProcess(FetchRequest request,FetchResult result) throws Exception;
	
}
