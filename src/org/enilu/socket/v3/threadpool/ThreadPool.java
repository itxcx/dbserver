package org.enilu.socket.v3.threadpool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.enilu.socket.v3.commons.Constants;

/**
 * 线程池
 * 
 * @author enilu
 * 
 */
public class ThreadPool {

	private static Logger logger = Logger.getLogger(ThreadPool.class.getName());
	private static ThreadPool instance;
	private static List<WorkerThread> idleThreads = new ArrayList<WorkerThread>();// 空闲线程队列
	private static List<WorkerThread> busyThreads = new ArrayList<WorkerThread>();// 工作状态线程队列
	private static BlockingDeque<Object> workers = new LinkedBlockingDeque<Object>(
			50);// 工作队列
	private static int threadNum = 10;

	private ThreadPool() {
		logger.setLevel(Constants.log_level);
	}

	/**
	 * 根据制定的线程数量构造线程池
	 * 
	 * @param threadNum
	 * @return
	 */
	public static ThreadPool getThreadPool(int num) {
		if (instance == null) {
			logger.log(Level.INFO, "create " + num
					+ " threads add to threadPool");
			instance = new ThreadPool();
			threadNum = num;
			for (int i = 0; i < num; i++) {
				idleThreads.add(new WorkerThread());
			}
			Task task = new Task();
			new Thread(task).start();

		}
		int xiangchaNum = num > threadNum ? (num - threadNum) : 0;
		for (int i = 0; i < xiangchaNum; i++) {
			idleThreads.add(new WorkerThread());
		}
		return instance;

	}

	/**
	 * 使用默认的线程数目构造线程池
	 * 
	 * @return
	 */
	public static ThreadPool getTheadPool() {
		return getThreadPool(50);// 默认生成50个线程
	}

	/**
	 * 持续工作的线程，不停的从任务队列中获取任务，并从idle线程李表中获取空闲线程，使用idle线程来跑任务
	 * 
	 * @author burns
	 * 
	 */
	static class Task implements Runnable {
		public Task() {
			super();
		}

		@Override
		public void run() {

			logger.log(Level.INFO, "threadPool startWork");
			while (true) {
				WorkerThread thread = idleThreads.remove(0);

				try {
					logger.log(Level.INFO, "begin get worker");
					Worker worker = (Worker) workers.take();
					logger.log(Level.INFO, "geted worker");
					thread.setWorker(worker);
					thread.start();
					busyThreads.add(thread);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}

	}

	/**
	 * 将一个工作任务加入到线程池中
	 * 
	 * @param worker
	 */
	public void push(Worker worker) {
		logger.log(Level.INFO,
				"push a worker to thredPool,before push the size of queue is:"
						+ this.workers.size());
		// this.workers.add(worker);
		this.workers.offer(worker);
		logger.log(Level.INFO,
				"push a worker to thredPool,after push the size of queue is:"
						+ this.workers.size());
	}

	/**
	 * 返还使用的thread
	 * 
	 * @param thread
	 */
	public void returnThread(WorkerThread thread) {
		logger.log(Level.INFO, "return thread to idleThreads");
		logger.log(Level.INFO,
				"the size of idleThreads is:" + idleThreads.size()
						+ " size of busyThreads is:" + busyThreads.size());
		idleThreads.add(thread);// 返还到空闲进程列表中
		busyThreads.remove(thread);// 从繁忙进程列表中移除当前线程
		logger.log(Level.INFO,
				"the size of idleThreads is:" + idleThreads.size()
						+ " size of busyThreads is:" + busyThreads.size());
	}
}
