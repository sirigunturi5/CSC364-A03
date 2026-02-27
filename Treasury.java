import java.util.HashSet;
import java.util.Set;

public class Treasury {
    private static final int INITIAL_BALANCE = 1000;
   
    private int balance;
    private int payout;
    private final Set<Long> paidJobIds;

    public Treasury() {
        this.balance = INITIAL_BALANCE;
        this.paidJobIds = new HashSet<>();
        this.payout = 2;
    }

    public synchronized void deposit(int amount) {
        if (amount != 2) {
            throw new IllegalArgumentException("Deposit amount must be 2");
        }
        balance += amount;
    }

    public synchronized boolean pay(long jobId) {
        if (jobId <= 0) {
            throw new IllegalArgumentException("jobId must be > 0");
        }
    

        if (paidJobIds.contains(jobId)) {
            return false;
        }

        if (balance < payout) {
            return false;
        }

        balance -= payout;
        System.out.println(balance);
        paidJobIds.add(jobId);
        return true;
    }

    public synchronized int getBalance() {
        return balance;
    }


}
