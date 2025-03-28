package stack;

import java.io.IOException;
import java.util.Stack;

/*
괄호 짝 검사

입출력 예시)
  입력: "("
  출력: Fail

  입력: "()()"
  출력: Pass

  입력: "(()())"
  출력: Pass
 */
public class Practice2 {

  private static void checkPair(String input) {
    char[] inputChar = input.toCharArray();

//    Stack<Character> stack = new Stack<>();
    MyStack<Character> stack = new MyStack<>();

    for (int i = 0; i < inputChar.length; i++) {
      char curChar = inputChar[i];

      if (curChar == '(') {
        stack.push(curChar);
      } else if (curChar == ')') {
        if (!stack.isEmpty()) {
          char beforeChar = stack.pop();
          if (beforeChar != '(') {
            System.out.println(input + " -> Fail");
            return;
          }
        } else {
          System.out.println(input + " -> Fail");
          return;
        }
      } else {
        throw new RuntimeException("잘못된 입력입니다.");
      }
    }

    if (stack.isEmpty()) {
      System.out.println(input + " -> Pass");
    } else {
      System.out.println(input + " -> Fail");
    }
  }

  public static void main(String[] args) throws IOException {
    long start = System.currentTimeMillis();
    checkPair("(");
    checkPair("()()");
    checkPair("(()())");
    checkPair("((()))");
    checkPair(")))(((");
    System.out.println("execute time = " + (System.currentTimeMillis() - start));
  }
}
