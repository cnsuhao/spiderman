package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

public class Targets {
	
	/**
	 * 来源页面的url规则
	 */
	private Rules sourceRules ;
	
	private List<Target> target = new ArrayList<Target>();

	public List<Target> getTarget() {
		return target;
	}

	public void setTarget(List<Target> target) {
		this.target = target;
	}

	public Rules getSourceRules() {
		return sourceRules;
	}

	public void setSourceRules(Rules sourceRules) {
		this.sourceRules = sourceRules;
	}
	
}
