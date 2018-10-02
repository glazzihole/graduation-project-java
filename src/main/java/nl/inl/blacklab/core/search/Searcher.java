
package nl.inl.blacklab.core.search;

import lombok.extern.slf4j.Slf4j;
import nl.inl.blacklab.core.analysis.BLDutchAnalyzer;
import nl.inl.blacklab.core.analysis.BLNonTokenizingAnalyzer;
import nl.inl.blacklab.core.analysis.BLStandardAnalyzer;
import nl.inl.blacklab.core.analysis.BLWhitespaceAnalyzer;
import nl.inl.blacklab.core.externalstorage.ContentStore;
import nl.inl.blacklab.core.externalstorage.ContentStoresManager;
import nl.inl.blacklab.core.forwardindex.ForwardIndex;
import nl.inl.blacklab.core.forwardindex.Terms;
import nl.inl.blacklab.core.highlight.XmlHighlighter;
import nl.inl.blacklab.core.index.complex.ComplexFieldUtil;

import nl.inl.blacklab.core.perdocument.DocResults;
import nl.inl.blacklab.core.search.indexstructure.IndexStructure;
import nl.inl.blacklab.core.search.lucene.BLSpanQuery;
import nl.inl.blacklab.core.search.lucene.SpanQueryFiltered;
import nl.inl.blacklab.core.util.VersionFile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.LockObtainFailedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.Collator;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

// Referenced classes of package nl.inl.blacklab.search:
//            SearcherImpl, HitsSettings, QueryExplanation, Hit,
//            Concordance, ConcordanceType, Hits, TextPattern,
//            Kwic, DocContentsFromForwardIndex, QueryExecutionContext
@Slf4j
public abstract class Searcher
{
    public static interface LuceneDocTask
    {

        public abstract void perform(Document document1);
    }


    public static Searcher fromIndexReader(IndexReader reader)
    {
        return (Searcher)searcherFromIndexReader.get(reader);
    }

    public static Searcher openForWriting(File indexDir, boolean createNewIndex)
            throws IOException
    {
        return new SearcherImpl(indexDir, true, createNewIndex, (File)null);
    }

    public static Searcher openForWriting(File indexDir, boolean createNewIndex, File indexTemplateFile)
            throws IOException
    {
        return new SearcherImpl(indexDir, true, createNewIndex, indexTemplateFile);
    }

    public static Searcher createIndex(File indexDir)
            throws IOException
    {
        return createIndex(indexDir, null, null, false);
    }

    public static Searcher createIndex(File indexDir, String displayName)
            throws IOException
    {
        return createIndex(indexDir, displayName, null, false);
    }

    public static Searcher createIndex(File indexDir, String displayName, String documentFormat, boolean contentViewable)
            throws IOException
    {
        Searcher rv = openForWriting(indexDir, true);
        if(displayName != null && displayName.length() > 0) {
            rv.getIndexStructure().setDisplayName(displayName);
        }
        if(documentFormat != null) {
            rv.getIndexStructure().setDocumentFormat(documentFormat);
        }
        rv.getIndexStructure().setContentViewable(contentViewable);
        rv.getIndexStructure().writeMetadata();
        return rv;
    }

    public static Searcher open(File indexDir)
            throws CorruptIndexException, IOException
    {
        return new SearcherImpl(indexDir, false, false, (File)null);
    }

