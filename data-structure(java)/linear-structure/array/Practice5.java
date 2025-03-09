package array;

// Practice5
// 배열 arr 에서 중복 값을 제거한 새 배열을 만드시오.

// 입출력 예시)
// arr: 1, 5, 3, 2, 2, 3, 1, 4, 1, 2, 3, 5
// 결과: 1, 5, 3, 2, 4

import java.util.Arrays;

public class Practice5 {
  public static void main(String[] args) {
//    int[] arr = new int[]{1, 5, 3, 2, 2, 3, 1, 4, 1, 2, 3, 5};
//    int maxValue = Integer.MIN_VALUE;
//    for (int i = 0; i < arr.length; i++) {
//      maxValue = Math.max(maxValue, arr[i]);
//    }
//
//    int[] valueCount = new int[maxValue + 1];
//
//    int[] newArr = new int[arr.length];
//    int newArrIndex = 0;
//
//    for (int i = 0; i < arr.length; i++) {
//      int curValue = arr[i];
//      if (valueCount[curValue] == 0) {
//        newArr[newArrIndex++] = curValue;
//      }
//      valueCount[curValue]++;
//    }
//
//    System.out.println(Arrays.toString(newArr));
//
//    int[] resultArr = new int[newArrIndex];
//    int resultArrIndex = 0;
//    for (int i = 0; i < resultArr.length; i++) {
//      resultArr[resultArrIndex++] = newArr[i];
//    }

    MyArray arr = new MyArray(new int[]{1, 5, 3, 2, 2, 3, 1, 4, 1, 2, 3, 5});

    int maxValue = Integer.MIN_VALUE;
    for (int i = 0; i < arr.length; i++) {
      maxValue = Math.max(maxValue, arr.get(i));
    }

    MyArray valueCount = new MyArray(maxValue + 1);

    MyArray newArr = new MyArray(arr.length);
    int newArrIndex = 0;

    for (int i = 0; i < arr.length; i++) {
      int curValue = arr.get(i);
      if (valueCount.get(curValue) == 0) {
        newArr.assign(newArrIndex++, curValue);
      }
      valueCount.assign(curValue, valueCount.get(curValue) + 1);
    }

    System.out.println(Arrays.toString(newArr.getInstance()));

    MyArray resultArr = new MyArray(newArrIndex);
    int resultArrIndex = 0;
    for (int i = 0; i < resultArr.length; i++) {
      resultArr.assign(resultArrIndex++, newArr.get(i));
    }

    System.out.println(Arrays.toString(resultArr.getInstance()));
  }
}
