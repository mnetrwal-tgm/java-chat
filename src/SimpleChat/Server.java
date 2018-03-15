package SimpleChat;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;

public class Server {

     ServerSocket server;
     Map<PrintWriter,String> list_clientWriter;
     ArrayList <Thread> clientThread=new ArrayList<Thread>();
    
     final int LEVEL_ERROR = 1;
     final int LEVEL_NORMAL = 0;

     public static void main(String[] args) {
             Server s = new Server();
             if (s.runServer()) {
            	
    		    // start a new thread
    		   Thread t = new Thread(new Runnable() {
    		        public void run() {
    		            try {
    		            	s.listenToClients();

    		            } catch (Exception e) {
    		                e.printStackTrace();
    		            }
    		        }
    		    });
    		   t.start();
    		   Scanner scanner = new Scanner(System.in);
    		   System.err.print("Type '/help' to see options. Else interact to your liking\n");
    		   while (true) {
    			   String scannerin = scanner.next();
    			   if(scannerin.equals("/exit")) {
    				   s.sendToAllClients("exit");
    				   t.interrupt();
    				   Iterator i = s.clientThread.iterator();
    				   while(i.hasNext()) {
    					   t=(Thread)i.next();
    					   t.interrupt();
    				   }
    				   System.exit(0);
    				   break;
    			   }else if(scannerin.equals("/ls")){
    				   System.out.println(s.listAllClients());
    			   }else if(scannerin.equals("/help")){
    				   System.err.println("/help ... shows this page\n/exit ... closes the server and disconnects clients\n/ls ... lists all active clients");
    			   }else{
    				   s.sendToAllClients("<SERVER> "+scannerin);
    				   System.out.println("<SERVER> "+scannerin);
    			   }
    		   }
             } else {
                     // Do nothing
             }
     }
    
     public class ClientHandler implements Runnable {

             Socket client;
             BufferedReader reader;
            
             public ClientHandler(Socket client) {
                     try {
                             this.client = client;
                             reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                             
                     } catch (IOException e) {
                             e.printStackTrace();
                     }
             }
            
             @Override
             public void run() {
                     String nachricht;
                     
                    

                     boolean first=true;
                     try {
                    	 PrintWriter writer = new PrintWriter(client.getOutputStream());
                             while((nachricht = reader.readLine()) != null) {
                            	 	 if (first) {
	               	                     if(nachricht.equals("none")) {
	               	                    	 list_clientWriter.put(writer,"Client "+(list_clientWriter.size()+1));
	               	                    	 writer.println("Client "+list_clientWriter.size());
	               	                    	 writer.flush();
	               	                     }else {
	               	                    	 list_clientWriter.put(writer,nachricht.substring(0, nachricht.length()-2));
	               	                     }
	               	                     first=false;
                            	 	 }else {
	                            	 	 if(nachricht.equals("exit")) {
	                            	 		 sendToAllClients("\n"+list_clientWriter.get(writer)+" disconnected.\n");
	                            	 		 writer.println("exit");
	                            	 		 writer.flush();
	                            	 		 client.close();
	                            	 		 list_clientWriter.remove(writer);
	                            	 		 break;
	                            	 	 }else if(nachricht.equals("ls")) {
	                            	 		 listAllClients(writer);
	                            	 	 }else {
		                                     appendTextToConsole(nachricht, LEVEL_NORMAL);
		                                     sendToAllClients(nachricht);
		                                     
	                            	 	 }
	                            	}
                             }
                     } catch (IOException e) {
                             e.printStackTrace();
                     }
             }
     }
    
     public void listenToClients() {
             while(true) {
                     try {
                             Socket client = server.accept();
                            
                             Thread t=new Thread(new ClientHandler(client));
                             t.start();
                             clientThread.add(t);
                             
                     } catch (IOException e) {
                             e.printStackTrace();
                     }              
             }
     }

     public boolean runServer() {
             try {
                     server = new ServerSocket(5050);
                     appendTextToConsole("Server wurde gestartet!", LEVEL_ERROR);
                    
                     list_clientWriter = new HashMap<PrintWriter,String>();
                     return true;
             } catch (IOException e) {
                     appendTextToConsole("Server konnte nicht gestartet werden!", LEVEL_ERROR);
                     e.printStackTrace();
                     return false;
             }
     }
    
     public void appendTextToConsole(String message, int level) {
             if(level == LEVEL_ERROR) {
                     System.err.println(message + "\n");
             } else {
                     System.out.println(message + "\n");
             }
     }
    
     public void listAllClients(PrintWriter specwriter) {
         Iterator it = list_clientWriter.entrySet().iterator();
         String message ="\n";
         while(it.hasNext()) {
        	 	 Map.Entry pair = (Map.Entry) it.next();
                 message=message+pair.getValue()+"\n";
         }
         specwriter.println(message);
         specwriter.flush();
 }
     
     
     public String listAllClients() {
         Iterator it = list_clientWriter.entrySet().iterator();
         String message ="\n";
         while(it.hasNext()) {
        	 	 Map.Entry pair = (Map.Entry) it.next();
                 message=message+pair.getValue()+"\n";
         }
         return message;
 }
     
     public void sendToAllClients(String message) {
         Iterator it = list_clientWriter.entrySet().iterator();
        
         while(it.hasNext()) {
        	 	 Map.Entry pair = (Map.Entry) it.next();
                 PrintWriter writer = (PrintWriter) pair.getKey();
                 writer.println(message);
                 writer.flush();
         }
 }
}