//Tanner Cornejo
//Used for cleaning up GE Centricity crash logs through a Citrix environment using Xenprofiles.  
//Will start searching at given base directory, find the total size of subfolders, and delete the crash log folder out of them if they are over a given size

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.List;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;

public class Xenprofile{
   
   public UserQueue selUsers;
   
   //Temp directories, should be replaced by your ini file.  If you do not have one, one will be created when you launch the program.
   private String basedir = "C:\\xenprofile\\";
   private String targetdir = "C:\\A to be deleted\\";
   //Removedir is the directory you want to remove.  It comes after your username.  Full path should be: \\share\\user\\removedir\\
   private String removedir = "\\UPM_Profile\\AppData\\Local\\Centricity\\CrashLogs\\";
   private long maxSizeMb = 30;  //Initial check size.  Can change when you launch program.
   
   //GUI elements
   private JFrame frame;
   private JPanel mainWindow, listPane, settingsPane, btnPane;
   private JTextField txt_maxSize;
   private JButton btn_go, btn_clean, btn_clear;
   private JLabel lbl_size;
   private JTable table;

   public Xenprofile(){
      //Initialize the queue and output table
      selUsers = new UserQueue<User>();
      DefaultTableModel model = new DefaultTableModel();
      model.addColumn("User");
      model.addColumn("Size (bytes)");
      table = new JTable(model);
      initSettings();
      
      //Create GUI
      frame = new JFrame("GE Log Cleaner");
      frame.setLayout(new FlowLayout());
      
      SpringLayout sl = new SpringLayout();
      mainWindow = new JPanel();
      mainWindow.setLayout(sl);
      mainWindow.setPreferredSize(new Dimension(300, 500));
      mainWindow.setBackground(Color.WHITE);
      
      settingsPane = new JPanel();
      settingsPane.setPreferredSize(new Dimension(290, 65));
      settingsPane.setBackground(Color.WHITE);
      mainWindow.add(settingsPane);
      sl.putConstraint(SpringLayout.WEST, settingsPane, 5, SpringLayout.WEST, mainWindow);
      sl.putConstraint(SpringLayout.EAST, settingsPane, -5, SpringLayout.EAST, mainWindow);
      sl.putConstraint(SpringLayout.NORTH, settingsPane, 0, SpringLayout.NORTH, mainWindow);
      
      lbl_size = new JLabel("Enter size threshold (Mb):");
      txt_maxSize = new JTextField();
      txt_maxSize.setText(Long.toString(maxSizeMb));
      txt_maxSize.setPreferredSize(new Dimension(100, 26));
      btn_go = new JButton("Search");
      
      settingsPane.add(lbl_size);
      settingsPane.add(txt_maxSize);
      settingsPane.add(btn_go);
      //Go button launches the search.  Looks at your set basedir for all subfolders and gets their name and size.  Displays in table if they are larger than maxSizeMb
      btn_go.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e){
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            selUsers.erase();
            try{
               maxSizeMb = Long.parseLong(txt_maxSize.getText());
               initUsers(selUsers);
            }catch(Exception ex){
               System.out.println(ex);
               JOptionPane.showMessageDialog(null, "Error, please enter an integer or ensure the directories are correct in your ini file.");
            }
         }
      });
      sl.putConstraint(SpringLayout.WEST, lbl_size, 5, SpringLayout.WEST, settingsPane);
      sl.putConstraint(SpringLayout.WEST, txt_maxSize, 5, SpringLayout.EAST, lbl_size);
      sl.putConstraint(SpringLayout.EAST, btn_go, 5, SpringLayout.EAST, settingsPane);
      
      listPane = new JPanel();
      listPane.setBackground(Color.WHITE);
      listPane.setPreferredSize(new Dimension(290, 325));
      SpringLayout sl3 = new SpringLayout();
      listPane.setLayout(sl3);
      mainWindow.add(listPane);
      sl.putConstraint(SpringLayout.WEST, listPane, 5, SpringLayout.WEST, mainWindow);
      sl.putConstraint(SpringLayout.EAST, listPane, -5, SpringLayout.EAST, mainWindow);
      sl.putConstraint(SpringLayout.NORTH, listPane, 5, SpringLayout.SOUTH, settingsPane);
      JScrollPane sc = new JScrollPane(table);
      sc.setPreferredSize(new Dimension(290, 320));
      listPane.add(sc);
      sl3.putConstraint(SpringLayout.WEST, sc, 5, SpringLayout.WEST, listPane);
      sl3.putConstraint(SpringLayout.EAST, sc, 5, SpringLayout.EAST, listPane);
      sl3.putConstraint(SpringLayout.NORTH, sc, 5, SpringLayout.NORTH, listPane);
      
      
      btnPane = new JPanel();
      btnPane.setPreferredSize(new Dimension(290, 30));
      mainWindow.add(btnPane);
      sl.putConstraint(SpringLayout.WEST, btnPane, 10, SpringLayout.WEST, mainWindow);
      sl.putConstraint(SpringLayout.EAST, btnPane, -10, SpringLayout.EAST, mainWindow);
      sl.putConstraint(SpringLayout.NORTH, btnPane, 5, SpringLayout.SOUTH, listPane);
      sl.putConstraint(SpringLayout.SOUTH, btnPane, -5, SpringLayout.SOUTH, mainWindow);
      
      SpringLayout sl2 = new SpringLayout();
      btnPane.setLayout(sl2);
      btnPane.setBackground(Color.WHITE);

      btn_clean = new JButton("Clean Selected");
      btn_clear = new JButton("Clear Selection");
      btnPane.add(btn_clear);
      //Clears the selection from queue and table
      btn_clear.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e){
            selUsers.erase();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            selUsers.erase();
         }
      });
      btnPane.add(btn_clean);
      //Removes removedir from the selected folders
      btn_clean.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e){
            processUsers(selUsers);
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            selUsers.erase();
         }
      });
      
      sl2.putConstraint(SpringLayout.NORTH, btn_clear, 5, SpringLayout.NORTH, btnPane);
      sl2.putConstraint(SpringLayout.WEST, btn_clear, 5, SpringLayout.WEST, btnPane);
      sl2.putConstraint(SpringLayout.NORTH, btn_clean, 5, SpringLayout.NORTH, btnPane);
      sl2.putConstraint(SpringLayout.EAST, btn_clean, -5, SpringLayout.EAST, btnPane);

      frame.add(mainWindow);
      frame.setVisible(true);
      frame.setSize(300,500);
      frame.setResizable(true);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
   public void initSettings(){
      //Imports the settings
      File settings = new File("xenprofile.ini");
      List lines;
      //Attempts to read the settings.  Will fail if not configured correctly
      try{
         lines = FileUtils.readLines(settings);
         basedir = ((String)(lines.get(0))).split(": ", 2)[1];
         targetdir = ((String)(lines.get(1))).split(": ", 2)[1];
      }catch(Exception e){ //If ini file does not exist or is not configured correctly, this will create a sample ini file.
         JOptionPane.showMessageDialog(null, "Error cannot open ini file.  Creating sample ini file now...");
         try{
            FileWriter f = new FileWriter("xenprofile.ini");
            BufferedWriter out = new BufferedWriter(f);
            out.write("basedir: C:\\xenprofile\\");
            out.newLine();
            out.write("targetdir: C:\\A to be deleted\\");
            out.close();
            return;
         }catch(IOException e2){
            JOptionPane.showMessageDialog(null, "Error creating sample ini");
         }   
      }
   }
   //Looks at all immediate subfolders of basedir and adds them to the queue if they are larger than maxSizeMb
   public void initUsers(UserQueue q){
      long dirSize;
      File file = new File(basedir); 
      File t;     
      String[] dirs = file.list();  //Lists subfolders of basedir
      for(String name : dirs){      //for each subfolder...
         t = new File(basedir + name + removedir);
         if(t.isDirectory()){
            dirSize = getDirSize(t, 0);         //Get size of folder
            if(dirSize > maxSizeMb * 1000000){  //If it's larger than maxSizeMb
               q.enqueue(new User(name, dirSize));          //Add to queue
               DefaultTableModel model = (DefaultTableModel) table.getModel();
               model.addRow(new Object[]{name, dirSize});   //Add to table
            }
         }
      }
   }
   //Processes the queue and deletes all of the queued folders
   public void processUsers(UserQueue q){
      User v;
      File src, trg, del;
      while(!q.isEmpty()){
         v = (User)q.dequeue();
         src = new File(basedir + v.getDir());
         trg = new File(targetdir);
         try{
            FileUtils.copyDirectoryToDirectory(src, trg);   //Copy the logs to your backup target set at targetdir in the ini
         }catch(IOException e){
            JOptionPane.showMessageDialog(null, "Error moving files.");
         }
         try{
            del = new File(basedir + v.getDir() + removedir);  //Try to delete the files.  Does not delete directories that are not empty so it is recursive.
         }catch(Exception e){
         
         }
      }
   }
   //Deletes the directory at the given path
   static public boolean deleteDirectory(File path){
      if(path.exists()){
         File[] files = path.listFiles(); //for each file inside the path
         for(int i = 0; i < files.length; i++){
            if(files[i].isDirectory()){   //If it's a directory, search that one recursively
               deleteDirectory(files[i]);
            }else{                        //Else delete the file
               files[i].delete();
            }
         }
      }
      return(path.delete());  //When all subfolders and files are deleted, delete the current folder
   }
   
   //Get the given directory size and return it
   public long getDirSize(File dir, long length){
      for(File file : dir.listFiles()){   //For each file in the directory
         if(file.isFile()){               //If it's a file
            length += file.length();      //Add the length
         }else{                                 //Else
            length = getDirSize(file, length);  //Search that folder recursively and return the length
         }
      }
      return length;
   }
   
   //Start the application
   public static void main(String args[]){
      Xenprofile app = new Xenprofile();
   }
}