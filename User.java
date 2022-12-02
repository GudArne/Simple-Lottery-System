import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User implements Serializable
{
    private String id;
    private String numbers;
    private String date;
    private String email;


    public User(String id, String numbers, String email, String date) 
    {
        this.id = id;
        this.numbers = numbers;
        this.email = email;
        this.date = date;
    }


    public static void main(String[] args) throws IOException 
    {
        int serverPort = 2000;
        InetAddress host = InetAddress.getByName("localhost"); 
        System.out.println("Connecting to server on port " + serverPort); 

        Socket socket = new Socket(host,serverPort); 
        Client client = new Client(socket);

        System.out.println("Enter user ID: ");
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String id = consoleReader.readLine();

        System.out.println("Enter lottery numbers: ");
        String numbers = consoleReader.readLine();

        System.out.println("Enter email: ");
        String email = consoleReader.readLine();

        System.out.println("Enter date (yyMMdd-hh): ");
        String date = consoleReader.readLine();

        var user = new User(id, numbers, email, date);


        client.sendMessage(user);
		// client.listenForMessages();
    }


    public String getId() 
    {
        return id;
    }


    public String getNumbers() 
    {
        return numbers;
    }


    public String getEmail() 
    {
        return email;
    }


    public Date getDate() throws ParseException 
    {
        // Convert date string to Date object
        return new SimpleDateFormat("yyMMdd-hh").parse(date);
    }
}
