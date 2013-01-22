package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

import org.eweb4j.util.xml.AttrTag;

public class Extension {
	@AttrTag
	private String point;
	
	private List<Impl> impl = new ArrayList<Impl>();
	
	public String getPoint() {
		return point;
	}
	public void setPoint(String point) {
		this.point = point;
	}
	public List<Impl> getImpl() {
		return impl;
	}
	public void setImpl(List<Impl> impl) {
		this.impl = impl;
	}
	
}
