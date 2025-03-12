package linked_list;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/*
https://leetcode.com/problems/check-if-it-is-a-straight-line/description/
기울기가 직선인지 판별

Input: [[1,2],[2,3],[3,4],[4,5],[5,6],[6,7]]
Output: true

Input: [[1,1],[2,2],[3,4],[4,5],[5,6],[7,7]]
Output: false
 */
public class Practice5 {

  public static void main(String[] args) {
//    LinkedList<int[]> list = new LinkedList<>(Arrays.asList(
//        new int[]{1, 2},
//        new int[]{2, 3},
//        new int[]{3, 4},
//        new int[]{4, 5},
//        new int[]{5, 6},
//        new int[]{6, 7}
//    ));

//    LinkedList<int[]> list = new LinkedList<>(Arrays.asList(
//        new int[]{1, 1},
//        new int[]{2, 2},
//        new int[]{3, 4},
//        new int[]{4, 5},
//        new int[]{5, 6},
//        new int[]{7, 7}
//    ));

    MyLinkedList<int[]> list = new MyLinkedList<>(Arrays.asList(
        new int[]{1, 2},
        new int[]{2, 3},
        new int[]{3, 4},
        new int[]{4, 5},
        new int[]{5, 6},
        new int[]{6, 7}
    ));

    Double gradient = null;
    Iterator<int[]> iterator = list.iterator();
    int index = 0;

    while (iterator.hasNext()) {
      if (index != 0 || index != list.size() - 1) {
        int[] left = iterator.next();
        int[] right = iterator.next();

        double curGradient = calculateGradient(left[0], left[1], right[0], right[1]);

        if (gradient == null) {
          gradient = curGradient;
        } else {
          if (!gradient.equals(curGradient)) {
            System.out.println("false");
            return;
          }
        }
      }
      index++;
    }

    System.out.println("true");
  }

  private static double calculateGradient(int x1, int y1, int x2, int y2) {
    if ((x2 - x1) == 0) {
      return Double.MAX_VALUE;
    }

    return (double) (y2 - y1) / (x2 - x1);
  }

}
