package nl.inl.blacklab.server.requesthandlers;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.search.grouping.GroupPropertySize;
import nl.inl.blacklab.search.grouping.HitGroup;
import nl.inl.blacklab.search.grouping.HitGroups;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.jobs.JobHitsGrouped;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.search.BlsConfig;

import javax.servlet.http.HttpServletRequest;

/**
 * @author HU Gailei
 * @date 2018/10/11
 * <p>
 * description: 通过CQL表达式查询单词
 * </p>
 **/
@Slf4j
public class WordRequestHandler extends RequestHandler {
    public WordRequestHandler(BlackLabServer servlet, HttpServletRequest request, User user, String corpus, String urlResource, String urlPathPart) {
        super(servlet, request, user, corpus, urlResource, urlPathPart);
    }

    @Override
    public int handle(DataStream ds) throws BlsException {
        // Get the window we're interested in
        JobHitsGrouped search = (JobHitsGrouped) searchMan.search(user, searchParam.hitsGrouped(), isBlockingOperation());
        try {
            if (BlsConfig.traceRequestHandling) {
                log.info("WordRequestHandler | handle start");
            }

            // If search is not done yet, indicate this to the user
            if (!search.finished()) {
                return Response.busy(ds, servlet);
            }

            // Search is done; construct the results object
            final HitGroups groups = search.getGroups();

            int pageNo = (searchParam.getInteger("first")/searchParam.getInteger("number")) + 1 ;
            int pageSize =  searchParam.getInteger("number") < 0 || searchParam.getInteger("number") > searchMan.config().maxPageSize() ? searchMan.config().defaultPageSize() : searchParam.getInteger("number");
            int totalItems = (groups == null) ? 0 : groups.getGroups().size();
            double totalPages = Math.ceil( (double)totalItems/ (double)pageSize );

            ds.startItem("result").startMap();
            ds.entry("status", CorpusConstant.SUCCESS);
            ds.entry("code", CorpusConstant.SUCCESS_CODE);
            ds.entry("msg", "");
            ds.entry("error", "");
            ds.startDataEntry("data");
            ds.entry("pageNumber", pageNo);
            ds.entry("pageSize", pageSize);
            ds.entry("totalPages", totalPages);
            ds.entry("totalElements", totalItems);

            ds.startEntry("page").startList();
            int i = 0;

            //按每个group的大小降序排序
            groups.sortGroups( new GroupPropertySize(), false );
            for (HitGroup group: groups) {
                i++;
                if (i >= searchParam.getInteger("first") && i < searchParam.getInteger("first") + pageSize) {
                    ds.startItem("item").startMap();
                    ds  .entry( "id", i );
                    ds  .entry("word", group.getIdentity().toString());
                    ds  .entry("lemma", "");
                    ds  .entry("pos", "");
                    ds  .entry("freq", group.size());
                    ds  .entry("corpus", searchParam.getIndexName());
                    ds.endMap().endItem();
                }
            }
            ds.endList().endEntry();
            ds.endDataEntry("data");
            ds.endMap().endItem();

            if (BlsConfig.traceRequestHandling) {
                log.info("WordRequestHandler | handle end");
            }
            return HTTP_OK;
        }
        catch(Exception e) {
            log.error("WordRequestHandler | error: {}", e);
            return Response.badRequest(ds, "SEARCHWORD_ERROR", e.getMessage());
        }
        finally {
            search.decrRef();
        }
    }
}
