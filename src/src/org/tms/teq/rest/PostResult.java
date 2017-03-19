package org.tms.teq.rest;

import java.io.StringReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.tms.teq.RemoteValueService;

@Path("/postResult")
public class PostResult 
{
	@POST
	@Path("{tmsId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String postResult(@PathParam("tmsId") String tmsId, String valueStr)
	{
		RemoteValueService.postRemoteValue(tmsId, valueStr);
		return "ok";
	}
	
	@POST
	@Path("{tmsId}/{dataType}")
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
			
			RemoteValueService.postRemoteValue(tmsId, value);
			return "ok";
		} 
		catch (ClassNotFoundException | JAXBException e) {
			return "fail";
		}
	}
	
	@GET
	public Response postResultGet(@QueryParam("tmsId") String tmsId,
				    			  @QueryParam("value") String value,
				    			  @QueryParam("class") String clazz)
	{
		if (tmsId == null || (tmsId = tmsId.trim()).length() == 0)
			return Response.status(406).entity("fail: no tmsId").build();
		
		if (value == null || (value = value.trim()).length() == 0)
			return Response.status(406).entity("fail: no tmsId").build();
		
		if (clazz == null || (clazz = value.trim()).length() == 0)
			postResult(tmsId, value);
		else
			postJSONResult(tmsId, clazz, value);

		return Response.status(200).entity("ok").build();
	}	
}
