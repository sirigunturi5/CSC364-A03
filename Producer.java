import java.util.concurrent.ThreadLocalRandom;

public class Producer implements Runnable {
    private static final long PRODUCER_SLEEP_MS = 10_000L;
    private static final int MIN_OPERAND = -100;
    private static final int MAX_OPERAND = 100;

    private final String producerName;
    private final JobRepository<Job> repository;
    private volatile boolean running;

    public Producer(String producerName, JobRepository<Job> repository) {
        if (producerName == null || producerName.isBlank()) {
            throw new IllegalArgumentException("Producer name cannot be blank");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }

        this.producerName = producerName;
        this.repository = repository;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Job job = createRandomJob();
                repository.put(job);
                System.out.println("[" + producerName + "] produced " + job);
                Thread.sleep(PRODUCER_SLEEP_MS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    public void stop() {
        running = false;
    }

    private Job createRandomJob() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int operation = random.nextInt(Job.ADD, Job.DIVIDE + 1);
        int leftOperand = random.nextInt(MIN_OPERAND, MAX_OPERAND + 1);
        int rightOperand = random.nextInt(MIN_OPERAND, MAX_OPERAND + 1);

        if (operation == Job.DIVIDE) {
            while (rightOperand == 0) {
                rightOperand = random.nextInt(MIN_OPERAND, MAX_OPERAND + 1);
            }
        }

        return new Job(operation, leftOperand, rightOperand);
    }
}
