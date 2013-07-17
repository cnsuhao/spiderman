package controller;

import javax.ws.rs.Path;

import org.eweb4j.cache.Props;

@Path("/")
public class ApplicationControl {

	private final static String control_action = Props.getMap("spiderman").get("controller");
	
	@Path("/")
	public String index(){
		return "action:"+control_action+"/admin@GET";
	}
	
}
