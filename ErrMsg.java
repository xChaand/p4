/**
 * ErrMsg
 *
 * This class is used to generate warning and fatal error messages.
 */
class ErrMsg {

    // Initialized to false and is set to true if the fatal method is ever called
    private static boolean fatal = false;

    /**
     * Generates a fatal error message.
     * 
     * @param lineNum line number for error location
     * @param charNum character number (i.e., column) for error location
     * @param msg     associated message for error
     */
    static void fatal(int lineNum, int charNum, String msg) {
        fatal = true;
        System.err.println(lineNum + ":" + charNum + " ****ERROR**** " + msg);
    }

    /**
     * Generates a warning message.
     * 
     * @param lineNum line number for warning location
     * @param charNum character number (i.e., column) for warning location
     * @param msg     associated message for warning
     */
    static void warn(int lineNum, int charNum, String msg) {
        System.err.println(lineNum + ":" + charNum + " ****WARNING**** " + msg);
    }

    /**
     * Get function if fatal error was ever called
     * 
     * @return boolean value associated to fatal
     */
    static boolean isFatal() {
        return fatal;
    }
}
