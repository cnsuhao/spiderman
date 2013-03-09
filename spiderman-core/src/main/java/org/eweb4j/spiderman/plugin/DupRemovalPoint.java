package org.eweb4j.spiderman.plugin;

import java.util.Collection;

import org.eweb4j.spiderman.task.Task;


public interface DupRemovalPoint extends Point{

//	void context(Task task, Collection<String> newUrls);
	
	Collection<Task> removeDuplicateTask(Task task, Collection<String> newUrls, Collection<Task> tasks);
}
