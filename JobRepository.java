import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class JobRepository<T> {
    private final int capacity;
    private final Semaphore emptySlots;
    private final Semaphore availableItems;
    private final ReentrantLock lock;
    private final Deque<T> queue;

    public JobRepository() {
        //doing n - 1 to maintain one processer for local worker 
        this(Runtime.getRuntime().availableProcessors() - 1);
    }

    public JobRepository(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }

        this.capacity = capacity;
        this.emptySlots = new Semaphore(capacity);
        this.availableItems = new Semaphore(0);
        this.lock = new ReentrantLock();
        this.queue = new ArrayDeque<>(capacity);
    }

    public void put(T job) throws InterruptedException {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }

        emptySlots.acquire();
        lock.lock();
        try {
            queue.addLast(job);
            if (job instanceof Job) {
                Job j = (Job) job;  // cast it first
                System.out.println(j.getOperation() + ", " + j.getLeftOperand() + ", " + j.getRightOperand());
            }
        } finally {
            lock.unlock();
        }
        availableItems.release();
    }

    public T take() throws InterruptedException {
        availableItems.acquire();
        lock.lock();
        try {
            return queue.removeFirst();
        } finally {
            lock.unlock();
            emptySlots.release();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public int capacity() {
        return capacity;
    }
}
