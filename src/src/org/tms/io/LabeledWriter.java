package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.LabeledIOOption;

public abstract class LabeledWriter<E extends LabeledIOOption<?>> extends BaseWriter<E>
{
    protected LabeledWriter(TableExportAdapter tw, OutputStream out, E options)
    {
        super(tw, out, options);
    }

    @SuppressWarnings("resource")
	protected LabeledWriter(TableExportAdapter tw, File f, E options) 
    throws IOException
    {
        this(tw, new FileOutputStream(f), options);
    }
}
