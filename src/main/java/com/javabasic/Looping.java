package com.javabasic;

import java.sql.SQLOutput;

public class Looping {
public static int j=0;
    public static void forEach() {
        System.out.println(j);
        int values[] = {1, 2, 3, 4, 5};
        System.out.println("Foreach loop");
        for (int value : values) {
            System.out.println(value);
        }
    }

    public static void forloop() {
        char alphabets[] = {'J', 'A', 'V', 'A' };
        System.out.println("for loop");
        for (int i = 0; alphabets.length > i; i++) {
            System.out.println(alphabets[i]);
        }
    }

    public static void whileloop() {
        char alphabets[] = {'J', 'A', 'V', 'A' };
        System.out.println("while loop");
        int i=0;
        while (alphabets.length>i){
            System.out.println(alphabets[i]);
            i++;
        }

    }

    public static void doloop() {
        char alphabets[] = {'J', 'A', 'V', 'A' };
        System.out.println("do loop");
        int i=0;
        do{
            System.out.println(alphabets[i]);
            i++;
        }while (alphabets.length>i);

    }

}
