package linked_list;

/*
단방향 연결리스트에서 중복 데이터를 찾아 삭제하세요.

입력: 1, 3, 3, 1, 4, 2, 4, 2
출력: 1, 3, 4, 2
 */

import java.util.LinkedList;

public class Practice1 {

  public static void main(String[] args) {
//    LinkedList<Integer> list = new LinkedList<>();
    MyLinkedList<Integer> list = new MyLinkedList<>();
    list.add(1);
    list.add(3);
    list.add(3);
    list.add(1);
    list.add(4);
    list.add(2);
    list.add(4);
    list.add(2);

    MyLinkedList<Integer> resultList = new MyLinkedList<>();
    for (int i = 0; i < list.size(); i++) {
      Integer integer = list.get(i);
      if (!resultList.contains(integer)) {
        resultList.add(integer);
      }
    }

    System.out.println(resultList);
  }

}
