package org.tms.teq.ops;

import org.tms.api.derivables.Operator;
import org.tms.api.derivables.TokenType;

abstract public class BaseOp implements Operator
{
    private String m_label;
    private TokenType m_tokenType;
    private Class<?> m_resType;
    private Class<?> [] m_argTypes;
    
    public BaseOp(String label, TokenType tt, Class<?> [] argTypes, Class<?> resultType)
    {
        m_label = label;
        m_tokenType = tt;
        m_resType = resultType;
        m_argTypes = argTypes;
    }

    @Override
    public String getLabel()
    {
        return m_label;
    }

    @Override
    public TokenType getTokenType()
    {
        return m_tokenType;
    }

    @Override
    public Class<?> getResultType()
    {
        return m_resType;
    }
    
    @Override
    public Class<?>[] getArgTypes()
    {
        return m_argTypes;
    }
}
