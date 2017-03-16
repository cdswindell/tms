package org.tms.web.rest;

import java.io.StringReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.tms.teq.RemoteValue;

@Path("/postResult/{tmsId}")
public class PostResult 
{
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String postResult(@PathParam("tmsId") String tmsId, String valueStr)
	{
		RemoteValue.postRemoteValue(tmsId, valueStr);
		return "ok";
	}
	
	@POST
	@Path("{dataType}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String postJSONResult(@PathParam("tmsId") String tmsId, @PathParam("dataType") String dataType, String jsonStr)
	{
		try {
	        Class<?> clazz = (Class<?>) Class.forName(dataType);
	
	        JAXBContext jc = JAXBContext.newInstance(clazz);
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
	        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
	        
	        StreamSource json = new StreamSource(new StringReader(jsonStr));
	        Object value = unmarshaller.unmarshal(json, clazz).getValue();
	        System.out.println(value.toString());
			
			RemoteValue.postRemoteValue(tmsId, value);
			return "ok";
		} 
		catch (ClassNotFoundException | JAXBException e) {
			return "fail";
		}
	}
}
