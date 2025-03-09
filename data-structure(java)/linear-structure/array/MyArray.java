package array;

public class MyArray {

  public final int[] arr;
  public final int length;

  public MyArray(int[] arrValues) {
    arr = arrValues;
    int count = 0;
    for (int arrValue : arrValues) {
      count++;
    }
    length = count;
  }

  public MyArray(int size) {
    arr = new int[size];
    length = size;
  }

  public int[] getInstance() {
    return arr;
  }

  public int get(int index) {
    if (index < 0 || index > length - 1) {
      throw new MyArrayIndexOutOfBoundsException();
    }

    return arr[index];
  }

  public void assign(int index, int value) {
    if (index < 0 || index > length - 1) {
      throw new MyArrayIndexOutOfBoundsException();
    }

    arr[index] = value;
  }

  public static class MyArrayIndexOutOfBoundsException extends RuntimeException {

    public MyArrayIndexOutOfBoundsException() {
      super("인덱스가 벗어났습니다.");
    }
  }
}
