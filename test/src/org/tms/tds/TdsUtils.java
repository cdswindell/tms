package org.tms.tds;

import org.tms.api.TableContext;

public class TdsUtils
{
    static public void clearGlobalTagCache(TableContext tc)
    {
        ((ContextImpl)tc).clearGlobalTagCache();
    }
}
