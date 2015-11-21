/**
 * 
 */
package org.eweb4j.spiderman.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangc
 *
 */
public enum SectionXmlEnum {
   danyin("&apos;","'"),shuangyin("&quot;","\""),binghao("&amp;","&"),dayu("&gt;",">"),xiaoyu("&lt;","<"),space("&nbsp;"," ");
   private String xmlSr;
   private String htmlSr;
   private SectionXmlEnum(String xmlSr,String htmlSr){
	   this.xmlSr=xmlSr;
	   this.htmlSr=htmlSr;
   }
	public String getXmlSr() {
		return xmlSr;
	}
	public void setXmlSr(String xmlSr) {
		this.xmlSr = xmlSr;
	}
	public String getHtmlSr() {
		return htmlSr;
	}
	public void setHtmlSr(String htmlSr) {
		this.htmlSr = htmlSr;
	}
	public static void main(String[] args) {
		String srcs="MM_preloadImages(\"images/link-bar01-over.gif\",'images/icon-mail-over','images/but-bar-over-01-light.gif','images/but021-over.gif','images/but022-over.gif','images/but023-over.gif','images/but024-over.gif','images/bar08-1-over.gif','images/bar08-2-over.gif')";
		//Pattern pt=Pattern.compile(".*(.jpg|.png|.gif|.jpeg)$");
		Pattern pt=Pattern.compile("\\([']*[^\\)]*[']*\\)");
		Matcher mc=pt.matcher(srcs);
		while(mc.find()){
			String str=mc.group();
		}
	}
}
