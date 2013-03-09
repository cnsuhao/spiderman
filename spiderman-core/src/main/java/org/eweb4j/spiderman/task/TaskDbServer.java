package org.eweb4j.spiderman.task;
/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-2-28 下午11:55:17
 */
public interface TaskDbServer {

	public int getDocId(String key);
	
	public int newDocID(String key);
	
	public void close();
	
	public void sync();
}
