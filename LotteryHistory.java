import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LotteryHistory 
{
    public List<DrawingResult> getDrawingResults(Date start, Date end) throws ParseException, FileNotFoundException 
    {
        // Retrieve the historical data for the specified time period

        List<DrawingResult> results = new ArrayList<DrawingResult>();
        
        var scanner = new Scanner(new File("History.csv"));
        scanner.useDelimiter(",");
        while (scanner.hasNext()) 
        {
            var line = scanner.nextLine();
            var tempDate = line.split(",")[0];
            var strDate = "";

            int month = getMonthNumber(tempDate.substring(4, 7));
            strDate = tempDate.substring(8, 19) + tempDate.substring(23, 28) + " " + month;

            var date = new SimpleDateFormat("dd HH:mm:ss yyyy MM", Locale.getDefault(Locale.Category.FORMAT)).parse(strDate);

            if (date.after(start) && date.before(end)) 
            {
                //var winningDate = line.split(",")
                var winningNumber = Integer.parseInt(line.split(",")[1]);
                var numWinners = Integer.parseInt(line.split(",")[2]);
                var totalWinnings = Integer.parseInt(line.split(",")[3]);

                var result = new DrawingResult(date, winningNumber, numWinners, totalWinnings);
                results.add(result);
            }
        }
        scanner.close();
        return results;
    }
    public void writeDataToFile(DrawingResult result)
    {
        // Write the results to the CSV file
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter("History.csv"));
            writer.write(String.format("%s,%d,%d,%d\n", result.getDateTime(), result.getWinningNumber(),
                result.getNumWinners(), result.getTotalWinnings()));

            writer.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    // Print the results to the console
    public void printResults(List<DrawingResult> results) 
    {
        for (DrawingResult result : results) 
        {
            System.out.println(result.getDateTime() + " " + result.getWinningNumber() + " " + result.getNumWinners() + " " + result.getTotalWinnings());
        }
    }

    private int getMonthNumber(String month) {
        int monthNumber;
    
        // Use a switch statement to map the month abbreviations to their corresponding numbers
        switch (month.toUpperCase()) {
            case "JAN":
                monthNumber = 1;
                break;
            case "FEB":
                monthNumber = 2;
                break;
            case "MAR":
                monthNumber = 3;
                break;
            case "APR":
                monthNumber = 4;
                break;
            case "MAY":
                monthNumber = 5;
                break;
            case "JUN":
                monthNumber = 6;
                break;
            case "JUL":
                monthNumber = 7;
                break;
            case "AUG":
                monthNumber = 8;
                break;
            case "SEP":
                monthNumber = 9;
                break;
            case "OCT":
                monthNumber = 10;
                break;
            case "NOV":
                monthNumber = 11;
                break;
            case "DEC":
                monthNumber = 12;
                break;
            default:
                monthNumber = 0;
                break;
        }
        return monthNumber;
    }
}


  