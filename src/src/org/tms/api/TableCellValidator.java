package org.tms.api;

import org.tms.api.exceptions.TableCellValidationException;

public interface TableCellValidator
{
    public void validate(Cell cell, Object newValue) throws TableCellValidationException;
}
