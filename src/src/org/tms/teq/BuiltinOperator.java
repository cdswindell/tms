package org.tms.teq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableElement;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.derivables.Labeled;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.IllegalTableStateException;

/**
 * 
 * Operator precedence in the TMS system is expressed using a numeric integer priority. 
 * The values used are as follows:
 * <p/>
 * <table border="1">
 * <tr><th>Operators</th><th>Priority</th><th>Examples</th></tr>
 * <tr><td>Parens</td><td>10</td><td>(, )</td></tr>
 * <tr><td>Functions</td><td>8</td><td>built-in and user-defined functions</td></tr>
 * <tr><td>Unary</td><td>7</td><td>!, %, -, neg</td></tr>
 * <tr><td>Multiplicative</td><td>6</td><td>*, /</td></tr>
 * <tr><td>Additive</td><td>5</td><td>+, -</td></tr>
 * <tr><td>Relational</td><td>4</td><td>&gt;, &lt;, &gt;=, &lt;=</td></tr>
 * <tr><td>Equality</td><td>3</td><td>=, ==, !=, &lt;&gt;</td></tr>
 * <tr><td>Logical</td><td>2</td><td>&&, ||</td></tr>
 * </table>
 * 
 * By default, user-defined operators are assigned a priority of 8. This can be overridden, as needed.
 */
public enum BuiltinOperator implements Labeled, Operator
{
    NULL_operator,

    // Basic math operators, implementation supported for numbers and strings (+ & -)
    PlusOper("+", TokenType.BinaryOp, 5),
    MinusOper("-", TokenType.BinaryOp, 5),
    MultOper("*", TokenType.BinaryOp, 6),
    DivOper("/", TokenType.BinaryOp, 6),
    
    // boolean comparison operators
    EqOper(TokenType.BinaryOp, 3, toLabels("=", "==", "EQ"), toArgs(Object.class, Object.class), boolean.class),
    NEqOper(TokenType.BinaryOp, 3, toLabels("!=", "<>", "NE"), toArgs(Object.class, Object.class), boolean.class),
    GtOper(TokenType.BinaryOp, 4, toLabels(">", "GT"), toArgs(Object.class, Object.class), boolean.class),
    LtOper(TokenType.BinaryOp, 4, toLabels("<", "LT"), toArgs(Object.class, Object.class), boolean.class),
    GtEOper(TokenType.BinaryOp, 4, toLabels(">=", "GE"), toArgs(Object.class, Object.class), boolean.class),
    LtEOper(TokenType.BinaryOp, 4, toLabels("<=", "LE"), toArgs(Object.class, Object.class), boolean.class),
    
    // Logical Operators
    AndOper(TokenType.BinaryOp, 2, toLabels("&&", "and"), toArgs(Object.class, Object.class), boolean.class),
    OrOper(TokenType.BinaryOp, 2, toLabels("||", "or"), toArgs(Object.class, Object.class), boolean.class),
    XorOper(TokenType.BinaryOp, 2, toLabels("xor"), toArgs(Object.class, Object.class), boolean.class),
    NotOper(TokenType.UnaryOp, 2, toLabels("~", "not"), toArgs(Object.class), boolean.class),
    
    // Unary is tests
    IsEvenOper(TokenType.UnaryFunc, 8, toLabels("isEven"), toArgs(Object.class), boolean.class),
    IsOddOper(TokenType.UnaryFunc, 8, toLabels("isOdd"), toArgs(Object.class), boolean.class),
    
    IsErrorOper(TokenType.UnaryFunc, 8, toLabels("isError"), toArgs(Object.class), boolean.class),
    IsNumberOper(TokenType.UnaryFunc, 8, toLabels("isNumber"), toArgs(Object.class), boolean.class),
    IsTextOper(TokenType.UnaryFunc, 8, toLabels("isText"), toArgs(Object.class), boolean.class),
    IsLogicalOper(TokenType.UnaryFunc, 8, toLabels("isLogical"), toArgs(Object.class), boolean.class),
    
    // special isNull operator, which is somewhat out of bounds
    IsNullOper(TokenType.UnaryFunc, 8, toLabels("isNull"), toArgs(Object.class), boolean.class),
    
    // special "if" operator
    IfOper("if", TokenType.GenericFunc, 8, (Class<?>)null, (String)null, Boolean.class, Object.class, Object.class),
    
    // special JSON Parser operator
    JsonOper(TokenType.GenericFunc, 8, toLabels("fromJSON", "jsonGet"), toArgs(JSONObject.class, String.class), Object.class, MathUtil.class, "jsonGet"),
    
