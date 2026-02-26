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

}
