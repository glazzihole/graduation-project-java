package nl.inl.blacklab.server.requesthandlers;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.jobs.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Display the contents of the cache.
 */
@Slf4j
public class RequestHandlerCreateIndex extends RequestHandler {
	public RequestHandlerCreateIndex(BlackLabServer servlet, HttpServletRequest request, User user, String indexName, String urlResource, String urlPathPart) {
		super(servlet, request, user, indexName, urlResource, urlPathPart);
	}

	@Override
	public int handle(DataStream ds) throws BlsException {
		// Create index and return success
		try {
			String newIndexName = request.getParameter("name");
			if (newIndexName == null || newIndexName.length() == 0) {
                return Response.badRequest(ds, "ILLEGAL_INDEX_NAME", "You didn't specify the required name parameter.");
            }
			String displayName = request.getParameter("display");
			String documentFormat = request.getParameter("format");

			log.info( "REQ create index: " + newIndexName + ", " + displayName + ", " + documentFormat);
			if (!user.isLoggedIn() || !newIndexName.startsWith(user.getUserId() + ":")) {
				log.info("(forbidden, cannot create index in another user's area)");
				return Response.forbidden(ds, "You can only create indices in your own private area.");
			}

			indexMan.createIndex(newIndexName, displayName, documentFormat);

			return Response.status(ds, "SUCCESS", "Index created succesfully.", HttpServletResponse.SC_CREATED);
			//DataObjectMapElement response = DataObject.statusObject("SUCCESS", "Index created succesfully.");
			//response.put("url", ServletUtil.getServletBaseUrl(request) + "/" + indexName);
			//return new Response(response);
		} catch (BlsException e) {
			throw e;
		} catch (Exception e) {
			return Response.internalError(ds, e, debugMode, 11);
		}
	}
}
