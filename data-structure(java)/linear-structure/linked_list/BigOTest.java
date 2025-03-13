package linked_list;

/** tail을 두고 add하는 것과 그렇지 않은 것 차이
 * -----noTailList-----
 * 실행 시간: 8059 ms
 * -----tailList-----
 * 실행 시간: 4 ms
 */

public class BigOTest {

  public static void main(String[] args) {
    MyLinkedList<Integer> noTailList = new MyLinkedList<>();
    MyLinkedList<Integer> tailList = new MyLinkedList<>();

    int size = 100000;

    System.out.println("-----noTailList-----");
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < size; i++) {
      noTailList.addFori(i);
    }
    long endTime = System.currentTimeMillis();
    print(startTime, endTime);

    System.out.println("-----tailList-----");
    startTime = System.currentTimeMillis();
    for (int i = 0; i < size; i++) {
      tailList.add(i);
    }
    endTime = System.currentTimeMillis();
    print(startTime, endTime);
  }

  private static void print(long startTime, long endTime) {
    long executionTime = endTime - startTime;
    System.out.println("실행 시간: " + executionTime + " ms");
  }

}
