package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableFactory;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidExpressionException;

public class DerivationTest
{
    @Test
    public final void testSetDerivationColumn()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 10);
        Column c1 = tbl.addColumn(Access.ByIndex, 8);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        // create the derivation
        c1.setDerivation("RowIndex + Cidx");
        assertThat(c1.isDerived(), is(true));
        
        String expr = c1.getDerivation();
        assertThat(expr, notNullValue());
        assertThat(expr, is("RowIndex + Cidx"));      
        assertThat(tbl.getCellValue(r1,  c1), is(18.0));
        
        // get the cell and make sure it's value is correct
        Cell c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        assertThat(c.getCellValue(), is(18.0));
    }

    @Test
    public final void testSetDerivationRow()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Row r1 = tbl.addRow(Access.ByIndex, 10);
        Column c1 = tbl.addColumn(Access.ByIndex, 8);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        // create the derivation
        r1.setDerivation("ColumnIndex * 2");
        assertThat(r1.isDerived(), is(true));
        
        String expr = r1.getDerivation();
        assertThat(expr, notNullValue());
        assertThat(expr, is("ColumnIndex * 2"));      
        assertThat(tbl.getCellValue(r1,  c1), is(16.0));
        
        // get the cell and make sure it's value is correct
        Cell c = tbl.getCell(r1,  c1);
        assertThat(c, notNullValue());
        assertThat(c.getCellValue(), is(16.0));
    }
    
    @Test
    public final void testCreate()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c1 = tbl.addColumn(Access.ByIndex, 8);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        // create the derivation
        Derivation d = Derivation.create("10+11", c1);
        assertThat(d, notNullValue());
        assertThat(d.isParsed(), is(true));
        assertThat(d.isConverted(), is(true));
        
        assertThat(d.getAsEnteredExpression(), is("10+11"));
        assertThat(d.getInfixExpression(), is("10.0 + 11.0"));
        assertThat(d.getPostfixExpression(), is("10.0 11.0 +"));
    }
    
    @Test
    public final void testVetCircularReference()
    {
        Table tbl = TableFactory.createTable(12, 10);        
        assert (tbl != null);
        assertThat(tbl.getPropertyInt(TableProperty.numCells), is (0));
        
        Column c1 = tbl.addColumn(Access.Next);
        Column c2 = tbl.addColumn(Access.Next);
        Column c3 = tbl.addColumn(Access.Next);
        Column c4 = tbl.addColumn(Access.Next);
        Column c5 = tbl.addColumn(Access.Next);
        Column c6 = tbl.addColumn(Access.Next);
        Column c7 = tbl.addColumn(Access.Next);
        Column c8 = tbl.addColumn(Access.Next);
        
        // these derivations should all succeed
        c1.setDerivation("col 2 + col 4");
        c2.setDerivation("col 3");
        c3.setDerivation("col 4 ");
        c4.setDerivation("col 5");
        c5.setDerivation("col 6");
        c6.setDerivation("col 7");
        c7.setDerivation("col 8");
        
        // this should fail
        try {
            c8.setDerivation("col 1");
            fail("circular derivation allowed");
        }
        catch (InvalidExpressionException e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.CircularReference));
        }       
    }
}