    ColRefOper(TokenType.GenericFunc, 8, toLabels("colRef"), toArgs(Object.class), Column.class),
    RowRefOper(TokenType.GenericFunc, 8, toLabels("rowRef"), toArgs(Object.class), Row.class),
    
    // Special math operators, implemented in Java Math class
    ModOper(TokenType.BinaryOp, 6, MathUtil.class, "mod", "mod"),  
    PowerOper(TokenType.BinaryOp, 7, Math.class, "pow", "^"),
    
    // Factorial operator, implemented in code
    FactOper(TokenType.UnaryTrailingOp, 7, MathUtil.class, "fact", "!"),
    PercentOper(TokenType.UnaryTrailingOp, 7, MathUtil.class, "percent", "%"),
    
    // Unary functions, mostly supported in Java Math
    FracOper("frac", TokenType.UnaryFunc, 8, MathUtil.class),
    NegOper("neg", TokenType.UnaryFunc, 8, MathUtil.class),
    
    AbsOper("abs", TokenType.UnaryFunc, 8, Math.class),
    SqrtOper("sqrt", TokenType.UnaryFunc, 8, Math.class),
    CbrtOper("cbrt", TokenType.UnaryFunc, 8, Math.class),
    ExpOper(TokenType.UnaryFunc, 8, Math.class, "exp"),
    LogOper(TokenType.UnaryFunc, 8, Math.class, "log", "ln", "loge"),
    Log10Oper(TokenType.UnaryFunc, 8, Math.class, "log10", "log", "log10"),
    RandOper(TokenType.BuiltIn, 8, toLabels("random", "rand"), null, double.class, Math.class),
    RandIntOper(TokenType.UnaryFunc, 8, toLabels("randomInt", "randomInt", "randInt"), toArgs(double.class), int.class, MathUtil.class),
    RandBetweenOper(TokenType.BinaryFunc, 8, toLabels("randomBetween", "randomBetween", "randBetween"), toArgs(double.class, double.class), int.class, MathUtil.class),

    // TVM Calculations
    PmtOper(TokenType.GenericFunc, 8, toLabels("pmt", "paymentPerTerm"), toArgs(double.class, int.class, double.class, double.class), double.class, MathUtil.class),
    FvOper(TokenType.GenericFunc, 8, toLabels("fv", "futureValue"), toArgs(double.class, int.class, double.class, double.class), double.class, MathUtil.class),
    PvOper(TokenType.GenericFunc, 8, toLabels("pv", "presentValue"), toArgs(double.class, int.class, double.class, double.class), double.class, MathUtil.class),
    NPerOper(TokenType.GenericFunc, 8, toLabels("nper", "numPeriods"), toArgs(double.class, double.class, double.class, double.class), double.class, MathUtil.class),
    RateOper(TokenType.GenericFunc, 8, toLabels("rate", "interestRate"), toArgs(int.class, double.class, double.class, double.class), double.class, MathUtil.class),
    
    // trig functions, radians
    toDegreesOper("toDegrees", TokenType.UnaryFunc, 8, Math.class),
    toRadiansOper("toRadians", TokenType.UnaryFunc, 8, Math.class),
    
    SinOper("sin", TokenType.UnaryFunc, 8, Math.class),
    CosOper("cos", TokenType.UnaryFunc, 8, Math.class),
    TanOper("tan", TokenType.UnaryFunc, 8, Math.class),
    ASinOper("asin", TokenType.UnaryFunc, 8, Math.class),
    ACosOper("acos", TokenType.UnaryFunc, 8, Math.class),
    ATanOper("atan", TokenType.UnaryFunc, 8, Math.class),

    SinDOper("sinD", TokenType.UnaryFunc, 8, MathUtil.class),
    CosDOper("cosD", TokenType.UnaryFunc, 8, MathUtil.class),
    TanDOper("tanD", TokenType.UnaryFunc, 8, MathUtil.class),
    ASinDOper("asinD", TokenType.UnaryFunc, 8, MathUtil.class),
    ACosDOper("acosD", TokenType.UnaryFunc, 8, MathUtil.class),
    ATanDOper("atanD", TokenType.UnaryFunc, 8, MathUtil.class),

    SinHOper("sinh", TokenType.UnaryFunc, 8, Math.class),
    CosHOper("cosh", TokenType.UnaryFunc, 8, Math.class),
    TanHOper("tanh", TokenType.UnaryFunc, 8, Math.class),

