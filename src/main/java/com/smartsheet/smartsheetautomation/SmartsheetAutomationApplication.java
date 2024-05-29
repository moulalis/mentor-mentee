package com.smartsheet.smartsheetautomation;

import com.smartsheet.api.SmartsheetException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartsheetAutomationApplication {

	public static void main(String[] args) throws SmartsheetException {
		String authToken = System.getenv("ACCESS_TOKEN");
		String sheetId = System.getenv("SHEET_ID");
		UpdateSmartsheetDateCalculation dateCalculation = new UpdateSmartsheetDateCalculation(authToken,sheetId);
		dateCalculation.writeSmartsheetData();

	}

}
