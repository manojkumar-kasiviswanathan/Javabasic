package com.javabasic;

import java.sql.SQLOutput;

public class ConditionalAssignment {

    public static void ConditionalAssignment() {
        int a = 5, b = 10;
        int greaterNumber = a > b ? a : b;

        System.out.println("Called ConditionalAssignment method from main method");
        System.out.println("Greatest number between " + a + " & " + b + " is " + greaterNumber);
    }


}
