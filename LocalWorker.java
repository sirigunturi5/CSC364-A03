import java.util.concurrent.atomic.AtomicInteger;

public class LocalWorker implements Runnable {
    private static final long WORKER_SLEEP_MS = 10_000L;

    private final String workerName;
    private final JobRepository<Job> repository;
    private int moneyCount;
    private boolean running;

    public LocalWorker(String workerName, JobRepository<Job> repository) {
        if (workerName == null || workerName.isBlank()) {
            throw new IllegalArgumentException("Worker name cannot be blank");
        }
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }

        this.workerName = workerName;
        this.repository = repository;
        this.moneyCount = 0;
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Job job = repository.take();
                double result = solve(job);
                int updatedMoney = moneyCount + 1;
                this.moneyCount += 1;

                System.out.println("[" + workerName + "] solved job #" + job.getId()
                        + " -> " + formatExpression(job) + " = " + result
                        + " | moneyCount=" + updatedMoney);

                Thread.sleep(WORKER_SLEEP_MS);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    public void stop() {
        running = false;
    }

    public int getMoneyCount() {
        return moneyCount;
    }


    private double solve(Job job) {
        if (job.getOperation() == (Job.ADD)) {
            return job.getLeftOperand() + job.getRightOperand();
        } else if (job.getOperation() == (Job.SUBTRACT)) {
            return job.getLeftOperand() - job.getRightOperand();
        } else if (job.getOperation() == (Job.MULTIPLY)) {
            return job.getLeftOperand() * job.getRightOperand();
        } else if (job.getOperation() == (Job.DIVIDE)) {
            return job.getLeftOperand() / job.getRightOperand();
        }
        throw new IllegalArgumentException("Unknown operation: " + job.getOperation());
    }

    private String formatExpression(Job job) {
        return job.getLeftOperand() + " " + job.getOperatorSymbol() + " " + job.getRightOperand();
    }
}
