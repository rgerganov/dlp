package dlp;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Solves the discrete logarithm problem.
 * Find value "a" such that g^a mod p = A.
 *
 * "a" is 31 bit unsigned integer.
 * "g", "p" and "A" are given (in conf file)
 *
 * @author rgerganov
 *
 */
public class DLP implements Runnable {
    final AtomicInteger start = new AtomicInteger();
    static final int BATCH_SIZE = 1<<20;

    BigInteger g, p, A;

    @Override
    public void run() {
        while (true) {
            int loc_start = start.getAndAdd(BATCH_SIZE);
            if (loc_start < 0) {
                break;
            }
            int end = loc_start + BATCH_SIZE;
            if (end < 0) {
                end = Integer.MAX_VALUE;
            }
            System.out.printf("Testing from %d to %d\n", loc_start, end);
            BigInteger curr = g.modPow(BigInteger.valueOf(loc_start), p);
            for (int i = loc_start; i < end; i++) {
                if (A.equals(curr)) {
                    System.out.printf("solution found, a=%d\n", i);
                    System.exit(2);
                }
                curr = curr.multiply(g).mod(p);
            }
        }
    }

    void bruteForce(int threads) throws InterruptedException {
        System.out.printf("Solving g^a mod p = A using brute force with %d threads\n\n", threads);
        Thread[] t = new Thread[threads];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(this);
            t[i].start();
        }
        for (int i = 0; i < t.length; i++) {
            t[i].join();
        }
    }

    void babyStepGiantStep() {
        System.out.println("\nSolving g^a mod p = A using Baby-step Giant-step algorithm\n");
        int m = (int) Math.ceil(Math.sqrt(Integer.MAX_VALUE));
        Map<BigInteger, Integer> table = new HashMap<BigInteger, Integer>();
        BigInteger tmp = BigInteger.valueOf(1);
        for (int i = 0; i < m; i++) {
            table.put(tmp, i);
            tmp = tmp.multiply(g).mod(p);
        }
        BigInteger gi = g.modInverse(p);
        BigInteger factor = gi.modPow(BigInteger.valueOf(m), p);
        BigInteger e = A;
        for (int i = 0; i < m; i++) {
            if (table.containsKey(e)) {
                int a = i * m + table.get(e);
                System.out.printf("solution found, a=%d\n", a);
                break;
            }
            e = e.multiply(factor).mod(p);
        }
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java dlp.DLP [--fast] <conf_file>");
            System.exit(1);
        }
        boolean fast = false;
        Properties props = new Properties();
        if (args[0].equals("--fast")) {
            props.load(new FileInputStream(args[1]));
            fast = true;
        } else {
            props.load(new FileInputStream(args[0]));
        }
        DLP dlp = new DLP();
        dlp.g = new BigInteger(props.getProperty("g"), 16);
        System.out.println("g=" + dlp.g.toString(16));
        dlp.p = new BigInteger(props.getProperty("p"), 16);
        System.out.println("p=" + dlp.p.toString(16));
        dlp.A = new BigInteger(props.getProperty("A"), 16);
        System.out.println("A=" + dlp.A.toString(16));
        if (fast) {
            dlp.babyStepGiantStep();
        } else {
            dlp.start.set(Integer.valueOf(props.getProperty("start")));
            int threads = Integer.valueOf(props.getProperty("threads"));
            dlp.bruteForce(threads);
        }
    }
}
