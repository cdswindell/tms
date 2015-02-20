package org.tms.api;

import org.tms.api.exceptions.ConstraintViolationException;

public interface TableCellValidator
{
    public void validate(Cell cell, Object newValue) throws ConstraintViolationException;
}
