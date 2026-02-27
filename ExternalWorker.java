import org.eclipse.paho.client.mqttv3.*;

import java.util.UUID;

public class ExternalWorker implements MqttCallback {

    private static final String BROKER              = "tcp://test.mosquitto.org:1883";
    private static final String TOPIC_READY_TO_WORK = "cal-poly/readyToWork";
    private static final String TOPIC_JOB           = "cal-poly/job";
    private static final String TOPIC_JOB_DONE      = "cal-poly/jobDone";

    private final String workerId;
    private MqttClient client;

    private int moneyCount;
    public ExternalWorker() {
        // Unique ID per worker instance so multiple workers can run simultaneously
        this.workerId = "worker-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static void main(String[] args) {
        ExternalWorker worker = new ExternalWorker();
        try {
            worker.start();
        } catch (MqttException | InterruptedException exception) {
            exception.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

  

    public void start() throws MqttException, InterruptedException {
        client = new MqttClient(BROKER, workerId);
        client.setCallback(this);
        client.connect();

        client.subscribe(TOPIC_JOB);
        System.out.println("[" + workerId + "] Connected to broker: " + BROKER);

        announceReady();

        while (client.isConnected()) {
            Thread.sleep(1000);
        }
    }

    private void announceReady() throws MqttException {
        MqttMessage readyMessage = new MqttMessage(workerId.getBytes());
        readyMessage.setQos(2);
        client.publish(TOPIC_READY_TO_WORK, readyMessage);
        System.out.println("[" + workerId + "] Announced: ready to work");
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("[" + workerId + "] Connection lost: " + throwable.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());
        System.out.println("[" + workerId + "] >> Message arrived. Topic: " + topic + " | Message: " + message);

        if (topic.equals(TOPIC_JOB)) {
            handleJob(message);
        }
    }

    private void handleJob(String jobPayload) throws MqttException, InterruptedException {
        Job job = parseJobPayload(jobPayload);

        System.out.println("[" + workerId + "] Working on: " + formatExpression(job) + " ...");
        Thread.sleep(2000); // simulate work

        double result = solve(job);
        System.out.println("[" + workerId + "] Result: " + formatExpression(job) + " = " + result);

        // Notify outsourcer this job is done, sending back the job ID
        MqttMessage doneMessage = new MqttMessage(String.valueOf(job.getId()).getBytes());
        doneMessage.setQos(2);
        client.publish(TOPIC_JOB_DONE, doneMessage);
        System.out.println("[" + workerId + "] Published job done: " + job.getId());
        moneyCount += 1;

        // Ready for the next job
        announceReady();
    }

    private Job parseJobPayload(String jobPayload) {
        try {
            return Job.fromString(jobPayload);
        } catch (IllegalArgumentException parseError) {
            throw new IllegalArgumentException(
                    "[" + workerId + "] Invalid job payload from MQTT: " + jobPayload,
                    parseError);
        }
    }

    private double solve(Job job) {
        return switch (job.getOperation()) {
            case Job.ADD      -> (double) (job.getLeftOperand() + job.getRightOperand());
            case Job.SUBTRACT -> (double) (job.getLeftOperand() - job.getRightOperand());
            case Job.MULTIPLY -> (double) (job.getLeftOperand() * job.getRightOperand());
            case Job.DIVIDE   -> (double) job.getLeftOperand() / job.getRightOperand();
            default -> throw new IllegalArgumentException("Unknown operation: " + job.getOperation());
        };
    }

    private String formatExpression(Job job) {
        return job.getLeftOperand() + " " + job.getOperatorSymbol() + " " + job.getRightOperand();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("[" + workerId + "] Delivery complete.");
    }
}
