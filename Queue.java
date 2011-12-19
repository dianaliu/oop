package xtc.oop;

 
 // Queue.java modified from http://algs4.cs.princeton.edu/43mst/Queue.java.html
 
 
/*************************************************************************
*  Compilation:  javac Queue.java
*  Execution:    java Queue < input.txt
*  Data files:   http://algs4.cs.princeton.edu/13stacks/tobe.txt
 *
*  A generic queue, implemented using a linked list.
*
*  % java Queue < tobe.txt
*  to be or not to be (2 left on queue)
*
*************************************************************************/
 
import java.util.Iterator;
import java.util.NoSuchElementException;
 
/**
*  The <tt>Queue</tt> class represents a first-in-first-out (FIFO)
*  queue of generic items.
*  It supports the usual <em>enqueue</em> and <em>dequeue</em>
*  operations, along with methods for peeking at the top item,
*  testing if the queue is empty, and iterating through
*  the items in FIFO order.
*  <p>
*  All queue operations except iteration are constant time.
*  <p>
*  For additional documentation, see <a href="http://algs4.cs.princeton.edu/13stacks">Section 1.3</a> of
*  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
*/
public class Queue<Item> implements Iterable<Item> {
public int N;         // number of elements on queue
public Node first;    // beginning of queue
public Node last;     // end of queue
 
public class Node {
  public Item item;
  public Node next;
  public int task;
  public int resource = 0;
  public int nReq = 0;
  public int nRel;
  public int nOfCycle = 0;
  public int initClaim;
  public int leftCycle;
  public int process;
  public int frame;
}
// helper linked list class
 
/**
  * Create an empty queue.
  */
public Queue() {
  first = null;
  last  = null;
  N = 0;
}
 
/**
  * Is the queue empty?
  */
public boolean isEmpty() {
  return first == null;
}
 
/**
  * Return the number of items in the queue.
  */
public int size() {
  return N;   
 }
 
/**
  * Return the item least recently added to the queue.
  * Throw an exception if the queue is empty.
  */
public Item peek() {
  if (isEmpty()) throw new RuntimeException("Queue underflow");
  return first.item;
}
 
/**
  * Add the item to the queue.
  */
public void enqueue2(Item item) {
  Node nlast = new Node();
  if (isEmpty())    { 
   last = new Node();
   last.item = item;
   last.next = null;
   first = last;
  }
  else if (size() == 1) {
   nlast.item = last.item;
   first.item = item;
   last = new Node();
   first.next = last;
   last.item = nlast.item;
   last.next = null;
  }
  else {
   nlast.item = last.item;
   nlast.next = null;
   last.item = item;
   last.next = nlast;
  }
  N++;
}
public void kaynak(Item item, int task, int nReq, int initClaim) {
  if(isEmpty())
   enqueue(item);
  else
  {
   Node nfirst = new Node();
 
   nfirst.item = first.item;
   nfirst.next = first.next;
   first.item = item;
   first.task = task;
   first.nReq = nReq;
   first.initClaim = initClaim;
   first.next = nfirst;
   N++;
  }
}
//bir switch koyup normal kaynak ekleyip attribute lar eklenbilir
//queue yu modifiye edip sayiyla getiren fonksiyon yaz
// oha amina koyim
public Item getItem(int i) {
  int j = 0;
  Node result = new Node();
  result.item = first.item;
  result.next = first.next;
  while(j < i) {
   result.item = result.next.item;
   result.next = result.next.next;
   j++;
  }
  return result.item;
}
public void switchN(Node one, Node two) {
Node three = new Node();
three.item = one.item;
one.item = two.item;
two.item = three.item;
}
public Node getNode(int i) {
  if(i > N) throw new RuntimeException("Queue underflow");
  else {
   int j = 0;
   Node result = first;
   //result.item = first.item;   //node stayla travers lazim
   //result.next = first.next;
   while(j < i && result.next != null) {
    result = result.next;
    //result.next = result.next.next;
    j++;
   }
   return result;}
}
public int getSecond(int i) {
  Node get = getNode(i);
  if(get.resource != 0) return get.resource;
  else if(get.nOfCycle != 0) return get.nOfCycle;
  return 0;
}
public int getThird(int i) {
  Node get = getNode(i);
  if(get.nReq != 0) return get.nReq;
  else if(get.nRel != 0) return get.nRel;
  else if(get.initClaim != 0) return get.initClaim;
  return 0;
}
public int getRow(Item item) {
  Node cont=first;
  int j = 0;
  boolean contains = false;
  while(cont != null) {
   if(cont.item == item)
    break;
   cont = cont.next;
   j++;
  }
  return j;
 
}
public void eComp(Item item,int tas, int nOfCyc) { //enqueue with attributes
  Node oldlast = last;
last = new Node();
last.item = item;
last.next = null;
last.task = tas;
last.nOfCycle = nOfCyc;
if (isEmpty()) first = last;
else   
  oldlast.next = last;
N++;
}
public void eTerm(Item item, int tas) { //enqueue with attributes
  Node oldlast = last;
  last = new Node();
  last.item = item;
  last.next = null;
  last.task = tas;
  if (isEmpty()) first = last;
  else   
   oldlast.next = last;
  N++;
}
public void eInit(Item item, int tas, int resource, int initClaim) { //enqueue with attributes
  Node oldlast = last;
  last = new Node();
  last.item = item;
  last.next = null;
  last.task = tas;
  last.resource = resource;
  last.initClaim = initClaim;
  if (isEmpty()) first = last;
  else   
   oldlast.next = last;
  N++;
}
public void eRel(Item item, int tas, int resource, int nRel) { //enqueue with attributes
  Node oldlast = last;
  last = new Node();
  last.item = item;
  last.next = null;
  last.task = tas;
  last.resource = resource;
  last.nRel = nRel;
  if (isEmpty()) first = last;
  else   
   oldlast.next = last;
  N++;
}
public void eReq(Item item, int tas, int resource, int nReq) { //enqueue with attributes
  Node oldlast = last;
  last = new Node();
  last.item = item;
  last.next = null;
  last.task = tas;
  last.resource = resource;
  last.nReq = nReq;
  if (isEmpty()) first = last;
  else   
   oldlast.next = last;
  N++;
}
public Node dequeueNode() {
  if (isEmpty()) throw new RuntimeException("Queue underflow");
  Node node = first;
  Item item = first.item;
  first = first.next;
  N--;
  if (isEmpty()) last = null;   // to avoid loitering
  return node;
}
 
public void enqueueNode(Node node) {
  Node oldlast = last;
  last = new Node();
  last.item = node.item;
  last.task = node.task;
  last.resource = node.resource;
  last.nReq = node.nReq;
  last.nRel = node.nRel;
  last.initClaim = node.initClaim;
  last.nOfCycle = node.nOfCycle;
  last.leftCycle = node.leftCycle;
  last.process = node.process;
  last.frame = node.frame;
  last.next = null;
  if (isEmpty()) first = last;
  else   
   oldlast.next = last;
  N++;
}
public void arayaSok(Item item, int i, int task, int nOfCycle, int leftCycle, int resource, int nReq) {
  Node node = new Node();
  node.item = item;
  node.task = task;
  node.nOfCycle = nOfCycle;
  node.leftCycle = leftCycle;
  node.resource = resource;
  node.nReq = nReq;
  if (isEmpty()) enqueueNode(node);
  else if(N < i)
   enqueueNode(node);
  else if(i == 0 ) {
   node.next = first;
   first = node;
  }
  else {
   int j = 0;
   Node result = first;
   while(j < (i-1)) {
    result = result.next;
    j++;
   }
   N++;
   node.next = result.next;
   result.next = node;
  }
}
public void arayaKoy(Node node, int i) {
  if (isEmpty()) enqueueNode(node);
  else if(N < i)
   enqueueNode(node);
  else {
   int j = 0;
   Node result = first;
   while(j < (i-1)) {
    result = result.next;
    j++;
   }
   N++;
   node.next = result.next;
   result.next = node;
  }
}
public void enqueue(Item item) {
  Node oldlast = last;
  last = new Node();
  last.item = item;
  last.next = null;
  if (isEmpty()) first = last;
  else   
   oldlast.next = last;
  N++;
}
public boolean contains(Item item) {
  Node cont=first;
  boolean contains = false;
  while(cont != null) {
   if(cont.item == item)
    contains = true;
   cont = cont.next;
  }
  return contains;
}
public int findTask(int task) {
  Node cont=first;
  int p = 0;
  boolean contains = false;
  while(cont != null) {
   if(cont.task == task) {
    contains = true;
   break;
   }
   cont = cont.next;
   p++;
  
  }
  return p;
}
public int findProcess(int process) {
  Node cont = first;
  int p = 0;
  boolean contains = false;
  while(cont != null) {
   if(cont.process == process) {
    contains = true;
   break;
   }
   cont = cont.next;
   p++;
  
  }
  return p;
}
public int findProcessAndFrame(int process, int frame) {
  Node cont = first;
  int p = 0;
  boolean contains = false;
  while(cont != null) {
   if(cont.process == process && cont.frame == frame) {
    contains = true;
    break;
   }
   cont = cont.next;
   p++;
  
  }
  return p;
}
  public boolean containsProcessAndFrame(int process , int frame) {
  Node cont=first;
  boolean contains = false;
  while(cont != null) {
   if(cont.process == process && cont.frame == frame)
    contains = true;
   cont = cont.next;
  }
  return contains;
}
  public boolean containsProcess(int process) {
  Node cont=first;
  boolean contains = false;
  while(cont != null) {
   if(cont.process == process)
    contains = true;
   cont = cont.next;
  }
  return contains;
}
public boolean containsTask(int task) {
  Node cont=first;
  boolean contains = false;
  while(cont != null) {
   if(cont.task == task)
    contains = true;
   cont = cont.next;
  }
  return contains;
}
/**
  * Remove and return the item on the queue least recently added.
  * Throw an exception if the queue is empty.
  */
public Item dequeue() {
  if (isEmpty()) throw new RuntimeException("Queue underflow");
  Item item = first.item;
  first = first.next;
  N--;
  if (isEmpty()) last = null;   // to avoid loitering
  return item;
}
 
/**
  * Return string representation.
  */
public String toString() {
  StringBuilder s = new StringBuilder();
  for (Item item : this)
   s.append(item + " ");
  return s.toString();
}
 
public void aradanCik(int i) {
  if (isEmpty()) throw new RuntimeException("Queue underflow");
  else if(N  <= i)
   ;
  else if(i == 0) {
   dequeueNode();
}
  else if((N - 1) == i) {
   int j = 0;
   Node result = first;
   while(j < (i-1)) {    // buraya result.next != null konmali 
    result = result.next;
    j++;
   }
   N--;
   result.next = null;
  }
  else {
   int j = 0;
   Node result = first;
   while(j < (i-1)) {
    result = result.next;
    j++;
   }
   N--;
   Node node = result.next;
  
   result.next = result.next.next;
  }
 
 }
public Node aradanGetir(int i) {
  Node netice = getNode(i);
  if (isEmpty()) throw new RuntimeException("Queue underflow");
  else if(N  <= i)
   ;
  else if(i == 0) {
   dequeueNode();
}
  else if((N - 1) == i) {
   int j = 0;
   Node result = first;
   while(j < (i-1)) {    // buraya result.next != null konmali 

     result = result.next;

    j++;
   }
   N--;
            last = result;
   result.next = null;
  }
  else {
   int j = 0;
   Node result = first;
   while(j < (i-1)) {
    result = result.next;
    j++;
   }
   N--;
   Node node = result.next;
  
   result.next = result.next.next;
  }
 return netice;
 }
 
/**
  * Return an iterator that iterates over the items on the queue in FIFO order.
  */
public Iterator<Item> iterator()  {
  return new ListIterator();
 }
// an iterator, doesn't implement remove() since it's optional
public class ListIterator implements Iterator<Item> {
  public Node current = first;
 
