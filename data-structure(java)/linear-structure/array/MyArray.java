package array;

public class MyArray {

  public final int[] arr;
  public final int length;

  public MyArray(int[] arrValues) {
    length = arrValues.length;

    arr = new int[length];
    for (int i = 0; i < length; i++) {
      arr[i] = arrValues[i];
    }
  }

  public MyArray(int size) {
    arr = new int[size];
    length = size;
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < this.length - 1; i++) {
      sb.append(arr[i]).append(", ");
    }
    sb.append(arr[length - 1]).append("]");
    return sb.toString();
  }

  public static class MyArrayIndexOutOfBoundsException extends RuntimeException {

    public MyArrayIndexOutOfBoundsException() {
      super("인덱스가 벗어났습니다.");
    }
  }
}
