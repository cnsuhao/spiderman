package org.eweb4j.spiderman.plugin;

import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Target;


public interface TargetPoint extends Point{

//	void context(Task task) throws Exception;
	Target confirmTarget(Task task, Target target) throws Exception;

}
