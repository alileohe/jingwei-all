package com.taobao.jingwei.core.drc;

/**
 * Describes special exceptions produced in the @see DRCClient.
 */
public class DRCClientException extends Exception {

    private static final long serialVersionUID = -1633175516763954910L;

    public DRCClientException() {
        super();
    }

    public DRCClientException(final String message) {
        super(message);
    }

    public DRCClientException(final String message, Throwable cause) {
        super(message, cause);
    }
}