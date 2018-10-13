package org.tms.io;

import java.io.OutputStream;

import org.tms.io.options.ArchivalIOOptions;

abstract class ArchivalWriter<T extends ArchivalIOOptions<T>> extends BaseWriter<T>
{  
    public ArchivalWriter(TableExportAdapter tea, OutputStream out, T options)
    {
        super(tea, out, options);       
    }    
}
