package org.tms.web.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("rest")
public class TmsRestProvider extends ResourceConfig 
{
	public TmsRestProvider()
	{
		packages("org.tms.teq.rest");
	}
}
