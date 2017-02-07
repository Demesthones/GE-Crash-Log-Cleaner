

public class User{
   
   private String dir;
   private long logSize;

   public User(){
      dir = "";
      logSize = 0;
   }
   public User(String n, long d){
      dir = n;
      logSize = d;
   }
   public void addLogSize(long size){
      logSize += size;
   }
   public long getLogSize(){
      return logSize;
   }
   public String getDir(){
      return dir;
   }
}