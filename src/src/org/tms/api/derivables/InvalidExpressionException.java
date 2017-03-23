package org.tms.api.derivables;

import org.tms.api.exceptions.TableException;
import org.tms.teq.ParseResult;

public class InvalidExpressionException extends TableException
{
	private static final long serialVersionUID = 6033802096701854581L;
	
	private ParseResult m_parseResult;

    public InvalidExpressionException(ParseResult pr)
    {
        this(pr, pr.toString());
    }

    public InvalidExpressionException(ParseResult pr, String message)
    {
        super(message);
        m_parseResult = pr;
    }

    public ParseResult getParseResult()
    {
        return m_parseResult;
    }
}
