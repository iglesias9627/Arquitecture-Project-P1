package com.company;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    public static void main(String[] args) throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(10);
        // write your code here
        try{
            ServerSocket server = new ServerSocket(8888);
            int counter=0;
            System.out.println("Server Started ....");
            while(true){
                counter++;
                Socket serverClient = server.accept();  //server accept the client connection request
                System.out.println(" >> " + "Client No:" + counter + " on queue!");
                ThreadServer sct = new ThreadServer(serverClient, counter); //send  the request to a separate thread
                service.execute(sct);
                
            }
        }catch(Exception e){
            System.out.println("Error:" + e);
        }
    }
}
