package com.hugailei.graduation.corpus.controller;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.search.RegexpTooLargeException;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataFormat;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.exceptions.InternalServerError;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.RequestHandlerStaticResponse;
import nl.inl.blacklab.server.requesthandlers.Response;
import nl.inl.blacklab.server.search.IndexManager;
import nl.inl.blacklab.server.search.SearchManager;
import nl.inl.blacklab.server.util.ServletUtil;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author HU Gailei
 * @date 2018/10/1
 * <p>
 * description: 处理类
 * </p>
 **/
@Slf4j
public class Handler {

    /**
     * 处理request请求
     *
     * @param pageable
     * @param corpus
     * @param urlResource
     * @param urlPathPart
     * @param blackLabServer
     * @param request
     * @param response
     * @param requestHandler
     */
    void checkAndHandler (Pageable pageable,
                          String corpus,
                          String urlResource,
                          String urlPathPart,
                          BlackLabServer blackLabServer,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          RequestHandler requestHandler) {

        User user = User.loggedIn("admin", "1");
        // === Create RequestHandler object
        boolean debugMode = blackLabServer.getSearchManager().config().isDebugMode(request.getRemoteAddr());
        RequestHandlerStaticResponse errorRequestHandler = commonParamErrorCheck(corpus, user, blackLabServer, request, debugMode);
        if (errorRequestHandler != null) {
            handleRequestHandler(errorRequestHandler, request, response, blackLabServer.getSearchManager(), debugMode);
        } else {
            errorRequestHandler = checkIndexErrorStatus(corpus, user, blackLabServer, request, debugMode);
            if (errorRequestHandler != null) {
                handleRequestHandler(errorRequestHandler, request, response, blackLabServer.getSearchManager(), debugMode);
            } else {
                handleRequestHandler(requestHandler, request, response, blackLabServer.getSearchManager(), debugMode);
            }
        }

    }

    /**
     * 检查索引状态，如果返回为null，则说明状态正常
     *
     * @param indexName
     * @param user
     * @param blackLabServer
     * @param request
     * @param debugMode
     * @return
     */
    private RequestHandlerStaticResponse checkIndexErrorStatus(String indexName,
                                                               User user,
                                                               BlackLabServer blackLabServer,
                                                               HttpServletRequest request,
                                                               boolean debugMode) {
        IndexManager.IndexStatus status;
        try {
            status = blackLabServer.getSearchManager().getIndexManager().getIndexStatus(indexName);
            if (status != IndexManager.IndexStatus.AVAILABLE) {
                RequestHandlerStaticResponse errorObj = new RequestHandlerStaticResponse(blackLabServer, request, user, indexName, null, null);
                return errorObj.unavailable(indexName, status.toString());
            } else {
                return null;
            }
        } catch (BlsException e) {
            log.error("checkIndexErrorStatus | error: {}", e);
            RequestHandlerStaticResponse errorObj = new RequestHandlerStaticResponse(blackLabServer, request, user, indexName, null, null);
            return errorObj.error(e.getBlsErrorCode(), e.getMessage(), e.getHttpStatusCode());
        }

    }

    boolean checkConfig(HttpServletRequest request,
                        HttpServletResponse responseObject,
                        BlackLabServer blackLabServer) {
        if (!blackLabServer.isConfigRead()) {
            try {
                blackLabServer.readConfig();
                blackLabServer.setConfigRead(true);
                return true;
            } catch (BlsException e) {
                // 异常，则返回false
                // Write HTTP headers (status code, encoding, content type and cache)
                responseObject.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseObject.setCharacterEncoding(BlackLabServer.OUTPUT_ENCODING.name().toLowerCase());
                responseObject.setContentType("application/json");
                ServletUtil.writeCacheHeaders(responseObject, 0);

                // === Write the response that was captured in buf
                try {
                    Writer realOut = new OutputStreamWriter(responseObject.getOutputStream(), BlackLabServer.OUTPUT_ENCODING);
                    realOut.write("{'status':'ERROR'," +
                                        "'code':500''," +
                                        "'msg':''," +
                                        "'error':'" + e.getMessage() + "'," +
                                        "'data':" + null +
                                    "}");
                    realOut.flush();
                } catch (IOException e2) {
                    // Client cancelled the request midway through.
                    // This is okay, don't raise the alarm.
                    log.debug("(couldn't send response, client probably cancelled the request)");
                }
                return false;
            }
        } else {
            return true;
        }
    }

