package org.eweb4j.spiderman.plugin.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.CommonUtil;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Node;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-5 下午07:44:16
 */
public class ParserUtil {

	public static String checkUnicodeString(String value) throws Exception{
		char[] xmlChar = value.toCharArray();
		for (int i=0; i < xmlChar.length; ++i) {
	        if (xmlChar[i] > 0xFFFD)
	        	xmlChar[i] =' ';// 用空格替换

	        if (xmlChar[i] < 0x20 && xmlChar[i] != 't' & xmlChar[i] != 'n' & xmlChar[i] != 'r')
	        	xmlChar[i] =' ' ;// 用空格替换
		}

		return new String(xmlChar);
	}

	public static Object evalXpath(String html, String xpath){
		return evalXpath(html, xpath, null);
	}

	public static void main(String[] args){
		String html = "<p> So, when traveling is a must and a quick bag will do the deed, then the best answer for your travel needs is the Sutton Signature swingpack. From a quick ride from one office to the other to taking the train to another city, the swingpack can keep your needs before you and your job ahead of you. Coach was founded in 1941 as a family-run workshop. In a Manhattan loft, six artisans handcrafted a collection of leather goods using skills handed down from generation to generation. Discerning consumers soon began to seek out the quality and unique nature of Coach craftsmanship.</p><p> <img src=\"http://file.honeybay.com/images/product/Coach/Coach47162/Desc.jpg\" /></p>";
		Object objs = ParserUtil.evalXpath(html, "//img[@src]", "src");
		System.out.println(objs);
	}

	public static Object evalXpath(String html, String xpath, String attribute){
		List<Object> result = new ArrayList<Object>();
		HtmlCleaner cleaner = new HtmlCleaner();
		try {
			TagNode tagNode = cleaner.clean(html);
			Object[] nodeVals = tagNode.evaluateXPath(xpath);
			for (Object tag : nodeVals){
				TagNode _tag = (TagNode)tag;
				Object val = null;
				if (attribute != null)
					val = _tag.getAttributeByName(attribute);
				else if (xpath.endsWith("/text()")){
					result.add(tag.toString());
				}else 
					val = tag;

				result.add(val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static String xml(Object node, boolean keepHeader){
		String xml = "";
		try{
			if (node instanceof Node){
				xml = CommonUtil.toXml((Node)node, keepHeader);
				return CommonUtil.toHTML(xml);
			}else if (node instanceof TagNode){
				StringWriter sw = new StringWriter();
				//TODO 从配置文件里加载这个CleanerProperties
				CleanerProperties prop = new HtmlCleaner().getProperties();
				SimpleXmlSerializer ser = new SimpleXmlSerializer(prop);
				ser.write((TagNode)node, sw, "UTF-8");
		    	String html = sw.getBuffer().toString();
		    	if (keepHeader)
		    		xml = html;
		    	else
		    		xml = html.substring(html.indexOf("?>")+2);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return xml;
	}
	
}
