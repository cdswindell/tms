package org.tms;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SimpleRepeatRule implements TestRule 
{
    private static class SimpleRepeatStatement extends Statement 
    {

        private final Statement statement;
        private int m_numRepeats;
        
        private SimpleRepeatStatement(Statement statement, int numRepeats) 
        {
            this.statement = statement;
        	m_numRepeats = numRepeats;
        }

        @Override
        public void evaluate() throws Throwable 
        {
            for (int i = 0; i < m_numRepeats; i++) {
            	System.out.println("Test attempt: " + (i + 1));
                statement.evaluate();
            }
        }
    }

    private int m_numRepeats;
    
    public SimpleRepeatRule(int numRepeats) 
    {
    	m_numRepeats = numRepeats;
	}

	@Override
    public Statement apply(Statement statement, Description description) 
    {
        return new SimpleRepeatStatement(statement, m_numRepeats);
    }
}
