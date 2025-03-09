package array;

// Practice1
// 배열 arr 의 모든 데이터에 대해서,
// 짝수 데이터들의 평균과 홀수 데이터들의 평균을 출력하세요.

// 입출력 예시)
// 배열 arr: 1, 2, 3, 4, 5, 6, 7, 8, 9
// 결과:
// 짝수 평균: 5.0
// 홀수 평균: 5.0

public class Practice1 {
  public static void main(String[] args) {
//    int[] arr = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    MyArray arr = new MyArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

    int odd = 0;
    int oddCount = 0;
    int even = 0;
    int evenCount = 0;

//    for (int i : arr) {
//      if (i % 2 == 0) {
//        even += i;
//        evenCount++;
//      } else {
//        odd += i;
//        oddCount++;
//      }
//    }

    for (int i = 0; i < arr.length; i++) {
      int value = arr.get(i);
      if (value % 2 == 0) {
        even += value;
        evenCount++;
      } else {
        odd += value;
        oddCount++;
      }
    }


    double evenAvg = evenCount > 0 ? even / evenCount : 0.0;
    double oddAvg = oddCount > 0 ? odd / oddCount : 0.0;

    System.out.println("짝수 평균: " + evenAvg);
    System.out.println("홀수 평균: " + oddAvg);
  }

}