    FactFuncOper(TokenType.UnaryFunc, 8, MathUtil.class, "fact", "factorial", "fact"),
    FloorOper(TokenType.UnaryFunc, 8, Math.class, "floor", "roundDown", "floor"),
    CeilOper(TokenType.UnaryFunc, 8, Math.class, "ceil", "roundUp", "ceil"),
    SignOper(TokenType.UnaryFunc, 8, Math.class, "signum", "sign", "signum"),
    RoundOper("round", TokenType.UnaryFunc, 8, Math.class),
  
    // Useful functions from Apache Math Commons
    IsPrimeOper(TokenType.UnaryFunc, 8, toLabels("isPrime"), toArgs(int.class), boolean.class, org.apache.commons.math3.primes.Primes.class),    
    NextPrimeOper("nextPrime", TokenType.UnaryFunc, 8, org.apache.commons.math3.primes.Primes.class, "nextPrime", int.class),
    PrimeFactorsOper("primeFactors", TokenType.UnaryFunc, 8, org.apache.commons.math3.primes.Primes.class, "primeFactors", int.class),
    PowerOf2Oper("isPowerOfTwo", TokenType.UnaryFunc, 8, org.apache.commons.math3.util.ArithmeticUtils.class, "isPowerOfTwo", long.class),
    
    GcdOper("gcd", TokenType.BinaryFunc, 8, org.apache.commons.math3.util.ArithmeticUtils.class, "gcd", long.class, long.class),   
    LcmOper("lcm", TokenType.BinaryFunc, 8, org.apache.commons.math3.util.ArithmeticUtils.class, "lcm", long.class, long.class),
    
    // String functions
    LenOper(TokenType.UnaryFunc, 8, toLabels("len", "length"), toArgs(String.class), int.class, MathUtil.class, "length"),
    LeftOper(TokenType.BinaryFunc, 8, toLabels("left", "instrLeft"), toArgs(String.class, int.class), String.class, MathUtil.class, "instrLeft"),
    RightOper(TokenType.BinaryFunc, 8, toLabels("right", "instrRight"), toArgs(String.class, int.class), String.class, MathUtil.class, "instrRight"),
    MidOper(TokenType.GenericFunc, 8, toLabels("mid", "instrMid"), toArgs(String.class, int.class, int.class), String.class, MathUtil.class, "instrMid"),
    toLowerOper(TokenType.UnaryFunc, 8, toLabels("toLower"), toArgs(String.class), String.class, MathUtil.class),
    toUpperOper(TokenType.UnaryFunc, 8, toLabels("toUpper"), toArgs(String.class), String.class, MathUtil.class),
    IndexOfOper(TokenType.BinaryFunc, 8, toLabels("indexOf"), toArgs(String.class, String.class), int.class, MathUtil.class, "indexOf"),
    ContainsOper(TokenType.BinaryFunc, 8, toLabels("contains"), toArgs(String.class, String.class), boolean.class, MathUtil.class, "contains"),
    trimOper(TokenType.UnaryFunc, 8, toLabels("trim"), toArgs(String.class), String.class, MathUtil.class),
    reverseOper(TokenType.UnaryFunc, 8, toLabels("reverse"), toArgs(String.class), String.class, MathUtil.class),
    toStringOper(TokenType.UnaryFunc, 8, toLabels("toString"), toArgs(Object.class), String.class, MathUtil.class),
    toNumberOper(TokenType.UnaryFunc, 8, toLabels("toNumber"), toArgs(Object.class), double.class, MathUtil.class),
   
    // Binary functions, mostly supported in Java Math
    ReminderFuncOper(TokenType.BinaryFunc, 8, Math.class, "IEEEremainder", "remainder"),
    ModFuncOper(TokenType.BinaryFunc, 8, MathUtil.class, "mod", "mod"),
    PowerFuncOper(TokenType.BinaryFunc, 8, Math.class, "pow", "pow", "power"),

    BiggerOper(TokenType.BinaryFunc, 8, Math.class, "max", "bigger"),
    SmallerOper(TokenType.BinaryFunc, 8, Math.class, "min", "smaller"),
    HypotOper(TokenType.BinaryFunc, 8, org.apache.commons.math3.util.FastMath.class, "hypot"),
    NumberOfOper("numberOf", TokenType.GenericFunc, 8, MathUtil.class, "numberOf", TableElement.class, Object.class),    
    PermOper(TokenType.BinaryFunc, 8, MathUtil.class, "numPermutations", "perm", "nPk"),    
    CombOper(TokenType.BinaryFunc, 8, MathUtil.class, "numCombinations", "comb", "nCk", "nChooseK"),    