    RequestHandlerStaticResponse commonParamErrorCheck(String indexName,
                                                       User user,
                                                       BlackLabServer blackLabServer,
                                                       HttpServletRequest request,
                                                       boolean debugMode) {
        RequestHandlerStaticResponse errorObj = new RequestHandlerStaticResponse(blackLabServer, request, user, indexName, null, null);
        if (indexName.startsWith(":")) {
            if (!user.isLoggedIn()) {
                return errorObj.unauthorized("Log in to access your private indices.");
            }
            // Private index. Prefix with user id.
            indexName = user.getUserId() + indexName;
        }

        // If we're doing something with a private index, it must be our own.
        @SuppressWarnings( "unused" )
        boolean isPrivateIndex = false;
        // logger.debug("Got indexName = \"" + indexName + "\" (len=" + indexName.length() + ")");
        @SuppressWarnings( "unused" )
        String shortName = indexName;
        if (indexName.contains(":")) {
            isPrivateIndex = true;
            String[] userAndIndexName = indexName.split(":");
            if (userAndIndexName.length > 1) {
                shortName = userAndIndexName[1];
            } else {
                return errorObj.illegalIndexName("");
            }
            if (!user.isLoggedIn()) {
                return errorObj.unauthorized("Log in to access your private indices.");
            }
            if (!user.getUserId().equals(userAndIndexName[0])) {
                return errorObj.unauthorized("You cannot access another user's private indices.");
            }
        }

        return null;
    }

    void handleRequestHandler(RequestHandler requestHandler,
                              HttpServletRequest request,
                              HttpServletResponse responseObject,
                              SearchManager searchManager,
                              boolean debugMode) {
        // === Figure stuff out about the request
        DataFormat outputType = requestHandler.getOverrideType();
        if (outputType == null) {
            outputType = ServletUtil.getOutputType(request, searchManager.config().defaultOutputType());
        }

        // Is this a JSONP request?
        String callbackFunction = ServletUtil.getParameter(request, "jsonp", "");
        boolean isJsonp = callbackFunction.length() > 0;

        int cacheTime = requestHandler.isCacheAllowed() ? searchManager.config().clientCacheTimeSec() : 0;

        boolean prettyPrint = ServletUtil.getParameter(request, "prettyprint", debugMode);

        String rootEl = requestHandler.omitBlackLabResponseRootElement() ? null : "blacklabResponse";

        // === Handle the request
        StringWriter buf = new StringWriter();
        PrintWriter out = new PrintWriter(buf);
        DataStream ds = DataStream.create(outputType, out, prettyPrint, callbackFunction);
        ds.startDocument(rootEl);
        StringWriter errorBuf = new StringWriter();
        PrintWriter errorOut = new PrintWriter(errorBuf);
        DataStream es = DataStream.create(outputType, errorOut, prettyPrint, callbackFunction);
        es.outputProlog();
        int errorBufLengthBefore = errorBuf.getBuffer().length();
        int httpCode;
        if (isJsonp && !callbackFunction.matches("[_a-zA-Z][_a-zA-Z0-9]+")) {
            // Illegal JSONP callback name
            httpCode = Response.badRequest(es, "JSONP_ILLEGAL_CALLBACK", "Illegal JSONP callback function name. Must be a valid Javascript name.");
        } else {
            try {
                httpCode = requestHandler.handle(ds);
            } catch (InternalServerError e) {
                String msg = ServletUtil.internalErrorMessage(e, debugMode, e.getInternalErrorCode());
                httpCode = Response.error(es, e.getBlsErrorCode(), msg, e.getHttpStatusCode());
            } catch (BlsException e) {
                httpCode = Response.error(es, e.getBlsErrorCode(), e.getMessage(), e.getHttpStatusCode());
            } catch (InterruptedException e) {
                httpCode = Response.internalError(es, e, debugMode, 7);
            } catch (RegexpTooLargeException e) {
                httpCode = Response.badRequest(es, "REGEXP_TOO_LARGE", e.getMessage());
            } catch (RuntimeException e) {
                httpCode = Response.internalError(es, e, debugMode, 32);
            }
        }
        ds.endDocument(rootEl);

        // === Write the response headers

        // Write HTTP headers (status code, encoding, content type and cache)
        if (!isJsonp) {// JSONP request always returns 200 OK because otherwise script doesn't load
            responseObject.setStatus(httpCode);
        }
        responseObject.setCharacterEncoding(BlackLabServer.OUTPUT_ENCODING.name().toLowerCase());
        responseObject.setContentType(ServletUtil.getContentType(outputType));
        ServletUtil.writeCacheHeaders(responseObject, cacheTime);

        // === Write the response that was captured in buf
        try {
            Writer realOut = new OutputStreamWriter(responseObject.getOutputStream(), BlackLabServer.OUTPUT_ENCODING);
            boolean errorOccurred = errorBuf.getBuffer().length() > errorBufLengthBefore;
            StringWriter writeWhat = errorOccurred ? errorBuf : buf;
            realOut.write(writeWhat.toString());
            realOut.flush();
        } catch (IOException e) {
            // Client cancelled the request midway through.
            // This is okay, don't raise the alarm.
            log.error("(couldn't send response, client probably cancelled the request)");
            return;
        }
    }
}
