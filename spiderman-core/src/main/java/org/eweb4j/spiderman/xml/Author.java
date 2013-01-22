package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

/**
 * @author weiwei
 *
 */
public class Author {

	@AttrTag
	private String name = "weiwei";
	
	@AttrTag
	private String website = "http://laiweiweihi.iteye.com";
	
	@AttrTag
	private String email = "l.weiwei@163.com";
	
	@AttrTag
	private String weibo = "http://weibo.com/weiweimiss";
	
	@AttrTag
	private String desc = "一个喜欢自由、音乐、绘画的IT老男孩";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getWeibo() {
		return weibo;
	}

	public void setWeibo(String weibo) {
		this.weibo = weibo;
	}

	@Override
	public String toString() {
		return "Author [name=" + name + ", website=" + website + ", email="
				+ email + ", weibo=" + weibo + ", desc=" + desc + "]";
	}
	
}
