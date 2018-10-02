package nl.inl.blacklab.server.requesthandlers;

import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.jobs.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Display the contents of the cache.
 */
public class RequestHandlerDeleteIndex extends RequestHandler {
	public RequestHandlerDeleteIndex(BlackLabServer servlet, HttpServletRequest request, User user, String indexName, String urlResource, String urlPathPart) {
		super(servlet, request, user, indexName, urlResource, urlPathPart);
	}

	@Override
	public int handle(DataStream ds) throws BlsException {
		if (indexName != null && indexName.length() > 0) {
			// Delete index
			try {
				debug(logger, "REQ delete index: " + indexName);
				indexMan.deleteUserIndex(indexName);
				return Response.status(ds, "SUCCESS", "Index deleted succesfully.", HTTP_OK);
			} catch (BlsException e) {
				throw e;
			} catch (Exception e) {
				return Response.internalError(ds, e, debugMode, 12);
			}
		}

		return Response.badRequest(ds, "CANNOT_CREATE_INDEX", "Could not create index '" + indexName + "'. Specify a valid name.");
	}

}
