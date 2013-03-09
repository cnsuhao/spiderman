package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

/**
 * URL规则
 * @author weiwei l.weiwei@163.com
 * @date 2013-2-28 上午11:49:23
 */
public class Rules {

	@AttrTag
	private String policy = "and";
	
	@AttrTag
	private String isStrict ;
	
	private List<Rule> rule = new ArrayList<Rule>();

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public List<Rule> getRule() {
		return rule;
	}

	public void setRule(List<Rule> rule) {
		this.rule = rule;
	}

	public String getIsStrict() {
		return this.isStrict;
	}

	public void setIsStrict(String isStrict) {
		this.isStrict = isStrict;
	}
	
}
