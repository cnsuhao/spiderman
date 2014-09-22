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
	 * 目标页面的url规则
	 */
	private Rules urlRules ;
	
	/**
	 * 在解析最终的Model之前要解析的Model，然后当做参数传入，使用$before.xxx来使用
	 */
	private Model before;
	
	/**
	 * 目标的数据模型
	 */
	private Model model;
	
	@AttrTag
    private String isSkip ;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Model getBefore() {
        return this.before;
    }

    public void setBefore(Model before) {
        this.before = before;
    }

    public void setModel(Model model) {
		this.model = model;
	}

    public String getIsSkip() {
        return this.isSkip;
    }

    public void setIsSkip(String isSkip) {
        this.isSkip = isSkip;
    }

}
