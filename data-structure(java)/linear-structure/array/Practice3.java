package array;

// Practice3
// 배열 arr 의 데이터 순서를 거꾸로 변경하세요.
// 추가 배열을 사용하지 않고 구현

// 입출력 예시)
// arr: 1, 3, 5, 7, 9
// 결과: 9, 7, 5, 3, 1

import java.util.Arrays;

public class Practice3 {

  public static void main(String[] args) {
//    int[] arr = new int[]{1, 3, 5, 7, 9};
    MyArray arr = new MyArray(new int[]{1, 3, 5, 7, 9});

    int tmp = 0;
    int rightIndex = 0;
    for (int i = 0; i < arr.length / 2; i++) {
//      tmp = arr[i];
//      rightIndex = arr.length - i - 1;
//      arr[i] = arr[rightIndex];
//      arr[rightIndex] = tmp;
      tmp = arr.get(i);
      rightIndex = arr.length - i - 1;
      arr.assign(i, arr.get(rightIndex));
      arr.assign(rightIndex, tmp);
    }

//    System.out.println(Arrays.toString(arr));
    System.out.println(Arrays.toString(arr.getInstance()));

  }
}