  public boolean hasNext()  { return current != null;                     }
  public void remove()      { throw new UnsupportedOperationException();  }
 
  public Item next() {
   if (!hasNext()) throw new NoSuchElementException();
   Item item = current.item;
   current = current.next;
   return item;
  }
}
 
 
/**
  * A test client.
  */
public static void main(String[] args) {
  Queue<String> q = new Queue<String>();
 
  String item = "ananin ami"; //sonuncu elemanla oynayinca sorun oluyor
  q.enqueue(item);
  String item2 = "ananin ami2";
  q.enqueue(item2);
//  String item3 = "ananin ami3";
//  q.enqueue(item3);
  //q.kaynak("gavat");  + " "+ q.dequeue()
  //System.out.println(q.getNode(0).item + " " + q.getRow("ananin ami3"));
//  q.arayaSok(item2,0, 4,4,4,4,4);
//  q.getNode(0).task = 9;
q.getNode(1).process = 3;
q.getNode(1).frame = 3;
  //q.enqueueNode(q.getNode(0));
System.out.println(q.containsProcessAndFrame(3,3) +" " + q.findProcessAndFrame(3,3));
//  System.out.println(q.aradanGetir(q.findProcessAndFrame(3,3)).item);  // + " "+ q.getNode(3).item

  System.out.println(" frame'in yeri1 " + q.findProcessAndFrame(3,3));
            q.enqueueNode(q.aradanGetir(q.findProcessAndFrame(3,3)));
System.out.println(" frame'in yeri2 " + q.findProcessAndFrame(3,3));
  
  System.out.println("(" + q.size() + " left on queue)" + " "+ q.dequeue() );
     System.out.println("(" + q.size() + " left on queue)");
          System.out.println("(" + q.size() + " left on queue)"+ " " + q.dequeue());
 
 
  //if(q.containsTask(9))
  // System.out.println("GAVAT");
  /*
      q.enqueue2(item);
 
      q.enqueue2(item2);
      q.enqueue2(item3);
      System.out.println(q.dequeue() + " "+ q.dequeue() + " " + q.dequeue());*/
}
}
