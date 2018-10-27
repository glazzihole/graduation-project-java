package nl.inl.blacklab.server.exceptions;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;

@Slf4j
public class InternalServerError extends BlsException {

	private int internalErrorCode;

	public int getInternalErrorCode() {
		return internalErrorCode;
	}

	public InternalServerError(int code) {
		this("Internal error", code, null);
		log.debug("INTERNAL ERROR " + internalErrorCode + " (no message)");
	}

	public InternalServerError(String msg, int internalErrorCode) {
		this(msg, internalErrorCode, null);
		log.debug("INTERNAL ERROR " + internalErrorCode + ":" + msg);
	}

	public InternalServerError(String msg, int internalErrorCode, Throwable cause) {
		super(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", msg + (cause == null ? "" : " (" + cause + ")"), cause);
		this.internalErrorCode = internalErrorCode;
		log.debug("INTERNAL ERROR " + internalErrorCode + (cause == null ? "" : ":") );
		if (cause != null) {
            cause.printStackTrace();
        }
	}

}
