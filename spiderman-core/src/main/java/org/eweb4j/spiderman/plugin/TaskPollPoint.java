package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.task.Task;


public interface TaskPollPoint extends Point{

	Task pollTask() throws Exception;

}
