package array;

// Practice4
// 배열 arr 에서 peek 값 모두 출력

// 입출력 예시)
// arr: 3, 1, 2, 6, 2, 2, 5, 1, 9, 10, 1, 11
// 결과: 3, 6, 5, 10, 11

public class Practice4 {
  public static void main(String[] args) {
//    int[] arr = new int[]{3, 1, 2, 6, 2, 2, 5, 1, 9, 10, 1, 11};
    MyArray arr = new MyArray(new int[]{3, 1, 2, 6, 2, 2, 5, 1, 9, 10, 1, 11});

    int tmp = Integer.MIN_VALUE;
    for (int i = 0; i < arr.length; i++) {
      if (isPeek(arr, i)) {
//        System.out.println(arr[i]);
        System.out.println(arr.get(i));
      }
    }

  }

  private static boolean isPeek(int[] arr, int index) {
    if (arr.length >= 2 && index == 0) {
      return arr[index] > arr[index + 1];
    } else if (index == arr.length - 1) {
      return arr[index - 1] < arr[index];
    } else {
      return arr[index - 1] < arr[index] && arr[index] > arr[index + 1];
    }
  }

  private static boolean isPeek(MyArray arr, int index) {
    if (arr.length >= 2 && index == 0) {
      return arr.get(index) > arr.get(index + 1);
    } else if (index == arr.length - 1) {
      return arr.get(index - 1) < arr.get(index);
    } else {
      return arr.get(index - 1) < arr.get(index) && arr.get(index) > arr.get(index + 1);
    }
  }

}