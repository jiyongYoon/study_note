package linked_list;

import java.util.Iterator;
import java.util.List;

public class MyLinkedList<T> implements Iterable<T> {

  static class MyNode<T> {
    T data;
    MyNode<T> next;

    public MyNode(T data) {
      this.data = data;
    }

    public boolean hasNext() {
      return next != null;
    }

    public T getData() {
      return data;
    }

    public MyNode<T> getNext() {
      return next;
    }
  }

  private MyNode<T> head;
  private int size;

  public MyLinkedList() {}

  public MyLinkedList(List<T> elements) {
    this();
    if (!elements.isEmpty()) {
      int index = 0;
      MyNode<T> curNode = this.head;
      for (T element : elements) {
        if (index == 0) {
          MyNode<T> newNode = new MyNode<>(element);
          this.head = newNode;
          curNode = newNode;
        } else {
          MyNode<T> newNode = new MyNode<>(element);
          curNode.next = newNode;
          curNode = newNode;
        }
        index++;
      }
      this.size = index;
    }
  }

  public int size() {
    return this.size;
  }

  public T get(int index) {
    return getNode(index).getData();
  }

  public MyNode<T> getNode(int index) {
    try {
      MyNode<T> current = this.head;
      for (int i = 0; i < index; i++) {
        current = current.getNext();
      }
      return current;
    } catch (NullPointerException e) {
      throw new MyLinkedListIndexOutOfBoundsException();
    }
  }

  public void add(T data) {
    if (this.head == null) {
      this.head = new MyNode<>(data);
      this.size++;
    } else {
      MyNode<T> lastNode = getLastNode();
      addNextNode(lastNode, data);
    }
  }

  public void add(int index, T data) {
    if (index == 0) {
      MyNode<T> newNode = new MyNode<>(data);
      newNode.next = this.head;
      this.head = newNode;
      this.size++;
    } else {
      MyNode<T> curNode = getNode(index - 1);
      if (curNode.hasNext()) {
        MyNode<T> nextNode = curNode.getNext();
        MyNode<T> newNode = addNextNode(curNode, data);
        newNode.next = nextNode;
      } else {
        addNextNode(curNode, data);
      }
    }
  }

  public boolean contains(T data) {
    MyNode<T> curNode = getNode(0);
    if (curNode.getData().equals(data)) {
      return true;
    } else {
      while (curNode.hasNext()) {
        curNode = curNode.getNext();
        if (curNode.getData().equals(data)) {
          return true;
        }
      }
    }
    return false;
  }

  public MyLinkedList<T> subList(int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new MyLinkedListIllegalArgumentException(fromIndex, toIndex);
    }

    if (fromIndex < 0 || toIndex >= this.size) {
      throw new MyLinkedListIndexOutOfBoundsException();
    }

    MyLinkedList<T> newList = new MyLinkedList<>();

    int index = 0;
    MyNode<T> curNode = this.head;

    if (fromIndex == 0) {
      newList.add(this.head.getData());
    }

    while (curNode.hasNext()) {
      curNode = curNode.getNext();
      index++;
      if (fromIndex <= index && index < toIndex) {
        newList.add(curNode.getData());
      }
    }

    return newList;
  }

  private MyNode<T> getLastNode() {
    if (this.head == null) {
      return null;
    } else if (!this.head.hasNext()) {
      return this.head;
    } else {
      MyNode<T> current = this.head;
      while (current.hasNext()) {
        current = current.getNext();
      }
      return current;
    }
  }

  private MyNode<T> addNextNode(MyNode<T> node, T data) {
    MyNode<T> newNode = new MyNode<>(data);
    node.next = newNode;
    this.size++;
    return newNode;
  }

  @Override
  public String toString() {
    if (this.head == null) {
      return "[]";
    } else {
      StringBuilder sb = new StringBuilder("[" + this.head.getData());

      MyNode current = this.head;
      while (current.hasNext()) {
        current = current.getNext();
        sb.append(", ").append(current.getData());
      }

      sb.append("]");
      return sb.toString();
    }
  }

  ///// for ( : ) 순회를 위한 인터페이스 및 구현체 /////
  @Override
  public Iterator<T> iterator() {
    return new MyIterator();
  }

  // 필요한 메서드만 구현함
  private class MyIterator implements Iterator<T> {
    private MyNode<T> current = head; // 처음엔 head부터 시작

    @Override
    public boolean hasNext() {
      return current != null; // 다음 노드가 있으면 true
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new java.util.NoSuchElementException("No more elements");
      }
      T data = current.getData(); // 현재 데이터 반환
      current = current.getNext(); // 다음 노드로 이동
      return data;
    }
  }

  ///// Exception /////
  private static class MyLinkedListIndexOutOfBoundsException extends RuntimeException {

    public MyLinkedListIndexOutOfBoundsException() {
      super("인덱스가 범위를 벗어났습니다.");
    }
  }

  private static class MyLinkedListIllegalArgumentException extends RuntimeException {

    public MyLinkedListIllegalArgumentException(int fromIndex, int toIndex) {
      super("fromIndex(%d) > toIndex(%d)");
    }
  }
}
