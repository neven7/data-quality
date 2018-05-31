package org.talend.dataquality.datamasking.semantic;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class UnderstandSecureRandom {

    public static void main(String[] args) throws NoSuchAlgorithmException {

        for (int i = 0; i < 5; i++) {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG"); //$NON-NLS-1$
            secureRandom.setSeed(100L);
            System.out.println(secureRandom.nextInt());
        }
    }
}
