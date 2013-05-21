package org.eweb4j.spiderman.plugin.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.UrlRuleChecker;
import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;
import org.eweb4j.spiderman.xml.Target;
import org.eweb4j.util.CommonUtil;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class Util {

	public static Target matchTarget(Task task) throws Exception{
		for (Target target : task.site.getTargets().getTarget()){
			Rules rules = target.getUrlRules();
			Rule tgtRule = UrlRuleChecker.check(task.url, rules.getRule(), rules.getPolicy());
			if (tgtRule != null){
				if (task.target == null)
					task.target = target;
				task.httpMethod = tgtRule.getHttpMethod();
				
				return target;
			}
		}

		return null;
	}

	public static Collection<String> findAllLinkHref(String html, String hostUrl) throws Exception{
		Collection<String> urls = new ArrayList<String>();

		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode node = cleaner.clean(html);
		Object[] ns = node.evaluateXPath("//a[@href]");
		for (Object object : ns) {
			TagNode node2=(TagNode) object;
			String href = node2.getAttributeByName("href");
			if (href == null || href.trim().length() == 0)
				continue;

			if (!href.startsWith("https://") && !href.startsWith("http://")){
				StringBuilder sb = new StringBuilder("http://").append(new URL(hostUrl).getHost());
				if (!href.startsWith("/"))
					sb.append("/");
				href = sb.append(href).toString();
			}
//			href = URLCanonicalizer.getCanonicalURL(href);
			if (href == null)
				continue;
			if (href.startsWith("mailto:"))
				continue;

			urls.add(href);
		}

		return urls;
	}

	public static void main(String[] args){
		String html = getHtml("http://www.groupon.my/all-deals/klang");
		List<String> rs = CommonUtil.findByRegex(html, "(?<=(\"dealPermaLink\":\")).[^\"]*");
		System.out.println(rs);
	}

	public static String getHtml(String urlString) {  
	    try {  
	      StringBuffer html = new StringBuffer();  
	      URL url = new URL(urlString);  
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
	      InputStreamReader isr = new InputStreamReader(conn.getInputStream());  
	      BufferedReader br = new BufferedReader(isr);  
	      String temp;  
	      while ((temp = br.readLine()) != null) {  
	        html.append(temp).append("\n");  
	      }  
	      br.close();  
	      isr.close();  
	      return html.toString();  
	    } catch (Exception e) {  
	      e.printStackTrace();  
	      return null;  
	    }  
	}
}
