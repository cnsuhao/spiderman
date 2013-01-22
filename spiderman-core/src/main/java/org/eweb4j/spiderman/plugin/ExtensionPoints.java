package org.eweb4j.spiderman.plugin;

import java.util.Arrays;
import java.util.Collection;

/**
 * 扩展点
 * @author weiwei
 *
 */
public class ExtensionPoints {

	public final static String task_poll = "task_poll";
	public final static String begin = "begin";
	public final static String fetch = "fetch";
	public final static String dig = "dig";
	public final static String dup_removal = "dup_removal";
	public final static String task_sort = "task_sort";
	public final static String task_push = "task_push";
	public final static String target = "target";
	public final static String parse = "parse";
	public final static String pojo = "pojo";
	public final static String end = "end";
	
	public static String getPointImplClassName(String point){
		if (task_poll.equals(point))
			return "spiderman.plugin.impl.TaskPollPointImpl";
		if (begin.equals(point))
			return "spiderman.plugin.impl.BeginPointImpl";
		if (fetch.equals(point))
			return "spiderman.plugin.impl.FetchPointImpl";
		if (dig.equals(point))
			return "spiderman.plugin.impl.DigPointImpl";
		if (dup_removal.equals(point))
			return "spiderman.plugin.impl.DupRemovalPointImpl";
		if (task_sort.equals(point))
			return "spiderman.plugin.impl.TaskSortPointImpl";
		if (task_push.equals(point))
			return "spiderman.plugin.impl.TaskPushPointImpl";
		if (target.equals(point))
			return "spiderman.plugin.impl.TargetPointImpl";
		if (parse.equals(point))
			return "spiderman.plugin.impl.ParsePointImpl";
		if (end.equals(point))
			return "spiderman.plugin.impl.EndPointImpl";
		
		return null;
	}
	
	public static boolean contains(String name){
		return task_poll.equals(name) || begin.equals(name) || fetch.equals(name) || dig.equals(name) || dup_removal.equals(name) || task_sort.equals(name) || task_push.equals(name) || target.equals(name) || parse.equals(name) || pojo.equals(name) || end.equals(name) ;
	}
	
	public static String string(){
		return "[" + task_poll + ", "+ begin + ", " + fetch + ", " + dig + ", " + dup_removal + ", "+ task_sort +", "+ task_push + ", " +  target + ", " + parse + ", "+ pojo + ", "+ end +"]" ;
	}
	
	public static Collection<String> toArray(){
		return Arrays.asList(task_poll, begin, fetch, dig, dup_removal, task_sort, task_push, target, parse, pojo, end);
	}
}
