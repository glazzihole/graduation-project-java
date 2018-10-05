package nl.inl.blacklab.server.requesthandlers;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.search.*;
import nl.inl.blacklab.search.grouping.HitGroup;
import nl.inl.blacklab.search.grouping.HitGroups;
import nl.inl.blacklab.search.grouping.HitPropValue;
import nl.inl.blacklab.search.grouping.HitProperty;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.jobs.*;
import nl.inl.blacklab.server.search.BlsConfig;
import org.apache.lucene.document.Document;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * 例句查询<br>
 * 改动了返回数据结构及内容，原文件内容见RequestHandlerHits.java.txt
 * 
 * @author HU Gailei
 */
@Slf4j
public class RequestHandlerHits extends RequestHandler {
    public RequestHandlerHits(BlackLabServer servlet, HttpServletRequest request, User user, String indexName, String urlResource, String urlPathPart) {
        super(servlet, request, user, indexName, urlResource, urlPathPart);
    }

    @Override
    public int handle(DataStream ds) throws BlsException {
        if (BlsConfig.traceRequestHandling) {
            log.debug("RequestHandlerHits.handle start");
        }
        Job search = null;
        JobHitsGrouped searchGrouped = null;
        JobHitsWindow searchWindow = null;
        JobHitsTotal total = null;

        // Do we want to view a single group after grouping?
        String groupBy = searchParam.getString("group");
        if (groupBy == null) {
            groupBy = "";
        }
        String viewGroup = searchParam.getString("viewgroup");
        if (viewGroup == null) {
            viewGroup = "";
        }
        try {
            HitsWindow window;
            HitGroup group = null;
            boolean block = isBlockingOperation();
            if (groupBy.length() > 0 && viewGroup.length() > 0) {

                // TODO: clean up, do using JobHitsGroupedViewGroup or something (also cache sorted group!)

                // Yes. Group, then show hits from the specified group
                searchGrouped = (JobHitsGrouped) searchMan.search(user, searchParam.hitsGrouped(), block);
                search = searchGrouped;
                search.incrRef();

                // If search is not done yet, indicate this to the user
                if (!search.finished()) {
                    return Response.busy(ds, servlet);
                }

                // Search is done; construct the results object
                HitGroups groups = searchGrouped.getGroups();

                HitPropValue viewGroupVal = null;
                viewGroupVal = HitPropValue.deserialize(searchGrouped.getHits(), viewGroup);
                if (viewGroupVal == null) {
                    return Response.badRequest(ds, "ERROR_IN_GROUP_VALUE", "Cannot deserialize group value: " + viewGroup);
                }

                group = groups.getGroup(viewGroupVal);
                if (group == null) {
                    return Response.badRequest(ds, "GROUP_NOT_FOUND", "Group not found: " + viewGroup);
                } else {
                    group.getHits().settings().setContextSize( 500 );
                }
                
                String sortBy = searchParam.getString("sort");
                HitProperty sortProp = sortBy != null && sortBy.length() > 0 ? HitProperty.deserialize(group.getHits(), sortBy) : null;
                Hits hitsSorted;
                if (sortProp != null) {
                    hitsSorted = group.getHits().sortedBy(sortProp);
                } else {
                    hitsSorted = group.getHits();
                }

                int first = searchParam.getInteger("first");
                if (first < 0) {
                    first = 0;
                }
                int number = searchParam.getInteger("number");
                if (number < 0 || number > searchMan.config().maxPageSize()) {
                    number = searchMan.config().defaultPageSize();
                }
                if (!hitsSorted.sizeAtLeast(first)) {
                    return Response.badRequest(ds, "HIT_NUMBER_OUT_OF_RANGE", "Non-existent hit number specified.");
                }
                window = hitsSorted.window(first, number);

            } 
            else {
                // Regular set of hits (no grouping first)

                searchWindow = (JobHitsWindow) searchMan.search(user, searchParam.hitsWindow(), block);
                search = searchWindow;
                search.incrRef();

                // Also determine the total number of hits
                // (usually nonblocking, unless "waitfortotal=yes" was passed)
                total = (JobHitsTotal) searchMan.search(user, searchParam.hitsTotal(), searchParam.getBoolean("waitfortotal"));

                // If search is not done yet, indicate this to the user
                if (!search.finished()) {
                    return Response.busy(ds, servlet);
                }

                window = searchWindow.getWindow();
            }

            if (searchParam.getString("calc").equals("colloc")) {
                dataStreamCollocations(ds, window.getOriginalHits());
                return HTTP_OK;
            }

//            DocResults perDocResults = null;

            Searcher searcher = search.getSearcher();

//            boolean includeTokenCount = searchParam.getBoolean("includetokencount");
//            int totalTokens = -1;
//            IndexStructure struct = searcher.getIndexStructure();
//            if (includeTokenCount) {
//                perDocResults = window.getOriginalHits().perDocResults();
//                // Determine total number of tokens in result set
//                String fieldName = struct.getMainContentsField().getName();
//                DocProperty propTokens = new DocPropertyComplexFieldLength(fieldName);
//                totalTokens = perDocResults.intSum(propTokens);
//            }

            // Search is done; construct the results object

//            ds.startMap();

            // The summary
//            ds.startEntry("summary").startMap();
            Hits hits = searchWindow != null ? hits = searchWindow.getWindow().getOriginalHits() : group.getHits();
            
            double totalTime = 0;
            if (total != null) {
                totalTime = total.threwException() ? -1 : total.userWaitTime();
            } else {
                totalTime = searchGrouped.threwException() ? -1 : searchGrouped.userWaitTime();
            }
//            addSummaryCommonFields(ds, searchParam, search.userWaitTime(), totalTime, hits, false, (DocResults)null, (DocOrHitGroups)null, window);
//            if (includeTokenCount)
//                ds.entry("tokensInMatchingDocuments", totalTokens);
//            RequestHandler.dataStreamDocFields(ds, searcher.getIndexStructure());
//            ds.endEntry();
//            if (searchParam.getBoolean("explain")) {
//                TextPattern tp = searchParam.getPattern();
//                QueryExplanation explanation = searcher.explain(tp);
//                ds.startEntry("explanation").startMap()
//                    .entry("originalQuery", explanation.getOriginalQuery())
//                    .entry("rewrittenQuery", explanation.getRewrittenQuery())
//                .endMap().endEntry();
//            }
//            ds.endMap().endEntry();
            boolean countFailed = totalTime < 0;
            int totalHits = -1;
            if (hits != null) {
                // We have a hits object we can query for this information
               totalHits =  countFailed ? -1 : hits.countSoFarHitsCounted();        
            } 
            int pageNo = (searchParam.getInteger("first")/searchParam.getInteger("number")) + 1 ;
            int pageSize =  searchParam.getInteger("number") < 0 || searchParam.getInteger("number") > searchMan.config().maxPageSize() ? searchMan.config().defaultPageSize() : searchParam.getInteger("number");
            int totalPages = (int) Math.ceil( (double)totalHits/ (double)pageSize );
           
            ds.startItem("result").startMap();
            
            ds.entry("pageNo", pageNo);
            ds.entry("pageSize", pageSize);
            ds.entry("orderBy", null);
            ds.entry("orderDir", null);
            ds.entry("nextPage", pageNo>=totalPages?totalPages:pageNo+1);
            ds.entry("prePage", pageNo<=1?1:pageNo-1);
            ds.entry("totalPages", totalPages);
            ds.entry("totalItems", totalHits);
            ds.entry("startIndexNo", searchParam.getInteger("first"));
            ds.entry("hasNextPage", pageNo>=totalPages?false:true);
            ds.entry("hasPrePage", pageNo<=1?false:true);
            
            ds.startEntry("page").startList();
            Map<Integer, String> pids = new HashMap<>();
            for (Hit hit: window) {
                ds.startItem("hit").startMap();

                // Find pid
                String pid = pids.get(hit.doc);
                if (pid == null) {
                    Document document = searcher.document(hit.doc);
                    pid = getDocumentPid(searcher, hit.doc, document);
                    pids.put(hit.doc, pid);
                }

                boolean useOrigContent = searchParam.getString("usecontent").equals("orig");

                // TODO: use RequestHandlerDocSnippet.getHitOrFragmentInfo()

                // Add basic hit info
//                ds.entry("docPid", pid);
//                ds.entry("start", hit.start);
//                ds.entry("end", hit.end);
                ds.entry("id", pid+hit.start+hit.end);
                if (useOrigContent) {
                    // Add concordance from original XML
                    Concordance c = window.getConcordance(hit);
                    ds  .startEntry("left").plain(c.left()).endEntry()
                        .startEntry("match").plain(c.match()).endEntry()
                        .startEntry("right").plain(c.right()).endEntry();
                } 
                else {
                    String left = "", match = "", right = "";
                    // Add KWIC info
                    if(window!=null) {
                        Kwic c = window.getKwic(hit);                 
    //                    ds  .startEntry("left").contextList(c.getProperties(), c.getLeft()).endEntry()
    //                        .startEntry("match").contextList(c.getProperties(), c.getMatch()).endEntry()
    //                        .startEntry("right").contextList(c.getProperties(), c.getRight()).endEntry();
                        
                        List<String> leftWordsList = c.getLeft( "word" ),matchWordsList = c.getMatch( "word" ),rightWordsList = c.getRight( "word" );
                        
                        for(String word : leftWordsList) {
                            left = left + word + " ";
                        }
                        for(String word : matchWordsList) {
                            match = match + word + " ";
                        }
                        for(String word : rightWordsList) {
                            right = right + word + " ";
                        }
                    }
                    ds.entry("left", left);
                    ds.entry("match", match);
                    ds.entry("right", right);
                    
                }
                ds.entry("corpus", searchParam.getIndexName());
                ds.endMap().endItem();
            }
            ds.endList().endEntry();
            ds.endMap().endItem();
//            ds.startEntry("docInfos").startMap();
//            //DataObjectMapAttribute docInfos = new DataObjectMapAttribute("docInfo", "pid");
//            MutableIntSet docsDone = new IntHashSet();
//            Document doc = null;
//            String lastPid = "";
//            for (Hit hit: window) {
//                String pid = pids.get(hit.doc);
//
//                // Add document info if we didn't already
//                if (!docsDone.contains(hit.doc)) {
//                    docsDone.add(hit.doc);
//                    ds.startAttrEntry("docInfo", "pid", pid);
//                    if (!pid.equals(lastPid)) {
//                        doc = searcher.document(hit.doc);
//                        lastPid = pid;
//                    }
//                    dataStreamDocumentInfo(ds, searcher, doc);
//                    ds.endAttrEntry();
//                }
//            }
//            ds.endMap().endEntry();

//            if (searchParam.hasFacets()) {
//                // Now, group the docs according to the requested facets.
//                if (perDocResults == null)
//                    perDocResults = window.getOriginalHits().perDocResults();
//                ds.startEntry("facets");
//                dataStreamFacets(ds, perDocResults, searchParam.facets());
//                ds.endEntry();
//            }

//            ds.endMap();

            if (BlsConfig.traceRequestHandling) {
                log.debug("RequestHandlerHits.handle end");
            }
            return HTTP_OK;
        } finally {
            if (search != null) {
                search.decrRef();
            }
            if (searchWindow != null) {
                searchWindow.decrRef();
            }
            if (searchGrouped != null) {
                searchGrouped.decrRef();
            }
            if (total != null) {
                total.decrRef();
            }
        }
    }

    private void dataStreamCollocations(DataStream ds, Hits originalHits) {
        originalHits.settings().setContextSize(searchParam.getInteger("wordsaroundhit"));
        ds.startMap().startEntry("tokenFrequencies").startMap();
        TermFrequencyList tfl = originalHits.getCollocations();
        tfl.sort();
        for (TermFrequency tf: tfl) {
            ds.attrEntry("token", "text", tf.term, tf.frequency);
        }
        ds.endMap().endEntry().endMap();
    }

}
