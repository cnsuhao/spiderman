package org.eweb4j.spiderman.plugin;

import java.util.Collection;

import org.eweb4j.spiderman.task.Task;


public interface TaskPushPoint extends Point{
	
	public Collection<Task> pushTask(Collection<Task> tasks) throws Exception;
	
}
