package org.eweb4j.spiderman.plugin.util;

import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Target;

public interface ModelParser {

    public void init(Task task, Target target, SpiderListener listener);
    
    public void setFinalFields(Map<String, Object> finalFields);
    
    public void setBeforeModel(Map<String, Object> beforeModel);
    
    public List<Map<String, Object>> parse(Page page) throws Exception;
    
}