    public static boolean isIndex(File indexDir)
    {
        try
        {
            if(VersionFile.exists(indexDir))
            {
                VersionFile vf = VersionFile.read(indexDir);
                String version = vf.getVersion();
                if(vf.getType().equals("blacklab") && (version.equals("1") || version.equals("2"))) {
                    return true;
                }
            }
        }
        catch(FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    protected static String getWordsFromString(String content, int startAtWord, int endAtWord)
    {
        if(startAtWord == -1 && endAtWord == -1) {
            return content;
        }
        String words[] = content.split("\\s+");
        if(startAtWord == -1) {
            startAtWord = 0;
        }
        if(endAtWord == -1) {
            endAtWord = words.length;
        }
        StringBuilder b = new StringBuilder();
        for(int i = startAtWord; i < endAtWord; i++)
        {
            if(b.length() > 0) {
                b.append(" ");
            }
            b.append(words[i]);
        }

        return b.toString();
    }

    public static Collator getDefaultCollator()
    {
        return defaultCollator;
    }

    public static void setDefaultCollator(Collator defaultCollator)
    {
        defaultCollator = defaultCollator;
    }

    public static String getBlackLabBuildTime()
    {
        return getValueFromManifest("Build-Time", "UNKNOWN");
    }

    public static String getBlackLabVersion()
    {
        return getValueFromManifest("Implementation-Version", "UNKNOWN");
    }

    static String getValueFromManifest(String key, String defaultValue)
    {
        try {
            URL res = Searcher.class.getResource(Searcher.class.getSimpleName() + ".class");
            URLConnection conn = res.openConnection();
            if (!(conn instanceof JarURLConnection)) {
                // Not running from a JAR, no manifest to read
                return defaultValue;
            }
            JarURLConnection jarConn = (JarURLConnection) res.openConnection();
            Manifest mf = jarConn.getManifest();
            String value = null;
            if (mf != null) {
                Attributes atts = mf.getMainAttributes();
                if (atts != null) {
                    value = atts.getValue(key);
                }
            }
            return value == null ? defaultValue : value;
        } catch (IOException e) {
            throw new RuntimeException("Could not read '" + key + "' from manifest", e);
        }
    }

    static Analyzer getAnalyzerInstance(String analyzerName)
    {
        analyzerName = analyzerName.toLowerCase();
        if(analyzerName.equals("whitespace")) {
            return whitespaceAnalyzer;
        }
        if(analyzerName.equals("default")) {
            return defaultAnalyzer;
        }
        if(analyzerName.equals("standard")) {
            return standardAnalyzer;
        }
        if(analyzerName.matches("(non|un)tokeniz(ing|ed)")) {
            return nonTokenizingAnalyzer;
        } else {
            return null;
        }
    }

    public static void setTraceIndexOpening(boolean traceIndexOpening)
    {
        traceIndexOpening = traceIndexOpening;
    }

    public static void setTraceOptimization(boolean traceOptimization)
    {
        traceOptimization = traceOptimization;
    }

    public static void setTraceQueryExecution(boolean traceQueryExecution)
    {
        traceQueryExecution = traceQueryExecution;
    }

    public static List getConfigDirs()
    {
        if(configDirs == null)
        {
            configDirs = new ArrayList();
            String strConfigDir = System.getenv("BLACKLAB_CONFIG_DIR");
            if(strConfigDir != null && strConfigDir.length() > 0)
            {
                File configDir = new File(strConfigDir);
                if(configDir.exists())
                {
                    if(!configDir.canRead()) {
                        log.warn((new StringBuilder()).append("BLACKLAB_CONFIG_DIR points to a unreadable directory: ").append(strConfigDir).toString());
                    }
                    configDirs.add(configDir);
                } else
                {
                    log.warn((new StringBuilder()).append("BLACKLAB_CONFIG_DIR points to a non-existent directory: ").append(strConfigDir).toString());
                }
            }
            configDirs.add(new File(System.getProperty("user.home"), ".blacklab"));
            configDirs.add(new File("/etc/blacklab"));
            configDirs.add(new File("/vol1/etc/blacklab"));
            configDirs.add(new File(System.getProperty("java.io.tmpdir")));
        }
        return configDirs;
    }

    public HitsSettings hitsSettings()
    {
        return hitsSettings;
    }

    public Searcher()
    {
        collator = defaultCollator;
        analyzer = new BLStandardAnalyzer();
        contentStores = new ContentStoresManager();
        forwardIndices = new HashMap();
        mainContentsFieldName = "contents";
        defaultCaseSensitive = false;
        defaultDiacriticsSensitive = false;
        defaultUnbalancedTagsStrategy = nl.inl.blacklab.core.highlight.XmlHighlighter.UnbalancedTagsStrategy.ADD_TAG;
        indexMode = false;
        hitsSettings = new HitsSettings();
    }

    /**
     * @deprecated Method getDefaultMaxHitsToRetrieve is deprecated
     */

    public int getDefaultMaxHitsToRetrieve()
    {
        return hitsSettings().maxHitsToRetrieve();
    }

    /**
     * @deprecated Method setDefaultMaxHitsToRetrieve is deprecated
     */

    public void setDefaultMaxHitsToRetrieve(int n)
    {
        hitsSettings().setMaxHitsToRetrieve(n);
    }

    /**
     * @deprecated Method getDefaultMaxHitsToCount is deprecated
     */

    public int getDefaultMaxHitsToCount()
    {
        return hitsSettings().maxHitsToCount();
    }

    /**
     * @deprecated Method setDefaultMaxHitsToCount is deprecated
     */

    public void setDefaultMaxHitsToCount(int n)
    {
        hitsSettings().setMaxHitsToCount(n);
    }

    public nl.inl.blacklab.core.highlight.XmlHighlighter.UnbalancedTagsStrategy getDefaultUnbalancedTagsStrategy()
    {
        return defaultUnbalancedTagsStrategy;
    }

    public void setDefaultUnbalancedTagsStrategy(nl.inl.blacklab.core.highlight.XmlHighlighter.UnbalancedTagsStrategy strategy)
    {
        defaultUnbalancedTagsStrategy = strategy;
    }

    /**
     * @deprecated Method getDefaultConcordanceType is deprecated
     */

    public ConcordanceType getDefaultConcordanceType()
    {
        return hitsSettings().concordanceType();
    }

    /**
     * @deprecated Method setDefaultConcordanceType is deprecated
     */

    public void setDefaultConcordanceType(ConcordanceType type)
    {
        hitsSettings().setConcordanceType(type);
    }

    public void setCollator(Collator collator)
    {
        this.collator = collator;
    }

    public Collator getCollator()
    {
        return collator;
    }

    /**
     * @deprecated Method getMakeConcordancesFromForwardIndex is deprecated
     */

    public boolean getMakeConcordancesFromForwardIndex()
    {
        return getDefaultConcordanceType() == ConcordanceType.FORWARD_INDEX;
    }

    /**
     * @deprecated Method setMakeConcordancesFromForwardIndex is deprecated
     */

    public void setMakeConcordancesFromForwardIndex(boolean concordancesFromForwardIndex)
    {
        setDefaultConcordanceType(concordancesFromForwardIndex ? ConcordanceType.FORWARD_INDEX : ConcordanceType.CONTENT_STORE);
    }

    public abstract boolean isEmpty();

    public abstract void rollback();

    public void close()
    {
        contentStores.close();
        ForwardIndex fi;
        for(Iterator i$ = forwardIndices.values().iterator(); i$.hasNext(); fi.close()) {
            fi = (ForwardIndex)i$.next();
        }

    }

    public IndexStructure getIndexStructure()
    {
        return indexStructure;
    }

    public abstract Document document(int i);

    public abstract Set docIdSet();

    public void forEachDocument(LuceneDocTask task)
    {
        Integer docId;
        for(Iterator i$ = docIdSet().iterator(); i$.hasNext(); task.perform(document(docId.intValue()))) {
            docId = (Integer)i$.next();
        }

    }

    public abstract boolean isDeleted(int i);

    public abstract int maxDoc();

    /**
     * @deprecated Method filterDocuments is deprecated
     */

    public BLSpanQuery filterDocuments(SpanQuery query, Filter filter)
    {
        if(!(query instanceof BLSpanQuery)) {
            throw new IllegalArgumentException("Supplied query must be a BLSpanQuery!");
        } else {
            return new SpanQueryFiltered((BLSpanQuery)query, filter);
        }
    }

    /**
     * @deprecated Method createSpanQuery is deprecated
     */

    public BLSpanQuery createSpanQuery(TextPattern pattern, String fieldName, Filter filter)
    {
        if(filter == null || (filter instanceof QueryWrapperFilter))
        {
            Query filterQuery = filter != null ? ((QueryWrapperFilter)filter).getQuery() : null;
            return createSpanQuery(pattern, fieldName, filterQuery);
        } else
        {
            throw new UnsupportedOperationException("Filter must be a QueryWrapperFilter!");
        }
    }

    /**
     * @deprecated Method createSpanQuery is deprecated
     */

    public BLSpanQuery createSpanQuery(TextPattern pattern, Filter filter)
    {
        return createSpanQuery(pattern, getMainContentsFieldName(), filter);
    }

    /**
     * @deprecated Method createSpanQuery is deprecated
     */

    public BLSpanQuery createSpanQuery(TextPattern pattern, String fieldName)
    {
        return createSpanQuery(pattern, fieldName, (Query)null);
    }

    /**
     * @deprecated Method createSpanQuery is deprecated
     */

    public BLSpanQuery createSpanQuery(TextPattern pattern)
    {
        return createSpanQuery(pattern, getMainContentsFieldName(), (Query)null);
    }

    public BLSpanQuery createSpanQuery(TextPattern pattern, String fieldName, Query filter)
    {
        BLSpanQuery spanQuery = pattern.translate(getDefaultExecutionContext(fieldName));
        if(filter != null) {
            spanQuery = new SpanQueryFiltered(spanQuery, filter);
        }
        return spanQuery;
    }

    public Hits find(SpanQuery query, String fieldNameConc)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        if(!(query instanceof BLSpanQuery))
        {
            throw new IllegalArgumentException("Supplied query must be a BLSpanQuery!");
        } else
        {
            Hits hits = Hits.fromSpanQuery(this, query);
            hits.settings.setConcordanceField(fieldNameConc);
            return hits;
        }
    }

    public Hits find(BLSpanQuery query)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        return Hits.fromSpanQuery(this, query);
    }

