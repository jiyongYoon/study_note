package stack;

import java.util.Stack;

/*
프로그래머스 https://school.programmers.co.kr/learn/courses/30/lessons/12906
연속한 같은 숫자는 싫어

입출력 예시)
  입력: [1, 1, 3, 3, 0, 1, 1]
  출력: [1, 3, 0, 1]

  입력: [4, 4, 4, 3, 3]
  출력: [4, 3]
 */
public class Practice3 {

  public static void main(String[] args) {
    solution(new int[]{1, 1, 3, 3, 0, 1, 1});
    solution(new int[]{4, 4, 4, 3, 3});
  }

  public static void solution(int[] arr) {
//    Stack<Integer> stack = new Stack<>();
    MyStack<Integer> stack = new MyStack<>();

    for (int i = 0; i < arr.length; i++) {
      int curInt = arr[i];
      if (stack.isEmpty()) {
        stack.push(curInt);
      } else {
        Integer beforeInt = stack.peek();
        if (!beforeInt.equals(curInt)) {
          stack.push(curInt);
        }
      }
    }

    System.out.println(stack.toString());
  }

}
