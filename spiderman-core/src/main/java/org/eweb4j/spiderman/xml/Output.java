package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

public class Output {
	@AttrTag
	private String desc="输出到指定地方!";
	private List<Options> options = new ArrayList<Options>();

	public List<Options> getOptions() {
		return options;
	}

	public void setOptions(List<Options> options) {
		this.options = options;
	}
	
}
