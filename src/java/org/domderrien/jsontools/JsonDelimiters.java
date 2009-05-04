package org.domderrien.jsontools;

class JsonDelimiters {
    /** Default constructor provided for the JUnit test */

    protected JsonDelimiters() {}

    // Used by the JsonParser
    protected final static char END_OF_STRING = '\0';
    protected final static char BACK_SLASH = '\\';
    protected final static char SLASH = '/';
    protected final static char SPACE = ' ';
    protected final static char QUOTE = '\'';
    protected final static char DOUBLE_QUOTES = '"';
    protected final static char COLONS = ':';
    protected final static char COMMA = ',';
    protected final static char OPENING_BRACE = '{';
    protected final static char CLOSING_BRACE = '}';
    protected final static char OPENING_SQUARE_BRACKET = '[';
    protected final static char CLOSING_SQUARE_BRACKET = ']';
    protected final static char TABULATION_ID = 't';
    protected final static char CARRIAGE_RETURN_ID = 'r';
    protected final static char NEW_LINE_ID = 'n';
    protected final static char BELL_ID = 'b';
    protected final static char FORM_FEED_ID = 'f';
    protected final static char UNICODE_ID = 'u';
    protected final static char TABULATION = '\t';
    protected final static char CARRIAGE_RETURN = '\r';
    protected final static char NEW_LINE = '\n';
    protected final static char BELL = '\b';
    protected final static char FORM_FEED = '\f';
    protected final static String TRUE_LABEL = "true";
    protected final static String FALSE_LABEL = "false";
    protected final static char TRUE_LABEL_FIRST_CHAR = 't';
    protected final static char FALSE_LABEL_FIRST_CHAR = 'f';
    protected final static String NULL_LABEL = "null";
    protected final static char NULL_LABEL_FIRST_CHAR = 'n';
    protected final static String UNDEFINED_LABEL = "undefined";
    protected final static char UNDEFINED_LABEL_FIRST_CHAR = 'u';

    // Used by the JSONSerializer
    protected final static String EMPTY_STR = "";
    protected final static String SLASH_STR = EMPTY_STR + SLASH;
    protected final static String BACK_SLASH_STR = EMPTY_STR + BACK_SLASH;
    protected final static String BACK_SLASH_REGEX = BACK_SLASH_STR + BACK_SLASH_STR;
    protected final static String CARRIAGE_RETURN_STR = EMPTY_STR + CARRIAGE_RETURN;
    protected final static String NEW_LINE_STR = EMPTY_STR + NEW_LINE;
    protected final static String QUOTE_STR = EMPTY_STR + QUOTE;
    protected final static String DOUBLE_QUOTES_STR = EMPTY_STR + DOUBLE_QUOTES;
    protected final static String COLONS_STR = EMPTY_STR + COLONS;
    protected final static String COMMA_STR = EMPTY_STR + COMMA;
    protected final static String OPENING_BRACE_STR = EMPTY_STR + OPENING_BRACE;
    protected final static String CLOSING_BRACE_STR = EMPTY_STR + CLOSING_BRACE;
    protected final static String OPENING_SQUARE_BRACKET_STR = EMPTY_STR + OPENING_SQUARE_BRACKET;
    protected final static String CLOSING_SQUARE_BRACKET_STR = EMPTY_STR + CLOSING_SQUARE_BRACKET;
}