    public Hits find(TextPattern pattern, String fieldName, Query filter)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        Hits hits = Hits.fromSpanQuery(this, createSpanQuery(pattern, fieldName, filter));
        hits.settings.setConcordanceField(fieldName);
        return hits;
    }

    public Hits find(TextPattern pattern, Query filter)
    {
        return find(pattern, getMainContentsFieldName(), filter);
    }

    /**
     * @deprecated Method find is deprecated
     */

    public Hits find(TextPattern pattern, String fieldName, Filter filter)
    {
        if(filter == null || (filter instanceof QueryWrapperFilter))
        {
            Query filterQuery = filter != null ? ((QueryWrapperFilter)filter).getQuery() : null;
            return find(((SpanQuery) (createSpanQuery(pattern, fieldName, filterQuery))), fieldName);
        } else
        {
            throw new UnsupportedOperationException("Filter must be a QueryWrapperFilter!");
        }
    }

    /**
     * @deprecated Method find is deprecated
     */

    public Hits find(TextPattern pattern, Filter filter)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        return find(pattern, getMainContentsFieldName(), filter);
    }

    public Hits find(TextPattern pattern, String fieldName)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        return find(pattern, fieldName, ((Filter) (null)));
    }

    public Hits find(TextPattern pattern)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        return find(pattern, getMainContentsFieldName(), ((Filter) (null)));
    }

    public QueryExplanation explain(TextPattern pattern)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        return explain(pattern, getMainContentsFieldName());
    }

    public QueryExplanation explain(TextPattern pattern, String fieldName)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        return explain(createSpanQuery(pattern, fieldName, ((Filter) (null))), fieldName);
    }

    public QueryExplanation explain(BLSpanQuery query, String fieldName)
            throws org.apache.lucene.search.BooleanQuery.TooManyClauses
    {
        try
        {
            IndexReader indexReader = getIndexReader();
            return new QueryExplanation(query, query.optimize(indexReader).rewrite(indexReader));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public abstract void getCharacterOffsets(int i, String s, int ai[], int ai1[], boolean flag);

    public DocContentsFromForwardIndex getContentFromForwardIndex(int docId, String fieldName, int startAtWord, int endAtWord)
    {
        Hit hit = new Hit(docId, startAtWord, endAtWord);
        Hits hits = Hits.fromList(this, Arrays.asList(new Hit[] {
                hit
        }));
        hits.settings.setConcordanceField(fieldName);
        Kwic kwic = hits.getKwic(hit, 0);
        return kwic.getDocContents();
    }

    public String getContentByCharPos(int docId, String fieldName, int startAtChar, int endAtChar)
    {
        Document d = document(docId);
        if(!contentStores.exists(fieldName)) {
            return d.get(fieldName).substring(startAtChar, endAtChar);
        } else {
            return contentStores.getSubstrings(fieldName, d, new int[] {
                    startAtChar
            }, new int[] {
                    endAtChar
            })[0];
        }
    }

    public String getContent(int docId, String fieldName, int startAtWord, int endAtWord)
    {
        Document d = document(docId);
        if(!contentStores.exists(fieldName))
        {
            String content = d.get(fieldName);
            if(content == null) {
                throw new IllegalArgumentException((new StringBuilder()).append("Field not found: ").append(fieldName).toString());
            } else {
                return getWordsFromString(content, startAtWord, endAtWord);
            }
        } else
        {
            int startEnd[] = startEndWordToCharPos(docId, fieldName, startAtWord, endAtWord);
            return contentStores.getSubstrings(fieldName, d, new int[] {
                    startEnd[0]
            }, new int[] {
                    startEnd[1]
            })[0];
        }
    }

    private List getCharacterOffsets(int doc, String fieldName, Hits hits)
    {
        int starts[] = new int[hits.size()];
        int ends[] = new int[hits.size()];
        Iterator hitsIt = hits.iterator();
        for(int i = 0; i < starts.length; i++)
        {
            Hit hit = (Hit)hitsIt.next();
            starts[i] = hit.start;
            ends[i] = hit.end - 1;
        }

        getCharacterOffsets(doc, fieldName, starts, ends, true);
        List hitspans = new ArrayList(starts.length);
        for(int i = 0; i < starts.length; i++) {
            hitspans.add(new nl.inl.blacklab.core.highlight.XmlHighlighter.HitCharSpan(starts[i], ends[i]));
        }

        return hitspans;
    }

    private int[] startEndWordToCharPos(int docId, String fieldName, int startAtWord, int endAtWord)
    {
        if(startAtWord == -1 && endAtWord == -1) {
            return (new int[] {
                    -1, -1
            });
        }
        boolean startAtStartOfDoc = startAtWord == -1;
        boolean endAtEndOfDoc = endAtWord == -1;
        int starts[] = {
                startAtStartOfDoc ? 0 : startAtWord
        };
        int ends[] = {
                endAtEndOfDoc ? starts[0] : endAtWord
        };
        getCharacterOffsets(docId, fieldName, starts, ends, true);
        if(startAtStartOfDoc) {
            starts[0] = -1;
        }
        if(endAtEndOfDoc) {
            ends[0] = -1;
        }
        int startEnd[] = {
                starts[0], ends[0]
        };
        return startEnd;
    }

    public String getContent(Document d, String fieldName)
    {
        if(!contentStores.exists(fieldName)) {
            return d.get(fieldName);
        } else {
            return contentStores.getSubstrings(fieldName, d, new int[] {
                    -1
            }, new int[] {
                    -1
            })[0];
        }
    }

    public String getContent(Document d)
    {
        return getContent(d, getMainContentsFieldName());
    }

    public String getContent(int docId, String fieldName)
    {
        return getContent(docId, fieldName, -1, -1);
    }

    public String getContent(int docId)
    {
        return getContent(docId, mainContentsFieldName, -1, -1);
    }

    public abstract IndexReader getIndexReader();

    public String highlightContent(int docId, String fieldName, Hits hits, int startAtWord, int endAtWord)
    {
        int endAtWordForCharPos = endAtWord >= 0 ? endAtWord - 1 : endAtWord;
        int startEndCharPos[] = startEndWordToCharPos(docId, fieldName, startAtWord, endAtWordForCharPos);
        int startAtChar = startEndCharPos[0];
        int endAtChar = startEndCharPos[1];
        String content = getContentByCharPos(docId, fieldName, startAtChar, endAtChar);
        if(hits == null && startAtWord == -1 && endAtWord == -1) {
            return content;
        }
        List hitspans = null;
        if(hits != null) {
            hitspans = getCharacterOffsets(docId, fieldName, hits);
        }
        XmlHighlighter hl = new XmlHighlighter();
        hl.setUnbalancedTagsStrategy(getDefaultUnbalancedTagsStrategy());
        if(startAtChar == -1) {
            startAtChar = 0;
        }
        return hl.highlight(content, hitspans, startAtChar);
    }

    public String highlightContent(int docId, String fieldName, Hits hits)
    {
        return highlightContent(docId, fieldName, hits, -1, -1);
    }

    public String highlightContent(int docId, Hits hits)
    {
        return highlightContent(docId, getMainContentsFieldName(), hits, -1, -1);
    }

    public ContentStore getContentStore(String fieldName)
    {
        ContentStore cs = contentStores.get(fieldName);
        if(indexMode && cs == null) {
            return openContentStore(fieldName);
        } else {
            return cs;
        }
    }

    protected void registerContentStore(String fieldName, ContentStore contentStore)
    {
        contentStores.put(fieldName, contentStore);
    }

    protected abstract ContentStore openContentStore(String s);

    public ForwardIndex getForwardIndex(String fieldPropName)
    {
        ForwardIndex forwardIndex = (ForwardIndex)forwardIndices.get(fieldPropName);
        if(forwardIndex == null)
        {
            forwardIndex = openForwardIndex(fieldPropName);
            if(forwardIndex != null) {
                addForwardIndex(fieldPropName, forwardIndex);
            }
        }
        return forwardIndex;
    }

    protected void addForwardIndex(String fieldPropName, ForwardIndex forwardIndex)
    {
        forwardIndices.put(fieldPropName, forwardIndex);
    }

    protected abstract ForwardIndex openForwardIndex(String s);

    private String[] getSubstringsFromDocument(Document d, String fieldName, int starts[], int ends[])
    {
        if(!contentStores.exists(fieldName))
        {
            String luceneName = fieldName;
            String fieldContent = d.get(luceneName);
            String content[] = new String[starts.length];
            for(int i = 0; i < starts.length; i++) {
                content[i] = fieldContent.substring(starts[i], ends[i]);
            }

            return content;
        } else
        {
            return contentStores.getSubstrings(fieldName, d, starts, ends);
        }
    }

    public List makeConcordancesFromContentStore(int doc, String fieldName, int startsOfWords[], int endsOfWords[], XmlHighlighter hl)
    {
        int n = startsOfWords.length / 2;
        int starts[] = new int[n];
        int ends[] = new int[n];
        int i = 0;
        for(int j = 0; i < startsOfWords.length; j++)
        {
            starts[j] = startsOfWords[i];
            ends[j] = endsOfWords[i + 1];
            i += 2;
        }

        Document d = document(doc);
        String content[] = getSubstringsFromDocument(d, fieldName, starts, ends);
        List rv = new ArrayList();
        i = 0;
        for(int j = 0; i < startsOfWords.length; j++)
        {
            int absLeft = startsOfWords[i];
            int absRight = endsOfWords[i + 1];
            int relHitLeft = startsOfWords[i + 1] - absLeft;
            int relHitRight = endsOfWords[i] - absLeft;
            String currentContent = content[j];
            String hitText = relHitRight >= relHitLeft ? currentContent.substring(relHitLeft, relHitRight) : "";
            String leftContext = currentContent.substring(0, relHitLeft);
            String rightContext = currentContent.substring(relHitRight, absRight - absLeft);
            hitText = hl.makeWellFormed(hitText);
            leftContext = hl.makeWellFormed(leftContext);
            rightContext = hl.makeWellFormed(rightContext);
            rv.add(new Concordance(new String[] {
                    leftContext, hitText, rightContext
            }));
            i += 2;
        }

        return rv;
    }

    /**
     * @deprecated Method setForwardIndexConcordanceParameters is deprecated
     */

    public void setForwardIndexConcordanceParameters(String wordFI, String punctFI, Collection attrFI)
    {
        setConcordanceXmlProperties(wordFI, punctFI, attrFI);
    }

    /**
     * @deprecated Method setConcordanceXmlProperties is deprecated
     */

    public void setConcordanceXmlProperties(String wordFI, String punctFI, Collection attrFI)
    {
        hitsSettings().setConcordanceProperties(wordFI, punctFI, attrFI);
    }

    /**
     * @deprecated Method getDefaultContextSize is deprecated
     */

    public int getDefaultContextSize()
    {
        return hitsSettings().contextSize();
    }

    /**
     * @deprecated Method setDefaultContextSize is deprecated
     */

    public void setDefaultContextSize(int defaultContextSize)
    {
        hitsSettings().setContextSize(defaultContextSize);
    }

    public ContentStore openContentStore(File indexXmlDir, boolean create)
    {
        return ContentStore.open(indexXmlDir, create);
    }

    public Terms getTerms(String fieldPropName)
    {
        ForwardIndex forwardIndex = getForwardIndex(fieldPropName);
        if(forwardIndex == null) {
            throw new IllegalArgumentException((new StringBuilder()).append("Field ").append(fieldPropName).append(" has no forward index!").toString());
        } else {
            return forwardIndex.getTerms();
        }
    }

    public Terms getTerms()
    {
        return getTerms(ComplexFieldUtil.mainPropertyField(getIndexStructure(), getMainContentsFieldName()));
    }

    public boolean isDefaultSearchCaseSensitive()
    {
        return defaultCaseSensitive;
    }

    public boolean isDefaultSearchDiacriticsSensitive()
    {
        return defaultDiacriticsSensitive;
    }

    public void setDefaultSearchSensitive(boolean b)
    {
        defaultCaseSensitive = defaultDiacriticsSensitive = b;
    }

    public void setDefaultSearchSensitive(boolean caseSensitive, boolean diacriticsSensitive)
    {
        defaultCaseSensitive = caseSensitive;
        defaultDiacriticsSensitive = diacriticsSensitive;
    }

    public abstract QueryExecutionContext getDefaultExecutionContext(String s);

    public QueryExecutionContext getDefaultExecutionContext()
    {
        return getDefaultExecutionContext(getMainContentsFieldName());
    }

    public abstract String getIndexName();

    public abstract IndexWriter openIndexWriter(File file, boolean flag, Analyzer analyzer1)
            throws IOException, CorruptIndexException, LockObtainFailedException;

    public abstract IndexWriter getWriter();

    public abstract File getIndexDirectory();

    public abstract void delete(Query query);

    public Analyzer getAnalyzer()
    {
        return analyzer;
    }

    public DocResults queryDocuments(Query documentFilterQuery)
    {
        return DocResults._fromQuery(this, documentFilterQuery);
    }

    public abstract List getFieldTerms(String s, int i);

    public String getMainContentsFieldName()
    {
        return mainContentsFieldName;
    }

    /**
     * @deprecated Method getConcWordFI is deprecated
     */

    public String getConcWordFI()
    {
        return hitsSettings().concWordProp();
    }

    /**
     * @deprecated Method getConcPunctFI is deprecated
     */

    public String getConcPunctFI()
    {
        return hitsSettings().concPunctProp();
    }

    /**
     * @deprecated Method getConcAttrFI is deprecated
     */

    public Collection getConcAttrFI()
    {
        return hitsSettings().concAttrProps();
    }

    public abstract IndexSearcher getIndexSearcher();

    protected void deleteFromForwardIndices(Document d)
    {
        ForwardIndex fi;
        int fiid;
        for(Iterator i$ = forwardIndices.entrySet().iterator(); i$.hasNext(); fi.deleteDocument(fiid))
        {
            java.util.Map.Entry e = (java.util.Map.Entry)i$.next();
            String fieldName = (String)e.getKey();
            fi = (ForwardIndex)e.getValue();
            fiid = Integer.parseInt(d.get(ComplexFieldUtil.forwardIndexIdField(fieldName)));
        }

    }

    public Map getForwardIndices()
    {
        return forwardIndices;
    }

    public boolean canDoNfaMatching()
    {
        if(forwardIndices.size() == 0)
        {
            return false;
        } else
        {
            ForwardIndex fi = (ForwardIndex)forwardIndices.values().iterator().next();
            return fi.canDoNfaMatching();
        }
    }

    public static boolean traceIndexOpening = false;
    public static boolean traceOptimization = false;
    public static boolean traceQueryExecution = false;
    public static final int UNLIMITED_HITS = -1;
    public static final int DEFAULT_MAX_RETRIEVE = 1000000;
    public static final int DEFAULT_MAX_COUNT = -1;
    public static final String DEFAULT_CONTENTS_FIELD_NAME = "contents";
    public static final ConcordanceType DEFAULT_CONC_TYPE;
    public static final String DEFAULT_CONC_WORD_PROP;
    public static final String DEFAULT_CONC_PUNCT_PROP = "punct";
    public static final Collection DEFAULT_CONC_ATTR_PROP = null;
    public static final int DEFAULT_CONTEXT_SIZE = 5;
    protected static Collator defaultCollator = Collator.getInstance(new Locale("en", "GB"));
    private static List configDirs;
    protected static final Analyzer whitespaceAnalyzer = new BLWhitespaceAnalyzer();
    protected static final Analyzer defaultAnalyzer = new BLDutchAnalyzer();
    protected static final Analyzer standardAnalyzer = new BLStandardAnalyzer();
    protected static final Analyzer nonTokenizingAnalyzer = new BLNonTokenizingAnalyzer();
    protected static final Map searcherFromIndexReader = new IdentityHashMap();
    private Collator collator;
    protected Analyzer analyzer;
    protected IndexStructure indexStructure;
    protected ContentStoresManager contentStores;
    protected Map forwardIndices;
    protected HitsSettings hitsSettings;
    protected String mainContentsFieldName;
    protected boolean defaultCaseSensitive;
    protected boolean defaultDiacriticsSensitive;
    private nl.inl.blacklab.core.highlight.XmlHighlighter.UnbalancedTagsStrategy defaultUnbalancedTagsStrategy;
    protected boolean indexMode;

    static
    {
        DEFAULT_CONC_TYPE = ConcordanceType.CONTENT_STORE;
        DEFAULT_CONC_WORD_PROP = ComplexFieldUtil.WORD_PROP_NAME;
    }
}
