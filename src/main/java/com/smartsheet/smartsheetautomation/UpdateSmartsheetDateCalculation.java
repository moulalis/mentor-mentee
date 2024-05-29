package com.smartsheet.smartsheetautomation;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UpdateSmartsheetDateCalculation {
    public static final String RHOAI = "RHOAI";
    public static final String SPRINT_STARTS = "Sprint Starts";
    public static final String FEATURE_FREEZ = "Feature Freeze";
    public static final String CODE_FREEZ = "Code Freeze";
    public static final String RC_AVAIL_FOR_QE = "RC available for QE";
    public static final String PUSH_TO_STAGE = "Push to Stage (Cloud Service)";
    public static final String RELEASE_NOTES_FINAL = "Release Notes Final";
    public static final String ERRATA_IN_REL_PREP = "Errata in REL_PREP";
    public static final String GA = "GA";
    public static final String PUSH_TO_PROD = "Push to Prod (Cloud Service)";
    public static final String LIVE_TESTING_COMP = "Live testing completed";
    public static final String CSS_CONTENT_PUBLISH = "CCS content published";

    public final String EMPTY_SPACE = " ";

    public String version = "2.7";

    Map<String, String> resultData = new LinkedHashMap<String, String>();
    String accessToken;
    long sheetId;

    Sheet sheet;

    List<Cell> Cells;
    Smartsheet smartsheet;
    Properties properties;

    Map<String, String> rowMap = new LinkedHashMap<String, String>();

    public UpdateSmartsheetDateCalculation(String authToken, String id) throws SmartsheetException {

        properties = getProperties();
        accessToken = (authToken==null || authToken.equals("")) ? properties.getProperty("accessToken"):authToken;
        sheetId = Long.parseLong((id==null || id.equals("")) ? properties.getProperty("sheetId"):id);
        smartsheet = new SmartsheetBuilder().setAccessToken(accessToken).build();
        sheet = smartsheet.sheetResources().getSheet(sheetId, null, null, null, null, null, null, null);
        Cells = SmartsheetDateUtil.getRowData(sheet);
        loadCellDataToMap(Cells, loadColumnDataToMap(sheet));
        version = calculateNextVersion(SmartsheetDateUtil.getVersion(rowMap.get("Task Name")));


    }

    public String calculateNextVersion(String version) {

        String plusVersion = "0.1";

        String[] nextVersion = version.split("\\.");
        String[] decimal = plusVersion.split("\\.");

        // Parse the decimal part
        int decimalPart = Integer.parseInt(nextVersion[1]) + Integer.parseInt(decimal[1]); // Use radix 10

        return nextVersion[0] + "." + decimalPart;
    }

    public Cell addCell(Sheet sheet, String cellValue, int index) {
        Cell cell = new Cell();
        cell.setColumnId(sheet.getColumns().get(index).getId()); // Replace column ID
        cell.setValue(cellValue);
        return cell;
    }

    private Row createRow(Long parentId, List<Cell> cells) {
        Row row = new Row();
        row.setParentId(parentId);
        row.setCells(cells);
        return row;
    }

    public Row addsmartsheetdata(Sheet sheet, List<Row> rows) throws SmartsheetException {
        List<Row> createdChildRows = null;
        if (sheet != null) {
            createdChildRows = smartsheet.sheetResources().rowResources().addRows(sheetId, rows);

            System.out.println("New row added successfully to the sheet with subcolumn values.");
        } else {
            System.out.println("Subcolumn not found by title.");
        }

        return createdChildRows.get(0);
    }

    public void addcommentdata(Sheet sheet, Row rowToUpdate, String value) throws SmartsheetException {
        List<Row> createdChildRows = null;
        if (sheet != null) {
            try {
                // Modify the specific cell value in the fetched row data
                List<Cell> cells = rowToUpdate.getCells();
                Long columnId = sheet.getColumns().get(0).getId();
                for (Cell cell : cells) {
                    if (cell.getColumnId().equals(columnId)) {
                        cell.setValue(value);
                        break;
                    }
                }
                Row.UpdateRowBuilder updateRowBuilder = new Row.UpdateRowBuilder();
                List<Row> rowsToUpdate = new ArrayList<>();
                rowsToUpdate.add(rowToUpdate);

                // Update the row with the modified cell value
                smartsheet.sheetResources().rowResources().updateRows(sheetId, rowsToUpdate);
            } catch (SmartsheetException e) {
                e.printStackTrace();
            }

        }
    }

    public void updateRow(HashMap<Row, String> rowStringHashMap) throws SmartsheetException {

        Iterator<Map.Entry<Row, String>> iterator = rowStringHashMap.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<Row, String> entry = iterator.next();

            addcommentdata(sheet, entry.getKey(), entry.getValue());

        }


    }

    private Properties getProperties() {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }


    public long writeSmartsheetData() throws SmartsheetException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        Map<String, String> planMap;

        Map<String, String> dateMap = new LinkedHashMap<String, String>();
        Row parentrow;
        int numbDays = 0;
        try {
            // Add 7 days to the current date
            LocalDate futureDate;
            // String providedDate = properties.get("providedDate").equals("")?rowMap.get("Start"):properties.get("providedDate");//"2023-12-13T08:00:00";//
            String providedDate = (properties.get("providedDate") != null && !properties.get("providedDate").equals(""))
                    ? properties.get("providedDate").toString()
                    : rowMap.get("Start");
            // String finishDate = rowMap.get("Finish");// Example date "2024-01-20T08:00:00";

            String finishDate = (properties.get("finishDate") != null && !properties.get("finishDate").equals(""))
                    ? properties.get("finishDate").toString()
                    : rowMap.get("Start");
            LocalDateTime localDateTime = LocalDateTime.parse(providedDate);
            LocalDateTime finishDateTime = LocalDateTime.parse(finishDate);
            // Define the desired date format
            DateTimeFormatter dformatter = DateTimeFormatter.ofPattern("dd-MM-yy");


            String formattedDate;
            formattedDate = localDateTime.format(dformatter);
            System.out.println("Formatted date: " + formattedDate);
            // Parse the string to a LocalDate object using the defined formatter
            int quarter = 0;
            int i;
            Sheet sheet = smartsheet.sheetResources().getSheet(sheetId, null, null, null, null, null, null, null);

            numbDays = getDateDifference(finishDateTime.format(dformatter), dformatter, properties);
            List<Row> rowsToAdd;
            if (sheet != null) {
                for (i = 0; i < numbDays; i++) {

                    rowsToAdd = new ArrayList<>();
                    String GADate;
                    String CSDate;

                    LocalDate localDateq = LocalDate.parse(formattedDate, dformatter);
                    localDateq = getNextMonday(getDatesWithoutWeekends(localDateq, 25));
                    Month month = localDateq.getMonth();
                    int t = quarter;
                    quarter = (month.getValue() - 1) / 3 + 1;

                    if (t != quarter) {
                        System.out.println("############### " + "Q" + quarter + "-" + localDateq.getYear() + " ############");
                        List<Cell> cell1 = new ArrayList<>();
                        Cell cell = addCell(sheet, "Q" + quarter + "-" + localDateq.getYear(), 1);
                        cell1.add(cell);
                        List<Row> quarterRows = new ArrayList<>();
                        quarterRows.add(createRow(null, cell1));
                        addsmartsheetdata(sheet, quarterRows);

                    }

                    List<Cell> cell3 = new ArrayList<>();
                    cell3.add(addCell(sheet, version + EMPTY_SPACE + SPRINT_STARTS, 1));
                    cell3.add(addCell(sheet, "15d", 2));
                    cell3.add(addCell(sheet, localDateq.format(DateTimeFormatter.ISO_DATE), 3));
                    //cells.add(addCell(sheet, localDateq.format(DateTimeFormatter.ISO_DATE),4));


                    futureDate = localDateq.plusDays(14);
                    LocalDate nextFriday = getNextFriday(futureDate);
                    System.out.println(version + EMPTY_SPACE + FEATURE_FREEZ + nextFriday.format(dformatter));

                    List<Cell> cell4 = new ArrayList<>();
                    cell4.add(addCell(sheet, version + EMPTY_SPACE + FEATURE_FREEZ, 1));
                    cell4.add(addCell(sheet, "0d", 2));
                    cell4.add(addCell(sheet, nextFriday.format(DateTimeFormatter.ISO_DATE), 3));


                    futureDate = nextFriday.plusDays(7);
                    nextFriday = getNextFriday(futureDate);
                    System.out.println(version + " Code Freeze: " + nextFriday.format(dformatter));

                    List<Cell> cell5 = new ArrayList<>();
                    cell5.add(addCell(sheet, version + EMPTY_SPACE + CODE_FREEZ, 1));
                    cell5.add(addCell(sheet, "0d", 2));
                    cell5.add(addCell(sheet, nextFriday.format(DateTimeFormatter.ISO_DATE), 3));


                    LocalDate nextMonday = getNextMonday(nextFriday);
                    System.out.println(version + " RC Available for QE: " + nextMonday.format(dformatter));

                    List<Cell> cell6 = new ArrayList<>();
                    cell6.add(addCell(sheet, version + EMPTY_SPACE + RC_AVAIL_FOR_QE, 1));
                    cell6.add(addCell(sheet, "0d", 2));
                    cell6.add(addCell(sheet, nextMonday.format(DateTimeFormatter.ISO_DATE), 3));


                    futureDate = getDatesWithoutWeekends(nextMonday, 7);
                    System.out.println(version + " Push to Stage (Cloud Service) " + futureDate.format(dformatter));


                    List<Cell> cell7 = new ArrayList<>();
                    cell7.add(addCell(sheet, "estimate", 0));
                    cell7.add(addCell(sheet, version + EMPTY_SPACE + PUSH_TO_STAGE, 1));
                    cell7.add(addCell(sheet, "0d", 2));
                    cell7.add(addCell(sheet, futureDate.format(DateTimeFormatter.ISO_DATE), 3));


                    //futureDate = getDatesWithoutWeekends(futureDate, 4);
                    System.out.println(version + " Release Notes Final " + futureDate.format(dformatter));


                    List<Cell> cell8 = new ArrayList<>();
                    cell8.add(addCell(sheet, version + EMPTY_SPACE + RELEASE_NOTES_FINAL, 1));
                    cell8.add(addCell(sheet, "0d", 2));
                    cell8.add(addCell(sheet, futureDate.format(DateTimeFormatter.ISO_DATE), 3));


                    futureDate = getDatesWithoutWeekends(futureDate, 3); //getNextFriday(futureDate);
                    System.out.println(version + EMPTY_SPACE + ERRATA_IN_REL_PREP + futureDate.format(dformatter));


                    List<Cell> cell9 = new ArrayList<>();
                    cell9.add(addCell(sheet, version + EMPTY_SPACE + ERRATA_IN_REL_PREP, 1));
                    cell9.add(addCell(sheet, "0d", 2));
                    cell9.add(addCell(sheet, futureDate.format(DateTimeFormatter.ISO_DATE), 3));


                    System.out.println(version + " GA " + futureDate.format(dformatter));


                    List<Cell> cell10 = new ArrayList<>();
                    cell10.add(addCell(sheet, version + EMPTY_SPACE + GA, 1));
                    cell10.add(addCell(sheet, "0d", 2));
                    GADate = futureDate.format(DateTimeFormatter.ISO_DATE);
                    cell10.add(addCell(sheet, GADate, 3));


                    futureDate = getDatesWithoutWeekends(futureDate, 2);
                    System.out.println(version + EMPTY_SPACE + PUSH_TO_PROD + futureDate.format(dformatter));


                    List<Cell> cell11 = new ArrayList<>();
                    cell11.add(addCell(sheet, version + EMPTY_SPACE + PUSH_TO_PROD, 1));
                    cell11.add(addCell(sheet, "1d", 2));
                    CSDate = futureDate.format(DateTimeFormatter.ISO_DATE);
                    cell11.add(addCell(sheet, CSDate, 3));


                    System.out.println(version + " Live testing completed " + futureDate.format(dformatter));


                    List<Cell> cell12 = new ArrayList<>();
                    cell12.add(addCell(sheet, version + EMPTY_SPACE + LIVE_TESTING_COMP, 1));
                    cell12.add(addCell(sheet, "0.5d", 2));
                    cell12.add(addCell(sheet, futureDate.format(DateTimeFormatter.ISO_DATE), 3));


                    System.out.println(version + EMPTY_SPACE + CSS_CONTENT_PUBLISH + futureDate.format(dformatter));


                    List<Cell> cell13 = new ArrayList<>();
                    cell13.add(addCell(sheet, version + EMPTY_SPACE + CSS_CONTENT_PUBLISH, 1));
                    cell13.add(addCell(sheet, "0.5d", 2));
                    cell13.add(addCell(sheet, futureDate.format(DateTimeFormatter.ISO_DATE), 3));


                    System.out.println(version + EMPTY_SPACE + RHOAI + localDateq.format(dformatter));
                    List<Cell> cell2 = new ArrayList<>();
                    System.out.println(version + EMPTY_SPACE + RHOAI);
                    cell2.add(addCell(sheet, "Planned GA dates " + SmartsheetDateUtil.getMonthDate(GADate) + " (SM); " + SmartsheetDateUtil.getMonthDate(CSDate) + " (CS)", 0));

                    cell2.add(addCell(sheet, version + EMPTY_SPACE + RHOAI, 1));
                    cell2.add(addCell(sheet, "21d", 2));
                    cell2.add(addCell(sheet, localDateq.format(DateTimeFormatter.ISO_DATE), 3));
                    List<Row> parentRows = new ArrayList<>();
                    parentRows.add(createRow(null, cell2));
                    parentrow = addsmartsheetdata(sheet, parentRows);


                    rowsToAdd.add(createRow(parentrow.getId(), cell3));
                    rowsToAdd.add(createRow(parentrow.getId(), cell4));
                    rowsToAdd.add(createRow(parentrow.getId(), cell5));
                    rowsToAdd.add(createRow(parentrow.getId(), cell6));
                    rowsToAdd.add(createRow(parentrow.getId(), cell7));
                    rowsToAdd.add(createRow(parentrow.getId(), cell8));
                    rowsToAdd.add(createRow(parentrow.getId(), cell9));
                    rowsToAdd.add(createRow(parentrow.getId(), cell10));
                    rowsToAdd.add(createRow(parentrow.getId(), cell11));
                    rowsToAdd.add(createRow(parentrow.getId(), cell12));
                    rowsToAdd.add(createRow(parentrow.getId(), cell13));

                    addsmartsheetdata(sheet, rowsToAdd);

                    System.out.println("########################################################");

                    LocalDate startDate = LocalDate.parse(formattedDate, dformatter);
                    formattedDate = getDatesWithoutWeekends(startDate, 20).format(dformatter);

                    version = calculateNextVersion(version);


                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return numbDays;
    }

    private int getDateDifference(String formattedDate, DateTimeFormatter formatter, Properties properties) {

        LocalDate currentDate =  LocalDate.now();//LocalDate.of(2024,02,26);

        // Prompt the user to input a date
        //String dateString = "2024-06-10";//scanner.nextLine();
        LocalDate providedDate = LocalDate.parse(formattedDate, formatter);

        // Calculate the difference in years, months, and days
        Period period = Period.between(currentDate, providedDate);
        int yearsDifference = period.getYears();
        int monthsDifference = period.getMonths();
        int daysDifference = period.getDays();

        // Combine years, months, and days difference into a single number
        int totalDifference = 6 - ( yearsDifference * 12 + monthsDifference + (daysDifference > 0 ? 1 : 0));

        return totalDifference;
    }

    public LocalDate getNextFriday(LocalDate date) {
        // Parse the provided date to LocalDate

        // Calculate the day difference to the next Friday (Friday is DayOfWeek.FRIDAY, which has a value of 5)
        int daysToAdd = (DayOfWeek.FRIDAY.getValue() - date.getDayOfWeek().getValue() + 7) % 7;

        // Get the next Friday from the provided date
        return date.plusDays(daysToAdd);


    }

    public LocalDate getNextMonday(LocalDate date) {
        // Parse the provided date to LocalDate

        // Calculate the day difference to the next Friday (Friday is DayOfWeek.FRIDAY, which has a value of 5)
        int daysToAdd = (DayOfWeek.MONDAY.getValue() - date.getDayOfWeek().getValue() + 7) % 7;

        // Get the next Friday from the provided date
        return date.plusDays(daysToAdd);


    }

    public LocalDate getDatesWithoutWeekends(LocalDate currentDate, int numberOfDates) {

        // Define the number of dates you want to retrieve (for example, 10 days)
        int datesCount = 0;

        LocalDate futureDate = currentDate.plusDays(numberOfDates);

        // Loop to find and print dates excluding weekends
        while (datesCount <= 1) {
            if ((futureDate.getDayOfWeek() == DayOfWeek.SATURDAY || futureDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                futureDate = futureDate.plusDays(1);
            }
            datesCount++;
        }
        return futureDate;
    }

    public LocalDate getNextFridayOrMondayFromDate(LocalDate currentDate) {

        // Calculate the day differences to the next Friday and the next Monday
        int daysToAddToFriday = (DayOfWeek.FRIDAY.getValue() - currentDate.getDayOfWeek().getValue() + 7) % 7;
        int daysToAddToMonday = (DayOfWeek.MONDAY.getValue() - currentDate.getDayOfWeek().getValue() + 7) % 7;

        // Get the next Friday and the next Monday from the provided date
        LocalDate nextFriday = currentDate.plusDays(daysToAddToFriday);
        LocalDate nextMonday = currentDate.plusDays(daysToAddToMonday);

        // Choose either nextFriday or nextMonday based on your conditions
        LocalDate futureDate;
        if (nextFriday.isBefore(nextMonday)) {
            futureDate = nextFriday.plusDays(7); // If nextFriday is before nextMonday, add 7 days to nextFriday
        } else {
            futureDate = nextMonday.plusDays(7); // Otherwise, add 7 days to nextMonday
        }

        return futureDate;
    }

    private void loadCellDataToMap(List<Cell> cells, Map<String, String> columnMap) {

        for (Cell cell : cells) {

            System.out.println("   Cell Data - Column ID: " + cell.getColumnId() + ", Value: " + cell.getValue());

            rowMap.put(columnMap.get(String.valueOf(cell.getColumnId())), String.valueOf(cell.getValue()));

        }
    }

    private Map<String, String> loadColumnDataToMap(Sheet sheet) {
        Map<String, String> columnMap = new LinkedHashMap<String, String>();
        List<Column> columns = sheet.getColumns();

        for (Column column : columns) {
            System.out.println(column.getId() + " : " + column.getTitle());
            columnMap.put(String.valueOf(column.getId()), column.getTitle());
        }
        return columnMap;
    }


}
