package org.tms.tvw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class TableViewer extends JApplet implements TableModelListener
{
    private static final long serialVersionUID = 2711104089029057719L;

    public static void main(String[] args) 
    {
        TableViewer tv =new TableViewer();
        run(tv, 350, 200);
    }
    
    public static void run(JApplet applet, int width, int height) 
    {
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
        m_jTable.setGridColor(Color.BLACK);
        
        TableCellRenderer tcr = new TableCellRenderer(m_jTable);
        m_jTable.setDefaultRenderer(Object.class, tcr);
        m_jTable.getSelectionModel().addListSelectionListener(new LSL());
                
        m_jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        m_jTable.getModel().addTableModelListener( this );
        
        JScrollPane sp = new JScrollPane(m_jTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        JTable rowLabels = new RowNumberTable(m_jTable);
        sp.setRowHeaderView(rowLabels);
        sp.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowLabels.getTableHeader());
        
        cp.add(sp);
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

    @Override
    public void tableChanged(TableModelEvent e)
    {
        m_jTable.revalidate();       
    }

    /*
     *  Attempt to mimic the table header renderer
     */
    private static class ColumnNumberRenderer extends DefaultTableCellRenderer implements FocusListener
    {
        private JTable m_main;
        public ColumnNumberRenderer(JTable main)
        {
            setHorizontalAlignment(JLabel.CENTER);
            this.setFocusable(true);
            m_main = main;
        }

        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (table != null)
            {
                JTableHeader header = table.getTableHeader();

                if (header != null)
                {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (isSelected)
            {
                setFont( getFont().deriveFont(Font.BOLD) );
                if (hasFocus) {
                    m_main.setCellSelectionEnabled(false);
                    m_main.setRowSelectionAllowed(false);
                    m_main.setColumnSelectionAllowed(true);
                }
                else {
                    m_main.setCellSelectionEnabled(true);
                }
            }

            setText((value == null) ? "" : value.toString());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));

            return this;
        }

        @Override
        public void focusGained(FocusEvent e)
        {
            if (e != null)
            {
                
            }
            
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            // TODO Auto-generated method stub
            
        }
    }
    
    private static class TableCellRenderer extends DefaultTableCellRenderer
    {
        private JTable m_main;
        
        public TableCellRenderer(JTable main)
        {
            m_main = main;
        }

        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && value instanceof Number) 
                setHorizontalAlignment(JLabel.RIGHT);
            else if (value != null && value instanceof Boolean) 
                setHorizontalAlignment(JLabel.CENTER);
            else
                setHorizontalAlignment(JLabel.LEFT);
                           
            setText((value == null) ? "" : value.toString());
 
            return this;
        }
    }
}
