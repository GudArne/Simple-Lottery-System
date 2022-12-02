import java.net.*;
import java.io.*;

public class Client{

    private Socket socket;
    private BufferedReader in;
    private ObjectOutputStream out;

    public Client(Socket socket) throws IOException {
        try 
        {
            this.socket = socket;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new ObjectOutputStream(socket.getOutputStream());
        } 
        catch (Exception e) 
        {
            closeClient(socket, out, in);
        }
    }

    private void closeClient(Socket socket, ObjectOutputStream out, BufferedReader reader) 
    {
        try 
        {
            if (!socket.isClosed()) 
            {
                socket.close();
            }
            if (out != null) 
            {
                out.close();
            }
            {
                out.close();
            }
            if (reader != null) 
            {
                reader.close();
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    public void sendMessage(User entry)
    {
        try 
        {
            out.writeObject(entry);

            out.flush();

            // closeClient(socket, out, in);
        }
        catch(IOException e)
        {
            // Something went wrong -> close the client
            closeClient(socket, out, in);
        }
    }

    // Always listen for messages from the server in a separate thread
    public void listenForMessages()
    {
        new Thread(new Runnable() 
        {
            @Override
            public void run() 
            {
                String msg;
                while(socket.isConnected())
                {
                    try 
                    {
                        msg = in.readLine();

                        // Never print empty messages
                        if(msg != null)
                        {
                            System.out.println(msg);
                        }
                    } 
                    catch (IOException e) 
                    {
                        // Something went wrong -> close the client
                        closeClient(socket, out, in);
                    }
                }
                
            }
        }).start();
    }


}