package org.tms.api.utils;

public interface Validatable
{
    public TableCellValidator getValidator();
    public void setValidator(TableCellValidator validator);
    public void setTransformer(TableCellTransformer transformer);
}
