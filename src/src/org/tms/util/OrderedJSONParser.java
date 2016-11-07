package org.tms.util;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class OrderedJSONParser extends JSONParser implements ContainerFactory 
{
	@SuppressWarnings("rawtypes")
	@Override
	public Map createObjectContainer() 
	{
		return new OrderedJSONObject();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List creatArrayContainer() 
	{
		return null;
	}

	@Override
	public Object parse(String s) throws ParseException 
	{
		return super.parse(s, this);
	}

	@Override
	public Object parse(Reader in) throws IOException, ParseException 
	{
		return super.parse(in, this);
	}
}
