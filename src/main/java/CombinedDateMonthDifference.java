import java.time.LocalDate;
import java.time.Period;
import java.util.Scanner;

public class CombinedDateMonthDifference {
    public static void main(String[] args) {
        // Create a Scanner object to read input
      //  Scanner scanner = new Scanner(System.in);

        // Get the current date
        LocalDate currentDate = LocalDate.of(2024,02,12);// LocalDate.now();

        // Prompt the user to input a date
        System.out.println("Enter a date (YYYY-MM-DD): ");
        String dateString = "2024-06-10";//scanner.nextLine();
        LocalDate providedDate = LocalDate.parse(dateString);

        // Calculate the difference in years, months, and days
        Period period = Period.between(currentDate, providedDate);
        int yearsDifference = period.getYears();
        int monthsDifference = period.getMonths();
        int daysDifference = period.getDays();

        // Combine years, months, and days difference into a single number
        int totalDifference = 6 - ( yearsDifference * 12 + monthsDifference + (daysDifference > 0 ? 1 : 0));


        // Print the combined difference as a single number
        System.out.println("Combined difference in years and months: " + totalDifference);

        // Close the Scanner
        //scanner.close();
    }
}
