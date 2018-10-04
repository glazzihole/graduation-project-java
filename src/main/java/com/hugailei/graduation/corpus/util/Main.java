package com.hugailei.graduation.corpus.util;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        String line = sc.nextLine();
//        String a = line.split(" ")[0];
//        String b = line.split(" ")[1];
//
//        String regex = "[a-z]{1,}$";
//        if(a.matches(regex) && b.matches(regex)) {
//            if (a.length() == 0 || b.length() == 0) {
//                System.out.println(0);
//            } else if (a.length() != b.length()) {
//                System.out.println(0);
//            } else {
//                int count = 0;
//                int[] indexArray = new int[2];
//                for (int i = 0; i < a.length(); i++) {
//                    String wordA = String.valueOf(a.charAt(i));
//                    String wordB = String.valueOf(b.charAt(i));
//                    if (!wordA.equals(wordB)) {
//                        count++;
//                        if (count > 2) {
//                            System.out.println(0);
//                            break;
//                        } else {
//                            indexArray[count - 1] = i;
//                        }
//
//                    }
//
//                }
//
//                if (count == 2 &&
//                        String.valueOf(a.charAt(indexArray[0])).equals(String.valueOf(b.charAt(indexArray[1])))) {
//                    System.out.println(1);
//                } else {
//                    System.out.println(0);
//                }
//            }
//        } else {
//            System.out.println(0);
//        }

//        Scanner sc = new Scanner(System.in);
//        double a = sc.nextInt();
//        double b = Math.floor(Math.sqrt(a*2));
//        if(b*(b+1) == a*2){
//            System.out.println((int)b);
//        }

        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        String a = line.split(" ")[0];
        String b = line.split(" ")[1];
        String aWithoutDot = a.replace(".", "");
        String bWithoutDot = b.replace(".", "");

        int lengthA = aWithoutDot.length();
        int lengthB = bWithoutDot.length();
        int finalLength = Math.max(lengthA, lengthB);

        if(lengthA > lengthB){
            for(int i=1; i <= finalLength-lengthB; i++) {
                bWithoutDot = bWithoutDot + "0";
            }
        } else {
            for(int i=1; i <= finalLength-lengthA; i++) {
                aWithoutDot = aWithoutDot + "0";
            }
        }

        long aLong = Long.valueOf(aWithoutDot);
        long bLong = Long.valueOf(bWithoutDot);
        if(aLong > bLong){
            System.out.println(1);
        } else if(aLong < bLong) {
            System.out.println(-1);
        } else {
            System.out.println(0);
        }
    }


}
