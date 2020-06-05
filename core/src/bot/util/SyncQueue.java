package bot.util;

import java.util.LinkedList;

import org.jetbrains.annotations.Nullable;

public class SyncQueue<E> {
	
	private final LinkedList<E> queue;
	private final Object lock;
	
	public SyncQueue() {
		queue = new LinkedList<>();
		lock = new Object();
	}
	
	public boolean queueCheckEmpty(E e) {
		synchronized (lock) {
			boolean empty = queue.size() == 0;
			queue.addLast(e);
			return empty;
		}
	}
	
	/*public boolean queue(E e) {
		synchronized (lock) {
			queue.addLast(e);
			return true;
		}
	}*/
	
	@Nullable
	public E nextInQueue() {
		synchronized (lock) {
			queue.removeFirst();
			return queue.peekFirst();
		}
	}
}
