package org.tms.tds.util;

public enum FieldAccess
{
    CreateIfNull,
    ReturnEmptyIfNull,
    Clone,
    ;
    
    public static FieldAccess checkAccess(FieldAccess... values)
    {
        if (values == null || values.length == 0)
            return CreateIfNull;
        else
            return values[0];
    }
}
