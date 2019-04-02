package com.hugailei.graduation.corpus.scripts;

import java.util.Scanner;

/**
 * @author HU Gailei
 * @date 2019/3/27
 * <p>
 * description:
 * </p>
 **/
public class Main {
    public static void main(String args[]) {

        double x1 = 0 , x2= 0;
        double i,i1 = 0;
        Scanner input = new Scanner(System.in);
        double a = input.nextDouble();
        double b = input.nextDouble();
        double c = input.nextDouble();
        i = b*b-4*a*c;
        if(a == 0){
            if(b == 0) {
                System.out.println("");
            }
            else{
                x1 = c/b;
                System.out.println(x1%1==0?(int) x1:String.format("%.4f", x1));
            }
        }
        else {
            if (i < 0) {
                //System.out.println("");
            } else {
                i1 = Math.sqrt(i);
                x1 = (-b + i1) / (2 * a);
                x2 = (-b - i1) / (2 * a);
                if (x1 > x2) {
                    double temp = x1;
                    x1 = x2;
                    x2 = temp;
                    System.out.println(x1%1==0?(int) x1:String.format("%.4f", x1) + " " + (x2%1==0?(int) x2:String.format("%.4f", x2)));
                } else if (x1 == x2) {
                    System.out.println(x1%1==0?(int) x1:String.format("%.4f", x1));
                } else {
                    System.out.println(x1%1==0?(int) x1:String.format("%.4f", x1) + " " + (x2%1==0?(int) x2:String.format("%.4f", x2)));
                }

            }
        }
    }
}

