package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TokenTypeTest
{

    @Test
    public final void testIsLeading()
    {
        assertThat(TokenType.Comma.isLeading(), is(true)); 
        assertThat(TokenType.RightParen.isLeading(), is(false)); 
    }

    @Test
    public final void testGetLabel()
    {
        assertThat(TokenType.RightParen.getLabel(), is(")")); 
        assertThat(TokenType.LeftParen.getLabel(), is("(")); 
        assertThat(TokenType.ColumnRef.getLabel(), is("Column")); 
    }

    @Test
    public final void testGetLabelLength()
    {
        assertThat(TokenType.RightParen.getLabelLength(), is(1)); 
        assertThat(TokenType.LeftParen.getLabelLength(), is(1)); 
        assertThat(TokenType.ColumnRef.getLabelLength(), is("Column".length())); 
        assertThat(TokenType.Constant.getLabelLength(), is(0)); 
    }

    @Test
    public final void testIsLabeled()
    {
        assertThat(TokenType.RightParen.isLabeled(), is(true)); 
        assertThat(TokenType.LeftParen.isLabeled(), is(true)); 
        assertThat(TokenType.ColumnRef.isLabeled(), is(true)); 
        assertThat(TokenType.Constant.isLabeled(), is(false)); 
    }

    @Test
    public final void testGetLabels()
    {
        assertThat(TokenType.ColumnRef.getLabels(), notNullValue()); 
        assertThat(TokenType.ColumnRef.getLabels().contains("Column"), is(true)); 
        assertThat(TokenType.ColumnRef.getLabels().contains("Col"), is(true)); 
        assertThat(TokenType.ColumnRef.getLabels().size(), is(2)); 
    }

}
