package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weiwei
 *
 */
public class Providers {

	private List<Provider> provider = new ArrayList<Provider>();

	public List<Provider> getProvider() {
		return provider;
	}

	public void setProvider(List<Provider> provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return "Providers [provider=" + provider + "]";
	}
	
}
