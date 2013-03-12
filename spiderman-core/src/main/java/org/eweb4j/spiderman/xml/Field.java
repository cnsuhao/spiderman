package org.eweb4j.spiderman.xml;

import org.eweb4j.util.xml.AttrTag;

public class Field {

	@AttrTag
	private String name;
	
	@AttrTag
	private String isArray;
	
	@AttrTag
	private String isMergeArray;
	
	/**
	 * 是否也在下一页里解析
	 */
	@AttrTag
	private String isAlsoParseInNextPage;
	
	@AttrTag
	private String isTrim;//是否去掉前后的空格字符
	
	private Parsers parsers;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsArray() {
		return isArray;
	}

	public void setIsArray(String isArray) {
		this.isArray = isArray;
	}

	public String getIsMergeArray() {
		return isMergeArray;
	}

	public void setIsMergeArray(String isMergeArray) {
		this.isMergeArray = isMergeArray;
	}

	public Parsers getParsers() {
		return parsers;
	}

	public void setParsers(Parsers parsers) {
		this.parsers = parsers;
	}

	public String getIsTrim() {
		return this.isTrim;
	}

	public void setIsTrim(String isTrim) {
		this.isTrim = isTrim;
	}

	public String getIsAlsoParseInNextPage() {
		return isAlsoParseInNextPage;
	}

	public void setIsAlsoParseInNextPage(String isAlsoParseInNextPage) {
		this.isAlsoParseInNextPage = isAlsoParseInNextPage;
	}

}
