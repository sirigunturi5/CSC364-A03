public class Main {
    public static void main(String[] args) {
        JobRepository<Job> repository = new JobRepository<>();
        Treasury treasury = new Treasury();

        // Spawn (CPU cores - 1) producers
        int producerCount = Runtime.getRuntime().availableProcessors() - 1;
        for (int i = 0; i < producerCount; i++) {
            String producerId = "producer-" + (i + 1);
            Producer producer = new Producer(producerId, repository);
            Thread producerThread = new Thread(producer, producerId + "-thread");
            producerThread.start();
        }
        System.out.println("[main] Started " + producerCount + " producers");

        Outsourcer outsourcer = new Outsourcer(repository, treasury);
        Thread outsourcerThread = new Thread(() -> {
            try { outsourcer.start(); }
            catch (Exception e) { e.printStackTrace(); }
        }, "outsourcer-thread");
        outsourcerThread.start();

        LocalWorker localWorker = new LocalWorker("lw-1", repository, treasury);
        Thread localWorkerThread = new Thread(localWorker, "lw1-thread");
        localWorkerThread.start();

        try {
            Thread.sleep(25_000L);
            System.out.println("[main] repository size after test: " + repository.size());
            System.out.println("[main] repository capacity: " + repository.capacity());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("[main] Done");
        }
    }
}