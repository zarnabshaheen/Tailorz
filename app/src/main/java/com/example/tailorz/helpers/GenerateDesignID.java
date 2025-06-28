package com.example.tailorz.helpers;

import java.util.Random;

public class GenerateDesignID {
    private final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private final String NUMBERS = "0123456789";
    private final char[] ID = (NUMBERS).toCharArray();
    private final char[] ALPHANUMERIC =(LETTERS+LETTERS.toUpperCase()+NUMBERS).toCharArray();

    public String generateRandomID(int len){
        StringBuilder result = new StringBuilder();
        for (int i=0; i<len; i++){
            result.append(ALPHANUMERIC[new Random().nextInt(ALPHANUMERIC.length)]);
        }
        return result.toString();
    }

    public String generateOrderID(int len){
        StringBuilder result = new StringBuilder();
        for (int i=0; i<len; i++){
            result.append(new Random().nextInt(ID.length));
        }
        return result.toString();
    }
}
