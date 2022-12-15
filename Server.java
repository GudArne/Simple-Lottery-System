import java.net.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.io.*;

public class Server {
    private ServerSocket serverSocket;
    public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    public static volatile ArrayList<LotteryPool> lotteryPools = new ArrayList<LotteryPool>();
    public static int reserveMoney = 0;

    public Server(ServerSocket serverSocket) 
    {
        this.serverSocket = serverSocket;
    }

    public void run() 
    {
        try 
        {      
            // Create a new thread for LotteryHandler. One time only.
            var lh = new LotteryHandler();
            Thread lotteryHandler = new Thread(lh);
            lotteryHandler.start();
            
            while(!serverSocket.isClosed())
            {
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "..."); 
    
                // Accept connection from client
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
    
                // Create a new thread for the client
                ClientHandler client = new ClientHandler(server);
    
                // Start the thread for the client
                Thread t = new Thread(client);
                t.start();
            }     
        }
        catch(UnknownHostException ex) 
        {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException 
    {
        Server server = new Server(new ServerSocket(2000));

		server.run();
    }

    static class LotteryHandler implements Runnable
    {
        @Override
        public void run() 
        {
            var LotteryHistory = new LotteryHistory();
            var currDate = new Date();
            System.out.println("LotteryHandler started at " + currDate.toString());
            
            while(true)
            {
                if(lotteryPools.size() > 0)
                {
                    // Find the next lottery date
                    var nextLottery = getNextLotteryDate(currDate);
                    // System.out.println("Next lottery date: " + nextLottery);

                    // Sleep the thread for 3 second
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
        
                    // Wait until the next lottery date
                    if(currDate.after(nextLottery.getDate()) && nextLottery != null)
                    {
                        // Generate a number between 0 and 255
                        int lotteryNumber = (int)(Math.random() * 10);
                        System.out.println("Lottery number: " + lotteryNumber);
        
                        // Check if there is any winner
                        var winner = checkLotteryWinner(nextLottery, lotteryNumber);
        
                        // If there is a winner, send an email to the winner
                        if(!winner.isEmpty())
                        {
                            System.out.println("Winner amount: " + winner.size());
    
                            int price = (int) Math.round((nextLottery.getPricePool() + reserveMoney) / winner.size());
                            int numWinners = 0;
                            for (User user : winner) 
                            {
                                System.out.println("Winner: " + user.getId() + " won " + price + " kr");
                                System.out.println("User with Email: " + user.getEmail() + "won");
                                numWinners++;
                            }

                            // Create a LotteryHistory object
                            var drawingResult = new DrawingResult(currDate, lotteryNumber, numWinners, price);
                            LotteryHistory.writeDataToFile(drawingResult);

                            // Remove the lottery pool, update next lottery
                            lotteryPools.remove(nextLottery);
                            nextLottery = getNextLotteryDate(currDate);
                        }
                        else
                        {
                            // No winners. Add the money to the reserve
                            reserveMoney += nextLottery.getPricePool();

                            // Remove the lottery pool, update next lottery
                            lotteryPools.remove(nextLottery);
                            nextLottery = getNextLotteryDate(currDate);

                            System.out.println("No winner");
                        }
                    }
                }
            }
        }
    }

    static class ClientHandler implements Runnable 
    {
        private Socket clientSocket;
        private ObjectInputStream in;
        private BufferedWriter out;
        private String userId;

        public ClientHandler(Socket socket) 
        {
            try 
            {
                this.clientSocket = socket;
                this.in = new ObjectInputStream(socket.getInputStream());
                this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                
                clients.add(this);
                System.out.println("Client connected");

            } 
            catch (Exception e) 
            {
                // Something went wrong with the connection -> close the clienet
                closeClient(socket, out, in);
            }
        }

        @Override
        public void run() 
        {
            boolean hasError = false;

            while(clientSocket.isConnected())
            {
                try 
                {
                    var entry = in.readObject();

                    if(entry == null)
                    {
                        continue;
                    }

                    var user = (User) entry;

                    if (user instanceof User) 
                    {
                        String[] numbersArray = user.getNumbers().split(" ");
                        for (String num : numbersArray) 
                        {
                            // Error: nummer out of bounds
                            if(Integer.parseInt(num) > 255 || Integer.parseInt(num) < 0)
                            {
                                System.out.println("Number out of bounds");
                                hasError = true;
                                break;
                            }
                            // Error: samma nummer flera gånger på samma tid.
                            hasError = checkDuplicateNumbers(numbersArray, user);
                            break;
                        }


                        // Error: Datum passerat
                        // Lägg till sleep i LotteryHandler
                        // Lägg till en lista med vinnare i en csv fil
                        // 

                        if(!hasError)
                        {
                            userId = user.getId();
                            System.out.println("User " + userId + " connected");
                            System.out.println("User " + userId + " entered numbers: " + user.getNumbers());
                            System.out.println("User " + userId + " entered email: " + user.getEmail());
                            System.out.println("User " + userId + " entered date: " + user.getDate());
                            System.out.println("User " + userId + " entered price: " + user.getBettingSum());
    
                            // Check if there is a lottery pool for the date
                            var date = user.getDate();
                            var lotteryPool = getLotteryPool(date);
    
                            lotteryPool.addUser(user);
                            lotteryPool.addMoney(user.getBettingSum());
    
                            lotteryPools.add(lotteryPool);
    
                            // Send response to client
                            out.write("Success");
                            out.flush();
                        }
                        else
                        {
                            out.write("Error");
                            out.flush();
                        }
                    }
                    closeClient(clientSocket, out, in);
                    break;
                } 
                catch (IOException | ClassNotFoundException | ParseException e) 
                {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
            }
        }
        private Boolean checkDuplicateNumbers(String[] numbersArray, User user) 
        {
            for (var pool : lotteryPools) 
            {
                for (var userInPool : pool.getUsers()) 
                {
                    if(userInPool.getId().equals(user.getId()))
                    {
                        // Add the users betting numbers to the numbersArray
                        String[] numbersInPool = userInPool.getNumbers().split(" ");
                        for (String num : numbersInPool) 
                        {
                            // Add the number to the array
                            numbersArray = Arrays.copyOf(numbersArray, numbersArray.length + 1);
                            numbersArray[numbersArray.length - 1] = num;
                        }
                        
                    }
                }
            }
            for (int i = 0; i < numbersArray.length; i++) 
            {
                for (int j = i + 1; j < numbersArray.length; j++) 
                {
                    if(numbersArray[i].equals(numbersArray[j]))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        private void closeClient(Socket socket, BufferedWriter writer, ObjectInputStream in) 
        {
            System.out.println("Closing client " + userId);
            try 
            {
                if(clients.contains(this))
                {
                    // clients.remove(this);
                }
                if (socket != null) 
                {
                    socket.close();
                }
                if (writer != null) 
                {
                    writer.close();
                }
                if (in != null) 
                {
                    in.close();
                }
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    }

    public static LotteryPool getLotteryPool(Date date) 
    {
        for (LotteryPool lotteryPool : lotteryPools) 
        {
            if(lotteryPool.getDate().equals(date))
            {
                System.out.println("Lottery pool found");
                return lotteryPool;
            }
        }
        return new LotteryPool(date);
    }

    public static ArrayList<User> checkLotteryWinner(LotteryPool lotteryPool, int lotteryNumber) 
    {
        var users = lotteryPool.getUsers();
        var winners = new ArrayList<User>();

        for (User user : users) 
        {
            // TODO: split numbers and check each number
            var numbers = user.getNumbers().split(" ");
            for (var num : numbers) 
            {
                if(num.equals(lotteryNumber + ""))
                {
                    System.out.println("Winner found: " + user.getId());
                    winners.add(user);
                    break;
                }
            }
        }
        return winners;
    }

    public static LotteryPool getNextLotteryDate(Date currDate) 
    {
        LotteryPool tempPool = null;

        // Iterate through all the lottery pools
        for (LotteryPool lotteryPool : lotteryPools) 
        {
            // Find the lottery pool with the closest date to currDate
            if (tempPool == null || lotteryPool.getDate().before(tempPool.getDate()) && lotteryPool.getDate().after(currDate))
            {
                tempPool = lotteryPool;
            }
        }
        return tempPool;
    }
}
