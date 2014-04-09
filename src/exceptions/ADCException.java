package exceptions;

public class ADCException extends Exception {

    public ADCException() {
        super("Generic AdHoc-Chat exception.");
    }

    public ADCException(String msg) {
        super(msg);
    }

    public ADCException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ADCException(Throwable cause) {
        super(cause);
    }

}
