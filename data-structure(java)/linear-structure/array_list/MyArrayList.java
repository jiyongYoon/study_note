package array_list;

import java.util.Iterator;
import java.util.List;

public class MyArrayList<T> implements Iterable<T> {

  private Object[] arrayList;
  private int size;

  public MyArrayList() {}

  public MyArrayList(List<T> list) {
    this.size = list.size();
    this.arrayList = new Object[this.size];
    for (int i = 0; i < size; i++) {
      arrayList[i] = list.get(i);
    }
  }

  public int size() {
    return this.size;
  }

  public void add(T value) {
    Object[] newArrayList = new Object[this.size + 1];
    for (int i = 0; i < this.size; i++) {
      newArrayList[i] = this.arrayList[i];
    }
    newArrayList[this.size] = value;
    this.arrayList = newArrayList;
    this.size++;
    newArrayList = null;
  }

  public void add(int index, T value) {
    Object[] newArrayList = new Object[this.size + 1];
    int space = 0;
    for (int i = 0; i < this.size + 1; i++) {
      if (i == index) {
        newArrayList[i] = value;
        space++;
      } else {
        newArrayList[i] = this.arrayList[i - space];
      }
    }
    this.arrayList = newArrayList;
    this.size++;
    newArrayList = null;
  }

  public void addAll(List<T> list) {
    Object[] newArrayList = new Object[this.size + list.size()];
    for (int i = 0; i < this.size; i++) {
      newArrayList[i] = this.arrayList[i];
    }
    for (int i = this.size; i < newArrayList.length; i++) {
      newArrayList[i] = list.get(i - this.size);
    }
    arrayList = newArrayList;
    size += list.size();
    newArrayList = null;
  }

  public int indexOf(T value) {
    for (int i = 0; i < this.size; i++) {
      if (arrayList[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  public int lastIndexOf(T value) {
    for (int i = size - 1; i >= 0; i--) {
      if (arrayList[i].equals(value)) {
        return i;
      }
    }
    return -1;
  }

  public T get(int index) {
    return (T) this.arrayList[index];
  }

  public boolean contains(T value) {
    for (int i = 0; i < this.size; i++) {
      Object curValue = this.arrayList[i];
      if (curValue == value) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public MyArrayList<T> subList(int from, int to) {
    if (from < 0 || to > this.size) {
      throw new MyArrayListIndexOutOfBoundsException();
    }

    if (from > to) {
      throw new MyArrayListIllegalArgumentException(from, to);
    }

    Object[] objectArr = new Object[to - from];
    for (int i = 0; i < to - from; i++) {
      objectArr[i] = this.arrayList[i + from];
    }

    MyArrayList<T> newArrayList = new MyArrayList<>();
    newArrayList.arrayList = objectArr;
    newArrayList.size = objectArr.length;

    return newArrayList;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < this.size - 1; i++) {
      sb.append(this.arrayList[i]).append(", ");
    }
    sb.append(this.arrayList[this.size - 1]).append("]");
    return sb.toString();
  }

  @Override
  public Iterator<T> iterator() {
    return new MyArrayListIterator();
  }

  private class MyArrayListIterator implements Iterator<T> {

    private int curIndex = 0;

    @Override
    public boolean hasNext() {
      return curIndex < size;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new java.util.NoSuchElementException("No more elements");
      }

      return (T) arrayList[curIndex++];
    }
  }

  public static class MyArrayListIndexOutOfBoundsException extends RuntimeException {

    public MyArrayListIndexOutOfBoundsException() {
      super("인덱스가 범위를 벗어났습니다.");
    }
  }

  private static class MyArrayListIllegalArgumentException extends RuntimeException {

    public MyArrayListIllegalArgumentException(int fromIndex, int toIndex) {
      super("fromIndex(%d) > toIndex(%d)");
    }
  }
}
