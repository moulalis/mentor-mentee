import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.models.*;

import java.util.*;

public class MentorMenteeMatcher1 {
    private static final String ACCESS_TOKEN = "YOUR_ACCESS_TOKEN";
    private static final long SHEET_ID = 123456789L; // Replace with your sheet ID

    // Replace with actual column IDs
    private static final long MENTOR_NAME_COLUMN_ID = 111111L;
    private static final long MENTOR_EXPERTISE_COLUMN_ID = 222222L;
    private static final long MENTEE_NAME_COLUMN_ID = 333333L;
    private static final long MENTEE_INTERESTS_COLUMN_ID = 444444L;
    private static final long MATCHED_MENTOR_COLUMN_ID = 555555L;

    public static void main(String[] args) throws Exception {
        Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(ACCESS_TOKEN).build();

        // Load all rows from the sheet
        List<Row> rows = loadSheetRows(smartsheet, SHEET_ID);

        // Extract mentors and mentees information
        Map<String, List<String>> mentors = new HashMap<>();
        Map<String, List<String>> mentees = new HashMap<>();
        extractMentorsAndMentees(rows, mentors, mentees);

        // Match mentors to mentees
        Map<String, List<String>> matches = matchMentorsAndMentees(mentors, mentees);

        // Update the sheet with the matched pairs
        updateSheetWithMatches(smartsheet, SHEET_ID, rows, matches);
    }

    private static List<Row> loadSheetRows(Smartsheet smartsheet, long sheetId) throws Exception {
        Sheet sheet = smartsheet.sheetResources().getSheet(sheetId, null, null, null, null, null, null, null);
        return sheet.getRows();
    }

    private static void extractMentorsAndMentees(List<Row> rows, Map<String, List<String>> mentors, Map<String, List<String>> mentees) {
        for (Row row : rows) {
            String mentorName = null;
            List<String> mentorExpertise = new ArrayList<>();
            String menteeName = null;
            List<String> menteeInterests = new ArrayList<>();

            for (Cell cell : row.getCells()) {
                if (cell.getColumnId() == MENTOR_NAME_COLUMN_ID) {
                    mentorName = cell.getDisplayValue();
                } else if (cell.getColumnId() == MENTOR_EXPERTISE_COLUMN_ID) {
                    mentorExpertise = Arrays.asList(cell.getDisplayValue().split(","));
                } else if (cell.getColumnId() == MENTEE_NAME_COLUMN_ID) {
                    menteeName = cell.getDisplayValue();
                } else if (cell.getColumnId() == MENTEE_INTERESTS_COLUMN_ID) {
                    menteeInterests = Arrays.asList(cell.getDisplayValue().split(","));
                }
            }

            if (mentorName != null && !mentorExpertise.isEmpty()) {
                mentors.put(mentorName, mentorExpertise);
            }
            if (menteeName != null && !menteeInterests.isEmpty()) {
                mentees.put(menteeName, menteeInterests);
            }
        }
    }

    private static Map<String, List<String>> matchMentorsAndMentees(Map<String, List<String>> mentors, Map<String, List<String>> mentees) {
        Map<String, List<String>> matches = new HashMap<>();

        for (Map.Entry<String, List<String>> mentee : mentees.entrySet()) {
            List<String> matchedMentors = new ArrayList<>();
            for (Map.Entry<String, List<String>> mentor : mentors.entrySet()) {
                for (String interest : mentee.getValue()) {
                    if (mentor.getValue().contains(interest)) {
                        matchedMentors.add(mentor.getKey());
                        break;
                    }
                }
            }
            matches.put(mentee.getKey(), matchedMentors);
        }

        return matches;
    }

    private static void updateSheetWithMatches(Smartsheet smartsheet, long sheetId, List<Row> rows, Map<String, List<String>> matches) throws Exception {
        List<Row> rowsToUpdate = new ArrayList<>();

        for (Row row : rows) {
            String menteeName = null;
            for (Cell cell : row.getCells()) {
                if (cell.getColumnId() == MENTEE_NAME_COLUMN_ID) {
                    menteeName = cell.getDisplayValue();
                    break;
                }
            }

            if (menteeName != null && matches.containsKey(menteeName)) {
                StringBuilder mentorsString = new StringBuilder();
                for (String mentor : matches.get(menteeName)) {
                    if (mentorsString.length() > 0) {
                        mentorsString.append(", ");
                    }
                    mentorsString.append(mentor);
                }

                Row updatedRow = new Row();
                updatedRow.setId(row.getId());
                updatedRow.setCells(Arrays.asList(
                        new Cell().setColumnId(MATCHED_MENTOR_COLUMN_ID).setValue(mentorsString.toString())
                ));

                rowsToUpdate.add(updatedRow);
            }
        }

        if (!rowsToUpdate.isEmpty()) {
            smartsheet.sheetResources().rowResources().updateRows(sheetId, rowsToUpdate);
        }
    }
}
