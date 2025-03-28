package stack;

public class MyStack<T> {
  private final int defaultSize = 100;

  private int size;
  private Object[] arr;

  public MyStack() {
    this.size = 0;
    this.arr = new Object[defaultSize];
  }

  public int size() {
    return this.size;
  }

  public boolean isEmpty() {
    return this.size == 0;
  }

  public void push(T value) {
    // 준비된 배열에 들어갈 수 있으면
    if (this.size < this.arr.length) {
      this.arr[size] = value;
      this.size++;
    }
    // 준비된 배열보다 크면 100개 추가
    else {
      Object[] newArr = new Object[arr.length + defaultSize];
      for (int i = 0; i < arr.length; i++) {
        newArr[i] = arr[i];
      }
      this.arr = newArr;
      newArr = null;
      this.push(value);
    }
  }

  public T peek() {
    if (this.size == 0) {
      throw new MyStackEmptyStackException();
    }
    return (T) this.arr[size - 1];
  }

  public T pop() {
    if (this.size == 0) {
      throw new MyStackEmptyStackException();
    }
    T returnValue = (T) this.arr[size - 1];
    this.arr[size - 1] = null;
    this.size--;
    return returnValue;
  }

  @Override
  public String toString() {
    if (this.size == 0) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder("[").append(arr[0]);
    for (int i = 1; i < this.arr.length; i++) {
      Object cur = arr[i];
      if (cur == null) break;
      sb.append(", ").append(arr[i]);
    }
    sb.append("]");
    return sb.toString();
  }

  public static class MyStackEmptyStackException extends RuntimeException {
    public MyStackEmptyStackException() {
      super("Stack이 비어있습니다.");
    }
  }
}
