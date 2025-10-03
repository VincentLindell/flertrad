package network.sniffer;

import java.math.BigInteger;

@FunctionalInterface
public interface SnifferCallback {

    /**
     * Called whenever the sniffer intercepts an encrypted message on the network.
     * 
     * @param   m   sniffed message
     * @param   n   sender's public key
     */
    void onMessageIntercepted(String m, BigInteger n);
}
