package com.varun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.varun.cloud.integrations.BigQueryIntegration;
import com.varun.cloud.integrations.GoogleSheetsIntegration;
import com.varun.cloud.integrations.RefreshKey;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, })
public class App implements ApplicationRunner {

    @Autowired
    BigQueryIntegration bigQueryIntegration;

    @Autowired
    GoogleSheetsIntegration googleSheetsIntegration;

    @Autowired
    RefreshKey refreshKey;

    public static void main(final String[] args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);
        ctx.close();
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        System.out.println("Batch Started");
        // bigQueryIntegration.executeInsert();
        googleSheetsIntegration.executeInsert();
        refreshKey.refreshKey();

    }
}
