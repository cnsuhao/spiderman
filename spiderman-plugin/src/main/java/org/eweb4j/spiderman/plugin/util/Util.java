package org.eweb4j.spiderman.plugin.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eweb4j.spiderman.task.Task;
import org.eweb4j.spiderman.url.UrlRuleChecker;
import org.eweb4j.spiderman.xml.Target;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class Util {
	
	public static Target isTargetUrl(Task task) throws Exception{
		for (Target target : task.site.getTargets().getTarget()){
			if (UrlRuleChecker.check(task.url, target.getUrls().getRule()))
				return target;
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
				href = new StringBuilder("http://").append(new URL(hostUrl).getHost()).append("/").append(href).toString();
			}
			
			href = URLCanonicalizer.getCanonicalURL(href);
			if (href == null)
				continue;
			if (href.startsWith("mailto:"))
				continue;
			
			urls.add(href);
		}
		
		return urls;
	}
}
