package org.tms.io;

import static org.junit.Assert.assertNotNull;

import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class XMLTest extends BaseTest
{
    protected static final String SAMPLE1 = "sample1.csv";
    
    protected Table getBasicTable()
    {
        Table gst = TableFactory.importCSV(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(gst);
        gst.setLabel("Test XML & Export Table");
        gst.tag("red", "green");
        
        Column gsc3 = gst.getColumn(3);
        Cell r1c3 = gst.getCell(gst.getRow(1), gsc3);
        r1c3.clear();
        r1c3.setLabel("foo");
        r1c3.setUnits("mph");
        r1c3.setDescription("Cell Description");
        
        Subset s1 = gst.addSubset(Access.ByLabel, "Excluded Cols");
        Subset s2 = gst.addSubset(Access.ByLabel, "Some Cols");
        s2.tag("red", "subset");
        
        Column gsc = gst.addColumn();
        s1.add(gsc);
        s2.add(gsc, gsc3);
        
        gsc = gst.addColumn();
        s2.add(gsc);

        gsc.setDerivation("col 1 * col 1");
        gsc.tag("derived", "calculated", "no-user-data");
        
        return gst;
    }
}
