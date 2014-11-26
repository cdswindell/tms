package org.tms.api.exceptions;

import org.tms.api.TableProperty;
import org.tms.tds.BaseElement;

public final class ReadOnlyException extends TableException
{
    private static final long serialVersionUID = 7926030914035328745L;

    public ReadOnlyException(BaseElement te, TableProperty tp)
    {
        super(te.getTableElementType(), tp, TableErrorClass.ReadOnly);
    }
}
