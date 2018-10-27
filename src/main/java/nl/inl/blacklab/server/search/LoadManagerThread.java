package nl.inl.blacklab.server.search;

import lombok.extern.slf4j.Slf4j;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * A thread that regularly calls SearchCache.performLoadManagement(null)
 * to ensure that load management continues even if no new requests are coming in.
 */
@Slf4j
class LoadManagerThread extends Thread implements UncaughtExceptionHandler {
	private SearchCache searchCache;

	/**
	 * Construct the load manager thread object.
	 *
	 * @param searchCache cache of running and completed searches, on which we call load management
	 */
	public LoadManagerThread(SearchCache searchCache) {
		//log.debug("Creating LOADMGR thread...");
		this.searchCache = searchCache;
		setUncaughtExceptionHandler(this);
	}

	/**
	 * Run the thread, performing the requested search.
	 */
	@Override
	public void run() {
		while (!interrupted()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				return;
			}

			synchronized(searchCache) {
				searchCache.performLoadManagement(null);
			}
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("LoadManagerThread threw an exception!");
		e.printStackTrace();
	}

}
