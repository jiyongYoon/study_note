package array_list;

/*
리스트에서 중복 데이터를 찾아 삭제하세요.

입력: 1, 3, 3, 1, 4, 2, 4, 2
출력: 1, 3, 4, 2
 */

import java.util.ArrayList;
import java.util.Arrays;

public class Practice1 {

  public static void main(String[] args) {
//    ArrayList<Integer> list = new ArrayList<>();
    MyArrayList<Integer> list = new MyArrayList<>();
//    list.add(1);
//    list.add(3);
//    list.add(3);
//    list.add(1);
//    list.add(4);
//    list.add(2);
//    list.add(4);
//    list.add(2);
    list.addAll(Arrays.asList(1, 3, 3, 1, 4, 2, 4, 2));
    System.out.println(list.indexOf(1));
    System.out.println(list.lastIndexOf(1));

//    ArrayList<Integer> resultList = new ArrayList<>();
    MyArrayList<Integer> resultList = new MyArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      Integer integer = list.get(i);
      if (!resultList.contains(integer)) {
        resultList.add(integer);
      }
    }

    System.out.println(resultList);
  }

}
