package org.tms.io.xml;

import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.teq.BaseAsyncState;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PendingStateConverter extends BaseConverter 
{

    public PendingStateConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public PendingStateConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

	@Override
	public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) 
	{
        return BaseAsyncState.class.isAssignableFrom(clazz);
	}

	@Override
	public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) 
	{
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) 
	{
		return null;
	}
}
