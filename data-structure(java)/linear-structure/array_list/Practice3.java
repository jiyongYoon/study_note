package array_list;

/*
https://leetcode.com/problems/palindrome-linked-list/description/
리스트가 펠린드롬인지 확인하기
*/

import java.util.Arrays;

public class Practice3 {

  public static void main(String[] args) {
//    List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 2, 1));
//    MyArrayList<Integer> list = new MyArrayList<>(Arrays.asList(1, 2, 2, 1));
    MyArrayList<Integer> list = new MyArrayList<>(Arrays.asList(5, 1, 3, 3, 3, 1, 5));

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
