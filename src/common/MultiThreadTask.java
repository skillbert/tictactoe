package common;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Runs a set of tasks concurrently on different threads.
 * 
 * @author Wilbert
 *
 */
public class MultiThreadTask {
	public Queue<ThreadTask> tasks;
	public ThreadTask endTask;
	private Queue<Thread> threads;
	private boolean ended;
	
	public MultiThreadTask() {
		threads = new LinkedList<>();
		tasks = new LinkedList<>();
		ended = false;
	}
	
	/**
	 * Adds a task to the task list.
	 * 
	 * @param task
	 *            The function to run
	 * @param arg
	 *            The argument to pass to this function when it runs
	 */
	public void addTask(Consumer<Object> task, Object arg) {
		synchronized (this) {
			tasks.add(new ThreadTask(task, arg));
		}
	}
	
	/**
	 * Set the task to run when all other tasks are done.
	 * 
	 * @param onFinishEndTask
	 *            The function to run
	 * @param arg
	 *            The argument to pass to this function
	 */
	public void onFinish(Consumer<Object> onFinishEndTask, Object arg) {
		synchronized (this) {
			this.endTask = new ThreadTask(onFinishEndTask, arg);
		}
	}
	
	/**
	 * Starts a new thread that will run tasks from the task list.
	 */
	public void addThread() {
		synchronized (this) {
			Thread thread = new Thread(() -> threadRun());
			threads.add(thread);
			thread.start();
		}
	}
	
	/**
	 * Adds a number of threads to the task.
	 * 
	 * @param amount
	 *            The amount of threads to start
	 */
	public void addThreads(int amount) {
		for (int i = 0; i < amount; i++) {
			addThread();
		}
	}
	
	/**
	 * Returns the next task to run.
	 * 
	 * @return a task to run of null if there are none left and the thread
	 *         should terminate
	 */
	private ThreadTask getTask() {
		ThreadTask task = tasks.poll();
		if (task != null) {
			return task;
		}
		if (!ended && threads.size() == 1) {
			ended = true;
			return endTask;
		}
		return null;
	}
	
	/**
	 * The code to run for a new thread.
	 */
	private void threadRun() {
		while (true) {
			// obtain a new task or exit if we're out of tasks
			ThreadTask task;
			synchronized (this) {
				task = getTask();
				if (task == null) {
					threads.remove(Thread.currentThread());
					return;
				}
			}
			
			task.consumer.accept(task.arg);
		}
	}
	
	public void joinAll() throws InterruptedException {
		while (true) {
			Thread thread;
			synchronized (this) {
				thread = threads.peek();
			}
			if (thread == null) {
				return;
			}
			thread.join();
		}
	}
	
	/**
	 * A consumer function together with it's argument.
	 * 
	 * @author Wilbert
	 *
	 */
	private class ThreadTask {
		private Consumer<Object> consumer;
		private Object arg;
		
		public ThreadTask(Consumer<Object> task, Object arg) {
			this.arg = arg;
			this.consumer = task;
		}
	}
}























