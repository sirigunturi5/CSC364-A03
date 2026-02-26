public class Main {
    public static void main(String[] args) {
        JobRepository<Job> repository = new JobRepository<>();
        Producer producer = new Producer("producer-1", repository);

        Thread producerThread = new Thread(producer, "producer-1-thread");
        producerThread.start();


        //trying with one Local Worker 
        LocalWorker localWorker = new LocalWorker("lw-1", repository);
        Thread localWorkerThread = new Thread(localWorker, "lw1-thread");
        localWorkerThread.start();

        try {
            // Let the producer publish a few jobs.
            Thread.sleep(25_000L);
            System.out.println("[main] repository size after test: " + repository.size());
            System.out.println("[main] repository capacity: " + repository.capacity());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        } finally {
            producer.stop();
            producerThread.interrupt();
            try {
                producerThread.join();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            System.out.println("[main] producer thread stopped");
        }
    }
}
