package org.tms.api.derivables;

/**
 * A class implementing the {@code Precisionable} interface implements the 
 * {@link org.tms.api.TableProperty TableProperty}
 * {@link org.tms.api.TableProperty#Precision Precision}, which allows the developer to 
 * control the number of significant digits used to post {@link org.tms.api.derivables.Derivation Derivation}
 * results to derived table cells.
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
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
