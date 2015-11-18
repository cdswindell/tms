import org.tms.api.utils.StockTickerOp;
import org.tms.api.factories.TableContextFactory;

class dayChange extends StockTickerOp {
    public dayChange()
    {
        super("dayChg", "c");
    }
}


op = new dayChange();
TableContextFactory.fetchDefaultTableContext().registerOperator(new dayChange())
