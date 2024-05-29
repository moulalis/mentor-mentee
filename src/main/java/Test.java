import com.smartsheet.api.*;
import com.smartsheet.api.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Test {
    // Replace with your access token
    private static final String ACCESS_TOKEN = "J5rqrMyzhgHWdCx4HqFNDr6QTaXqYLrfs5iOZ";
    private static final long SHEET_ID = 4108344956505988L; // Replace with your sheet ID

    // Column IDs
    private static final long MENTOR_OR_MENTEE_COLUMN_ID = 4997607435751300L;
    private static final long MENTEE_SKILLS_COLUMN_ID = 2745807622066052L;
    private static final long MENTOR_SKILLS_COLUMN_ID = 1619907715223428L;
    private static final long MENTEE_MATCHED_COLUMN_ID = 8375307156279172L;

    private static final long MENTOR_MATCHED_COLUMN_ID = 1056957761802116L;
    private static final long ASSOCIATE_NAME_COLUMN_ID = 1901382691934084L;
    private static final long ASSOCIATE_EMAIL_COLUMN_ID = 6404982319304580L;

    public static void main(String[] args) {
        Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken(ACCESS_TOKEN).build();

        try {
            Sheet sheet = smartsheet.sheetResources().getSheet(SHEET_ID, null, null, null, null, null, null, null);
            Map<Long, Row> rows = new HashMap<>();
            for (Row row : sheet.getRows()) {
                rows.put(row.getId(), row);
            }

            Map<Long, Set<String>> mentors = new HashMap<>();
            Map<Long, Set<String>> mentees = new HashMap<>();

            for (Row row : sheet.getRows()) {
                String role = getCellValue(row, MENTOR_OR_MENTEE_COLUMN_ID);
                if ("Mentor".equalsIgnoreCase(role)) {
                    mentors.put(row.getId(), new HashSet<>(Arrays.asList(getCellValue(row, MENTOR_SKILLS_COLUMN_ID).split(","))));
                } else if ("Mentee".equalsIgnoreCase(role)) {
                    mentees.put(row.getId(), new HashSet<>(Arrays.asList(getCellValue(row, MENTEE_SKILLS_COLUMN_ID).split(","))));
                }
            }

            // Match mentors with mentees
            Map<Long, List<String>> mentorMenteesMap = new HashMap<>();
            for (Map.Entry<Long, Set<String>> mentorEntry : mentors.entrySet()) {
                for (Map.Entry<Long, Set<String>> menteeEntry : mentees.entrySet()) {
                    if (hasCommonSkills(mentorEntry.getValue(), menteeEntry.getValue()) || mentorEntry.getValue().contains(menteeEntry.getValue())) {
                        Long mentorRowId = mentorEntry.getKey();
                        Long menteeRowId = menteeEntry.getKey();

                        Row menteeRow = rows.get(menteeRowId);
                        String menteeName = getCellValue(menteeRow, ASSOCIATE_NAME_COLUMN_ID);
                       // String menteeEmail = getCellValue(menteeRow, ASSOCIATE_EMAIL_COLUMN_ID);
                        String menteeDetails = menteeName ;

                        mentorMenteesMap
                                .computeIfAbsent(mentorRowId, k -> new ArrayList<>())
                                .add(menteeDetails);
                    }
                }
            }
            // Update mentors with matched mentees
            for (Map.Entry<Long, List<String>> entry : mentorMenteesMap.entrySet()) {
                Long mentorRowId = entry.getKey();
                List<String> matchedMentees = entry.getValue();
                String menteeDetails = String.join("; ", matchedMentees);
                updateMenteeMatchedColumn(smartsheet, mentorRowId, menteeDetails,MENTEE_MATCHED_COLUMN_ID);
            }

            // Match mentee with mentor
            Map<Long, List<String>> menteesMentorMap = new HashMap<>();
            for (Map.Entry<Long, Set<String>> menteeEntry : mentees.entrySet()) {
                for (Map.Entry<Long, Set<String>> mentorEntry : mentors.entrySet()) {
                    if (hasCommonSkills(menteeEntry.getValue(), mentorEntry.getValue()) || menteeEntry.getValue().contains(mentorEntry.getValue())) {
                        Long menteeRowId = menteeEntry.getKey();
                        Long mentorRowId = mentorEntry.getKey();

                        Row mentorRow = rows.get(mentorRowId);
                        String mentorName = getCellValue(mentorRow, ASSOCIATE_NAME_COLUMN_ID);
                        // String menteeEmail = getCellValue(menteeRow, ASSOCIATE_EMAIL_COLUMN_ID);
                        String mentorDetails = mentorName ;

                        menteesMentorMap
                                .computeIfAbsent(menteeRowId, k -> new ArrayList<>())
                                .add(mentorDetails);
                    }
                }
            }
            // Update mentee with matched mentors
            for (Map.Entry<Long, List<String>> entry : menteesMentorMap.entrySet()) {
                Long menteeRowId = entry.getKey();
                List<String> matchedMentors = entry.getValue();
                String mentorDetails = String.join("; ", matchedMentors);
                updateMenteeMatchedColumn(smartsheet, menteeRowId, mentorDetails,MENTOR_MATCHED_COLUMN_ID);
            }



        } catch (SmartsheetException e) {
            e.printStackTrace();
        }
    }

    private static String getCellValue(Row row, long columnId) {
        for (Cell cell : row.getCells()) {
            if (cell.getColumnId() == columnId) {
                return cell.getDisplayValue();
            }
        }
        return null;
    }

    private static void updateMenteeMatchedColumn(Smartsheet smartsheet, long rowId, String menteeDetails, long columnId) {
        try {
            Cell cellToUpdate = new Cell();
            cellToUpdate.setColumnId(columnId);
            cellToUpdate.setValue(menteeDetails);

            Row rowToUpdate = new Row();
            rowToUpdate.setId(rowId);
            rowToUpdate.setCells(List.of(cellToUpdate));

            smartsheet.sheetResources().rowResources().updateRows(SHEET_ID, List.of(rowToUpdate));
        } catch (SmartsheetException e) {
            e.printStackTrace();
        }
    }
    private static boolean hasCommonSkills(Set<String> mentorSkills, Set<String> menteeSkills) {
        for (String skill : mentorSkills) {
            if (menteeSkills.contains(skill.trim())) {
                return true;
            }
        }
        return false;
    }
}
