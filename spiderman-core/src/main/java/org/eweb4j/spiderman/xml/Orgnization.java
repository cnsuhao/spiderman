package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

/**
 * <organization name="深圳优扣科技有限公司" website="http://www.shoplay.com" desc="打造一流的社交分享购物社区!">
	</organization>
 * @author weiwei
 *
 */
public class Orgnization {

	@AttrTag
	private String name = "深圳优扣科技有限公司";
	
	@AttrTag
	private String website = "http://www.shoplay.com";
	
	@AttrTag
	private String desc = "打造一流的社交分享购物社区!";
	
	private List<Author> author = new ArrayList<Author>();

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Author> getAuthor() {
		return author;
	}

	public void setAuthor(List<Author> author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "Orgnization [name=" + name + ", website=" + website + ", desc="
				+ desc + ", author=" + author + "]";
	}
	
}
