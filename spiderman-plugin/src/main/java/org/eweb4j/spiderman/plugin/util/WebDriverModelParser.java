package org.eweb4j.spiderman.plugin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Field;
import org.eweb4j.spiderman.xml.Parsers;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.xml.Attrs;
import org.eweb4j.util.xml.Tags;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.function.CommonFunction;
import com.greenpineyu.fel.function.Function;

public class WebDriverModelParser implements ModelParser{

	private Task task = null;
	private Target target = null;
	private SpiderListener listener = null;
	private FelEngine fel = new FelEngineImpl();
	private Map<String, Object> finalFields = null;
	private Map<String, Object> beforeModel = null;
	private WebDriver client;
	   
	public void setFinalFields(Map<String, Object> finalFields) {
	  this.finalFields = finalFields;
	}
	
	public void setBeforeModel(Map<String, Object> beforeModel) {
	    this.beforeModel = beforeModel;
	}
	
	private final static Function fun = new CommonFunction() {
		public String getName() {
			return "$output";
		}

		public Object call(Object[] arguments) {
			Object node = arguments[0];
			boolean keepHeader = false;
			if (arguments.length > 2)
				keepHeader = (Boolean) arguments[1];
			
			return ParserUtil.xml(node, keepHeader);
		}
	};
	
	public void init(Task task, Target target, SpiderListener listener){
		this.task = task;
		this.target = target;
		this.listener = listener;
		this.client = (WebDriver) task.site.fetcher.getClient();
		
    	fel.addFun(fun);
    	Tags $Tags = Tags.me();
    	Attrs $Attrs = Attrs.me();
    	fel.getContext().set("$Tags", $Tags);
    	fel.getContext().set("$Attrs", $Attrs);
    	fel.getContext().set("$Util", CommonUtil.class);
    	fel.getContext().set("$ParserUtil", ParserUtil.class);
		fel.getContext().set("$target", this.target);
		fel.getContext().set("$listener", this.listener);
		fel.getContext().set("$task_url", this.task.url);
		fel.getContext().set("$source_url", this.task.sourceUrl);
		fel.getContext().set("$Task", this.task);
        fel.getContext().set("$Site", this.task.site);
        fel.getContext().set("$Fetcher", this.task.site.fetcher);
	}
	
	public WebDriverModelParser() {}
	
	public WebDriverModelParser(Task task, Target target, SpiderListener listener) {
		init(task, target, listener);
	}
	
	public List<Map<String, Object>> parse(Page page) throws Exception {
	    //允许设置延迟解析时间，这招对那些动态解析的网页非常有效！
	    String delay = this.target.getModel().getDelay();
	    if (!CommonUtil.isBlank(delay)) {
	        listener.onInfo(Thread.currentThread(), task, "wait "+delay+" for model parser");
	        Thread.currentThread().sleep(CommonUtil.toSeconds(delay).intValue()*1000);
	        listener.onInfo(Thread.currentThread(), task, "now begin to parse the model content");
	    }
	    
		String contentType = this.target.getModel().getCType();
		if (contentType == null || contentType.trim().length() == 0)
			contentType = page.getContentType();
		
		if (contentType == null)
			contentType = "text/html";
		
		boolean isJson = "json".equalsIgnoreCase(contentType) || contentType.contains("text/json") || contentType.contains("application/json");
		if (isJson)
		    return parseJson(page);
		
		return parseContent(page);
	}

	private List<Map<String, Object>> parseJson(Page page) throws Exception {
		String content = page.getContent();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String isModelArray = target.getModel().getIsArray();
		final List<Field> fields = target.getModel().getField();
		if ("1".equals(isModelArray) || "true".equals(isModelArray)) {
			List<Map> models = CommonUtil.parseArray(content, Map.class);
			for (Map model : models){
				list.add(parseJsonMap(model, fields));
			}
		}else{
			Map model = CommonUtil.parse(content, Map.class);
			list.add(parseJsonMap(model, fields));
		}
		return list;
	}
	
