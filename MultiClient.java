import java.io.*; 
import java.net.*; 
import java.util.Scanner; 
  
public class MultiClientTest  
{ 
    private Socket client;
    private boolean quitd = false;
    private String send = "";
    private Scanner input;
    private PrintWriter output;
    private Scanner stdin;
  
    public static void main(String args[]){  MultiClientTest user = new MultiClientTest("localhost", 5000);  }   

        public MultiClientTest(String ip, int port)
        {
            try{
        client = new Socket(ip, port);  
        input = new Scanner(System.in);
        output = new PrintWriter(client.getOutputStream(), true);
        stdin = new Scanner(client.getInputStream());
        new Thread(new readMessage()).start();
        new Thread(new sendMessage()).start();

    }catch(Exception e){ e.printStackTrace();}

 }

        //these two threads will handle messages being sent and received simultaneously.
        private class sendMessage implements Runnable{  
            @Override
            public void run() {
                      try{
                        // write on the output stream 
                        while(!send.equals("Exit")) {
                       // while(true){
                            send = input.nextLine();
                            output.println(send);
                    }

                    }catch (Exception e) { e.printStackTrace(); } 
                 
                try{
            quitd = true;
            client.close();
            input.close();
           // read.close();
        }catch(IOException e){ e.printStackTrace(); }
    }
}         
        // readMessage thread 
        private class readMessage implements Runnable{  
            @Override
            public void run() { 
                while (quitd == false) { 
                    try { 
                        var response = stdin.nextLine();
                        System.out.println(response);
                    }catch (Exception e) { System.out.println("Connection Ended. Thanks for playing!"); } 
            }
        }
    } 
}