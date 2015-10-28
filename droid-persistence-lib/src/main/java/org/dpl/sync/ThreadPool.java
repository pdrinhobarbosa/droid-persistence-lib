package org.dpl.sync;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool {

	private static ThreadPool instance;
	private static final int threadPoolSize = 5;

	private final ExecutorService executorService;
	private final ArrayList<Callable<Boolean>> workers;
	private final ArrayList<Future<Boolean>> futures;

	public static ThreadPool getInstance() {
		if (instance == null) {
			instance = new ThreadPool();
		}

		return instance;
	}

	private ThreadPool() {
		super();

		executorService = Executors.newFixedThreadPool(threadPoolSize);
		workers = new ArrayList<Callable<Boolean>>();
		futures = new ArrayList<Future<Boolean>>();
	}

	public void run() {
		for (Callable<Boolean> work : getWorkers()) {
			futures.add(getExecutorService().submit(work));
		}

		for (Future<Boolean> future : futures) {
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		executorService.shutdown();
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public static void setInstance(ThreadPool instance) {
		ThreadPool.instance = instance;
	}

	public ArrayList<Callable<Boolean>> getWorkers() {
		return workers;
	}
}