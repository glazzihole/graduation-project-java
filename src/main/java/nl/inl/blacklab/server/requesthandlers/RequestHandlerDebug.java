package nl.inl.blacklab.server.requesthandlers;

import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.jobs.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Get debug info about the servlet and index.
 * Only available in debug mode (BlackLabServer.DEBUG_MODE == true)
 */
public class RequestHandlerDebug extends RequestHandler {
	public RequestHandlerDebug(BlackLabServer servlet, HttpServletRequest request, User user, String indexName, String urlResource, String urlPathPart) {
		super(servlet, request, user, indexName, urlResource, urlPathPart);
	}

	@Override
	public boolean isCacheAllowed() {
		return false;
	}

	@Override
	public int handle(DataStream ds) {
		ds.startMap()
			.entry("indexName", indexName)
			.entry("resource", urlResource)
			.entry("rest", urlPathInfo)
			.entry("queryString", request.getQueryString())
			.entry("searchParam", servlet.getSearchParameters(false, request, indexName).toString())
		.endMap();
		return HTTP_OK;
	}

}
