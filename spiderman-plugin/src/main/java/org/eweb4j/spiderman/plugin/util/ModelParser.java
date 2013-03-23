package org.eweb4j.spiderman.plugin.util;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.xpath.XPathFactoryImpl;

import org.eweb4j.spiderman.fetcher.Page;
import org.eweb4j.spiderman.spider.SpiderListener;
import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.xml.Field;
import org.eweb4j.spiderman.xml.NSMap;
import org.eweb4j.spiderman.xml.Namespaces;
import org.eweb4j.spiderman.xml.Parsers;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.xml.Attrs;
import org.eweb4j.util.xml.Tags;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.function.CommonFunction;
import com.greenpineyu.fel.function.Function;

public class ModelParser extends DefaultHandler{

	private Task task = null;
	private Target target = null;
	private SpiderListener listener = null;
	private FelEngine fel = new FelEngineImpl();
	private Map<String, Object> finalFields = null;
	   
	public Map<String, Object> getFinalFields() {
	  return this.finalFields;
	}
	public void setFinalFields(Map<String, Object> finalFields) {
	  this.finalFields = finalFields;
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
	
	private void init(Task task, Target target, SpiderListener listener){
		this.task = task;
		this.target = target;
		this.listener = listener;
		
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
	}
	
	public ModelParser(Task task, Target target, SpiderListener listener) {
		init(task, target, listener);
	}
	
	public List<Map<String, Object>> parse(Page page) throws Exception{
		String contentType = this.target.getModel().getCType();
		if (contentType == null || contentType.trim().length() == 0)
			contentType = page.getContentType();
		if (contentType == null)
			contentType = "text/html";
		boolean isXml = "xml".equalsIgnoreCase(contentType) || contentType.contains("text/xml") || contentType.contains("application/rss+xml") || contentType.contains("application/xml");
		if (isXml)
			return parseXml(page, false);
		else {
			String isForceUseXmlParser = this.target.getModel().getIsForceUseXmlParser();
			if (!"1".equals(isForceUseXmlParser))
				return parseHtml(page);
			HtmlCleaner cleaner = new HtmlCleaner();
			cleaner.getProperties().setTreatUnknownTagsAsContent(true);
			TagNode rootNode = cleaner.clean(page.getContent());
			String xml = ParserUtil.xml(rootNode, true);
			page.setContent(xml);
			return parseXml(page, true);
		}
	}

	private List<Map<String, Object>> parseXml(Page page, boolean isFromHtml) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(!isFromHtml); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        String validXml = ParserUtil.checkUnicodeString(page.getContent());
        fel.getContext().set("$page_content", validXml);
    	Document doc = builder.parse(new ByteArrayInputStream(validXml.getBytes()));
        XPathFactory xfactory = XPathFactoryImpl.newInstance();
        XPath xpathParser = xfactory.newXPath();
        //设置命名空间
        xpathParser.setNamespaceContext(new NamespaceContext() {
            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }
            public Iterator<?> getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
			public String getNamespaceURI(String prefix) {
				if (prefix == null) 
					throw new NullPointerException("Null prefix");
				else {
		        	Namespaces nss = target.getModel().getNamespaces();
		        	if (nss != null) {
			        	List<NSMap> nsList = nss.getNamespace();
			        	if (nsList != null) {
				        	for (NSMap ns : nsList){
				        		if (prefix.equals(ns.getPrefix()))
				        			return ns.getUri();
				        	}
			        	}
		        	}
		        }
				
				try {
					return "http://www." + new URI(task.site.getUrl()).getHost();
				} catch (URISyntaxException e) {
					return task.site.getUrl();
				}
//		        return XMLConstants.NULL_NS_URI;
			}
		});
        
