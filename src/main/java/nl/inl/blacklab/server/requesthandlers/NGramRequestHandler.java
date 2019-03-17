package nl.inl.blacklab.server.requesthandlers;

import com.hugailei.graduation.corpus.constants.CorpusConstant;
import com.hugailei.graduation.corpus.dto.NgramDto;
import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.search.Hits;
import nl.inl.blacklab.search.Searcher;
import nl.inl.blacklab.search.TextPattern;
import nl.inl.blacklab.search.grouping.GroupProperty;
import nl.inl.blacklab.search.grouping.HitGroup;
import nl.inl.blacklab.search.grouping.HitGroups;
import nl.inl.blacklab.search.grouping.HitProperty;
import nl.inl.blacklab.server.BlackLabServer;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.jobs.WindowSettings;
import nl.inl.blacklab.server.search.BlsConfig;
import nl.inl.blacklab.server.search.IndexManager;
import nl.inl.blacklab.server.util.BlsUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author HU Gailei
 * @date 2018/10/27
 * <p>
 * description: ngram查询处理，通过blacklab检索语料库中的连续语块
 * </p>
 **/
@Slf4j
public class NGramRequestHandler extends RequestHandler {
    private String patt;

    public NGramRequestHandler(BlackLabServer servlet,
                               HttpServletRequest request,
                               User user,
                               String indexName,
                               String patt,
                               String urlResource,
                               String urlPathPart) {
        super(servlet, request, user, indexName, urlResource, urlPathPart);
        this.patt = patt;
    }

    @Override
    public int handle(DataStream ds) throws BlsException {
        if (BlsConfig.traceRequestHandling) {
            log.info("NGramRequestHandler | handle start");
        }
        try {
            WindowSettings windowSettings = searchParam.getWindowSettings();
            int first = windowSettings.first() < 0 ? 0 : windowSettings.first();
            int pageNo = (windowSettings.first()/windowSettings.size()) + 1 ;
            int pageSize = windowSettings.size() < 0 || windowSettings.size() > searchMan.config().maxPageSize() ? searchMan.config().defaultPageSize() : windowSettings.size();


            List<NgramDto> ngramInfo = getNgramInfo(patt);

            int totalResults = ngramInfo.size();
            double totalPages = Math.ceil( (double)totalResults/ (double)pageSize );

            ds.startItem("result").startMap();
            ds.entry("status", CorpusConstant.SUCCESS);
            ds.entry("code", CorpusConstant.SUCCESS_CODE);
            ds.entry("msg", "");
            ds.entry("error", "");
            ds.startDataEntry("data");
            ds.startEntry(false,"pageNumber").value(pageNo).endEntry();
            ds.entry("pageSize", pageSize);
            ds.entry("totalPages", totalPages);
            ds.entry("totalElements", totalResults);

            ds.startEntry("page").startList();
            Set<String> rankWordSet = null;
            // 获取等级
            int rankNum = 0;
            if (request.getParameter("rank_num") != null) {
                // 获取等级
                rankNum = Integer.valueOf(request.getParameter("rank_num"));

                // 获取当前级别及当前级别之上的所有词汇
                rankWordSet = CorpusConstant.RANK_NUM_TO_DIFFICULT_WORD_SET.get(rankNum);
            }
            int i = 1;
            for (NgramDto ngram : ngramInfo) {
                if (i > first && i <= first + pageSize) {
                    ds.startItem("chunk").startMap();
                    ds  .entry("id", ngram.getId());
                    if (request.getParameter("rank_num") == null) {
                        ds  .entry("ngramStr", ngram.getNgramStr());
                    } else {
                        String newNgramString = "";
                        for (String word : ngram.getNgramStr().split(" ")) {
                            if (rankWordSet.contains(word)) {
                                word = CorpusConstant.RANK_WORD_STRENGTHEN_OPEN_LABEL + word + CorpusConstant.RANK_WORD_STRENGTHEN_CLOSE_LABEL;
                                newNgramString = newNgramString + word + " ";
                            }
                        }
                        newNgramString = newNgramString.trim();
                        ds  .entry("ngramStr", newNgramString);
                    }
                    ds  .entry("nValue", ngram.getNValue());
                    ds  .entry("freq", ngram.getFreq());
                    ds  .entry("corpus", indexName);
                    ds.endItem().endMap();
                }
                i++;
            }
            ds.endList().endEntry();
            ds.endDataEntry("data");
            ds.endMap().endItem();

            if (BlsConfig.traceRequestHandling) {
                log.info("NGramRequestHandler | handle end");
            }
            return HTTP_OK;
        } catch(Exception e) {
            log.error("NGramRequestHandler | error: {}", e);
            return Response.badRequest(ds, "NGRAM_ERROR", e.getMessage());
        }
    }

    /**
     * Ngram查询
     * @param patt  查询表达式
     * @return
     * @throws BlsException
     */
    private List<NgramDto> getNgramInfo(String patt) throws BlsException {
        IndexManager indexManager = searchMan.getIndexManager();
        Searcher searcher = indexManager.getSearcher(indexName);
        patt = "[]{2,5}containing" + patt;
        TextPattern pattern = BlsUtils.parsePatt(getSearcher(), patt, "corpusql");

        Hits hits=searcher.find( pattern );
        HitProperty groupProp = HitProperty.deserialize(hits, "hit:word:i");
        HitGroups groups = hits.groupedBy(groupProp);
        groups.sortGroups( GroupProperty.size(), false );

        List<NgramDto> ngramInfo = new ArrayList<>();
        for (HitGroup group: groups) {
            //只取频率大于2的ngram
            if( group.size() > 2 ) {
                String ngramStr = group.getIdentity().toString();
                Pattern p = Pattern.compile(CorpusConstant.STOP_WORD_REG);
                Matcher matcher = p.matcher(ngramStr);
                //只取不包含特殊字符等符号的ngram
                if(!matcher.find()) {
                    ngramInfo.add(new NgramDto(group.hashCode(), ngramStr, ngramStr.split( " " ).length, group.size()) );
                }
            }
        }
        return ngramInfo;
    }
}
