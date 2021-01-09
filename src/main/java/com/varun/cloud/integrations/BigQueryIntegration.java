package com.varun.cloud.integrations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.TableId;

@Component
public class BigQueryIntegration {

    @Autowired
    BigQuery bigQueryService;

    public void executeInsert() {

        final TableId tableId = TableId.of("transpro_credit", "driver_issue_credit_report");
        final Map<String, Object> rowContent = createCreditData();

        final InsertAllResponse response =
                bigQueryService.insertAll(InsertAllRequest.newBuilder(tableId).addRow(rowContent)

                                                          .build());
        if (response.hasErrors()) {
            for (final Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                System.out.println(entry);
            }
        }

    }

    public Map<String, Object> createCreditData() {
        final Map<String, Object> rowContent = new HashMap<>();
        rowContent.put("customer_name", "Starbucks");
        rowContent.put("customer_contact", "616-987-5739");
        rowContent.put("order_id", "57897");
        rowContent.put("amount", 200);
        rowContent.put("item_details", "Coffee Beans");
        return rowContent;
    }

}
