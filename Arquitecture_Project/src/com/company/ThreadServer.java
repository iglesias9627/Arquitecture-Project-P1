package com.company;

import java.io.*;
import java.net.Socket;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Scanner;

public class ThreadServer extends Thread{
    Socket serverClient;
    int clientNo;

    ThreadServer(Socket inSocket,int counter){
        serverClient = inSocket;
        clientNo=counter;
    }

    public static Request readRequest(DataInputStream in) throws IOException {
        byte [] hashPwd = new byte[20];
        int count = in.read(hashPwd,0, 20);
        if (count < 0){
            throw new IOException("Server could not read from the stream");
        }
        int pwdLength = in.readInt();
        long fileLength = in.readLong();

        return new Request(hashPwd, pwdLength, fileLength);
    }

    public static byte[] hashSHA1(String data) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(data.getBytes());
    }

    public static String convertmillis(long input) {
		int days = 0, hours = 0, minutes = 0, seconds = 0, millis = 0;
			        
		int day = 86400000;
		int hour = 3600000;
		int minute = 60000;
		int second = 1000;
			    
			       
		if(input >= day) {
		     days = (int) (input / day);
		     millis = (int) (input % day);
		} else 
			millis = (int) input;
			           
		if(millis >= hour) {
		     hours = millis / hour;
		     millis = millis% hour;
		}
			       
		if(millis >= minute) {
			 minutes = millis / minute;
			 millis = millis % minute;
		}
		
		if(millis >= second) {
			seconds = millis / second;
			millis = millis % second;
		}
			      
		return (days  + " day(s), " + hours + "h, " + minutes + "min, " + seconds + "s and " + millis + "ms");
	}

    public static boolean apply_improved_bruteForce(Request request, File  networkFile, File decryptedFile) throws IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException {
        StringBuilder string = new StringBuilder("");
        int min = 97, max = 123;
        long start;
        
        boolean result=false;
        start = System.currentTimeMillis();
        File passwordFile = new File("10k-most-common_filered.txt");
        int lengthpwdFile = (int)passwordFile.length();
        //System.out.println("len: "+lengthpwdFile);
        Scanner scanner = new Scanner(passwordFile);
        
        while (scanner.hasNextLine()) {
               // process the line
               String password = scanner.nextLine();
               //password="master";
               try {
                    byte[] hashPwd = hashSHA1(password);
                    if(Arrays.equals(hashPwd, request.getHashPassword())){
                        //System.out.println("Password matched: "+password);
                        SecretKey serverKey = CryptoUtils.getKeyFromPassword(password);
                        CryptoUtils.decryptFile(serverKey, networkFile, decryptedFile);
                        scanner.close();
                        //System.err.println("It took: " + convertmillis(System.currentTimeMillis() - start));
                        return true;
                    }
                    
            
                } catch (Exception e) {
                    //TODO: handle exception
                    System.out.println("Password Error: "+password);
                    
                }
        }
        scanner.close();
        //System.out.println("All combinations BruteForce");
        while(true) {
			string.append((char) min);
            //System.out.println("len string: "+string.length());
			for(int i = 0; i < string.length() - 1; i++) {
				for(int j = min; j  < max; j++) {
					string.setCharAt(i, (char) j);
					result=loop_bruteForce(i+1, min, max, string, request, networkFile, decryptedFile);
                    if(result==true){
                        //System.out.println("SUCCESS");
                        //System.err.println("SUCCESS, It took: " + convertmillis(System.currentTimeMillis() - start));
                        return true;
                    }
				}

			}
           
		}
          
    
    }

    
    public static boolean loop_bruteForce(int index, int min, int max, StringBuilder string, Request request, File  networkFile, File decryptedFile) throws InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeySpecException {
		for(int i = min; i < max; i++) {
			string.setCharAt(index, (char) i);
			if(index < string.length() - 1)
				loop_bruteForce(index + 1, min ,max ,string, request, networkFile, decryptedFile);
			
            if(string.toString().length()==request.getLengthPwd()){
                //System.out.println(string);
                try {
                    byte[] hashPwd = hashSHA1(string.toString());
                    //System.out.println("Password found: "+string.toString());
                    if((Arrays.equals(hashPwd, request.getHashPassword()))){
                        //System.out.println("Password matched: "+string.toString());
                        SecretKey serverKey = CryptoUtils.getKeyFromPassword(string.toString());
                        CryptoUtils.decryptFile(serverKey, networkFile, decryptedFile);
                        return true;
                    }
                    
                } catch (Exception e) {
                    //TODO: handle exception
                    //System.out.println("Password Error: "+string.toString());
                }
            
                    
            }
		}
        return false;
	}
    

    public static boolean apply_bruteForce(Request request, File  networkFile, File decryptedFile) throws IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException {
        StringBuilder string = new StringBuilder("");
        int min = 97, max = 123;
        long start;
        
        boolean result=false;
        start = System.currentTimeMillis();
        while(true) {
			string.append((char) min);
            //System.out.println("len string: "+string.length());
			for(int i = 0; i < string.length() - 1; i++) {
				for(int j = min; j  < max; j++) {
					string.setCharAt(i, (char) j);
					result=loop_bruteForce(i+1, min, max, string, request, networkFile, decryptedFile);
                    if(result==true){
                        //System.out.println("SUCCESS");
                        //System.err.println("SUCCESS, It took: " + convertmillis(System.currentTimeMillis() - start));
                        return true;
                    }
				}

			}
           
		}


    }

    public void run(){
        try{
            File decryptedFile = new File("test_file-decrypted-server"+clientNo+".pdf");
            File networkFile = new File("temp-server"+clientNo+".pdf");
            System.out.println("Connection from: " + this.serverClient);
            InputStream inputStream = this.serverClient.getInputStream();

            //Stream to read request from socket
            DataInputStream dataInputStream = new DataInputStream(serverClient.getInputStream());
            //Stream to write response to socket
            DataOutputStream outSocket = new DataOutputStream(serverClient.getOutputStream());

            // Stream to write the file to decrypt
            OutputStream outFile = new FileOutputStream(networkFile);

            Request request = readRequest(dataInputStream);
            long fileLength = request.getLengthFile();

            FileManagement.receiveFile(inputStream, outFile, fileLength);
            System.out.println("File length: " + networkFile.length());

            boolean resultDecrypt=apply_improved_bruteForce(request,networkFile, decryptedFile);
            //System.out.println("Decrypt: "+resultDecrypt);
            
            // Send the decryptedFile
            InputStream inDecrypted = new FileInputStream(decryptedFile);
            outSocket.writeLong(decryptedFile.length());
            outSocket.flush();
            FileManagement.sendFile(inDecrypted, outSocket);
            
            dataInputStream.close();
            inputStream.close();
            inDecrypted.close();
            outFile.close();
            outSocket.close();
            this.serverClient.close();

        
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            System.out.println("Client #" + clientNo + " exit!! ");
        }
    }
}
