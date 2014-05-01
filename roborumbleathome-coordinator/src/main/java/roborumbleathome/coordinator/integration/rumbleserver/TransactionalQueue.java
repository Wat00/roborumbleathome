package roborumbleathome.coordinator.integration.rumbleserver;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionalQueue<T> {

    public static class Transaction<T> {

	private final T t;

	private final TransactionalQueue<T> queue;

	public Transaction(T t, TransactionalQueue<T> queue) {
	    this.t = t;
	    this.queue = queue;
	}

	public T get() {
	    return t;
	}

	public void commit() {
	    queue.commit(this);
	}

	public void rollback() {
	    queue.rollback(this);
	}

    }

    private final LinkedList<T> queue;

    private final Collection<Transaction<T>> transactions;

    private final int capacity;

    private final ReentrantLock lock;

    private final Condition notEmpty;
    private final Condition notFull;

    public TransactionalQueue(int capacity) {
	queue = new LinkedList<T>();
	transactions = new HashSet<Transaction<T>>();
	this.capacity = capacity;

	lock = new ReentrantLock();
	notEmpty = lock.newCondition();
	notFull = lock.newCondition();
    }

    private T take() throws InterruptedException {
	while (queue.isEmpty()) {
	    notEmpty.await();
	}

	T t = queue.removeFirst();

	notFull.signal();
	return t;

    }

    public void put(T t) throws InterruptedException {
	lock.lock();
	try {
	    while (size() >= capacity) {
		notFull.await();
	    }

	    queue.addLast(t);

	    notEmpty.signal();
	} finally {
	    lock.unlock();
	}
    }

    private int size() {
	return queue.size() + transactions.size();
    }

    public Transaction<T> beginTransaction() throws InterruptedException {
	Transaction<T> transaction;
	lock.lock();
	try {

	    T t = take();
	    transaction = new Transaction<T>(t, this);
	    transactions.add(transaction);

	} finally {
	    lock.unlock();
	}
	return transaction;
    }

    private void commit(Transaction<T> transaction) {
	lock.lock();
	try {

	    transactions.remove(transaction);

	} finally {
	    lock.unlock();
	}
    }

    private void rollback(Transaction<T> transaction) {
	lock.lock();
	try {

	    queue.addLast(transaction.get());

	} finally {
	    lock.unlock();
	}
    }
}
