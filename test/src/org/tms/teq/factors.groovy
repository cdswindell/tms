class factors {
    int firstFactor(double x) {
    	if (x == 1) return 1
        def factor = 2
        while (x % factor != 0) { factor++ }        
        factor 
    }  
    
    int lastFactor(double x) {
        def factor = (int)(x/2)
        while (x % factor != 0 && factor >= 1) { factor-- }        
        factor 
    }  
    
    List<Integer> allFactors(double x) {
        def factor = 2
        def factors = []
        while (factor*2 < x) {
            while (factor*2 < x && x % factor != 0) { factor++ } 
            if (factor*2 < x)    
                factors << factor++
        } 
        factors
    }
 }
