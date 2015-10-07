package org.tms.io.options;

class Constants
{
    protected static final char BACKSPACE = '\b';
    protected static final char COMMA = ',';

    /**
     * Starts a comment, the remainder of the line is the comment.
     */
    protected static final char COMMENT = '#';

    protected static final char CR = '\r';
    protected static final Character DOUBLE_QUOTE_CHAR = Character.valueOf('"');
    protected static final char BACKSLASH = '\\';
    protected static final char FF = '\f';
    protected static final char LF = '\n';
    protected static final char SP = ' ';
    protected static final char TAB = '\t';

    /** ASCII record separator */
    protected static final char RS = 30;

    /** ASCII unit separator */
    protected static final char US = 31;

    protected static final String EMPTY = "";

    /** The end of stream symbol */
    protected static final int END_OF_STREAM = -1;

    /** Undefined state for the lookahead char */
    static final int UNDEFINED = -2;

    /** According to RFC 4180, line breaks are delimited by CRLF */
    protected static final String CRLF = "\r\n";

    /**
     * Unicode line separator.
     */
    protected static final String LINE_SEPARATOR = "\u2028";

    /**
     * Unicode paragraph separator.
     */
    protected static final String PARAGRAPH_SEPARATOR = "\u2029";

    /**
     * Unicode next line.
     */
    protected static final String NEXT_LINE = "\u0085";

}
