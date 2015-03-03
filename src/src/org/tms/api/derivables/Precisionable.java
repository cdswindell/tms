package org.tms.api.derivables;

public interface Precisionable
{
    /**
     * Returns the number of significant digits used to post derivation results to derived cells. 
     * The default precision is {@value org.tms.teq.DerivationImpl#sf_DEFAULT_PRECISION} digits. 
     * @return the number of significant digits used to post derivation results to derived cells
     */
    public int getPrecision();
    
    /**
     * Sets he number of significant digits used to post derivation results to derived cells. 
     * The default precision is {@value org.tms.teq.DerivationImpl#sf_DEFAULT_PRECISION} digits. 
     * @param digits the number of significant digits used to post derivation results
     */
    public void setPrecision(int digits);
}
