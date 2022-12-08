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
    private int bettingSum = 0;


    public User(String id, String numbers, String email, String date) 
    {
        this.id = id;
        this.numbers = numbers;
        this.email = email;
        this.date = date;

        // Calculate betting sum
        String[] numbersArray = numbers.split(" ");
        this.bettingSum = numbersArray.length * 100;
    }

    public static void main(String[] args) throws IOException, ParseException 
    {
        int serverPort = 2000;
        InetAddress host = InetAddress.getByName("localhost"); 
        System.out.println("Connecting to server on port " + serverPort); 

        Socket socket = new Socket(host,serverPort); 
        Client client = new Client(socket);

        // Ask the user if he want to bet or retrieve historical data
        System.out.println("Do you want to bet or retrieve historical data? (b/h)");
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String answer = consoleReader.readLine();

        if (answer.equals("b")) 
        {
            // Ask the user for his ID, lottery numbers, email and date
            System.out.println("Enter user ID: ");
            String id = consoleReader.readLine();

            System.out.println("Enter lottery numbers: ");
            String numbers = consoleReader.readLine();

            System.out.println("Enter email: ");
            String email = consoleReader.readLine();

            System.out.println("Enter date (yyMMdd-hh): ");
            String date = consoleReader.readLine();

            var user = new User(id, numbers, email, date);

            client.sendMessage(user);
        }
        else if (answer.equals("h")) 
        {
            // Ask the user for his ID
            System.out.println("Enter start date (yyMMdd-hh): ");
            var startDate = new SimpleDateFormat("yyMMdd-hh").parse(consoleReader.readLine());

            System.out.println("Enter end date (yyMMdd-hh): ");
            var endDate = new SimpleDateFormat("yyMMdd-hh").parse(consoleReader.readLine());

            var lotteryHistory = new LotteryHistory();
            var result = lotteryHistory.getDrawingResults(startDate, endDate);
            lotteryHistory.printResults(result);
        }
        else 
        {
            System.out.println("Invalid input");
            return;
        }


		client.listenForMessages();
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

    public int getBettingSum() 
    {
        return bettingSum;
    }
}
