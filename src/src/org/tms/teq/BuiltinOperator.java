package org.tms.teq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    PlusOper(TokenType.BinaryOp, 5, "+", toCategories()),
    MinusOper(TokenType.BinaryOp, 5, "-", toCategories()),
    MultOper(TokenType.BinaryOp, 6, "*", toCategories()),
    DivOper(TokenType.BinaryOp, 6, "/", toCategories()),
    
    // boolean comparison operators
    EqOper(TokenType.BinaryOp, 3, toLabels("=", "==", "EQ"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical", "comparison")),
    NEqOper(TokenType.BinaryOp, 3, toLabels("!=", "<>", "NE"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical", "comparison")),
    GtOper(TokenType.BinaryOp, 4, toLabels(">", "GT"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical", "comparison")),
    LtOper(TokenType.BinaryOp, 4, toLabels("<", "LT"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical", "comparison")),
    GtEOper(TokenType.BinaryOp, 4, toLabels(">=", "GE"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical", "comparison")),
    LtEOper(TokenType.BinaryOp, 4, toLabels("<=", "LE"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical", "comparison")),
    
    // Logical Operators
    AndOper(TokenType.BinaryOp, 2, toLabels("&&", "and"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical")),
    OrOper(TokenType.BinaryOp, 2, toLabels("||", "or"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical")),
    XorOper(TokenType.BinaryOp, 2, toLabels("xor"), toArgs(Object.class, Object.class), boolean.class, toCategories("logical")),
    NotOper(TokenType.UnaryOp, 2, toLabels("~", "not"), toArgs(Object.class), boolean.class, toCategories("logical")),
    
    // Unary is tests
    IsEvenOper(TokenType.UnaryFunc, 8, toLabels("isEven"), toArgs(Object.class), boolean.class, toCategories("logical", "math")),
    IsOddOper(TokenType.UnaryFunc, 8, toLabels("isOdd"), toArgs(Object.class), boolean.class, toCategories("logical", "math")),
    
    IsErrorOper(TokenType.UnaryFunc, 8, toLabels("isError"), toArgs(Object.class), boolean.class, toCategories("information")),
    IsNumberOper(TokenType.UnaryFunc, 8, toLabels("isNumber"), toArgs(Object.class), boolean.class, toCategories("information")),
    IsTextOper(TokenType.UnaryFunc, 8, toLabels("isText"), toArgs(Object.class), boolean.class, toCategories("information")),
    IsLogicalOper(TokenType.UnaryFunc, 8, toLabels("isLogical"), toArgs(Object.class), boolean.class, toCategories("information")),
    IsNullOper(TokenType.UnaryFunc, 8, toLabels("isNull"), toArgs(Object.class), boolean.class, toCategories("information")),
    
    // special "if" operator
    IfOper(TokenType.GenericFunc, 8, toLabels("if"), toArgs(boolean.class, Object.class, Object.class), Object.class, toCategories("logical")),
    
    // special JSON Parser operator
    JsonOper(TokenType.GenericFunc, 8, toLabels("fromJSON", "jsonGet"), toArgs(JSONObject.class, String.class), Object.class, toCategories("information", "lookup", "json"), MathUtil.class, "jsonGet"),
    
    ColRefOper(TokenType.GenericFunc, 8, toLabels("colRef"), toArgs(Object.class), Column.class, toCategories("lookup", "table")),
    RowRefOper(TokenType.GenericFunc, 8, toLabels("rowRef"), toArgs(Object.class), Row.class, toCategories("lookup", "table")),
    NumberOfOper(TokenType.GenericFunc, 8, toLabels("numberOf"), toArgs(TableElement.class, Object.class), int.class, toCategories("information", "lookup"), MathUtil.class), 
    
    // Special math operators, implemented in Java Math class
    //ModOper(TokenType.BinaryOp, 6, toLabels("mod", "modulus", "remainder"), toArgs(double.class), double.class, toCategories("math"), MathUtil.class),  
    PowerOper(TokenType.BinaryOp, 7, toLabels("^"), toArgs(double.class, double.class), double.class, toCategories("math"), Math.class, "pow"),  
    
    // Factorial operator, implemented in code
    FactOper(TokenType.UnaryTrailingOp, 7, toLabels("!"), toArgs(double.class), double.class, toCategories("math"), MathUtil.class, "fact"),  
    PercentOper(TokenType.UnaryTrailingOp, 7, toLabels("%"), toArgs(double.class), double.class, toCategories("math"), MathUtil.class, "percent"),  
    
    // Unary functions, mostly supported in Java Math
    FracOper(TokenType.UnaryFunc, 8, toLabels("frac"), toArgs(double.class), double.class, toCategories("math"), MathUtil.class),  
    NegOper(TokenType.UnaryFunc, 8, toLabels("neg"), toArgs(double.class), double.class, toCategories("math"), MathUtil.class),  
    toEvenOper(TokenType.UnaryFunc, 8, toLabels("toEven"), toArgs(double.class), long.class, toCategories("math"), MathUtil.class),  
    toOddOper(TokenType.UnaryFunc, 8, toLabels("toOdd"), toArgs(double.class), long.class, toCategories("math"), MathUtil.class),  
    
    AbsOper(TokenType.UnaryFunc, 8, toLabels("abs"), toArgs(double.class), double.class, toCategories("math"), Math.class),  
    SqrtOper(TokenType.UnaryFunc, 8, toLabels("sqrt"), toArgs(double.class), double.class, toCategories("math"), Math.class),  
    CbrtOper(TokenType.UnaryFunc, 8, toLabels("cbrt"), toArgs(double.class), double.class, toCategories("math"), Math.class),  
    ExpOper(TokenType.UnaryFunc, 8, toLabels("exp"), toArgs(double.class), double.class, toCategories("math"), Math.class),  
    LogOper(TokenType.UnaryFunc, 8, toLabels("ln", "loge"), toArgs(double.class), double.class, toCategories("math"), Math.class, "log"),  
    Log10Oper(TokenType.UnaryFunc, 8, toLabels("log", "log10"), toArgs(double.class), double.class, toCategories("math"), Math.class, "log10"),  
    
    RandOper(TokenType.BuiltIn, 8, toLabels("random", "rand"), null, double.class, toCategories("math", "statistic"), Math.class),
    RandIntOper(TokenType.UnaryFunc, 8, toLabels("randomInt", "randomInt", "randInt"), toArgs(double.class), int.class, toCategories("math", "statistic"), MathUtil.class),
    RandBetweenOper(TokenType.BinaryFunc, 8, toLabels("randomBetween", "randomBetween", "randBetween"), toArgs(double.class, double.class), int.class, toCategories("math", "statistic"), MathUtil.class),

    // TVM Calculations
    PmtOper(TokenType.GenericFunc, 8, toLabels("pmt", "paymentPerTerm"), toArgs(double.class, int.class, double.class, double.class), double.class, toCategories("Financial"), MathUtil.class),
    FvOper(TokenType.GenericFunc, 8, toLabels("fv", "futureValue"), toArgs(double.class, int.class, double.class, double.class), double.class, toCategories("Financial"), MathUtil.class),
    PvOper(TokenType.GenericFunc, 8, toLabels("pv", "presentValue"), toArgs(double.class, int.class, double.class, double.class), double.class, toCategories("Financial"), MathUtil.class),
    NPerOper(TokenType.GenericFunc, 8, toLabels("nper", "numPeriods"), toArgs(double.class, double.class, double.class, double.class), double.class, toCategories("Financial"), MathUtil.class),
    RateOper(TokenType.GenericFunc, 8, toLabels("rate", "interestRate"), toArgs(int.class, double.class, double.class, double.class), double.class, toCategories("Financial"), MathUtil.class),
    
    // trig functions, radians
    toDegreesOper(TokenType.UnaryFunc, 8, toLabels("toDegrees"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    toRadiansOper(TokenType.UnaryFunc, 8, toLabels("toRadians"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    
    SinOper(TokenType.UnaryFunc, 8, toLabels("sin"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    CosOper(TokenType.UnaryFunc, 8, toLabels("cos"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    TanOper(TokenType.UnaryFunc, 8, toLabels("tan"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    ASinOper(TokenType.UnaryFunc, 8, toLabels("asin"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    ACosOper(TokenType.UnaryFunc, 8, toLabels("acos"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    ATanOper(TokenType.UnaryFunc, 8, toLabels("atan"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),

    SinDOper(TokenType.UnaryFunc, 8, toLabels("sinD"), toArgs(double.class), double.class, toCategories("math","trigonometry"), MathUtil.class),
    CosDOper(TokenType.UnaryFunc, 8, toLabels("cosD"), toArgs(double.class), double.class, toCategories("math","trigonometry"), MathUtil.class),
    TanDOper(TokenType.UnaryFunc, 8, toLabels("tanD"), toArgs(double.class), double.class, toCategories("math","trigonometry"), MathUtil.class),
    ASinDOper(TokenType.UnaryFunc, 8, toLabels("asinD"), toArgs(double.class), double.class, toCategories("math","trigonometry"), MathUtil.class),
    ACosDOper(TokenType.UnaryFunc, 8, toLabels("acosD"), toArgs(double.class), double.class, toCategories("math","trigonometry"), MathUtil.class),
    ATanDOper(TokenType.UnaryFunc, 8, toLabels("atanD"), toArgs(double.class), double.class, toCategories("math","trigonometry"), MathUtil.class),

    SinHOper(TokenType.UnaryFunc, 8, toLabels("sinh"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    CosHOper(TokenType.UnaryFunc, 8, toLabels("cosh"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),
    TanHOper(TokenType.UnaryFunc, 8, toLabels("tanh"), toArgs(double.class), double.class, toCategories("math","trigonometry"), Math.class),

    FactFuncOper(TokenType.UnaryFunc, 8, toLabels("fact", "factorial"), toArgs(double.class), double.class, toCategories("math"), MathUtil.class),
    FloorOper(TokenType.UnaryFunc, 8, toLabels("roundDown", "floor"), toArgs(double.class), double.class, toCategories("math"), Math.class, "floor"),
    CeilOper(TokenType.UnaryFunc, 8, toLabels("roundUp", "ceil"), toArgs(double.class), double.class, toCategories("math"), Math.class, "ceil"),
    SignOper(TokenType.UnaryFunc, 8, toLabels("sign", "signum"), toArgs(double.class), double.class, toCategories("math"), Math.class, "signum"),
    RoundOper(TokenType.UnaryFunc, 8, toLabels("round"), toArgs(double.class), long.class, toCategories("math"), Math.class),
  
    // Useful functions from Apache Math Commons
    IsPrimeOper(TokenType.UnaryFunc, 8, toLabels("isPrime"), toArgs(int.class), boolean.class, toCategories("logical", "math"), org.apache.commons.math3.primes.Primes.class),    
    NextPrimeOper(TokenType.UnaryFunc, 8, toLabels("nextPrime"), toArgs(int.class), boolean.class, toCategories("math"), org.apache.commons.math3.primes.Primes.class),    
    PrimeFactorsOper(TokenType.UnaryFunc, 8, toLabels("primeFactors"), toArgs(int.class), boolean.class, toCategories("math"), org.apache.commons.math3.primes.Primes.class),    
    IsPowerOf2Oper(TokenType.UnaryFunc, 8, toLabels("isPowerOfTwo"), toArgs(long.class), boolean.class, toCategories("math"), org.apache.commons.math3.util.ArithmeticUtils.class),    
    
    GcdOper(TokenType.BinaryFunc, 8, toLabels("gcd"), toArgs(long.class, long.class), long.class, toCategories("math"), org.apache.commons.math3.util.ArithmeticUtils.class),   
    LcmOper(TokenType.BinaryFunc, 8, toLabels("lcm"), toArgs(long.class, long.class), long.class, toCategories("math"), org.apache.commons.math3.util.ArithmeticUtils.class),
    
    // String functions
    LenOper(TokenType.UnaryFunc, 8, toLabels("len", "length"), toArgs(String.class), int.class, toCategories("text"), MathUtil.class, "length"),
    LeftOper(TokenType.BinaryFunc, 8, toLabels("left", "instrLeft"), toArgs(String.class, int.class), String.class, toCategories("text"), MathUtil.class, "instrLeft"),
    RightOper(TokenType.BinaryFunc, 8, toLabels("right", "instrRight"), toArgs(String.class, int.class), String.class, toCategories("text"), MathUtil.class, "instrRight"),
    MidOper(TokenType.GenericFunc, 8, toLabels("mid", "instrMid"), toArgs(String.class, int.class, int.class), String.class, toCategories("text"), MathUtil.class, "instrMid"),
    toLowerOper(TokenType.UnaryFunc, 8, toLabels("toLower"), toArgs(String.class), String.class, toCategories("text"), MathUtil.class),
    toUpperOper(TokenType.UnaryFunc, 8, toLabels("toUpper"), toArgs(String.class), String.class, toCategories("text"), MathUtil.class),
    IndexOfOper(TokenType.BinaryFunc, 8, toLabels("indexOf"), toArgs(String.class, String.class), int.class, toCategories("text"), MathUtil.class),
    ContainsOper(TokenType.BinaryFunc, 8, toLabels("contains"), toArgs(String.class, String.class), boolean.class, toCategories("text"), MathUtil.class),
    trimOper(TokenType.UnaryFunc, 8, toLabels("trim"), toArgs(String.class), String.class, toCategories("text"), MathUtil.class),
    reverseOper(TokenType.UnaryFunc, 8, toLabels("reverse"), toArgs(String.class), String.class, toCategories("text"), MathUtil.class),
    toStringOper(TokenType.UnaryFunc, 8, toLabels("toString"), toArgs(Object.class), String.class, toCategories("text"), MathUtil.class),
    toNumberOper(TokenType.UnaryFunc, 8, toLabels("toNumber"), toArgs(Object.class), double.class, toCategories("text"), MathUtil.class),
   
    // Binary functions, mostly supported in Java Math
    ReminderFuncOper(TokenType.BinaryFunc, 8, toLabels("IEEEremainder"), toArgs(double.class, double.class), double.class, toCategories("math"), Math.class),
    ModFuncOper(TokenType.BinaryFunc, 8, toLabels("mod", "modulus", "remainder"), toArgs(double.class, double.class), double.class, toCategories("math"), MathUtil.class),
    QuotientFuncOper(TokenType.BinaryFunc, 8, toLabels("quotient", "intDiv", "integerDivision"), toArgs(double.class, double.class), long.class, toCategories("math"), MathUtil.class),
    PowerFuncOper(TokenType.BinaryFunc, 8, toLabels("pow", "power"), toArgs(double.class, double.class), double.class, toCategories("math"), Math.class),

    BiggerOper(TokenType.BinaryFunc, 8, toLabels("bigger"), toArgs(double.class, double.class), double.class, toCategories("math"), Math.class, "max"),
    SmallerOper(TokenType.BinaryFunc, 8, toLabels("smaller"), toArgs(double.class, double.class), double.class, toCategories("math"), Math.class, "min"),
    HypotOper(TokenType.BinaryFunc, 8, toLabels("hypot"), toArgs(double.class, double.class), double.class, toCategories("math"), org.apache.commons.math3.util.FastMath.class),   
    
    PermOper(TokenType.BinaryFunc, 8, toLabels("perm", "numPermutations", "perm", "nPk"), toArgs(double.class, double.class), double.class, toCategories("math"), MathUtil.class, "numPermutations"),    
    CombOper(TokenType.BinaryFunc, 8, toLabels("comb", "numCombinations", "nCk", "nChooseK"), toArgs(double.class, double.class), double.class, toCategories("math"), MathUtil.class, "numCombinations"),    

    // Builtin functions
    NullOper(TokenType.BuiltIn, 8, "null", toCategories("constant")),
    TrueOper(TokenType.BuiltIn, 8, "true", toCategories("logical", "constant")),
    FalseOper(TokenType.BuiltIn, 8, "false", toCategories("logical", "constant")),
    EOper(TokenType.BuiltIn, 8, toLabels("e"), null, double.class, toCategories("math", "constant"), MathUtil.class),
    PiOper(TokenType.BuiltIn, 8, toLabels("pi"), null, double.class, toCategories("math", "constant"), MathUtil.class),
    
    ColumnIndexOper(TokenType.BuiltIn, 8, toLabels("columnIndex", "cidx"), null, int.class, toCategories("lookup", "information")),
    RowIndexOper(TokenType.BuiltIn, 8, toLabels("rowIndex", "ridx"), null, int.class, toCategories("lookup", "information")),
    
    NowOper(TokenType.BuiltIn, 8, toLabels("now"), null, java.util.Date.class, toCategories("Date/Time")),

    // Single Variable Stat Functions 
    SumOper(TokenType.StatOp, 8, toLabels("sum"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    Sum2Oper(TokenType.StatOp, 8, toLabels("sumOfSquares", "sumSqs", "ss", "sumsq"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    SumSqD2Oper(TokenType.StatOp, 8, toLabels("sumOfSquaredDeviates", "ss", "ssd", "devsq"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    MeanOper(TokenType.StatOp, 8, toLabels("mean", "average", "avg"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    MedianOper(TokenType.StatOp, 8, toLabels("median"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    QuartileOper(TokenType.StatOp, 8, toLabels("quartile"), toArgs(TableElement.class, int.class), double.class, toCategories("statistic")),
    FirstQuartileOper(TokenType.StatOp, 8, toLabels("firstQuartile", "firstQ"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    ThirdQuartileOper(TokenType.StatOp, 8, toLabels("thirdQuartile", "thirdQ"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    ModeOper(TokenType.StatOp, 8, toLabels("mode"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    StDevPopulationOper(TokenType.StatOp, 8, toLabels("stDevPopulation", "stDevOfPopulation", "stDev.p"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    StDevSampleOper(TokenType.StatOp, 8, toLabels("stDevSample", "stDevOfSample", "stDev", "stDev.s"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    VarPopulationOper(TokenType.StatOp, 8, toLabels("variancePopulation", "varianceOfPopulation", "var.p"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    VarSampleOper(TokenType.StatOp, 8, toLabels("varianceSample", "varianceOfSample", "var", "var.s", "variance"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    MinOper(TokenType.StatOp, 8, toLabels("min", "minimum"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    MaxOper(TokenType.StatOp, 8, toLabels("max", "maximum"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    RangeOper(TokenType.StatOp, 8, toLabels("range", "spread"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    CountOper(TokenType.StatOp, 8, toLabels("count", "cnt"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    SkewOper(TokenType.StatOp, 8, toLabels("skewness", "skew"), toArgs(TableElement.class), double.class, toCategories("statistic")),
    KurtosisOper(TokenType.StatOp, 8, toLabels("kurtosis", "kurt"), toArgs(TableElement.class), double.class, toCategories("statistic")),

     // Moving window statistics    
    MSumOper(TokenType.StatOp, 8, toLabels("msum"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MSum2Oper(TokenType.StatOp, 8, toLabels("msumOfSquares", "msumSqs", "mss", "msumsq"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MSumSqD2Oper(TokenType.StatOp, 8, toLabels("msumOfSquaredDeviates", "mss", "mssd", "mdevsq"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MMeanOper(TokenType.StatOp, 8, toLabels("mmean", "mavg", "maverage"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MMedianOper(TokenType.StatOp, 8, toLabels("mmedian"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MQuartileOper(TokenType.StatOp, 8, toLabels("mquartile"), toArgs(TableRowColumnElement.class, int.class, int.class), double.class, toCategories("statistic", "moving")),
    MFirstQuartileOper(TokenType.StatOp, 8, toLabels("mfirstQuartile", "mfirstQ"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MThirdQuartileOper(TokenType.StatOp, 8, toLabels("mthirdQuartile", "mthirdQ"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MModeOper(TokenType.StatOp, 8, toLabels("mmode"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MStDevPopulationOper(TokenType.StatOp, 8, toLabels("mstDevPopulation", "mstDevOfPopulation", "mstDev.p"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MStDevSampleOper(TokenType.StatOp, 8, toLabels("mstDevSample", "mstDevOfSample", "mstDev", "mstDev.s"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MVarPopulationOper(TokenType.StatOp, 8, toLabels("mvariancePopulation", "mvarianceOfPopulation", "mvar.p"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MVarSampleOper(TokenType.StatOp, 8, toLabels("mvarianceSample", "mvarianceOfSample", "mvar", "mvar.s", "mvariance"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MMinOper(TokenType.StatOp, 8, toLabels("mmin", "mminimum"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MMaxOper(TokenType.StatOp, 8, toLabels("mmax", "mmaximum"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MRangeOper(TokenType.StatOp, 8, toLabels("mrange", "mspread"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MCountOper(TokenType.StatOp, 8, toLabels("mcount", "mcnt"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MSkewOper(TokenType.StatOp, 8, toLabels("mskewness", "mskew"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),
    MKurtosisOper(TokenType.StatOp, 8, toLabels("mkurtosis", "mkurt"), toArgs(TableRowColumnElement.class, int.class), double.class, toCategories("statistic", "moving")),

    // T Tests
    PValueOper(TokenType.StatOp, 8, toLabels("tSigLevel",  "pValue", "oneSampleTwoTailTTestPValue"), toArgs(TableRowColumnElement.class, double.class), double.class, toCategories("statistic")),
    TValueOper(TokenType.StatOp, 8, toLabels("tStatistic", "tValue", "oneSampleTwoTailTTestTValue"), toArgs(TableRowColumnElement.class, double.class), double.class, toCategories("statistic")),
    TTestOper(TokenType.StatOp, 8, toLabels("tTest"), toArgs(TableRowColumnElement.class, double.class, double.class), double.class, toCategories("statistic")),
    
    TwoSamplePValueOper(TokenType.StatOp, 8, toLabels("twoSampleTSigLevel",  "tsPValue",  "twoSamplePValue", "twoSampleTwoTailTTestPValue"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic")),
    TwoSampleTValueOper(TokenType.StatOp, 8, toLabels("twoSampleTStatistic", "tsTValue",  "twoSampleTValue", "twoSampleTwoTailTTestTValue"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic")),
    TwoSampleTTestOper(TokenType.StatOp, 8, toLabels("twoSampleTTest", "tsTTest"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class, double.class), double.class, toCategories("statistic")),
    
    // Normal Distribution Single Variable Stat Functions 
    NormSampleOper(TokenType.BinaryFunc, 8, toLabels("normalSample", "normalSample", "normS", "normalS"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    NormDensityOper(TokenType.GenericFunc, 8, toLabels("normalDensity", "normalDensity", "normD", "normalD", "normPDF"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    NormCumProbOper(TokenType.GenericFunc, 8, toLabels("normalCumProb", "normalCumProb", "normCP", "normalCP", "normCDF"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    NormInvCumProbOper(TokenType.GenericFunc, 8, toLabels("normalInvCumProb", "normalInvCumProb", "normICP", "normalICP", "normInvCDF"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    NormProbOper(TokenType.GenericFunc, 8, toLabels("normalProbability", "normalProbability", "normalProb", "normP", "normalP", "normPMF"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    NormProbInRangeOper(TokenType.GenericFunc, 8, toLabels("normalProbInRange", "normalProbInRange", "normalProbabilityInRange","normPIR", "normalPIR"), 
    											  toArgs(double.class, double.class, double.class, double.class), 
    											  double.class, 
    											  toCategories("statistic", "distribution"), 
    											  MathUtil.class), 
    
    // Exponential Distribution Single Variable Stat Functions 
    ExpSampleOper(TokenType.UnaryFunc, 8, toLabels("exponentialSample", "expS"), toArgs(double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ExpDensityOper(TokenType.BinaryFunc, 8, toLabels("exponentialDensity", "expD", "expPDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ExpCumProbOper(TokenType.BinaryFunc, 8, toLabels("exponentialCumProb", "expCP", "expCDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ExpInvCumProbOper(TokenType.BinaryFunc, 8, toLabels("exponentialInvCumProb", "expICP", "expInvCDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ExpProbOper(TokenType.BinaryFunc, 8, toLabels("exponentialProbability", "expProb", "expP", "expPMF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ExpProbInRangeOper(TokenType.GenericFunc, 8,toLabels("exponentialProbInRange", "exponentialProbabilityInRange","expPIR"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class), 
    
    // Chi Squared Distribution Single Variable Stat Functions                                                               
    ChiSqSampleOper(TokenType.UnaryFunc, 8, toLabels("chiSqSample", "ChiSqS"), toArgs(double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ChiSqDensityOper(TokenType.BinaryFunc, 8, toLabels("chiSqDensity", "ChiSqD", "ChiSqPDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ChiSqCumProbOper(TokenType.BinaryFunc, 8, toLabels("chiSqCumProb", "chiSqCP", "chiSqCDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ChiSqInvCumProbOper(TokenType.BinaryFunc, 8, toLabels("chiSqInvCumProb", "ChiSqICP", "ChiSqInvCDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ChiSqProbOper(TokenType.BinaryFunc, 8, toLabels("chiSqProbability", "ChiSqProb", "ChiSqP", "ChiSqPMF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    ChiSqProbInRangeOper(TokenType.GenericFunc, 8, toLabels("chiSqProbInRange", "ChiSqProbabilityInRange","ChiSqPIR"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),    
    ChiSqScoreOper(TokenType.GenericFunc, 8, toLabels("chiSqScore"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),    
    ChiSqStatOper(TokenType.StatOp, 8, toLabels("chiSqStat"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic", "distribution")),
    ChiSqTestOper(TokenType.StatOp, 8, toLabels("chiSqTest"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class, double.class), double.class, toCategories("statistic", "distribution")),

    // T Distribution Single Variable Stat Functions                                                               
    TSampleOper(TokenType.UnaryFunc, 8, toLabels("tSample", "tS"), toArgs(double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    TDensityOper(TokenType.BinaryFunc, 8, toLabels("tDensity", "tD", "tPDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    TCumProbOper(TokenType.BinaryFunc, 8, toLabels("tCumProb", "tCP", "tCDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    TInvCumProbOper(TokenType.BinaryFunc, 8, toLabels("tInvCumProb", "tICP", "tInvCDF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    TProbOper(TokenType.BinaryFunc, 8, toLabels("tProbability", "tProb", "tP", "tPMF"), toArgs(double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),
    TProbInRangeOper(TokenType.GenericFunc, 8, toLabels("tProbInRange", "tProbabilityInRange","tPIR"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),    

    TScoreOper(TokenType.GenericFunc, 8, toLabels("tScore"), toArgs(double.class, double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),    
    PopMeanOper(TokenType.GenericFunc, 8, toLabels("popMean"), toArgs(double.class, double.class, double.class, double.class), double.class, toCategories("statistic", "distribution"), MathUtil.class),    
    
    // Two Variable Stat Functions
    LinearSlopeOper(TokenType.StatOp, 8, toLabels("slope"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic", "regression")),
    LinearInterceptOper(TokenType.StatOp, 8, toLabels("intercept"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic", "regression")),
    LinearROper(TokenType.StatOp, 8, toLabels("correlation", "ccr", "correl"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic", "regression")),
    LinearR2Oper(TokenType.StatOp, 8, toLabels("correlation2", "ccr2", "r2", "rsq"), toArgs(TableRowColumnElement.class, TableRowColumnElement.class), double.class, toCategories("statistic", "regression")),
    LinearComputeXOper(TokenType.GenericFunc, 8, toLabels("lrComputeX", "computeX", "lrComputeX"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "regression"), MathUtil.class),
    LinearComputeYOper(TokenType.GenericFunc, 8, toLabels("lrComputeY", "computeY", "lrComputeY"), toArgs(double.class, double.class, double.class), double.class, toCategories("statistic", "regression"), MathUtil.class),
    
    // Transformation Functions
    MeanCenterOper(TokenType.TransformOp, 8, toLabels("meanCenter"), toArgs(TableElement.class), null, toCategories("transform")),
    NormalizeOper(TokenType.TransformOp, 8, toLabels("normalize", "standardize"), toArgs(TableElement.class), null, toCategories("transform")),    
    ScaleOper(TokenType.TransformOp, 8, toLabels("scale"), toArgs(TableElement.class, double.class, double.class), null, toCategories("transform")),

    Paren(10, TokenType.LeftParen, TokenType.RightParen),
    NOP(0, TokenType.Comma, TokenType.ColumnRef, TokenType.RowRef, TokenType.SubsetRef, TokenType.CellRef, TokenType.TableRef),

    LAST_operator;
    
    static private String [] toLabels(String... labels)
    {
        return labels;        
    }
    
    static private String [] toCategories(String... cats)
    {
        return cats;        
    }
    
    static private Class<?> [] toArgs(Class<?>... args)
    {
        return args;        
    }
    
    static private Map<BuiltinOperator, BuiltinOperator> sf_MovingStatToBaseStatMap  = null;
    static final public int MAX_PRIORITY = 10;
    
    private String m_label;
    private Set<String> m_aliases;
    private Set<TokenType> m_tokenTypes;
    private int m_priority;
    private Class<? extends Object> m_clazz;
    private String m_methodName;
    private Method m_method;
    private Class<?>[] m_methodArgs;
    private String[]  m_categories;

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
            Class<? extends Object> resultType,
            String [] categories)
	{
    	this(tt, priority, labels, args, resultType, categories, (Class<?>)null, (String)null);
	}

    private BuiltinOperator(TokenType tt, 
                            int priority, 
                            String labels[], 
                            Class<? extends Object > args[], 
                            Class<? extends Object> resultType, 
                            String [] categories,
                            Class<? extends Object> clazz)
    {
        this(tt, priority, labels, args, resultType, categories, clazz, null);
    }
    
    private BuiltinOperator(TokenType tt, 
                            int priority, 
                            String labels[], 
                            Class<? extends Object > args[], 
                            Class<? extends Object> resultType, 
                            String [] categories,
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
            m_label = methodName == null ? m_label : methodName;
            m_aliases.add(methodName.toLowerCase());
        }
        
        m_resultType = resultType;
        m_methodArgs = args == null ? new Class<?> [] {} : args;
        m_categories = categories;
        
        m_clazz = clazz;
        if (clazz != null)
            m_methodName = methodName != null ? methodName : m_label;
    }
    
    private BuiltinOperator(TokenType tt, int priority, String label, String [] categories)
    {
        this();
        m_label = label;
        if (label != null && label.trim().length() >= 0)
        	m_aliases.add(label.trim().toLowerCase());
        
        m_priority = priority;
        m_tokenTypes.add(tt);
        m_categories = categories;
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
                return isRequiresRetainedSequence();
        }
    }
    
    public boolean isRequiresRetainedSequence()
    {
        switch(this) {
            default:
                return isMovingStatistic();
        }
    }
    
    public BuiltinOperator getBaseStatistic()
    {
        if (this.isMovingStatistic())
        	return sf_MovingStatToBaseStatMap.get(this);
        else
        	return this;
    }
    
    public boolean isMovingStatistic()
    {
    	if (sf_MovingStatToBaseStatMap == null) {
    		synchronized (BuiltinOperator.class) {
    			if (sf_MovingStatToBaseStatMap == null) {
    				sf_MovingStatToBaseStatMap = new HashMap<BuiltinOperator, BuiltinOperator>();
    				
    				// map all moving stats to their basic counterpart
       				sf_MovingStatToBaseStatMap.put(MSumOper, SumOper);
       				sf_MovingStatToBaseStatMap.put(MSum2Oper, Sum2Oper);
       				sf_MovingStatToBaseStatMap.put(MSumSqD2Oper, SumSqD2Oper);
       				sf_MovingStatToBaseStatMap.put(MMeanOper, MeanOper);
       				sf_MovingStatToBaseStatMap.put(MMedianOper, MedianOper);
       				sf_MovingStatToBaseStatMap.put(MQuartileOper, QuartileOper);
       				sf_MovingStatToBaseStatMap.put(MFirstQuartileOper, FirstQuartileOper);
       				sf_MovingStatToBaseStatMap.put(MThirdQuartileOper, ThirdQuartileOper);
       				sf_MovingStatToBaseStatMap.put(MModeOper, ModeOper);
       				sf_MovingStatToBaseStatMap.put(MStDevPopulationOper, StDevPopulationOper);
       				sf_MovingStatToBaseStatMap.put(MStDevSampleOper, StDevSampleOper);
       				sf_MovingStatToBaseStatMap.put(MVarPopulationOper, VarPopulationOper);
       				sf_MovingStatToBaseStatMap.put(MVarSampleOper, VarSampleOper);
       				sf_MovingStatToBaseStatMap.put(MMinOper, MinOper);
       				sf_MovingStatToBaseStatMap.put(MMaxOper, MaxOper);
       				sf_MovingStatToBaseStatMap.put(MRangeOper, RangeOper);
       				sf_MovingStatToBaseStatMap.put(MCountOper, CountOper);
       				sf_MovingStatToBaseStatMap.put(MSkewOper, SkewOper);
       				sf_MovingStatToBaseStatMap.put(MKurtosisOper, KurtosisOper);
       			}
    		}
    	}
    	
    	return sf_MovingStatToBaseStatMap.containsKey(this);
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
    public String [] getCategories()
    {
    	return m_categories;
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