    // Builtin functions
    NullOper(TokenType.BuiltIn, 8, "null"),
    TrueOper(TokenType.BuiltIn, 8, "true"),
    FalseOper(TokenType.BuiltIn, 8, "false"),
    EOper(TokenType.BuiltIn, 8, toLabels("e"), null, double.class, MathUtil.class),
    PiOper(TokenType.BuiltIn, 8, toLabels("pi"), null, double.class, MathUtil.class),
    ColumnIndexOper(TokenType.BuiltIn, 8, toLabels("columnIndex", "cidx"), null, int.class),
    RowIndexOper(TokenType.BuiltIn, 8, toLabels("rowIndex", "ridx"), null, int.class),
    
    NowOper(TokenType.BuiltIn, 8, toLabels("now"), null, java.util.Date.class),

    // Single Variable Stat Functions 
    SumOper(TokenType.StatOp, 8, "sum"),
    Sum2Oper(TokenType.StatOp, 8, "sumOfSquares", "sumSqs", "ss", "sumsq"),
    SumSqD2Oper(TokenType.StatOp, 8, "sumOfSquaredDeviates", "ss", "ssd", "devsq"),
    MeanOper(TokenType.StatOp, 8, "mean", "average", "avg"),
    MedianOper(TokenType.StatOp, 8, "median"),
    QuartileOper(TokenType.StatOp, 8, toLabels("quartile"), toArgs(TableElement.class, int.class), double.class),
    FirstQuartileOper(TokenType.StatOp, 8, "firstQuartile", "firstQ"),
    ThirdQuartileOper(TokenType.StatOp, 8, "thirdQuartile", "thirdQ"),
    ModeOper(TokenType.StatOp, 8, "mode"),
    StDevPopulationOper(TokenType.StatOp, 8, "stDevPopulation", "stDevOfPopulation", "stDev.p"),
    StDevSampleOper(TokenType.StatOp, 8, "stDevSample", "stDevOfSample", "stDev", "stDev.s"),
    VarPopulationOper(TokenType.StatOp, 8, "variancePopulation", "varianceOfPopulation", "var.p"),
    VarSampleOper(TokenType.StatOp, 8, "varianceSample", "varianceOfSample", "var", "var.s", "variance"),
    MinOper(TokenType.StatOp, 8, "min", "minimum"),
    MaxOper(TokenType.StatOp, 8, "max", "maximum"),
    RangeOper(TokenType.StatOp, 8, "range", "spread"),
    CountOper(TokenType.StatOp, 8, "count", "cnt"),
    SkewOper(TokenType.StatOp, 8, "skewness", "skew"),
    KurtosisOper(TokenType.StatOp, 8, "kurtosis", "kurt"),
    
    PValueOper(TokenType.StatOp, 8, "tSigLevel",  "pValue", "oneSampleTwoTailTTestPValue"),
    TValueOper(TokenType.StatOp, 8, "tStatistic", "tValue", "oneSampleTwoTailTTestTValue"),
    TTestOper(TokenType.StatOp, 8, "tTest"),
    
    TwoSamplePValueOper(TokenType.StatOp, 8, "twoSampleTSigLevel",  "tsPValue",  "twoSamplePValue", "twoSampleTwoTailTTestPValue"),
    TwoSampleTValueOper(TokenType.StatOp, 8, "twoSampleTStatistic", "tsTValue",  "twoSampleTValue", "twoSampleTwoTailTTestTValue"),
    TwoSampleTTestOper(TokenType.StatOp, 8, "twoSampleTTest", "tsTTest"),
    
