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
		String html = "<IMG border=\"0\" src=\"http://sadpanda.us/images/1434157-2EC73ZO.jpg\" /><br/>S Model 75 ～家出っこに生姦中出し制裁に！～ : 鏑木みゆ<br/>商品番号: SMD-75 <br/>主演女優: 鏑木みゆ<br/>スタジオ : スーパーモデルメディア<br/>シリーズ : S Model<br/>カテゴリで探す : 完全無修正,美女?美人,生中出し,生姦?ゴム無し,美肌?美白,顔射,バキュームフェラ,背面騎乗位,巨大電マ責め,美尻?ケツがいい,美乳?素敵なオッパイ,巨乳?爆乳?超乳,強烈ピストンバック,最新入荷済み商品,サンプル動画上映中<br/>収録時間 : Apx. 110 Min<br/><IMG border=\"0\" src=\"http://102.imagebam.com/download/ic-OG9UFOwyddeCo7rBGCg/23926/239254426/1.jpg\" /><br/><A href=\"http://imgs.aventertainments.com/new/bigcover/DVD1SMD-75.jpg\" target=_blank >http://imgs.aventertainments.com/new/bigcover/DVD1SMD-75.jpg</A><br/><IMG border=\"0\" src=\"http://101.imagebam.com/download/GmFGvuxdYMoybeSxqHo1Ug/23926/239254440/2.jpg\" /><IMG border=\"0\" src=\"http://xpics.us/images/518150229440_2.jpg\" /><br/><A href=\"http://img52.imagetwist.com/i/03104/ofi33m9z8ac1.jpg\" target=_blank >http://img52.imagetwist.com/i/03104/ofi33m9z8ac1.jpg</A><br/>AVI 968MB<br/><a href=\"http://www3.pidown.info/bf2/file.php/MIZN1OW.html\" target=\"_blank\">http://www3.pidown.info/bf2/file.php/MIZN1OW.html</a><br/>";
		Object objs = ParserUtil.evalXpath(html, "//img[@src]", "src");
		System.out.println(objs);
	}

	public static Object evalXpath(String html, String xpath, String attribute){
		if (html == null || html.trim().length() == 0){
			return null;
		}
		
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
		} catch (Throwable e) {
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
