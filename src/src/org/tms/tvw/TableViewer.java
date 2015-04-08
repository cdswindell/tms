package org.tms.tvw;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class TableViewer extends JApplet
{
    private static final long serialVersionUID = 2711104089029057719L;

    public static void main(String[] args) 
    {
        TableViewer tv =new TableViewer();
        run(tv, 350, 200);
    }
    
    public static void run(JApplet applet, int width, int height) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(false);
        frame.getContentPane().add(applet);
        frame.setSize(width, height);
        applet.init();
        applet.start();
        frame.setVisible(true);
      }
    
    private Table m_table;
    private TableModel m_tableModel;
    private JTextArea m_entryBox;
    private JTable m_jTable;
    
    public TableViewer()
    {
        this(TableFactory.createTable());
        
        m_table.addRow(Access.ByIndex, 10);
        m_table.addColumn(Access.ByIndex, 10);
        m_table.fill(null);
    }
    
    public TableViewer(Table table) throws HeadlessException
    {
        super();
        
        m_table = table;
    }
    
    @Override
    public void init()
    {
        super.init();
        Container cp = getContentPane();
        
        m_entryBox = new JTextArea(1, 40);
        cp.add(BorderLayout.NORTH, m_entryBox);
       
        m_tableModel = new TableModel(m_table);
        
        m_jTable = new JTable(m_tableModel);
        m_jTable.setFillsViewportHeight(true);
        m_jTable.setSelectionMode( javax.swing.ListSelectionModel.SINGLE_SELECTION);
        m_jTable.setCellSelectionEnabled(true);
        m_jTable.getSelectionModel().addListSelectionListener(new LSL());
        
        cp.add(new JScrollPane(m_jTable));
    }
    
    @Override
    public void start()
    {
        super.start();
    }
    
    @Override
    public void stop()
    {
        super.stop();
    }
    
    class LSL implements ListSelectionListener 
    {
        @Override
        public void valueChanged(ListSelectionEvent e)
        {
            m_entryBox.setText(""); // Clear it
            
            int rowIdx = m_jTable.getSelectedRow();
            int colIdx = m_jTable.getSelectedColumn();
            
            if (rowIdx >= 0)
                rowIdx = m_jTable.convertRowIndexToModel(rowIdx) + 1;
            if (colIdx >= 0)
                colIdx = m_jTable.convertColumnIndexToModel(colIdx) + 1;
            
            if (rowIdx > 0 && colIdx > 0) {
                Cell c = m_table.getCell(m_table.getRow(rowIdx), m_table.getColumn(colIdx));
                if (c != null) {
                    if (c.isDerived())
                        m_entryBox.setText("=" + c.getDerivation().getAsEnteredExpression());
                    else if (!c.isNull())
                        m_entryBox.setText(c.getFormattedCellValue());
                }
            }
        }
      }
}
