package org.eweb4j.spiderman.url;

import java.util.List;

import org.eweb4j.spiderman.xml.Rule;
import org.eweb4j.spiderman.xml.Rules;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-2-28 下午08:34:54
 */
public class SourceUrlChecker {
	public static boolean checkSourceUrl(Rules rules, String sourceUrl) {
		if (sourceUrl != null) {
			// 判断下当前Target的sourceURL是否是我们要的来源URL
			if (rules == null)
				return true;
			else {
				List<Rule> ruleList = rules.getRule();
				if (ruleList == null || ruleList.isEmpty())
					return true;
				else {
					return UrlRuleChecker.check(sourceUrl, ruleList);
				}
			}
		}
		
		return true;
	}
}
