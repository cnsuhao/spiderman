package org.eweb4j.spiderman.plugin;

import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.task.Task;


public interface EndPoint extends Point{

	void context(Task task, List<Map<String, Object>> models) throws Exception;
	
	List<Map<String, Object>> complete(List<Map<String, Object>> models) throws Exception;

}
