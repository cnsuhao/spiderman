package org.eweb4j.spiderman.plugin;

import java.util.Collection;

public interface ExtensionPoint<T> {

	public Collection<T> getExtensions();
	
}
