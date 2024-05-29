import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.models.Cell;
import com.smartsheet.api.models.Column;
import com.smartsheet.api.models.Row;
import com.smartsheet.api.models.Sheet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MentorMenteeMatcher {
    private static final String ACCESS_TOKEN = "J5rqrMyzhgHWdCx4HqFNDr6QTaXqYLrfs5iOZ";
    private static final long MENTORS_SHEET_ID = 4108344956505988L; // Replace with your sheet ID
    private static final long MENTEES_SHEET_ID = 6954244242362244L; // Replace with your sheet ID
    private static final long RESULTS_SHEET_ID = 6808858257215364L; // Replace with your sheet ID

    public static void main(String[] args) throws Exception {
        Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(ACCESS_TOKEN).build();

        List<Mentor> mentors = loadMentors(smartsheet, MENTORS_SHEET_ID);
       // List<Mentee> mentees = loadMentees(smartsheet, MENTEES_SHEET_ID);

       // Map<Mentor, List<Mentee>> matches = match_MentorsAndMentees(mentors, mentees);

      //  writeMatches(smartsheet, RESULTS_SHEET_ID, matches);


    }

    private static List<Mentor> loadMentors(Smartsheet smartsheet, long sheetId) throws Exception {
        Sheet sheet = smartsheet.sheetResources().getSheet(sheetId, null, null, null, null, null, null, null);
        List<Mentor> mentors = new ArrayList<>();

        ArrayList COLUMN_Name = new ArrayList<>(); // Replace with your sheet ID

        for (Column column : sheet.getColumns()) {
            System.out.println("Column Title: " + column.getTitle() + ", Column ID: " + column.getId());
            COLUMN_Name.add(column.getTitle());
        }


       /* for (Row row : sheet.getRows()) {
            String name = null;
            List<String> expertise = new ArrayList<>();
            int i=0;
            for (Cell cell : row.getCells()) {

                if ("Name".equals(COLUMN_Name.get(i))) {
                    name = cell.getDisplayValue();
                }
                else if ("Expertise".equals(COLUMN_Name.get(i))) {
                    if(cell.getDisplayValue()!=null){
                        expertise = Arrays.asList(cell.getDisplayValue().split(","));
                    }

                }
                i++;

            }
            if (name != null) {
                mentors.add(new Mentor(name, expertise));
            }

        }*/

        return mentors;
    }

    private static List<Mentee> loadMentees(Smartsheet smartsheet, long sheetId) throws Exception {
        Sheet sheet = smartsheet.sheetResources().getSheet(sheetId, null, null, null, null, null, null, null);
        List<Mentee> mentees = new ArrayList<>();

        List columnlist = new ArrayList();

        for (Column column : sheet.getColumns()) {
            columnlist.add(column.getTitle());
        }

        for (Row row : sheet.getRows()) {
            String name = null;
            List<String> interests = new ArrayList<>();
            int i=0;
            for (Cell cell : row.getCells()) {

                if ("Name".equals(columnlist.get(i))) {
                    name = cell.getDisplayValue();
                }
                else if ("Interests".equals(columnlist.get(i))) {
                    if(cell.getDisplayValue()!=null){
                        interests = Arrays.asList(cell.getDisplayValue().split(","));
                    }
                }
                i++;
            }

            if (name != null) {
                mentees.add(new Mentee(name, interests));
            }

        }

        return mentees;
    }

    public static Map<Mentee, List<Mentor>> matchMentorsAndMentees(List<Mentor> mentors, List<Mentee> mentees) {
        Map<Mentee, List<Mentor>> matches = new HashMap<>();

        for (Mentee mentee : mentees) {
            List<Mentor> matchedMentors = new ArrayList<>();
            for (Mentor mentor : mentors) {
                for (String interest : mentee.interests) {
                    if (mentor.expertise.contains(interest)) {
                        matchedMentors.add(mentor);
                        break;
                    }
                }
            }
            matches.put(mentee, matchedMentors);
        }

        return matches;
    }


    public static Map<Mentor, List<Mentee>> match_MentorsAndMentees(List<Mentor> mentors, List<Mentee> mentees) {

        Map<Mentor, List<Mentee>> matches = new HashMap<>();

        for (Mentor mentor : mentors) {
            List<Mentee> matchedMentors = new ArrayList<>();
            for (Mentee mentee : mentees) {
                for (String interest : mentor.expertise) {
                    if (mentee.interests.contains(interest)) {
                        matchedMentors.add(mentee);
                        break;
                    }
                }
            }
            matches.put(mentor, matchedMentors);
        }

        return matches;
    }


    private static void writeMatches(Smartsheet smartsheet, long sheetId, Map<Mentor, List<Mentee>> matches) throws Exception {
        List<Row> rowsToUpdate = new ArrayList<>();
        Sheet sheet = smartsheet.sheetResources().getSheet(sheetId, null, null, null, null, null, null, null);
        ArrayList<Long> COLUMN_ID = new ArrayList<>(); // Replace with your sheet ID
       //  long COLUMN_ID2 ; // Replace with your sheet ID

        for (Column column : sheet.getColumns()) {
            System.out.println("Column Title: " + column.getTitle() + ", Column ID: " + column.getId());
            COLUMN_ID.add(column.getId());
        }

        for (Map.Entry<Mentor, List<Mentee>> entry : matches.entrySet()) {
            Mentor mentor = entry.getKey();
            List<Mentee> matchedMentors = entry.getValue();

            StringBuilder mentorsString = new StringBuilder();
            for (Mentee mentee : matchedMentors) {
                if (mentorsString.length() > 0) {
                    mentorsString.append(", ");
                }
                mentorsString.append(mentee.name);
            }

            Row row = new Row();
            row.setToBottom(true);
            row.setCells(Arrays.asList(
                    new Cell().setColumnId(COLUMN_ID.get(0)).setValue(mentor.name), // Replace with your column ID
                    new Cell().setColumnId(COLUMN_ID.get(1)).setValue(mentorsString.toString()) // Replace with your column ID
            ));

            rowsToUpdate.add(row);
        }

        smartsheet.sheetResources().rowResources().addRows(sheetId, rowsToUpdate);
    }
}

class Mentor {
    String name;
    List<String> expertise;

    Mentor(String name, List<String> expertise) {
        this.name = name;
        this.expertise = expertise;
    }
}

class Mentee {
    String name;
    List<String> interests;

    Mentee(String name, List<String> interests) {
        this.name = name;
        this.interests = interests;
    }
}
