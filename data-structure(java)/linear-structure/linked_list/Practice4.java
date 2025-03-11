package linked_list;

/*
https://leetcode.com/problems/middle-of-the-linked-list/description/
리스트의 중간 이후부터 반을 출력

Input: [1,2,3,4,5]
Output: [3,4,5]

Input: [1,2,3,4,5,6]
Output: [4,5,6]
 */

import java.util.Arrays;
import java.util.LinkedList;

public class Practice4 {

  public static void main(String[] args) {
//    LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5));
    LinkedList<Integer> list = new LinkedList<>(Arrays.asList(1, 2, 3, 4, 5, 6));

    int halfOfSize = list.size() / 2;

    LinkedList<Integer> result = new LinkedList<>(list.subList(halfOfSize, list.size()));

    System.out.println(result);
  }
}
