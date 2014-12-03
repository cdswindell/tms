package org.tms.tds;

public enum FieldAccess
{
    CreateIfNull,
    ReturnEmptyIfNull,
    ReturnCopy,
    ;
    
    public static FieldAccess checkAccess(FieldAccess... values)
    {
        if (values == null || values.length == 0)
            return CreateIfNull;
        else
            return values[0];
    }
}
