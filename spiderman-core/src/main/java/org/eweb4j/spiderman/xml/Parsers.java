package org.eweb4j.spiderman.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-9 下午12:23:53
 */
public class Parsers {

	List<Parser> parser = new ArrayList<Parser>();

	public List<Parser> getParser() {
		return this.parser;
	}

	public void setParser(List<Parser> parser) {
		this.parser = parser;
	}
	
}
