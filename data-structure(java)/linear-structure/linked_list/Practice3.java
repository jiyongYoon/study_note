package linked_list;

/*
https://leetcode.com/problems/palindrome-linked-list/description/
리스트가 펠린드롬인지 확인하기
*/

import java.util.Arrays;
import java.util.LinkedList;

public class Practice3 {

  public static void main(String[] args) {
//    LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 2, 1));
//    LinkedList<Integer> list = new LinkedList<>(Arrays.asList(5, 1, 3, 3, 3, 1, 5));
    MyLinkedList<Integer> list = new MyLinkedList<>(Arrays.asList(5, 1, 3, 3, 3, 1, 5));

    int halfOfSize = list.size() / 2;
    int[] leftArr = new int[halfOfSize];
    int[] rightArr = new int[halfOfSize];

    for (int i = 0; i < list.size(); i++) {
      if (i < halfOfSize) {
        leftArr[i] = list.get(i);
      } else if (i == halfOfSize) {
        continue;
      } else {
        rightArr[list.size() - i - 1] = list.get(i);
      }
    }

    for (int i = 0; i < halfOfSize; i++) {
      if (leftArr[i] != rightArr[i]) {
        System.out.println("false");
        return;
      }
    }

    System.out.println("true");
  }
}
