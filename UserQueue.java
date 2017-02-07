import java.io.*;
import javax.swing.*;

public class UserQueue<ItemType>{
   
   //data node constructor
   private class Node{
      ItemType value;
      Node next;
      Node(ItemType val, Node n){
         value = val;
         next = n;
      }
   }
   
   
   private Node front, rear;   
   public UserQueue(){   //queue constructor
      front = null;
      rear = null;            
   }
   
   public void enqueue(ItemType val){  //enqueue entry time
      if(rear == null){
         rear = new Node(val, null);
         front = rear;
      }else{
         rear.next = new Node(val, null);
         rear = rear.next;
      }
   }
   public ItemType dequeue(){  //dequeue entry time
      ItemType val = front.value;
      front = front.next;
      if(front == null)
         rear = null;
      return val;
   }
   
   public boolean isEmpty(){  //test if queue is empty
      return front == null;
   }
   
   public int getQueueSize(){ //get size of queue
      int count = 0;
      Node t = this.front;
      while(t != null){
         count++;
         t = t.next;
      }
      
      return count;
   }
   public void toFile(String f){
      PrintWriter out;
      Node t = this.front;
      try{
         out = new PrintWriter(f);
         while(t != null){
            out.println(t.value.toString());
            t = t.next;
         }
      }catch(FileNotFoundException e){
         System.out.println(e);
         JOptionPane.showMessageDialog(null, "File not found.");
      }
   }
   public void erase(){
      this.front = null;
      this.rear = null;
   }
}