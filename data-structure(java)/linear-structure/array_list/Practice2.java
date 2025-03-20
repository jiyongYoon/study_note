package array_list;

/*
리스트 부분 뒤집기

입력: 1, 2, 3, 4, 5 / 2(시작위치) / 4(끝위치)
출력: 1, 4, 3, 2, 5
 */

import java.util.ArrayList;
import java.util.List;

public class Practice2 {

  public static void main(String[] args) {
//    List<Integer> list = new ArrayList<>();
    MyArrayList<Integer> list = new MyArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    list.add(4);
    list.add(5);

    int startIndex = 2;
    int endIndex = 4;

    MyArrayList<Integer> resultList = new MyArrayList<>();

    MyArrayList<Integer> reverseTarget = list.subList(startIndex - 1, endIndex);
    System.out.println(reverseTarget);

    for (int i = 1; i <= list.size(); i++) {
      if (startIndex <= i && i <= endIndex) {
        int selectIndex = endIndex - i;
        resultList.add(reverseTarget.get(selectIndex));
      } else {
        resultList.add(list.get(i - 1));
      }
    }

    System.out.println(resultList);
  }
}
