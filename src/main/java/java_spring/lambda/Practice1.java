package java_spring.lambda;

import java.util.Scanner;

/**
 * 특정 수를 입력받아 해당 수의 제곱과 루트를 구하는 람다 표현식 작성해보기 <br>
 * Condition1: interface 정의
 * Condition2: 하나의 메서드로 두가지 기능 구현
 */
public class Practice1 {

    public static void main(String[] args) {
        PowAndRoot powAndRoot = i -> {
            int pow = i * i;
            double root = Math.sqrt(i);
            return new Result(pow, root);
        };

        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        System.out.println("i = " + i);

        // 방법 1
        System.out.println("powAndRoot.run(i) = " + powAndRoot.run(i));

        // 방법 2
        Calculator<Integer> powCalculator = new Calculator<Integer>() {
            @Override
            public Integer calculate(int i) {
                return i * i;
            }
        };

        Calculator<Double> rootCalculator = new Calculator<Double>() {
            @Override
            public Double calculate(int i) {
                return Math.sqrt(i);
            }
        };

        System.out.println("pow = " + powCalculator.calculate(i));
        System.out.println("root = " + rootCalculator.calculate(i));
    }

    interface PowAndRoot {
        Result run(int i);
    }

    static class Result {
        private int pow;
        private double root;

        public Result(int pow, double root) {
            this.pow = pow;
            this.root = root;
        }

        public int getPow() {
            return pow;
        }

        public double getRoot() {
            return root;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "pow=" + pow +
                    ", root=" + root +
                    '}';
        }
    }

    /////////////////////////////////////

    interface Calculator<T> {
        T calculate(int i);
    }
//
//    static class PowCalculator implements Calculator<Integer> {
//
//        @Override
//        public Integer calculate(int i) {
//            return i * i;
//        }
//    }
//
//    static class RootCalculator implements Calculator<Double> {
//
//        @Override
//        public Double calculate(int i) {
//            return Math.sqrt(i);
//        }
//    }
}
