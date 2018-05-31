package org.talend.dataquality.datamasking.semantic;

import java.security.SecureRandom;

public class UnderstandSecureRandom {

    public static void main(String[] args){

        for (int i=0;i<5;i++) {
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.setSeed(100L);
            System.out.println(secureRandom.nextInt());
        }
    }
}
