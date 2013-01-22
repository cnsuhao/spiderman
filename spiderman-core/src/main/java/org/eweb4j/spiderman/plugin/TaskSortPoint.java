package org.eweb4j.spiderman.plugin;

import java.util.Collection;

import org.eweb4j.spiderman.task.Task;


public interface TaskSortPoint extends Point{

	Collection<Task> sortTasks(Collection<Task> tasks) throws Exception;
	
}
