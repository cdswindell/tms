package org.tms.teq.exceptions;

import org.tms.api.derivables.InvalidExpressionException;
import org.tms.teq.ParseResult;

public class InvalidExpressionExceptionImpl extends InvalidExpressionException
{
    private static final long serialVersionUID = -3219816701335766403L;
    
    private ParseResult m_parseResult;

    public InvalidExpressionExceptionImpl(ParseResult pr)
    {
        this(pr, pr.toString());
    }

    public InvalidExpressionExceptionImpl(ParseResult pr, String message)
    {
        super(message);
        m_parseResult = pr;
    }

    public ParseResult getParseResult()
    {
        return m_parseResult;
    }
}
