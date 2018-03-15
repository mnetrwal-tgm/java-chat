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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    
     final int LEVEL_ERROR = 1;
     final int LEVEL_NORMAL = 0;

     public static void main(String[] args) {
             Server s = new Server();
             if (s.runServer()) {
                     s.listenToClients();
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
	               	                     System.out.println(nachricht);
	               	                     if(nachricht.equals("none")) {
	               	                    	 list_clientWriter.put(writer,"Client "+list_clientWriter.size());
	               	                    	 writer.println("Client "+list_clientWriter.size());
	               	                    	 writer.flush();
	               	                     }else {
	               	                    	 list_clientWriter.put(writer,nachricht.substring(0, nachricht.length()-2));
	               	                     }
	               	                     first=false;
                            	 	 }else {
	                            	 	 if(nachricht.equals("exit")) {
	                            	 		 writer.println("exit");
	                            	 		 writer.flush();
	                            	 		 client.close();
	                            	 		 list_clientWriter.remove(client);
	                            	 		 break;
	                            	 	 }else if(nachricht.equals("ls")) {
	                            	 		 listAllClients(writer);
	                            	 	 }else {
		                                     appendTextToConsole("Vom Client: \n" + nachricht, LEVEL_NORMAL);
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
                            
                            
                             Thread clientThread = new Thread(new ClientHandler(client));
                             clientThread.start();
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