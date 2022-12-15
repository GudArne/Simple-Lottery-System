import java.util.Date;


public class DrawingResult {
    private Date dateTime;
    private int winningNumber;
    private int numWinners;
    private int totalWinnings;
  
    public DrawingResult(Date dateTime, int winningNumber, int numWinners, int totalWinnings) 
    {
      this.dateTime = dateTime;
      this.winningNumber = winningNumber;
      this.numWinners = numWinners;
      this.totalWinnings = totalWinnings;
    }
  
    public Date getDateTime() {
      return dateTime;
    }
  
    public int getWinningNumber() {
      return winningNumber;
    }
  
    public int getNumWinners() {
      return numWinners;
    }
  
    public int getTotalWinnings() {
      return totalWinnings;
    }
  }