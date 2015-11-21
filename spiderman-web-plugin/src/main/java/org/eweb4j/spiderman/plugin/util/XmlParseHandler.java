package org.eweb4j.spiderman.plugin.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eweb4j.spiderman.xml.Field;
import org.eweb4j.spiderman.xml.Target;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-3 下午10:34:18
 */
public class XmlParseHandler extends DefaultHandler{

	private Stack<String> stack = new Stack<String>();
	private Map<String, List<String>> map = new HashMap<String, List<String>>();
	private List<Field> fields = null;
	
	public void init(Target target){
		fields = target.getModel().getField();
		
	}
	
	@Override  
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {  
//		for (Field field : fields){
//			String key = field.getName();
////			String node = field.getParser().getNode();
//			String xpath = field.getParser().getXpath();
//			String attribute = field.getParser().getAttribute();
//			String regex = field.getParser().getRegex();
//			String isArray = field.getIsArray();
			
			//按给定的node配置，支持正则
//			if (qName.matches(node)){
				//将当前元素的名字压到栈中
				stack.push(qName);
//			}
//		}
    }  
    @Override  
    public void characters(char[] ch, int start, int length) throws SAXException {  
    	//将栈顶中的元素出栈  
    	String content = new String(ch,start,length);
        String tag = stack.peek();
//        for (Field field : fields){
//			String key = field.getName();
////			String node = field.getParser().getNode();
//			String xpath = field.getParser().getXpath();
//			String attribute = field.getParser().getAttribute();
//			String regex = field.getParser().getRegex();
//			String isArray = field.getIsArray();
			
			//按给定的node配置，支持正则
//			if (tag.matches(node)){
				
		        List<String> values = map.get(tag);
		        if (values == null) {
		        	values = new ArrayList<String>();
		        	map.put(tag, values);
		        }
		        values.add(content);
//			}
//		}
        
    }  
    @Override  
    public void endElement(String uri, String localName, String qName) throws SAXException {  
    	//将栈顶元素移除  
        stack.pop();
    }
    
	public Map<String, Object> getMap() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.putAll(this.map);
		return result;
	}
}
