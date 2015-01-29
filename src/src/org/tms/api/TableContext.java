package org.tms.api;

import org.tms.teq.TokenMapper;

public interface TableContext extends BaseElement
{
    public Table getTable(Access mode, Object...mda);

    public TokenMapper getTokenMapper();
}
