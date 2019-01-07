package dlp;

import java.io.FileInputStream;
import java.math.BigInteger;
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
            for (int i = loc_start ; i < end ; i++) {
                if (A.equals(curr)) {
                    System.out.printf("solution found, a=%d\n", i);
                    System.exit(2);
                }
                curr = curr.multiply(g).mod(p);
            }
        }
    }

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java dlp.DLP <conf_file>");
            System.exit(1);
        }
        DLP dlp = new DLP();
        Properties props = new Properties();
        props.load(new FileInputStream(args[0]));
        dlp.g = new BigInteger(props.getProperty("g"), 16);
        System.out.println("g=" + dlp.g.toString(16));
        dlp.p = new BigInteger(props.getProperty("p"), 16);
        System.out.println("p=" + dlp.p.toString(16));
        dlp.A = new BigInteger(props.getProperty("A"), 16);
        System.out.println("A=" + dlp.A.toString(16));
        dlp.start.set(Integer.valueOf(props.getProperty("start")));
        int threads = Integer.valueOf(props.getProperty("threads"));

        System.out.printf("Solving g^a mod p = A with %d threads\n\n", threads);

        Thread[] t = new Thread[threads];
        for (int i = 0 ; i < t.length ; i++) {
            t[i] = new Thread(dlp);
            t[i].start();
        }
        for (int i = 0 ; i < t.length ; i++) {
            t[i].join();
        }
    }
}
