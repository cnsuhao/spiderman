package org.eweb4j.spiderman.container;

import java.util.ArrayList;
import java.util.Collection;

public class ContainerManager {
	
	private static Collection<Container> containers = null;
	
	private static ContainerManager instance = null;
	
	public static ContainerManager me()
	{
		if(instance == null)
		{
			instance = new ContainerManager();
			containers = new ArrayList<Container>();
		}
		return instance;
	}
	
	public void add(Container container)throws Exception
	{
		if (container.getId() == null || container.getId().trim().length() == 0)
			throw new Exception("container id required");
		if(get(container.getId().trim()) != null)
		{
			throw new Exception("container id ["+container.getId().trim()+"] can not be repeated!");
		}
		containers.add(container);
	}
	
	public Container get(String id)
	{
		for(Container container : containers)
		{
			if(id.trim().equals(container.getId()))
			{
				return container;
			}
		}
		return null;
	}
	
	public Collection<Container> getContainers() {
		return containers;
	}
}
