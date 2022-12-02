import java.net.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.io.*;
import java.util.List;

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
            // Create a new thread for LotterHandler. One time only.
            Thread lotteryHandler = new Thread(new LotteryHandler());
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
            var currDate = new Date();
            
            while(lotteryPools.size() > 0)
            {
                // Find the next lottery date
                var nextLottery = getNextLotteryDate(currDate);
                System.out.println("Next lottery date: " + nextLottery);
    
                // Wait until the next lottery date
                if(currDate.after(nextLottery.getDate()))
                {
                    // Generate a number between 0 and 255
                    int lotteryNumber = (int)(Math.random() * 4);
                    System.out.println("Lottery number: " + lotteryNumber);
    
                    // Check if there is any winner
                    var winner = checkLotteryWinner(nextLottery, lotteryNumber);
    
                    // If there is a winner, send an email to the winner
                    if(winner != null)
                    {
                        System.out.println("Winner amount: " + winner.size());

                        float price = (nextLottery.getPricePool() + reserveMoney) / winner.size();
                        for (User user : winner) 
                        {
                            System.out.println("Winner: " + user.getId() + " won " + price + " kr");
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

                        // TODO: Error check, send response to client

                        userId = user.getId();
                        System.out.println("User " + userId + " connected");
                        System.out.println("User " + userId + " entered numbers: " + user.getNumbers());
                        System.out.println("User " + userId + " entered email: " + user.getEmail());
                        System.out.println("User " + userId + " entered date: " + user.getDate());

                        // Check if there is a lottery pool for the date
                        var date = user.getDate();
                        var lotteryPool = getLotteryPool(date);

                        lotteryPool.addUser(user);
                        lotteryPool.addMoney(100);
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
            if(user.getNumbers().contains(lotteryNumber + ""))
            {
                System.out.println("Winner found: " + user.getId());
                winners.add(user);
            }
        }
        return winners;
    }

    public static LotteryPool getNextLotteryDate(Date currDate) 
    {
        var tempPool = lotteryPools.get(0);

        // Iterate through all the lottery pools
        for (LotteryPool lotteryPool : lotteryPools) 
        {
            if(tempPool.getDate().after(lotteryPool.getDate()))
            {
                tempPool = lotteryPool;
            }
        }
        return tempPool;
    }
}
