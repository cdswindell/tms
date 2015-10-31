package org.tms.io.options;

public class IOConstants
{
    public static final char BACKSPACE = '\b';
    public static final char COMMA = ',';

    /**
     * Starts a comment, the remainder of the line is the comment.
     */
    public static final char COMMENT = '#';

    public static final char CR = '\r';
    public static final Character DOUBLE_QUOTE_CHAR = Character.valueOf('"');
    public static final char BACKSLASH = '\\';
    public static final char FF = '\f';
    public static final char LF = '\n';
    public static final char SP = ' ';
    public static final char TAB = '\t';

    /** ASCII record separator */
    public static final char RS = 30;

    /** ASCII unit separator */
    public static final char US = 31;

    public static final String EMPTY = "";

    /** The end of stream symbol */
    public static final int END_OF_STREAM = -1;

    /** Undefined state for the lookahead char */
    static final int UNDEFINED = -2;

    /** According to RFC 4180, line breaks are delimited by CRLF */
    public static final String CRLF = "\r\n";

    /**
     * Unicode line separator.
     */
    public static final String LINE_SEPARATOR = "\u2028";

    /**
     * Unicode paragraph separator.
     */
    public static final String PARAGRAPH_SEPARATOR = "\u2029";

    /**
     * Unicode next line.
     */
    public static final String NEXT_LINE = "\u0085";

}
