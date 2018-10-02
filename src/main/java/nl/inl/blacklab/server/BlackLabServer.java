package nl.inl.blacklab.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hugailei.graduation.corpus.utils.WebFileUtil;
import nl.inl.blacklab.core.search.RegexpTooLargeException;
import nl.inl.blacklab.core.search.Searcher;
import nl.inl.blacklab.core.util.FileUtil;
import nl.inl.blacklab.core.util.Json;
import nl.inl.blacklab.server.datastream.DataFormat;
import nl.inl.blacklab.server.datastream.DataStream;
import nl.inl.blacklab.server.exceptions.BlsException;
import nl.inl.blacklab.server.exceptions.ConfigurationException;
import nl.inl.blacklab.server.exceptions.InternalServerError;
import nl.inl.blacklab.server.jobs.User;
import nl.inl.blacklab.server.requesthandlers.RequestHandler;
import nl.inl.blacklab.server.requesthandlers.Response;
import nl.inl.blacklab.server.requesthandlers.SearchParameters;
import nl.inl.blacklab.server.search.SearchManager;
import nl.inl.blacklab.server.util.ServletUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BlackLabServer {
	private static final Logger logger = LogManager.getLogger(BlackLabServer.class);

	public static final Charset CONFIG_ENCODING = Charset.forName("utf-8");

	public static final Charset OUTPUT_ENCODING = Charset.forName("utf-8");

	/** Manages all our searches */
	private SearchManager searchManager;

	private boolean configRead = false;

	public void readConfig() throws BlsException {
		try {
			// 下面方法略有改动
			File servletPath = new File(getServletContext());
			System.out.println(servletPath);
			logger.debug("Running from dir: " + servletPath);
			String configFileName = "blacklab-server";
			List<File> searchDirs = new ArrayList<>();
			// searchDirs.add(new File(servletPath.getAbsolutePath() + "/WEB-INF/classes/"));
			// searchDirs.add(new File(servletPath.getAbsolutePath() + "/config/"));
			searchDirs.add(servletPath.getParentFile().getCanonicalFile());
			searchDirs.addAll(Searcher.getConfigDirs());

			List<String> exts = Arrays.asList("json", "yaml", "yml");
			File configFile = FileUtil.findFile(searchDirs, configFileName, exts);
			InputStream is = null;
			boolean isJson = true;
			if (configFile != null && configFile.exists()) {
				// Read from dir
				try {
					logger.debug("Reading configuration file " + configFile);
					is = new BufferedInputStream(new FileInputStream(configFile));
					isJson = configFile.getName().endsWith(".json");
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			} else {
				// Read from classpath
				logger.debug(configFileName + ".(json|yaml) not found in webapps dir; searching classpath...");
				for (String ext : exts) {
					is = getClass().getClassLoader().getResourceAsStream(configFileName + "." + ext);
					if (is != null) {
						isJson = ext.equals("json");
						break;
					}
				}
				if (is == null) {
					logger.debug(configFileName + ".(json|yaml) not found on classpath either. Using internal defaults.");
					// configFileName = "blacklab-server-defaults.json"; // internal defaults file
					// is = getClass().getClassLoader().getResourceAsStream(configFileName);
					// if (is == null) {
					// throw new ServletException("Could not find " + configFileName + "!");
					// }

					String descDirs = StringUtils.join(searchDirs, ", ");
					throw new ConfigurationException("Couldn't find blacklab-server.(json|yaml) in dirs " + descDirs + ", or on classpath. Please place "
							+ "blacklab-server.json in one of these locations containing at least the following:\n" + "{\n" + "  \"indexCollections\": [\n"
							+ "    \"/my/indices\" \n" + "  ]\n" + "}\n\n"
							+ "With this configuration, one index could be in /my/indices/my-first-index/, for example.. For additional documentation, please see http://inl.github.io/BlackLab/");
				}
				logger.debug("Reading configuration file from classpath: " + configFileName);
			}

			try {
				ObjectMapper mapper = isJson ? Json.getJsonObjectMapper() : Json.getYamlObjectMapper();
				searchManager = new SearchManager(mapper.readTree(new InputStreamReader(is, CONFIG_ENCODING)));
			} finally {
				is.close();
			}
		} catch (JsonProcessingException e) {
			throw new ConfigurationException("Invalid JSON in configuration file", e);
		} catch (IOException e) {
			throw new ConfigurationException("Error reading configuration file", e);
		}
	}

	public void handleRequest(User user, HttpServletRequest request, HttpServletResponse responseObject) {

		if (!configRead) {
			try {
				readConfig();
				configRead = true;
			} catch (BlsException e) {
				// Write HTTP headers (status code, encoding, content type and cache)
				responseObject.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				responseObject.setCharacterEncoding(OUTPUT_ENCODING.name().toLowerCase());
				responseObject.setContentType("json/application");
				ServletUtil.writeCacheHeaders(responseObject, 0);

				// === Write the response that was captured in buf
				try {
					Writer realOut = new OutputStreamWriter(responseObject.getOutputStream(), OUTPUT_ENCODING);
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
					logger.debug("(couldn't send response, client probably cancelled the request)");
				}
				return;
			}
		}

		// === Create RequestHandler object
		boolean debugMode = searchManager.config().isDebugMode(request.getRemoteAddr());
		RequestHandler requestHandler = RequestHandler.create(user, this, request, debugMode);

		// === Figure stuff out about the request
		DataFormat outputType = requestHandler.getOverrideType();
		// DataFormat outputType = response.getOverrideType(); // some responses override the user's request (i.e. article XML)
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
			callbackFunction = "";
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
		if (!isJsonp) // JSONP request always returns 200 OK because otherwise script doesn't load
        {
            responseObject.setStatus(httpCode);
        }
		responseObject.setCharacterEncoding(OUTPUT_ENCODING.name().toLowerCase());
		responseObject.setContentType(ServletUtil.getContentType(outputType));
		ServletUtil.writeCacheHeaders(responseObject, cacheTime);

		// === Write the response that was captured in buf
		try {
			Writer realOut = new OutputStreamWriter(responseObject.getOutputStream(), OUTPUT_ENCODING);
			boolean errorOccurred = errorBuf.getBuffer().length() > errorBufLengthBefore;
			StringWriter writeWhat = errorOccurred ? errorBuf : buf;
			realOut.write(writeWhat.toString());
			realOut.flush();
		} catch (IOException e) {
			// Client cancelled the request midway through.
			// This is okay, don't raise the alarm.
			logger.debug("(couldn't send response, client probably cancelled the request)");
			return;
		}
	}

	/**
	 * Get the search-related parameteers from the request object.
	 *
	 * This ignores stuff like the requested output type, etc.
	 *
	 * Note also that the request type is not part of the SearchParameters, so from looking at these parameters alone, you can't always tell what type of search
	 * we're doing. The RequestHandler subclass will add a jobclass parameter when executing the actual search.
	 *
	 * @param isDocs
	 *            is this a docs operation? influences how the "sort" parameter is interpreted
	 * @param request
	 *            the HTTP request
	 * @param indexName
	 *            the index to search
	 * @return the unique key
	 */
	public SearchParameters getSearchParameters(boolean isDocs, HttpServletRequest request, String indexName) {
		return SearchParameters.get(searchManager, isDocs, indexName, request);
	}

	public SearchManager getSearchManager() {
		return searchManager;
	}

	// ======以下为自行编写内容，以上从BlackServer中拷贝而来======
	/**
	 *
	 * @return
	 */
	public String getServletContext() {
		String realPath = WebFileUtil.getROOTPath();
		return realPath;
	}

	public void init() {
		;
	}

	public void destroy() {
		// Stops the load management thread
		searchManager.cleanup();
	}

	public boolean isConfigRead() {
		return configRead;
	}

	public void setConfigRead(boolean configRead) {
		this.configRead = configRead;
	}

	public void setSearchManager(SearchManager searchManager) {
		this.searchManager = searchManager;
	}

	public static Logger getLogger() {
		return logger;
	}

	// ========以下来自MainServlet

	/** Our Velocity templates */
	private Map<String, Template> templates = new HashMap<>();

	/** Our context path (first part of our URI path) */
	private String contextPath;

	/** Properties from the external config file, e.g. BLS URLs, Google Analytics key, etc. */
	private Properties adminProps;

	/** Our cached XSL stylesheets */
	private Map<String, String> stylesheets = new HashMap<>();

	/**
	 * Time the WAR was built.
	 */
	private String warBuildTime = null;


	/**
	 * Get a file from the directory belonging to this corpus.
	 *
	 * If Corpus is null, the default file is returned. If the file cannot be found (interface data directory not configured, or simply missing, or the corpus
	 * has no custom directory), the default file is returned.
	 *
	 * The default file is only returned when getDefaultIfMissing is true. Null is returned otherwise. Note that an exception is thrown if the default file is
	 * also missing, so for any files that might not have a default pass false for getDefaultIfMissing.
	 *
	 * @param corpus
	 *            - corpus for which to get the file. If null, falls back to the default files.
	 * @param fileName
	 *            - path to the file relative to the directory for the corpus.
	 * @param getDefaultIfMissing
	 *            - attempt to retrieve the default file if the file was not found.
	 * @return the file, or null if not found
	 * @throws RuntimeException
	 *             when getDefaultIfMissing is true, and the default file is missing.
	 */
	// TODO re-enable caching
	public InputStream getProjectFile(String corpus, String fileName, boolean getDefaultIfMissing) {
		if (corpus == null || isUserCorpus(corpus) || !adminProps.containsKey("corporaInterfaceDataDir")) {
            return getDefaultIfMissing ? getDefaultProjectFile(fileName) : null;
        }

		try {
			Path baseDir = Paths.get(adminProps.getProperty("corporaInterfaceDataDir"));
			Path corpusDir = baseDir.resolve(corpus).normalize();
			Path filePath = corpusDir.resolve(fileName).normalize();
			if (corpusDir.startsWith(baseDir) && filePath.startsWith(corpusDir)) {
                return new FileInputStream(new File(filePath.toString()));
            }

			// File path points outside the configured directory!
			return getDefaultIfMissing ? getDefaultProjectFile(fileName) : null;
		} catch (FileNotFoundException | SecurityException e) {
			return getDefaultIfMissing ? getDefaultProjectFile(fileName) : null;
		}
	}

	/**
	 *
	 * @param fileName
	 *            The file to get. This should NOT start with "/"
	 * @return the InputStream for the file.
	 * @throws RuntimeException
	 *             when the file cannot be found.
	 */
	private InputStream getDefaultProjectFile(String fileName) {
		InputStream stream=null;
		try {
			stream = new FileInputStream(getServletContext() + "/WEB-INF/interface-default/" + fileName);
			if (stream == null) {
                throw new RuntimeException("Default fallback for file " + fileName + " missing!");
            }
		} catch (FileNotFoundException e) {
			//            e.printStackTrace();
			throw new RuntimeException("Default fallback for file " + fileName + " missing!");
		}
		return stream;
	}

	public InputStream getHelpPage(String corpus) {
		return getProjectFile(corpus, "help.inc", true);
	}

	public InputStream getAboutPage(String corpus) {
		return getProjectFile(corpus, "about.inc", true);
	}

	public String getWebserviceUrl(String corpus) {
		String url = adminProps.getProperty("blsUrl");
		if (!url.endsWith("/")) {
            url += "/";
        }
		url += corpus + "/";
		return url;
	}

	public String getExternalWebserviceUrl(String corpus) {
		String url = adminProps.getProperty("blsUrlExternal");
		if (!url.endsWith("/")) {
            url += "/";
        }
		if (corpus != null && corpus.length() > 0) {
            url += corpus + "/";
        }
		return url;
	}

	public String getGoogleAnalyticsKey() {
		return adminProps.getProperty("googleAnalyticsKey", "");
	}

	public String getStylesheet(String corpus, String stylesheetName) {
		String key = corpus + "__" + stylesheetName;
		String stylesheet = stylesheets.get(key);
		if (stylesheet == null) {
			try (InputStream is = getProjectFile(corpus, stylesheetName, true)) {
				stylesheet = IOUtils.toString(is, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			stylesheets.put(key, stylesheet);
		}
		return stylesheet;
	}

	/**
	 * Return a timestamp for when the application was built.
	 *
	 * @return build timestamp (format: yyyy-MM-dd HH:mm:ss), or UNKNOWN if
	 *   the timestamp could not be found for some reason (i.e. not running from a
	 *   JAR, or JAR was not created with the Ant buildscript).
	 */
	public String getWarBuildTime() {
		if (warBuildTime == null) {
			try (InputStream inputStream =new  FileInputStream( getServletContext()+"/META-INF/MANIFEST.MF")) {
				if (inputStream == null) {
					warBuildTime = "(no manifest)";
				} else {
					try {
						Manifest manifest = new Manifest(inputStream);
						Attributes atts = manifest.getMainAttributes();
						String value = null;
						if (atts != null) {
							value = atts.getValue("Build-Time");
							if (value == null) {
                                value = atts.getValue("Build-Date"); // Old name for this info
                            }
						}
						warBuildTime = (value == null ? "UNKNOWN" : value);
					} finally {
						inputStream.close();
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Could not read build date from manifest", e);
			}
		}
		return warBuildTime;
	}

	public static boolean isUserCorpus(String corpus) {
		return corpus != null && corpus.indexOf(":") != -1;
	}

	public static String getCorpusName(String corpus) {
		if (corpus == null) {
            return null;
        }

		int i = corpus.indexOf(":");
		if (i != -1) {
            return corpus.substring(i + 1);
        }

		return corpus;
	}

	public static String getCorpusOwner(String corpus) {
		if (corpus == null) {
            return null;
        }

		int i = corpus.indexOf(":");
		if (i != -1) {
            return corpus.substring(0, i);
        }

		return null;
	}

}

