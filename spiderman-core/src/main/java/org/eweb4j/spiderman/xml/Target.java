package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * 要抓取的目标 
 * @author weiwei l.weiwei@163.com
 * @date 2013-2-28 上午11:56:27
 */
public class Target {

	/**
	 * 目标名
	 */
	@AttrTag
	private String name;
	
	/**
	 * 目标的contentType
	 */
	@AttrTag
	private String cType;
	
	private Namespaces namespaces;
	
	/**
	 * 来源页面的url规则
	 */
	private Rules sourceRules ;
	
	/**
	 * 目标页面的url规则
	 */
	private Rules urlRules ;
	
	/**
	 * 目标的数据模型
	 */
	private Model model;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Namespaces getNamespaces() {
		return this.namespaces;
	}

	public void setNamespaces(Namespaces namespaces) {
		this.namespaces = namespaces;
	}

	public Rules getSourceRules() {
		return this.sourceRules;
	}

	public void setSourceRules(Rules sourceRules) {
		this.sourceRules = sourceRules;
	}

	public Rules getUrlRules() {
		return this.urlRules;
	}

	public void setUrlRules(Rules urlRules) {
		this.urlRules = urlRules;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getCType() {
		return this.cType;
	}

	public void setCType(String cType) {
		this.cType = cType;
	}
	
}
