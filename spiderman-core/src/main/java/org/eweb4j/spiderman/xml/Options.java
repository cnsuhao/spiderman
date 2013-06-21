package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * 其他额外的数据
 * @author weiwei l.weiwei@163.com
 * @date 2013-6-9 上午10:30:26
 */
public class Options {

	private List<Option> option = new ArrayList<Option>();

	public List<Option> getOption() {
		return this.option;
	}

	public void setOption(List<Option> option) {
		this.option = option;
	}
	
}
