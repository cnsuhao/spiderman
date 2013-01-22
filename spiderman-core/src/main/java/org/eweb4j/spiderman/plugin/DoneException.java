package org.eweb4j.spiderman.plugin;
/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-15 下午02:14:16
 */
public class DoneException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DoneException() {
		super("Spiderman has shutdown...");
	}
	
}