        final List<Field> fields = target.getModel().getField();
		String isModelArray = target.getModel().getIsArray();
		String modelXpath = target.getModel().getXpath();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if ("1".equals(isModelArray) || "tre".equals(isModelArray)){
			XPathExpression expr = xpathParser.compile(modelXpath);
	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//		    listener.onInfo(Thread.currentThread(), task, "modelXpath -> " + modelXpath + " parse result -> " + result);
	        if (result != null){
		        NodeList nodes = (NodeList) result;
		        if (nodes.getLength() > 0){
			        for (int i = 0; i < nodes.getLength(); i++) {
						list.add(parseXmlMap(nodes.item(i), xpathParser, fields));
			        }
		        }
	        }
		}else{
			list.add(parseXmlMap(doc, xpathParser, fields));
		}
		return list;
	}
	
	private Map<String, Object> parseXmlMap(Object item, XPath xpathParser, final List<Field> fields) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		
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
						
						XPathExpression expr = xpathParser.compile(xpath);
				        Object result = expr.evaluate(item, XPathConstants.NODESET);
				        
						if (result == null)
							continue;
						
						NodeList nodes = (NodeList) result;
						if (nodes.getLength() == 0)
							continue;
						
						if (attribute != null && attribute.trim().length() > 0){
							for (int j = 0; j < nodes.getLength(); j++){
								Node node = nodes.item(j);
								Element e = (Element)node;
								String attrVal = e.getAttribute(attribute);
								values.add(attrVal);
							}
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						}else if (xpath.endsWith("/text()")){
							for (int j = 0; j < nodes.getLength(); j++){
								Node node = nodes.item(j);
								values.add(node.getNodeValue());
							}
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						} else {
							for (int j = 0; j < nodes.getLength(); j++){
								Node node = nodes.item(j);
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
				} catch (Exception e) {
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
				
				//最终完成
				if ("1".equals(isArray)){
					if ("1".equals(isMergeArray)){
						StringBuilder sb = new StringBuilder();
						for (Object val : values){
							sb.append(String.valueOf(val));
						}
						map.put(key, sb.toString());
					}else
						map.put(key, values);
				} else {
					if (values.isEmpty())
						map.put(key, "");
					else
						map.put(key, new ArrayList<Object>(values).get(0));
				}
				
				if(isFinalParam){
					finalFields.put(key, map.get(key));
				}
			} catch (Exception e) {
				listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
	}
	
	private List<Map<String, Object>> parseHtml(Page page) throws Exception{
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		String html = page.getContent();
		TagNode rootNode = cleaner.clean(html);
		fel.getContext().set("$page_content", html);
		
        final List<Field> fields = target.getModel().getField();
		String isModelArray = target.getModel().getIsArray();
		String modelXpath = target.getModel().getXpath();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if ("1".equals(isModelArray) || "tre".equals(isModelArray)){
			Object[] nodeVals = rootNode.evaluateXPath(modelXpath);
	        if (nodeVals != null && nodeVals.length > 0){
		        for (int i = 0; i < nodeVals.length; i++) {
					list.add(parseHtmlMap(nodeVals[i], fields));
		        }
	        }
		}else{
			list.add(parseHtmlMap(rootNode, fields));
		}
		
		return list;
	}
	
	private Map<String, Object> parseHtmlMap(Object item, final List<Field> fields){
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		
		fel.getContext().set("$fields", map);
		
		for (Field field : fields){
			String key = field.getName();
			String isArray = field.getIsArray();
			String isTrim = field.getIsTrim();
			String isParam = field.getIsParam();
			String isFinal = field.getIsFinal();
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
						TagNode tagNode = (TagNode)item;
						Object[] nodeVals = tagNode.evaluateXPath(xpath);
						if (nodeVals == null || nodeVals.length == 0)
							continue;
						
						if (attribute != null && attribute.trim().length() > 0){
							for (Object nodeVal : nodeVals){
								TagNode node = (TagNode)nodeVal;
								String attrVal = node.getAttributeByName(attribute);
								values.add(attrVal);
							}
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						}else if (xpath.endsWith("/text()")){
							for (Object nodeVal : nodeVals){
								values.add(nodeVal.toString());
							}
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
							
							// EXP表达式
							parseByExp(exp, values);
						}else {
							for (Object nodeVal : nodeVals){
								TagNode node = (TagNode)nodeVal;
								values.add(node);
							}
							
							// 此种方式获取到的Node节点大部分都不是字符串，因此先执行表达式后执行正则
							// EXP表达式
							parseByExp(exp, values);
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
						}
					}else {
						
						//第一步获得的是一个List<String>对象，交给下面的步骤进行解析
						List<Object> newValues = new ArrayList<Object>();
						for (Object nodeVal : values){
							newValues.add(nodeVal.toString());
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
				} catch (Exception e) {
					if ("1".equals(skipErr) || "true".equals(skipErr))
						continue;
					
					listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
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
				
				if (values.isEmpty()) 
					values.add("");
				
				//最终解析完成
				if ("1".equals(isArray)){
					map.put(key, values);
				}else{
					map.put(key, values.get(0).toString());
				}
				
				if(isFinalParam){
					finalFields.put(key, map.get(key));
				}
			} catch (Exception e) {
				listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
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
			} catch (Exception e){
//				listener.onError(Thread.currentThread(), task, "exp->"+exp+" eval failed", e);
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
				} catch (Exception e){
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
			} catch (Exception e){
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

	public static void main(String[] args) throws Exception{
//		File file = new File("C:/Users/vivi/Downloads/9000425.xml");
//		String xml = FileUtil.readFile(file);
//		System.setProperty("javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true); // never forget this!
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
//        XPathFactory xfactory = XPathFactoryImpl.newInstance();
//        XPath xpath = xfactory.newXPath();
//        XPathExpression expr = xpath.compile("//item");
//        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//        NodeList nodes = (NodeList) result;
//        
//        FelEngine fel = new FelEngineImpl();
//        for (int i = 0; i < nodes.getLength(); i++) {
//        	if (i > 0)
//        		break;
//            
//        	NodeList subs = (NodeList)xpath.compile("deal:image/text()").evaluate(nodes.item(i), XPathConstants.NODESET);
//        	if (subs == null || subs.getLength() == 0)
//             	continue;
//            for (int j = 0; j < subs.getLength(); j++) {
//            	Node item = subs.item(j);
//             	String value = item.getNodeValue();
//             	System.out.println(value);
//            }
             
//        	FelContext ctx = fel.getContext();
//        	ctx.set("$this", node);
//        	Tags $Tags = Tags.me();
//        	Attrs $Attrs = Attrs.me();
//			ctx.set("$Tags", $Tags);
//			ctx.set("$Attrs", $Attrs);
//    		
//			System.out.println($Attrs.xml(ParserUtil.xml(node, false)).rm("style").Tags().kp("p").ok());
//    		
//    		System.out.println(fel.eval("$Attrs.xml($output($this)).rm('style').Tags().kp('p').ok()"));
//    		
//    		Object newVal =  MVEL.eval("org.eweb4j.util.CommonUtil.toXml($this, false)", ctx);
//    		System.out.println(newVal);
//        }

//        	
           
//        }
//        System.out.println("count->"+count);
        
		HtmlCleaner cleaner = new HtmlCleaner();
		cleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\" /><title>★㊣最新の日本同步nike等新作♂02.27♀</title><style>body{ arial,宋体; background:#fff; color:#000; margin:0px; text-align:center;}A:link {. COLOR: #069 ;}a{ color:#333; text-decoration:none;}#main{ width:88%; padding:1px; border:1px solid #000;}#header{ text-align:left; border:1px solid #000; padding:0px;}#content{ text-align:left; padding:5px;}#footer{ margin-top:5px; border:1px solid #000;}</style></head><body><div id=\"main\"><div id=\"header\"><script language=javascript src=\"ad1.js\"></script></div><div id=\"content\">BLK090 kira☆kira BLACK GAL パンチラ黒ギャル女子校生 生姦JK連続中出しハイスクール 茉莉もも<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/blk090/blk090pl.jpg\" /><br />AVI 966m<br /><a href=\"http://www3.17domn.com/bt9/file.php/MIU1BUQ.html\" target=_blank>http://www3.17domn.com/bt9/file.php/MIU1BUQ.html</a><br /><br />DV1482 美魔女の母性「愛」 小森愛<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/53dv1482/53dv1482pl.jpg\" /><br />AVI 815m<br /><a href=\"http://www3.17domn.com/bt9/file.php/MIU1CQG.html\" target=_blank>http://www3.17domn.com/bt9/file.php/MIU1CQG.html</a><br /><br />DV1485 デカチン味くらべ 麻美ゆま<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/53dv1485/53dv1485pl.jpg\" /><br />AVI 977m<br /><a href=\"http://www3.17domn.com/bt9/file.php/MIU1DNF.html\" target=_blank>http://www3.17domn.com/bt9/file.php/MIU1DNF.html</a><br /><br />DVDES-591 中文字幕_究極の中出し近親相姦企画 最愛の息子に伝えたい… 妊娠させるための性教育 3<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/digital/video/1dvdes00591/1dvdes00591pl.jpg\" /><br />AVI 1.05G<br /><a href=\"http://www3.17domn.com/bt9/file.php/MIU1EGH.html\" target=_blank>http://www3.17domn.com/bt9/file.php/MIU1EGH.html</a><br /><br />DVDES-593 中文字幕_奇跡の本物●●●空キャビンアテンダント 梶井ほたる（旧姓&amp;#12539;荻野）29歳 入籍5日目！ 旦那様には絶対言えない最初で最後の隠し事は…「主人以外の、しかも3K（臭い&amp;#12539;汚い&amp;#12539;危険）チ●ポでイってしまったことです…。」 <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/digital/video/1dvdes00593/1dvdes00593pl.jpg\" /><br />AVI 1.26G<br /><a href=\"http://www3.17domn.com/bt9/file.php/MIU1FAE.html\" target=_blank>http://www3.17domn.com/bt9/file.php/MIU1FAE.html</a><br /><br />FSET-408 中文字幕_博多弁訛りの彼女の親友とバレないようにやっちゃった俺<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/digital/video/1fset00408/1fset00408pl.jpg\" /><br />AVI 1.34G<br /><a href=\"http://www3.17domn.com/bt9/file.php/MIU1G2R.html\" target=_blank>http://www3.17domn.com/bt9/file.php/MIU1G2R.html</a><br /><br />inu055 従順ペット候補生 ＃35 桜花りり <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/118inu055/118inu055pl.jpg\" /><br />AVI 1.06G<br /><a href=\"http://www3.kidown.com/bt5/file.php/MIU1BZZ.html\" target=_blank>http://www3.kidown.com/bt5/file.php/MIU1BZZ.html</a><br /><br />jarm013 リメイク版～舐められ倶楽部3 《ベロ長美人&amp;#12539;さとう遥希》<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/11jarm013so/11jarm013sopl.jpg\" /><br />AVI 844m<br /><a href=\"http://www3.kidown.com/bt5/file.php/MIU1CVW.html\" target=_blank>http://www3.kidown.com/bt5/file.php/MIU1CVW.html</a><br /><br />JUC-974 中文字幕_犯された人妻コンビニ店長 ～売上げと引き換えに弄ばれた身体～ 当真ゆき <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/juc974/juc974pl.jpg\" /><br />AVI 993m<br /><a href=\"http://www3.kidown.com/bt5/file.php/MIU1DOZ.html\" target=_blank>http://www3.kidown.com/bt5/file.php/MIU1DOZ.html</a><br /><br />KAWD429 新人！kawaii*専属デビュ→ 明日も君に会おうかな☆ 蒼乃かな<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/kawd429/kawd429pl.jpg\" /><br />AVI 960m<br /><a href=\"http://www3.kidown.com/bt5/file.php/MIU1EID.html\" target=_blank>http://www3.kidown.com/bt5/file.php/MIU1EID.html</a><br /><br />MADA-063 中文字幕_カラダの疼きがおさまらない社宅妻 風間ゆみ <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84mada063/84mada063pl.jpg\" /><br />AVI 815m<br /><a href=\"http://www3.kidown.com/bt5/file.php/MIU1FDN.html\" target=_blank>http://www3.kidown.com/bt5/file.php/MIU1FDN.html</a><br /><br />MADA-067 中文字幕_借金に苦しむ人妻 有沢実紗<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84mada067/84mada067pl.jpg\" /><br />AVI 825m<br /><a href=\"http://www3.kidown.com/bt5/file.php/MIU1G7Y.html\" target=_blank>http://www3.kidown.com/bt5/file.php/MIU1G7Y.html</a><br /><br />MAMA322 親父の後妻が巨乳で裸族…<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/digital/video/49mama00322/49mama00322pl.jpg\" /><br />AVI 1.04G<br /><a href=\"http://www3.pidown.info/bf2/file.php/MIU1CBW.html\" target=_blank>http://www3.pidown.info/bf2/file.php/MIU1CBW.html</a><br /><br />MAS102 続&amp;#12539;素人娘、お貸しします。 VOL.64<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/118mas102/118mas102pl.jpg\" /><br />AVI 1.08G<br /><a href=\"http://www3.pidown.info/bf2/file.php/MIU1D6O.html\" target=_blank>http://www3.pidown.info/bf2/file.php/MIU1D6O.html</a><br /><br />mds735 純真 雪本芽衣<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84mds735/84mds735pl.jpg\" /><br />AVI 1.24G<br /><a href=\"http://www3.pidown.info/bf2/file.php/MIU1E1M.html\" target=_blank>http://www3.pidown.info/bf2/file.php/MIU1E1M.html</a><br /><br />MDS736 バス痴漢物語 成瀬心美<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84mds736/84mds736pl.jpg\" /><br />AVI 955m<br /><a href=\"http://www3.pidown.info/bf2/file.php/MIU1EVL.html\" target=_blank>http://www3.pidown.info/bf2/file.php/MIU1EVL.html</a><br /><br />mds738 学校で中出ししよッ 水樹うるは <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84mds738/84mds738pl.jpg\" /><br />AVI 1.31G<br /><a href=\"http://www3.pidown.info/bf2/file.php/MIU1FPF.html\" target=_blank>http://www3.pidown.info/bf2/file.php/MIU1FPF.html</a><br /><br />mds739 やさしい同級生の性教育 さとう遥希 つぼみ 木村つな<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84mds739/84mds739pl.jpg\" /><br />AVI 1.11G<br /><a href=\"http://www3.pidown.info/bf2/file.php/MIU1GJD.html\" target=_blank>http://www3.pidown.info/bf2/file.php/MIU1GJD.html</a><br /><br />OKSN130 息子の大きすぎるチンポが気になって… 新フェチモザイク 小早川怜子<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/oksn130/oksn130pl.jpg\" /><br />AVI 981m<br /><a href=\"http://www3.97down.info/qb/file.php/MIU1CFV.html\" target=_blank>http://www3.97down.info/qb/file.php/MIU1CFV.html</a><br /><br />otav001 痴女（デキ）るオンナ 志保<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/118otav001/118otav001pl.jpg\" /><br />AVI 966m<br /><a href=\"http://www3.97down.info/qb/file.php/MIU1DCN.html\" target=_blank>http://www3.97down.info/qb/file.php/MIU1DCN.html</a><br /><br />SCOP118 本番ありの裏風俗で、バックでついている時にこっそりゴムを外し、そのままドップリ生中出ししちゃいました！ 4<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84scop118/84scop118pl.jpg\" /><br />AVI 1.87G<br /><a href=\"http://www3.97down.info/qb/file.php/MIU1E7L.html\" target=_blank>http://www3.97down.info/qb/file.php/MIU1E7L.html</a><br /><br />SCOP119 妻が●●●をしている間に、目を盗んで自分に谷間やパンツを見せて誘惑する妻の友達。初めは冗談かと疑いつつも、完全に誘ってる？！すぐ隣にい���にも関わらず、淫らにカラダを交わらせる禁断の肉体関係。<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/84scop119/84scop119pl.jpg\" /><br />AVI 1.87G<br /><a href=\"http://www3.97down.info/qb/file.php/MIU1EYX.html\" target=_blank>http://www3.97down.info/qb/file.php/MIU1EYX.html</a><br /><br />sero0163 ２年ぶりの専属契約 仲丘たまき Tamaki Nakaoka <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/h_422sero0163/h_422sero0163pl.jpg\" /><br />AVI 1.09G<br /><a href=\"http://www3.97down.info/qb/file.php/MIU1FSL.html\" target=_blank>http://www3.97down.info/qb/file.php/MIU1FSL.html</a><br /><br />sero0164 すぐに破れるコンドーム×ＫＡＯＲＩ<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/h_422sero0164/h_422sero0164pl.jpg\" /><br />AVI 983m<br /><a href=\"http://www3.97down.info/qb/file.php/MIU1GOO.html\" target=_blank>http://www3.97down.info/qb/file.php/MIU1GOO.html</a><br /><br />SERO0165 性教育委員会 2-B 板野有紀<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/h_422sero0165/h_422sero0165pl.jpg\" /><br />AVI 1.01G<br /><a href=\"http://www3.wkdown.info/fs3/file.php/MIU1CMV.html\" target=_blank>http://www3.wkdown.info/fs3/file.php/MIU1CMV.html</a><br /><br />SS008 白いナースは黒いナースと絡み合う 巨乳外国人<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/18mess008/18mess008pl.jpg\" /><br />AVI 818G<br /><a href=\"http://www3.wkdown.info/fs3/file.php/MIU1DJC.html\" target=_blank>http://www3.wkdown.info/fs3/file.php/MIU1DJC.html</a><br /><br />star404 麻生希 性欲、覚醒 Rebirth <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/1star404/1star404pl.jpg\" /><br />AVI 1.03G<br /><a href=\"http://www3.wkdown.info/fs3/file.php/MIU1EDO.html\" target=_blank>http://www3.wkdown.info/fs3/file.php/MIU1EDO.html</a><br /><br />star408 初イキッ！！！ 吉川あいみ <br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/1star408/1star408pl.jpg\" /><br />AVI 1.19G<br /><a href=\"http://www3.wkdown.info/fs3/file.php/MIU1F5K.html\" target=_blank>http://www3.wkdown.info/fs3/file.php/MIU1F5K.html</a><br /><br />SW-158 中文字幕_夢の近親相姦！まだまだイケる母親のデカ尻とデカ乳に勃起した僕 父親の目を盗んでこっそり挿入させてくれる母<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/h_635sw158/h_635sw158pl.jpg\" /><br />AVI 1.11G<br /><a href=\"http://www3.wkdown.info/fs3/file.php/MIU1FYH.html\" target=_blank>http://www3.wkdown.info/fs3/file.php/MIU1FYH.html</a><br /><br />XV-1087 中文字幕_オグナナのヤリたいヤリすぎ◆男優オーディション！！ 小倉奈々<br /><IMG border=\"0\" src=\"http://pics.dmm.co.jp/mono/movie/adult/60xv1087/60xv1087pl.jpg\" /><br />AVI 1.01G<br /><a href=\"http://www3.wkdown.info/fs3/file.php/MIU1GUF.html\" target=_blank>http://www3.wkdown.info/fs3/file.php/MIU1GUF.html</a></div><div id=\"footer\"><script language=javascript src=\"ad2.js\"></script></div></div><br><br><br><br><br><br><br><br><br><br><STYLE type=text/css>A:hover {LEFT: 3pt; POSITION: relative; TOP: 3pt; VISIBILITY: visible}</STYLE></body></html>";
		TagNode tagNode = cleaner.clean(html);
//		String xml = ParserUtil.xml(tagNode,true);
//		System.out.println(xml);
		Object[] nodeList = tagNode.evaluateXPath("//img[@src]");
		for (Object n : nodeList){
			System.out.println(ParserUtil.xml(n, false));
		}
		
//		
//		System.setProperty("javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(false); // never forget this!
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
//        XPathFactory xfactory = XPathFactoryImpl.newInstance();
//        XPath xpath = xfactory.newXPath();
//        XPathExpression expr = xpath.compile("//div[@class='ep-pages']//a[@class='ep-pages-ctrl']");
//        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//        NodeList nodes = (NodeList) result;
//        for (int i = 0; i < nodes.getLength(); i++) {
//        	Node item = nodes.item(i);
//        	System.out.println("node->"+item);
//        	System.out.println("text->"+item.getTextContent());
//        	Element e = (Element)item;
//        	System.out.println("href->"+e.getAttribute("href"));
//         	System.out.println("xml->"+ParserUtil.xml(item, false));
//        }
        
//		Object[] nodeVals = tagNode.evaluateXPath("*");
//		for (Object tag : nodeVals){
//		    TagNode _tag = (TagNode)tag;
//		    String rs = ParserUtil.xml(_tag,true);
//		    System.out.println(rs);
//		}
//		
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true); // never forget this!
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        Document doc = builder.parse("http://www.streetdeal.sg/deals/view/4575/0/51_percent_off_JOHOR_Premium_Outlets_Shopping_Trip_Return_Coach_by_Transtar_Travel.html?utm_source=ilovedeals&utm_medium=referral&utm_campaign=cpc");
//        XPathFactory xfactory = XPathFactoryImpl.newInstance();
//        XPath xpath = xfactory.newXPath();
//        XPathExpression expr = xpath.compile("//div[@class='highlights']");
//        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//        NodeList nodes = (NodeList) result;
//		String rs = ParserUtil.xml(nodes.item(0), false);
//		System.out.println(rs);
//		//第一步：获得解析工厂的实例  
//        SAXParserFactory spf = SAXParserFactory.newInstance();  
//        //第二部：获得工厂解析器  
//        SAXParser sp = spf.newSAXParser();  
//        //第三部：对xml进行解析  
//        sp.parse(file, new ModelParser());
        
	}

}
