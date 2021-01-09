package com.varun.cloud.integrations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts.Keys;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

@Configuration
public class CloudConfig {

    private static final String APPLICATION_NAME = "Transpro";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/api_credentials.json";

    @Autowired
    private ApplicationContext context;

    @Bean
    public BigQuery bigQueryService() throws IOException {
        GoogleCredentials credentials;
        final File credentialsPath = ResourceUtils.getFile("classpath:bigQuery_credentials.json");
        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        }
        final BigQuery bigQueryService = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
        return bigQueryService;
        // // Use the client.
        // System.out.println("Datasets:");
        // for (final Dataset dataset : bigquery.listDatasets().iterateAll()) {
        // System.out.printf("%s%n", dataset.getDatasetId().getDataset());
        // }
    }

    @Bean
    public Sheets sheetsService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final Sheets sheetsService =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getAPICredentials(HTTP_TRANSPORT))
                                                                                                   .setApplicationName(APPLICATION_NAME)
                                                                                                   .build();
        return sheetsService;
    }

    private Credential getAPICredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        final InputStream in = GoogleSheetsIntegration.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        final GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                                                                                                            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                                                                                                            .setAccessType("offline")
                                                                                                            .build();

        final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Bean
    public Sheets sheetsServiceAcc() throws GeneralSecurityException, IOException {
        System.out.println("\nIN\n");
        final Sheets sheets =
                new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                                   geServiceAccountCredentialForKeys()).setApplicationName("Google Sheets Client")
                                                                       .build();
        return sheets;
    }

    @Bean
    public Credential geServiceAccounttCredential() throws IOException {

        final InputStream is = new FileInputStream(ResourceUtils.getFile("classpath:bigQuery_credentials.json"));
        final Credential credential =
                GoogleCredential.fromStream(is).createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        return credential;
    }

    @Bean
    public Keys keyService() throws IOException, GeneralSecurityException {
        final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        final GoogleCredential credential = geServiceAccountCredentialForKeys();
        final Iam iamService =
                new Iam.Builder(httpTransport, jsonFactory, credential).setApplicationName("Transpro").build();
        System.out.println(credential.getExpiresInSeconds());
        return iamService.projects().serviceAccounts().keys();
    }

    public static GoogleCredential geServiceAccountCredentialForKeys() throws IOException {

        final InputStream is = new FileInputStream(ResourceUtils.getFile("classpath:bigQuery_credentials.json"));
        final GoogleCredential credential = GoogleCredential.fromStream(is)
                                                            .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform",
                                                                                        SheetsScopes.SPREADSHEETS));
        System.out.println(credential.getServiceAccountPrivateKeyId());
        return credential;
    }

    public static GoogleCredentials geServiceAccountCredentialWithKey() throws IOException {

        final InputStream is = new FileInputStream(ResourceUtils.getFile("classpath:key.json"));
        final GoogleCredentials credentials =
                ServiceAccountCredentials.fromStream(is)
                                         .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform",
                                                                     SheetsScopes.SPREADSHEETS));
        System.out.println(credentials.toString());
        return credentials;
    }

}
