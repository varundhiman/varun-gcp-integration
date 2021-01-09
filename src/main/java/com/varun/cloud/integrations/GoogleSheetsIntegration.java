package com.varun.cloud.integrations;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

@Component
public class GoogleSheetsIntegration {

    @Autowired
    Sheets sheetsService;

    @Autowired
    Sheets sheetsServiceAcc;

    public void executeInsert() throws IOException, GeneralSecurityException {

        final String spreadsheetId = "1kHzbEGb2v3PzLFrM0gh3J0EGQa78MNBJpvdYC626x_g";
        final String range = "Sheet9!A:F";
        final List<List<Object>> values =
                Arrays.asList(Arrays.asList(3, "Big Dude", "616-516-7589", "45455", 3000, "Frozen Pizza"));
        final ValueRange body = new ValueRange().setValues(values);
        final AppendValuesResponse result = sheetsServiceAcc.spreadsheets().values().append(spreadsheetId, range, body)
                                                            .setValueInputOption("RAW").execute();
        System.out.printf("%d cells appended.", result.getUpdates().getUpdatedCells());
    }
}
