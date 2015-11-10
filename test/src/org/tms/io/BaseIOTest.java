package org.tms.io;

import org.tms.api.factories.TableFactory;
import org.tms.BaseTest;
import org.tms.api.Table;
import org.tms.api.io.CSVOptions;
import org.tms.tds.ContextImpl;

public class BaseIOTest extends BaseTest
{
    protected Table importCVSFile(String fileName, boolean hasRowNames, boolean hasColumnHeaders)
    {
        return TableFactory.importFile(fileName, ContextImpl.fetchDefaultContext(), 
                CSVOptions.Default.withRowLabels(hasRowNames).withColumnLabels(hasColumnHeaders));
    }
}
