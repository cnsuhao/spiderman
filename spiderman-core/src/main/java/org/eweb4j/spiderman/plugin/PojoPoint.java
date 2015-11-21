package org.eweb4j.spiderman.plugin;


import org.eweb4j.spiderman.fetcher.FetchRequest;
import org.eweb4j.spiderman.fetcher.FetchResult;
/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @author wchao wchaojava@163.com
 * @date 2013-1-2 下午07:01:00
 */
public interface PojoPoint extends Point{

	FetchResult mapping(FetchRequest request,FetchResult result,Class<?> mappingClass);

}
