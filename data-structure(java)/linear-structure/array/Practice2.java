package array;

// Practice2
// 배열 arr 에서 target 에 해당하는 값의 인덱스를 출력
// 해당 값이 여러 개인 경우 가장 큰 인덱스 출력

// 입출력 예시)
// 배열 arr: 1, 1, 100, 1, 1, 1, 100
// 결과: 6

public class Practice2 {
  public static void main(String[] args) {
//    int[] arr = new int[]{1, 1, 100, 1, 1, 1, 100};
    MyArray arr = new MyArray(new int[]{1, 1, 100, 1, 1, 1, 100});

    int max = Integer.MIN_VALUE;
    int answer = 0;

    for (int i = 0; i < arr.length; i++) {
//      int value = arr[i];
      int value = arr.get(i);
      if (max <= value) {
        answer = i;
      }
    }

    System.out.println(answer);
  }
}
