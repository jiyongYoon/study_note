package stack;
/*
숫자열 뒤집기

입출력 예시)
  입력: [1, 3, 5, 7, 9]
  출력: [9, 7, 5, 3, 1]
 */

import java.util.Arrays;
import java.util.Stack;

public class Practice1 {

  public static void main(String[] args) {
    int[] inputArr = {1, 3, 5, 7, 9};

//    Stack<Integer> stack = new Stack<>();
    MyStack<Integer> stack = new MyStack<>();

    for (int i : inputArr) {
      stack.push(i);
    }

    int[] resultArr = new int[inputArr.length];
    for (int i = 0; i < resultArr.length; i++) {
      resultArr[i] = stack.pop();
    }

    System.out.println(Arrays.toString(resultArr));
  }

}
