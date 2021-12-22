package com.company;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;



public class ThreadClient extends Thread{

    /**
     * This function hashes a string with the SHA-1 algorithm
     * @param data The string to hash
     * @return An array of 20 bytes which is the hash of the string
     */
    int clientNo;
    ThreadClient(int counter){
        clientNo=counter;
    }
    public static byte[] hashSHA1(String data) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(data.getBytes());
    }

    /**
     * This function is used by a client to send the information needed by the server to process the file
     * @param out Socket stream connected to the server where the data are written
     * @param hashPwd SHA-1 hash of the password used to derive the key of the encryption
     * @param pwdLength Length of the clear password
     * @param fileLength Length of the encrypted file
     */
    public static void sendRequest(DataOutputStream out, byte[] hashPwd, int pwdLength,
                                   long fileLength) throws IOException {
        out.write(hashPwd,0, 20);
        out.writeInt(pwdLength);
        out.writeLong(fileLength);
    }

    public static String getRandomPassword() throws IOException {
        String password="";
        try {
            Path pathFile =Paths.get("10k-most-common_filered.txt");
            int lines = (int) Files.lines(pathFile).count();
            //System.out.println("len: "+lines);
            
            while(password.length()!=4){
                int randomNum = ThreadLocalRandom.current().nextInt(0, lines-1);
                password = Files.readAllLines(pathFile).get(randomNum);//439//10
            }
            
            //System.out.println("pwd line: "+password);
            //System.out.println("pwd len: "+password.length());
            //myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return password;
    }

    @Override
    public void run() {
    //public static void main(String[] args) {
        try{
            //String password = "test";
            String password = getRandomPassword();
            SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);

            File inputFile = new File("test_file.pdf");
            //System.out.println(new File("test_file.pdf").exists());
            File encryptedFile = new File("test_file-encrypted-client"+clientNo+".pdf");
            File decryptedClient = new File("test_file-decrypted-client"+clientNo+".pdf");

            // This is an example to help you create your request
            CryptoUtils.encryptFile(keyGenerated, inputFile, encryptedFile);
            System.out.println("Encrypted file length: " + encryptedFile.length());


            // Creating socket to connect to server (in this example it runs on the localhost on port 3333)
            //Socket socket = new Socket("localhost", 3333);
            //Socket socket = new Socket("192.168.43.57", 3333);
            Socket socket = new Socket("127.0.0.1",8888);

            // For any I/O operations, a stream is needed where the data are read from or written to. Depending on
            // where the data must be sent to or received from, different kind of stream are used.
            OutputStream outSocket = socket.getOutputStream();
            DataOutputStream out = new DataOutputStream(outSocket);
            InputStream inFile = new FileInputStream(encryptedFile);
            DataInputStream inSocket = new DataInputStream(socket.getInputStream());


            // SEND THE PROCESSING INFORMATION AND FILE
            byte[] hashPwd = hashSHA1(password);
            hashPwd = hashSHA1(password);
            //System.out.print("SHA1: "+Arrays.toString(hashPwd));
            //int pwdLength = 4;
            int pwdLength = password.length();
            long fileLength = encryptedFile.length();
            
            sendRequest(out, hashPwd, pwdLength, fileLength);
            out.flush();

            FileManagement.sendFile(inFile, out);
      

            // GET THE RESPONSE FROM THE SERVER
            OutputStream outFile = new FileOutputStream(decryptedClient);
            long fileLengthServer = inSocket.readLong();
            System.out.println("Length from the server: "+ fileLengthServer);
            FileManagement.receiveFile(inSocket, outFile, fileLengthServer);
            
            out.close();
            outSocket.close();
            outFile.close();
            inFile.close();
            inSocket.close();
            socket.close();
            

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException |
                NoSuchPaddingException | IllegalBlockSizeException | IOException | BadPaddingException |
                InvalidKeyException e) {
            e.printStackTrace();
        }

    }
}

