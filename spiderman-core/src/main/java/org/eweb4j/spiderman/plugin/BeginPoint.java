package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.task.Task;

/**
 * 扩展点：爬虫开始时
 * @author weiwei
 *
 */
public interface BeginPoint extends Point{

	Task confirmTask(Task task) throws Exception;
	
}
