package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author weiwei
 *
 */
public class Provider {

	private List<Orgnization> orgnization = new ArrayList<Orgnization>();

	public List<Orgnization> getOrgnization() {
		return orgnization;
	}

	public void setOrgnization(List<Orgnization> orgnization) {
		this.orgnization = orgnization;
	}

	@Override
	public String toString() {
		return "Provider [orgnization=" + orgnization + "]";
	}
	
}
