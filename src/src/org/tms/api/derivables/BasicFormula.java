package org.tms.api.derivables;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;

public interface BasicFormula
{
    public Table getTable();
	public TableElement getTarget();
	public TableContext getTableContext();

    public String getExpression();
    public String getAsEnteredExpression();
    public String getPostfixExpression();
    public String getInfixExpression();
    public boolean isParsed();
    public boolean isConverted();
}
