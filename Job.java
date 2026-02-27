import java.util.concurrent.atomic.AtomicLong;

public final class Job {
    public static final int ADD = 1;
    public static final int SUBTRACT = 2;
    public static final int MULTIPLY = 3;
    public static final int DIVIDE = 4;

    private static final AtomicLong ID_SEQUENCE = new AtomicLong(1);

    private final long id;
    private final int operation;
    private final int leftOperand;
    private final int rightOperand;

    public Job(int operation, int leftOperand, int rightOperand) {
        validateOperation(operation);
        if (operation == DIVIDE && rightOperand == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }

        this.id = ID_SEQUENCE.getAndIncrement();
        this.operation = operation;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    private Job(long id, int operation, int leftOperand, int rightOperand) {
        validateOperation(operation);
        if (operation == DIVIDE && rightOperand == 0) {
            throw new IllegalArgumentException("Division by zero is not allowed");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id must be > 0");
        }

        this.id = id;
        this.operation = operation;
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;

        // Keep generated IDs ahead of any IDs reconstructed from the wire.
        ID_SEQUENCE.updateAndGet(current -> Math.max(current, id + 1));
    }

    public long getId() {
        return id;
    }

    public int getOperation() {
        return operation;
    }

    public int getLeftOperand() {
        return leftOperand;
    }

    public int getRightOperand() {
        return rightOperand;
    }

    public String getOperatorSymbol() {
        return switch (operation) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            default -> "?";
        };
    }

    private static void validateOperation(int operation) {
        if (operation < ADD || operation > DIVIDE) {
            throw new IllegalArgumentException("Operation must be 1(+), 2(-), 3(*), or 4(/)");
        }
    }

    @Override
    public String toString() {
        return id + "," + operation + "," + leftOperand + "," + rightOperand;
    }

    // Static factory to reconstruct a Job from wire format: id,operation,left,right
    public static Job fromString(String payload) {

        String[] parts = payload.trim().split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Invalid job payload. Expected format: id,operation,left,right");
        }

        long id = Long.parseLong(parts[0].trim());
        int operation = Integer.parseInt(parts[1].trim());
        int left = Integer.parseInt(parts[2].trim());
        int right = Integer.parseInt(parts[3].trim());

        return new Job(id, operation, left, right);
    }

}
