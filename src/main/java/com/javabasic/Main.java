package com.javabasic;

public class Main { //Class

    public static void main(String[] args) { //method
        System.out.println("=> Main Class");
        new Looping();// Creating an instance from our class
        new ConditionalAssignment();

        /*
        ConditionalAssignment
         */
        ConditionalAssignment.ConditionalAssignment();// using the class methods by the instance we created.

         /*
        Looping
         */
        Looping.forEach();
        Looping.forloop();
        Looping.whileloop();
        Looping.doloop();




    }


}