	private Map<String, Object> parseJsonMap(Map item, final List<Field> fields){
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		if (this.beforeModel != null) {
		    fel.getContext().set("$before", new HashMap<String, Object>(this.beforeModel));
		}
		
		fel.getContext().set("$fields", map);
		for (Field field : fields){
			String key = field.getName();
			String isArray = field.getIsArray();
			String isMergeArray = field.getIsMergeArray();
			String isTrim = field.getIsTrim();
			String isParam = field.getIsParam();
			String isFinal = field.getIsFinal();
			String isForDigNewUrl = field.getIsForDigNewUrl();
			boolean isFinalParam = ("1".equals(isParam) || "true".equals(isParam)) && ("1".equals(isFinal) || "true".equals(isFinal));
			if (isFinalParam && finalFields != null && finalFields.containsKey(key))
				continue;
			
			Parsers parsers = field.getParsers();
			if (parsers == null)
				continue;
			
			List<org.eweb4j.spiderman.xml.Parser> parserList = parsers.getParser();
			if (parserList == null || parserList.isEmpty())
				continue;
			
			//field最终解析出来的结果
			List<Object> values = new ArrayList<Object>();
			for (int i = 0; i < parserList.size(); i++) {
				org.eweb4j.spiderman.xml.Parser parser = parserList.get(i);
				String skipErr = parser.getSkipErr();
//				String attribute = parser.getAttribute();
				String exp = parser.getExp();
				String regex = parser.getRegex();
				String skipRgxFail = parser.getSkipRgxFail();
				try {
					//第一步获得的是一个List<String>对象，交给下面的步骤进行解析
					List<Object> newValues = new ArrayList<Object>();
					for (Object nodeVal : values){
						newValues.add(nodeVal.toString());
					}
					//正则
					parseByRegex(regex, skipRgxFail, newValues);
					// EXP表达式
					fel.getContext().set("$this", item);
					parseByExp(exp, newValues);
					
					if (!newValues.isEmpty()) {
						values.clear();
						values.addAll(newValues);
					}
				} catch (Throwable e) {
					if ("1".equals(skipErr) || "true".equals(skipErr))
						continue;
					String parserInfo = CommonUtil.toJson(parser);
					String err = "parser->" + parserInfo + " of field->" + key +" failed";
					listener.onError(Thread.currentThread(), task, err, e);
				}
			}
			
			try {
				if (values.isEmpty()) 
					values.add("");
				
				// 相同 key，若values不为空，继续沿用
				if (map.containsKey(key)){
					//将原来的值插入到前面
					Object obj = map.get(key);
					if (obj instanceof Collection) {
						values.addAll(0, (Collection<?>) obj);
					} else {
						values.add(0, obj);
					}
				}
				
				//数组的话，需要去除空元素
				if (values.size() >= 2){
					List<Object> noRepeatValues = new ArrayList<Object>();
					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
						Object obj = it.next();
						if (obj instanceof String) {
							if (((String)obj) == null || ((String)obj).trim().length() == 0)
								continue;
						}
						
						noRepeatValues.add(obj);
					}
					values.clear();
					values.addAll(noRepeatValues);
				}
				
				//如果设置了trim
				if ("1".equals(isTrim) || "true".equals(isTrim)) {
					List<String> results = new ArrayList<String>(values.size());
					for (Object obj : values){
						results.add(String.valueOf(obj).trim());
					}
					values.clear();
					values.addAll(results);
				}
				
				//如果是DigNewUrl
				if ("1".equals(isForDigNewUrl) || "true".equals(isForDigNewUrl)) {
					if ("1".equals(isArray)){
						for (Object val : values){
							task.digNewUrls.add(String.valueOf(val));
						}
					}else{
						if (!values.isEmpty())
							task.digNewUrls.add(String.valueOf(values.get(0)));
					}
				}
				
				Object value = null;
				if ("1".equals(isArray) || "true".equals(isArray)){
					List<Object> newValues = new ArrayList<Object>();
					for (Object val : values){
						if (values.size() == 1 && val.getClass().isArray()){
							Object[] newVals = (Object[])val;
							for (Object nv : newVals){
								if (nv == null || String.valueOf(nv).trim().length() == 0)
									continue;
								newValues.add(nv);
							}
						}
					}
					if (!newValues.isEmpty()){
						values.clear();
						values.addAll(newValues);
					}
					value = values;
					if ("1".equals(isMergeArray) || "true".equals(isMergeArray)){
						StringBuilder sb = new StringBuilder();
						for (Object val : values){
							sb.append(String.valueOf(val));
						}
						value = sb.toString();
					}else
						value = values;
				}else{
					if (values.isEmpty())
						value = "";
					else
						value = values.get(0);
				}
				
				if(isFinalParam){
					finalFields.put(key, value);
				}

				//最终完成
				map.put(key, value);
				
			} catch (Throwable e) {
				listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
	}
	
	private List<Map<String, Object>> parseContent(Page page) throws Exception{
	    fel.getContext().set("$page_content", ParserUtil.checkUnicodeString(page.getContent()));
        final List<Field> fields = target.getModel().getField();
		String isModelArray = target.getModel().getIsArray();
		String modelXpath = target.getModel().getXpath();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		if ("1".equals(isModelArray) || "true".equals(isModelArray)){
//		    List<WebElement> nodes = client.findElements(By.xpath(modelXpath));
		    List<WebElement> nodes = promiseForXpath(client, modelXpath);
	        if (nodes != null && nodes.size() > 0){
	            int size = nodes.size();
		        for (int i = 0; i < size; i++) {
		            Map<String, Object> map = parseToMap(nodes.get(i), fields);
		            try {
		                this.listener.onParseOne(Thread.currentThread(), task, size, i, map);
		                list.add(map);
		            } catch (Throwable e) {
		                this.listener.onError(Thread.currentThread(), task, "throw an exception on parse one data.", e);
		            }
		        }
	        }
		} else if (!CommonUtil.isBlank(modelXpath)) {
//		    WebElement ele = client.findElement(By.xpath(modelXpath));
		    WebElement ele = promise2ForXpath(client, modelXpath);
		    fel.getContext().set("$model_content", ParserUtil.checkUnicodeString(ele.getAttribute("outerHTML")));
			list.add(parseToMap(ele, fields));
		} else {
		    list.add(parseToMap(client, fields));
		}
		return list;
	}
	
	private Map<String, Object> parseToMap(SearchContext selector, final List<Field> fields) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		if (this.beforeModel != null) {
            fel.getContext().set("$before", new HashMap<String, Object>(this.beforeModel));
        }
		fel.getContext().set("$fields", map);
		for (Field field : fields){
			String key = field.getName();
			//是否数组
			String isArray = field.getIsArray();
			//是否合并数组
			String isMergeArray = field.getIsMergeArray();
			String isTrim = field.getIsTrim();
			String isParam = field.getIsParam();
			String isFinal = field.getIsFinal();
			String isForDigNewUrl = field.getIsForDigNewUrl();
			
			boolean isFinalParam = ("1".equals(isParam) || "true".equals(isParam)) && ("1".equals(isFinal) || "true".equals(isFinal));
			if (isFinalParam && finalFields != null && finalFields.containsKey(key))
				continue;
			
			Parsers parsers = field.getParsers();
			if (parsers == null)
				continue;
			
			List<org.eweb4j.spiderman.xml.Parser> parserList = parsers.getParser();
			if (parserList == null || parserList.isEmpty())
				continue;
			
			//field最终解析出来的结果
			List<Object> values = new ArrayList<Object>();
			for (int i = 0; i < parserList.size(); i++) {
				org.eweb4j.spiderman.xml.Parser parser = parserList.get(i);
				String skipErr = parser.getSkipErr();
				String xpath = parser.getXpath();
				String attribute = parser.getAttribute();
				String exp = parser.getExp();
				String regex = parser.getRegex();
				String skipRgxFail = parser.getSkipRgxFail();
				try {
					if (xpath != null && xpath.trim().length() > 0) {
						
						if (attribute != null && attribute.trim().length() > 0){
//						    List<WebElement> nodes = selector.findElements(By.xpath(xpath));
						    List<WebElement> nodes = promiseForXpath(selector, xpath);
	                        if (nodes == null || nodes.isEmpty())
	                            continue;
							for (int j = 0; j < nodes.size(); j++){
								WebElement node = nodes.get(j);
								String attrVal = node.getAttribute(attribute);
								values.add(attrVal);
							}
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						}else if (xpath.endsWith("/text()")){
						    String _xpath = xpath.replace("/text()", "");
						    List<WebElement> _nodes = promiseForXpath(selector, _xpath);
						    
						    if (_nodes == null || _nodes.isEmpty())
                                continue;
                            for (int j = 0; j < _nodes.size(); j++){
                                WebElement node = _nodes.get(j);
                                String textValue = node.getText();
                                values.add(textValue);
                            }
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						} else {
//						    List<WebElement> nodes = selector.findElements(By.xpath(xpath));
						    List<WebElement> nodes = promiseForXpath(selector, xpath);
	                        if (nodes == null || nodes.isEmpty())
	                            continue;
							for (int j = 0; j < nodes.size(); j++){
								WebElement node = nodes.get(j);
								values.add(node);
							}
							// 此种方式获取到的Node节点大部分都不是字符串，因此先执行表达式后执行正则
							// EXP表达式
							parseByExp(exp, values);
							//正则
							parseByRegex(regex, skipRgxFail, values);
						}
					}else{
						List<Object> newValues = new ArrayList<Object>(values.size());
						for (Object obj : values){
							newValues.add(obj.toString());
						}
						//正则
						parseByRegex(regex, skipRgxFail, newValues);
						// EXP表达式
						parseByExp(exp, newValues);
						
						if (!newValues.isEmpty()) {
							values.clear();
							values.addAll(newValues);
						}
					}
				} catch (Throwable e) {
					if ("1".equals(skipErr) || "true".equals(skipErr))
						continue;
					listener.onError(Thread.currentThread(), task, "key->"+key +" parse failed cause->"+e.toString(), e);
				}
			}
			
			try {
				if (values.isEmpty()) 
					values.add("");
				
				// 相同 key，若values不为空，继续沿用
				if (map.containsKey(key)){
					//将原来的值插入到前面
					Object obj = map.get(key);
					if (obj instanceof Collection) {
						values.addAll(0, (Collection<?>) obj);
					} else {
						values.add(0, obj);
					}
				}
				
				//数组的话，需要去除空元素
				if (values.size() >= 2){
					List<Object> noRepeatValues = new ArrayList<Object>();
					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
						Object obj = it.next();
						if (obj instanceof String) {
							if (((String)obj) == null || ((String)obj).trim().length() == 0)
								continue;
						}
						
						noRepeatValues.add(obj);
					}
					values.clear();
					values.addAll(noRepeatValues);
				}
				
				//如果设置了trim
				if ("1".equals(isTrim) || "true".equals(isTrim)) {
					List<String> results = new ArrayList<String>(values.size());
					for (Object obj : values){
						results.add(String.valueOf(obj).trim());
					}
					values.clear();
					values.addAll(results);
				}
				
				//如果是DigNewUrl
				if ("1".equals(isForDigNewUrl) || "true".equals(isForDigNewUrl)) {
					if ("1".equals(isArray)){
						for (Object val : values){
							task.digNewUrls.add(String.valueOf(val));
						}
					}else{
						if (!values.isEmpty())
							task.digNewUrls.add(String.valueOf(values.get(0)));
					}
				}
				
				Object value = null;
				if ("1".equals(isArray) || "true".equals(isArray)){
					List<Object> newValues = new ArrayList<Object>();
					for (Object val : values){
						if (values.size() == 1 && val.getClass().isArray()){
							Object[] newVals = (Object[])val;
							for (Object nv : newVals){
								if (nv == null || String.valueOf(nv).trim().length() == 0)
									continue;
								newValues.add(nv);
							}
						}
					}
					if (!newValues.isEmpty()){
						values.clear();
						values.addAll(newValues);
					}
					value = values;
					if ("1".equals(isMergeArray) || "true".equals(isMergeArray)){
						StringBuilder sb = new StringBuilder();
						for (Object val : values){
							sb.append(String.valueOf(val));
						}
						value = sb.toString();
					}else
						value = values;
				}else{
					if (values.isEmpty())
						value = "";
					else
						value = values.get(0);
				}
				
				if(isFinalParam){
					finalFields.put(key, value);
				}

				//最终完成
				map.put(key, value);
				listener.onParseField(Thread.currentThread(), task, selector, key, value);
			} catch (Throwable e) {
				listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
	}

	private WebElement promise2ForXpath(SearchContext selector, String _xpath) {
        WebElement _node = null;
        while (true) {
            try {
                _node = selector.findElement(By.xpath(_xpath));
                break;
            } catch (Throwable e) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e1) {
                }
                continue;
            }
        }
        return _node;
    }
	
    private List<WebElement> promiseForXpath(SearchContext selector, String _xpath) {
        List<WebElement> _nodes = null;
        while (true) {
            try {
                _nodes = selector.findElements(By.xpath(_xpath));
                break;
            } catch (Throwable e) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e1) {
                }
                continue;
            }
        }
        return _nodes;
    }
	
	private void parseByExp(String exp, Collection<Object> list) {
		if (exp == null || exp.trim().length() == 0)
			return ;
		
		List<Object> newValue = new ArrayList<Object>();
		if (list == null || list.isEmpty()){
			try {
	    		Object newVal = fel.eval(exp);
				if (newVal != null) {
					if (newVal instanceof Collection)
						newValue.addAll((Collection<?>)newVal);
					else
						newValue.add(newVal);
				}
			} catch (Throwable e){
				listener.onError(Thread.currentThread(), task, "exp->"+exp+" eval failed", e);
			}
		} else {
			for (Object val : list){
				boolean isValBlank = false;
				if (val != null){
					if (val instanceof String && ((String)val).trim().length() == 0){
						isValBlank = true;
					}else {
						fel.getContext().set("$this", val);
					}
				}
				try {
		    		Object newVal = fel.eval(exp);
					if (newVal != null) {
						if (newVal instanceof Collection)
							newValue.addAll((Collection<?>)newVal);
						else
							newValue.add(newVal);
					}
				} catch (Throwable e){
					if (!isValBlank)
						listener.onError(Thread.currentThread(), task, "exp->"+exp+" eval failed", e);
				} finally {
					fel.getContext().set("$this", "");//解析完表达式之后要重置这个this变量
				}
			}
		}
		
		if (!newValue.isEmpty()){
			list.clear();
			list.addAll(newValue);
		}
	}
	
	private void parseByRegex(String regex, String skipRgxFail, Collection<Object> list) {
		if (regex == null || regex.trim().length() == 0)
			return ;
		List<Object> newVals = new ArrayList<Object>(list.size());
		for (Object obj : list) {
			try {
				String input = (String)obj;
				if (input == null || input.trim().length() == 0)
					continue;
				List<String> vals = CommonUtil.findByRegex(input, regex);
				//如果REGEX找不到
				if (vals == null) {
					if ("1".equals(skipRgxFail) || "true".equals(skipRgxFail))
						continue;
					
					newVals.add("");
				} else {
					for (String val : vals){
						if (val == null || val.trim().length() == 0){
							if ("1".equals(skipRgxFail) || "true".equals(skipRgxFail))
								continue;
							val = "";
						}
						newVals.add(val);
					}
				}
			} catch (Throwable e){
				listener.onError(Thread.currentThread(), task, "regex->"+regex+" of "+obj+" parse failed", e);
				if ("1".equals(skipRgxFail) || "true".equals(skipRgxFail))
					continue;
				newVals.add("");
			}
		}
		
		if (!newVals.isEmpty()){
			list.clear();
			list.addAll(newVals);
		}
	}

}
