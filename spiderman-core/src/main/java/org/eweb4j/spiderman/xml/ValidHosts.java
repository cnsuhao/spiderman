package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-8 上午11:17:44
 */
public class ValidHosts {

	private List<ValidHost> validHost = new ArrayList<ValidHost>();

	public List<ValidHost> getValidHost() {
		return this.validHost;
	}

	public void setValidHost(List<ValidHost> validHost) {
		this.validHost = validHost;
	}

	@Override
	public String toString() {
		return "ValidHosts [validHost=" + this.validHost + "]";
	}
	
}
