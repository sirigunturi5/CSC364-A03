import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;

public class Outsourcer implements MqttCallback {

    private static final String BROKER              = "tcp://test.mosquitto.org:1883";
    private static final String TOPIC_READY_TO_WORK = "cal-poly/readyToWork";
    private static final String TOPIC_SEND_JOB      = "cal-poly/job";
    private static final String TOPIC_JOB_DONE      = "cal-poly/jobDone";
    private static final String CLIENT_ID            = "outsourcer-client";

    private int moneyCount = 0;
    private final Map<Long, String> inFlight = new HashMap<>();
    private final JobRepository<Job> jobRepository;
    private final Treasury treasury;

    private MqttClient client;

    public Outsourcer(JobRepository<Job> jobRepository, Treasury treasury) {
        this.jobRepository = jobRepository;
        this.treasury = treasury;
    }

    public void start() throws MqttException, InterruptedException {
        client = new MqttClient(BROKER, CLIENT_ID);
        client.setCallback(this);
        client.connect();

        client.subscribe(TOPIC_READY_TO_WORK);
        client.subscribe(TOPIC_JOB_DONE);

        System.out.println("Connected to broker: " + BROKER);
        System.out.println("Listening on: " + TOPIC_READY_TO_WORK + ", " + TOPIC_JOB_DONE);

        while (client.isConnected()) {
            Thread.sleep(1000);
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection lost: " + throwable.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        System.out.println(">> Message arrived. Topic: " + topic + " | Message: " + message);

        if (topic.equals(TOPIC_READY_TO_WORK)) {
            handleWorkerReady(message);
        } else if (topic.equals(TOPIC_JOB_DONE)) {
            handleJobDone(message);
        }
    }

    private void handleWorkerReady(String workerId) throws Exception {
        Job job = jobRepository.take();

        inFlight.put(job.getId(), workerId);
        System.out.println("In-flight: " + inFlight);

        MqttMessage jobMessage = new MqttMessage(job.toString().getBytes());
        jobMessage.setQos(2);
        client.publish(TOPIC_SEND_JOB, jobMessage);

    }

    private void handleJobDone(String completedJobId) {
        long jobId = Long.parseLong(completedJobId);

        if (inFlight.containsKey(jobId)) {
            String workerId = inFlight.remove(jobId);
            System.out.println("Job done: " + jobId + " completed by " + workerId);
            System.out.println("In-flight remaining: " + inFlight);
            if (!this.treasury.pay(jobId)) {
                throw new IllegalArgumentException("Local Worker failed to solve job");
             }

            moneyCount += 1;

        } else {
            System.out.println("Received completion for unknown job: " + jobId);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("Delivery complete.");
    }
}