package com.taobao.jingwei.core.drc;

/**
 * Describes special exceptions produced in the @see DRCClient.
 */
public class HttpBadResponseException extends Exception {

    private static final long serialVersionUID = 103644505251020948L;

    private final int responseCode;

    public HttpBadResponseException() {
        super();
        this.responseCode = 0;
    }

    public HttpBadResponseException(final int code, final String message) {
        super(message);
        this.responseCode = code;
    }

	public int getResponseCode() {
		return responseCode;
	}
}
