package org.eweb4j.spiderman.spider;

import java.util.Map;

import org.eweb4j.cache.Props;
import org.eweb4j.util.CommonUtil;
import org.eweb4j.util.FileUtil;

public class Settings {

	private final static Map<String, String> settings = Props.getMap("spiderman");
	
	private static String get(String key, String defaultValue) {
		if (settings == null) return defaultValue;
		String v = settings.get(key);
		return v == null ? defaultValue : v;
	}
	
	public static String website_xml_folder(){
		return get("website.xml.folder", "#{ClassPath}").replace("#{ClassPath}", FileUtil.getTopClassPath(Settings.class));
	}
	
	public static String website_visited_folder(){
		return get("website.visited.folder", "#{ClassPath}").replace("#{ClassPath}", FileUtil.getTopClassPath(Settings.class));
	}
	
	public static int http_fetch_retry(){
		Integer i = CommonUtil.toInt(get("http.fetch.retry", "0"));
		return i == null ? 0 : i;
	}
	
	public static long http_fetch_timeout(){
		return CommonUtil.toSeconds(get("http.fetch.timeout", "0")).longValue();
	}
}