    // Normal Distribution Single Variable Stat Functions 
    NormSampleOper(TokenType.BinaryFunc, 8, MathUtil.class, "normalSample", "normalSample", "normS", "normalS"),
    NormDensityOper(TokenType.GenericFunc, 8, MathUtil.class, "normalDensity", "normalDensity", "normD", "normalD", "normPDF"),
    NormCumProbOper(TokenType.GenericFunc, 8, MathUtil.class, "normalCumProb", "normalCumProb", "normCP", "normalCP", "normCDF"),
    NormInvCumProbOper(TokenType.GenericFunc, 8, MathUtil.class, "normalInvCumProb", "normalInvCumProb", "normICP", "normalICP", "normInvCDF"),
    NormProbOper(TokenType.GenericFunc, 8, MathUtil.class, "normalProbability", "normalProbability", "normalProb", "normP", "normalP", "normPMF"),
    NormProbInRangeOper(TokenType.GenericFunc, 8, MathUtil.class, "normalProbInRange", "normalProbInRange", 
                                                                  "normalProbabilityInRange","normPIR", "normalPIR"), 
    // Exponential Distribution Single Variable Stat Functions 
    ExpSampleOper(TokenType.UnaryFunc, 8, MathUtil.class, "exponentialSample", "exponentialSample", "expS"),
    ExpDensityOper(TokenType.BinaryFunc, 8, MathUtil.class, "exponentialDensity", "exponentialDensity", "expD", "expPDF"),
    ExpCumProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "exponentialCumProb", "exponentialCumProb", "expCP", "expCDF"),
    ExpInvCumProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "exponentialInvCumProb", "exponentialInvCumProb", "expICP", "expInvCDF"),
    ExpProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "exponentialProbability", "exponentialProbability", "expProb", "expP", "expPMF"),
    ExpProbInRangeOper(TokenType.GenericFunc, 8, MathUtil.class, "exponentialProbInRange", "exponentialProbInRange", 
                                                                    "exponentialProbabilityInRange","expPIR"), 
    // Chi Squared Distribution Single Variable Stat Functions                                                               
    ChiSqSampleOper(TokenType.UnaryFunc, 8, MathUtil.class, "chiSqSample", "ChiSqSample", "ChiSqS"),
    ChiSqDensityOper(TokenType.BinaryFunc, 8, MathUtil.class, "chiSqDensity", "ChiSqDensity", "ChiSqD", "ChiSqPDF"),
    ChiSqCumProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "chiSqCumProb", "chiSqCumProb", "chiSqCP", "chiSqCDF"),
    ChiSqInvCumProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "chiSqInvCumProb", "ChiSqInvCumProb", "ChiSqICP", "ChiSqInvCDF"),
    ChiSqProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "chiSqProbability", "ChiSqProbability", "ChiSqProb", "ChiSqP", "ChiSqPMF"),
    ChiSqProbInRangeOper(TokenType.GenericFunc, 8, MathUtil.class, "chiSqProbInRange", "ChiSqProbInRange", "ChiSqProbabilityInRange","ChiSqPIR"),    
    ChiSqScoreOper("chiSqScore", TokenType.GenericFunc, 8, MathUtil.class, "chiSqScore", double.class, double.class, double.class),    
    ChiSqStatOper("chiSqStat", TokenType.StatOp, 8, (Class<?>)null, (String)null, TableRowColumnElement.class, TableRowColumnElement.class),
    ChiSqTestOper("chiSqTest", TokenType.StatOp, 8, (Class<?>)null, (String)null, TableRowColumnElement.class, TableRowColumnElement.class, double.class),

    // T Distribution Single Variable Stat Functions                                                               
    TSampleOper(TokenType.UnaryFunc, 8, MathUtil.class, "tSample", "tSample", "tS"),
    TDensityOper(TokenType.BinaryFunc, 8, MathUtil.class, "tDensity", "tDensity", "tD", "tPDF"),
    TCumProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "tCumProb", "tCumProb", "tCP", "tCDF"),
    TInvCumProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "tInvCumProb", "tInvCumProb", "tICP", "tInvCDF"),
    TProbOper(TokenType.BinaryFunc, 8, MathUtil.class, "tProbability", "tProbability", "tProb", "tP", "tPMF"),
    TProbInRangeOper(TokenType.GenericFunc, 8, MathUtil.class, "tProbInRange", "tProbInRange", "tProbabilityInRange","tPIR"),    

    TScoreOper("tScore", TokenType.GenericFunc, 8, MathUtil.class, "tScore", double.class, double.class, double.class, double.class),    
    PopMeanOper("popMean", TokenType.GenericFunc, 8, MathUtil.class, "popMean", double.class, double.class, double.class, double.class),    
    
    // Two Variable Stat Functions
    LinearSlopeOper("slope", TokenType.StatOp, 8, (Class<?>)null, (String)null, TableRowColumnElement.class, TableRowColumnElement.class),
    LinearInterceptOper("intercept", TokenType.StatOp, 8, (Class<?>)null, (String)null, TableRowColumnElement.class, TableRowColumnElement.class),
    LinearROper(TokenType.StatOp, 8, "correlation", "ccr", "correl"),
    LinearR2Oper(TokenType.StatOp, 8, "correlation2", "ccr2", "r2", "rsq"),
    LinearComputeXOper(TokenType.GenericFunc, 8, MathUtil.class, "lrComputeX", "computeX", "lrComputeX"),
    LinearComputeYOper(TokenType.GenericFunc, 8, MathUtil.class, "lrComputeY", "computeY", "lrComputeY"),
    
    // Transformation Functions
    MeanCenterOper(TokenType.TransformOp, 8, toLabels("meanCenter"), toArgs(TableElement.class), null),
    NormalizeOper(TokenType.TransformOp, 8, toLabels("normalize", "standardize"), toArgs(TableElement.class), null),    
    ScaleOper(TokenType.TransformOp, 8, toLabels("scale"), toArgs(TableElement.class, double.class, double.class), null),

    Paren(10, TokenType.LeftParen, TokenType.RightParen),
    NOP(0, TokenType.Comma, TokenType.ColumnRef, TokenType.RowRef, TokenType.SubsetRef, TokenType.CellRef, TokenType.TableRef),

    LAST_operator;
    
    static private String [] toLabels(String... labels)
    {
        return labels;        
    }
    
    static private Class<?> [] toArgs(Class<?>... args)
    {
        return args;        
    }
    
    static final public int MAX_PRIORITY = 10;
    
    private String m_label;
    private Set<String> m_aliases;
    private Set<TokenType> m_tokenTypes;
    private int m_priority;
    private Class<? extends Object> m_clazz;
    private String m_methodName;
    private Method m_method;
    private Class<?>[] m_methodArgs;

    private Class<? extends Object> m_resultType;
    
    private BuiltinOperator()
    {
        m_priority = 0;
        m_tokenTypes = new LinkedHashSet<TokenType>();
        m_aliases = new LinkedHashSet<String>();
    }
    
    private BuiltinOperator(TokenType tt, 
                            int priority, 
                            String labels[], 
                            Class<? extends Object > args[], 
                            Class<? extends Object> resultType)
    {
        this(tt, priority, labels, args, resultType, (Class<?>)null, (String)null);
    }
    
    private BuiltinOperator(TokenType tt, 
                            int priority, 
                            String labels[], 
                            Class<? extends Object > args[], 
                            Class<? extends Object> resultType, 
                            Class<? extends Object> clazz)
    {
        this(tt, priority, labels, args, resultType, clazz, null);
    }
    
    private BuiltinOperator(TokenType tt, 
                            int priority, 
                            String labels[], 
                            Class<? extends Object > args[], 
                            Class<? extends Object> resultType, 
                            Class<? extends Object> clazz, 
                            String methodName)
    {
        this();
        m_tokenTypes.add(tt);
        m_priority = priority;
        
        if (labels != null && labels.length > 0) {
            for (String label : labels) {
                if (m_label == null)
                    m_label = label;
                m_aliases.add(label.toLowerCase());
            }
        }
        else {
            m_label = methodName;
            m_aliases.add(methodName.toLowerCase());
        }
        
        m_resultType = resultType;
        m_methodArgs = args == null ? new Class<?> [] {} : args;
        
        m_clazz = clazz;
        if (clazz != null)
            m_methodName = methodName != null ? methodName : m_label;
    }
    
    private BuiltinOperator(int priority)
    {
        this();
        m_priority = priority;
    }
    
    private BuiltinOperator(String label, TokenType tt)
    {
        this(label, tt, 6);
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority)
    {
        this();
        m_label = label;
        if (label != null && label.trim().length() >= 0)
        	m_aliases.add(label.trim().toLowerCase());
        
        m_priority = priority;
        m_tokenTypes.add(tt);
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority, Class<? extends Object> clazz)
    {
        this(label, tt, priority, clazz, label);
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority, Class<? extends Object> clazz, String methodName)
    {
        this(label, tt, priority);
        m_clazz = clazz;
        m_methodName = methodName;
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority, Class<? extends Object> clazz, String methodName, Class<?>... argTypes)
    {
        this(label, tt, priority);
        m_clazz = clazz;
        m_methodName = methodName;
        m_methodArgs = argTypes;
    }
    
    private BuiltinOperator(TokenType tt, int priority, Class<? extends Object> clazz, String methodName, String...labels)
    {
        this();
        m_priority = priority;
        m_tokenTypes.add(tt);
        if (labels != null && labels.length > 0) {
            for (String label : labels) {
                if (m_label == null)
                    m_label = label;
                m_aliases.add(label.toLowerCase());
            }
        }
        else {
            m_label = methodName;
            m_aliases.add(methodName.toLowerCase());
        }
        
        m_clazz = clazz;
        m_methodName = methodName;
    }
    
    private BuiltinOperator(int priority, TokenType... tts)
    {
        this();
        m_priority = priority;
        if (tts != null) {
            for (TokenType tt : tts) {
                m_tokenTypes.add(tt);
            }
        }
    }
    
    private BuiltinOperator(TokenType tt, int priority, String... labels)
    {
        this();
        m_priority = priority;
        m_tokenTypes.add(tt);
        if (labels != null) {
            for (String label : labels) {
            	if (m_label == null)
            		m_label = label;
            	m_aliases.add(label.toLowerCase());
            }
        }
    }
    
    /*
     * Class methods
     */
    public boolean isRequiresRetainedDataset()
    {
        switch(this) {
            case ModeOper:
            case MedianOper:
            case FirstQuartileOper:
            case ThirdQuartileOper:
            case QuartileOper:
                return true;
                
            default:
                return false;
        }
    }
    
    public TokenType getPrimaryTokenType()
    {
        if (m_tokenTypes != null && m_tokenTypes.size() == 1) 
            return m_tokenTypes.toArray(new TokenType [] {})[0];
        else
            return null;
    }
    
    public Set<TokenType> getTokenTypes()
    {
        return m_tokenTypes;
    }
    
    @Override
    public TokenType getTokenType()
    {
        return getPrimaryTokenType();
    }
    
    @Override
    public int getPriority()
    {
        return m_priority;
    }
    
    @Override
    public String getLabel()
    {
        return m_label;
    }
    
    public Set<String> getAliases()
    {
        return m_aliases;
    }
    
	@Override
    public int numArgs() 
	{
	    if (getArgTypes() != null)
	        return m_methodArgs.length;
	    else {
	        // handle special cases and default
	        switch (this) {
	            default:
	                TokenType tt = getPrimaryTokenType();
	                return tt != null ? tt.numArgs() : 0;
	        }
	    }
	}	

    @Override
    public Class<?>[] getArgTypes()
    {
        // allocate array and set all elements to Double
        if (m_methodArgs == null) {
            // handle special case operators first, then
            // use primary token type to resolve
            switch (this) {
                case PlusOper:
                case MinusOper:
                case MultOper:
                case DivOper:
                    m_methodArgs = new Class<?>[]{Object.class, Object.class};
                    break;
                    
                case PValueOper:
                case TValueOper:
                    m_methodArgs = new Class<?>[]{TableRowColumnElement.class, double.class};
                    break;
                    
                case TTestOper:
                    m_methodArgs = new Class<?>[]{TableRowColumnElement.class, double.class, double.class};
                    break;
                    
                case TwoSamplePValueOper:
                case TwoSampleTValueOper:
                    m_methodArgs = new Class<?>[]{TableRowColumnElement.class, TableRowColumnElement.class};
                    break;
                    
                case TwoSampleTTestOper:
                    m_methodArgs = new Class<?>[]{TableRowColumnElement.class, TableRowColumnElement.class, double.class};
                    break;
                    
                case LinearROper:
                case LinearR2Oper:
                    m_methodArgs = new Class<?>[]{TableRowColumnElement.class, TableRowColumnElement.class};
                    break;
                    
                case LinearComputeXOper:
                case LinearComputeYOper:
                    m_methodArgs = new Class<?>[]{double.class, double.class, double.class};
                    break;
                
                case NormDensityOper:
                case NormCumProbOper:
                case NormInvCumProbOper:
                case NormProbOper:
                    m_methodArgs = new Class<?>[]{double.class, double.class, double.class};
                    break;
                
                case NormProbInRangeOper:
                    m_methodArgs = new Class<?>[]{double.class, double.class, double.class, double.class};
                    break;
                    
                case ChiSqProbInRangeOper:
                case ExpProbInRangeOper:
                case TProbInRangeOper:
                    m_methodArgs = new Class<?>[]{double.class, double.class, double.class};
                    break;
                
                default:
                    TokenType tt = getPrimaryTokenType();
                    int numArgs = tt != null ? tt.numArgs() : 0;
                    if (numArgs > 0 && tt != null) {
                        m_methodArgs = new Class<?>[numArgs];
                        switch (tt) {
                            case StatOp:
                                m_methodArgs[0] = TableElement.class;
                                break;
                                
                            case TransformOp:
                                m_methodArgs[0] = TableRowColumnElement.class;
                                break;
                                
                            default:
                                Arrays.fill(m_methodArgs, double.class);
                                break;
                        }
                    }
                    break;
            }           
        }
        
        return m_methodArgs;
    }
    
    Method getMethod()
    {
        if (m_method == null && this.m_clazz != null) {
            try
            {
                m_method = m_clazz.getMethod(m_methodName, getArgTypes());
            }
            catch (NoSuchMethodException | SecurityException e)
            {
                throw new IllegalTableStateException(e);
            }
        }
        
        return m_method;
    }
    
    @Override
    public Token evaluate(Token... args)
    {
        assert numArgs() <= args.length : "Insufficent number of tokens suppled: " + this.toString();
        assert getMethod() != null : "No method available: " + this.toString();       
        
        Token retVal = null;
        int numArgs = numArgs();
        Object [] params = new Object[numArgs];
        Class<?> [] argTypes = getArgTypes();
        for (int i = 0; i < numArgs; i++)
        {
            if (args[i] == null || args[i].isNull())
                return Token.createNullToken();
            else if (!args[i].isA(argTypes[i]))
                return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);
                        
            params[i] = coerceNumericValue(args[i], argTypes[i]);
        }
        
        try
        {
            Object val = getMethod().invoke(null, params);
            retVal = new Token(val);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            return Token.createErrorToken(e.getMessage());
        }
        
        return retVal;
    }
    
    private Object coerceNumericValue(Token t, Class<?> requiredClass)
    {
        if (t.isNull() || !t.isNumeric() || t.isA(requiredClass, false))
            return t.getValue();
        
        // we know t is non-null and numeric, but not of the same class
        
        Double dValue = t.getNumericValue();
        if (requiredClass == int.class || requiredClass == Integer.class)
            return dValue.intValue();
        else if (requiredClass == long.class || requiredClass == Long.class)
            return dValue.longValue();
        else if (requiredClass == short.class || requiredClass == Short.class)
            return dValue.longValue();
        
        // coerce argument
        return null;
    }

    static public final List<Operator> listBuiltinOperators()
    {
        return listBuiltinOperators(null);
    }
    
    static public final List<Operator> listBuiltinOperators(TokenType tt)
    {
        List<Operator> ops = new ArrayList<Operator>(BuiltinOperator.values().length);
        
        for (BuiltinOperator o : BuiltinOperator.values()) {
            if (tt == null || o.getTokenType() == tt) {
                switch (o) {
                    case NOP:
                    case Paren:
                    case LAST_operator:
                    case NULL_operator:
                        break;
                        
                    default:
                        ops.add(o);
                        break;
                }
            }
        }
        
        return ops;
    }
    
    static public final Set<String> binaryOpLabels()
    {
        Set<String> labels = new HashSet<String>();
        
        for (BuiltinOperator o : BuiltinOperator.values()) {
            if (o.getTokenType() == TokenType.BinaryOp) {
                for (String s : o.getAliases()) {
                    labels.add(s.toLowerCase());
                }
            }
        }
        
        return labels;
    }
    
    static public final Set<String> unaryOpLabels()
    {
        Set<String> labels = new HashSet<String>();
        
        for (BuiltinOperator o : BuiltinOperator.values()) {
            if (o.getTokenType() == TokenType.UnaryOp) {
                for (String s : o.getAliases()) {
                    labels.add(s.toLowerCase());
                }
            }
        }
        
        return labels;
    }
    
    static public final boolean isValidBinaryOp(String label) 
    {
        if (label != null) {
            String key = label.trim().toLowerCase();
            return binaryOpLabels().contains(key);
        }
        else
            return false;
    }

    static public final boolean isValidUnaryOp(String label) 
    {
        if (label != null) {
            String key = label.trim().toLowerCase();
            return unaryOpLabels().contains(key);
        }
        else
            return false;
    }

    @Override
    public boolean isRightAssociative()
    {
        switch (this) {
            case PowerOper:
                return true;
            
            default:
                return false;
        }
    }
    
    public boolean isMathOper()
    {
        switch (this) {
            case PlusOper:
            case MinusOper:
            case MultOper:
            case DivOper:
                return true;
            
            default:
                return false;
        }
    }
    
    @Override
    public Class<?> getResultType()
    {
        if (m_resultType != null)
            return m_resultType;
        else
            return Object.class;
    }
    
    @Override
    public boolean isVariableArgs()
    {
        switch (this) {
            default:
                return false;
        }
    }
}
