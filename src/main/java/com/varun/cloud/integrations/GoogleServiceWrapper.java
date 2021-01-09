package com.varun.cloud.integrations;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.HOURS;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.util.ResourceUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts.Keys;
import com.google.api.services.iam.v1.model.CreateServiceAccountKeyRequest;
import com.google.api.services.iam.v1.model.ServiceAccountKey;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.SheetsScopes;

//@Component
public class GoogleServiceWrapper {

    private static final String SERVICE_ACCOUNT_NAME_FORMAT =
            "projects/gcp-gfs-transportation-sandbox/serviceAccounts/%s";

    private static final String KEY_NAME_FORMAT = "projects/gcp-gfs-transportation-sandbox/serviceAccounts/%s/keys/%s";

    private String keyPath;

    private Sheets sheetService;

    private Keys keyService;

    @PostConstruct
    public void loadSheetService() throws GeneralSecurityException, IOException {
        sheetService =
                new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                                   geServiceAccountCredentialForKeys()).setApplicationName("Google Sheets Client")
                                                                       .build();
    }

    private GoogleCredential geServiceAccountCredentialForKeys() throws IOException {

        final InputStream is = new FileInputStream(ResourceUtils.getFile("classpath:bigQuery_credentials.json"));
        final GoogleCredential credential = GoogleCredential.fromStream(is)
                                                            .createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform",
                                                                                        SheetsScopes.SPREADSHEETS));
        return credential;
    }

    @PostConstruct
    private void loadKeyService() throws IOException, GeneralSecurityException {
        final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        final GoogleCredential credential = geServiceAccountCredentialForKeys();
        final Iam iamService =
                new Iam.Builder(httpTransport, jsonFactory, credential).setApplicationName("Transpro").build();
        keyService = iamService.projects().serviceAccounts().keys();
    }

    public Spreadsheets spreadsheets() {
        return sheetService.spreadsheets();
    }

    public void refreshKeys() throws IOException {
        final GoogleCredential creds = geServiceAccountCredentialForKeys();
        final String currentKeyName = getKeyName(creds.getServiceAccountId(), creds.getServiceAccountPrivateKeyId());
        if (isAboutToExpire(currentKeyName)) {
            createNewKey(creds.getServiceAccountId());
            deleteKey(currentKeyName);
        }

    }

    private boolean isAboutToExpire(String currentKeyName) throws IOException {
        final ServiceAccountKey currentKey = keyService.get(currentKeyName).execute();
        final Instant keyExpiryTime = Instant.parse(currentKey.getValidBeforeTime());
        final Instant keyRefreshTime = keyExpiryTime.minus(50, HOURS);
        return Instant.now().isAfter(keyRefreshTime);

    }

    void createNewKey(String serviceAccountId) throws IOException {
        final CreateServiceAccountKeyRequest requestBody = new CreateServiceAccountKeyRequest();
        final ServiceAccountKey newKey =
                keyService.create(format(SERVICE_ACCOUNT_NAME_FORMAT, serviceAccountId), requestBody).execute();
        // save to a file
        System.out.println(newKey.toPrettyString());
        System.out.println(newKey.getName());

        saveKey(newKey);
    }

    void saveKey(ServiceAccountKey key) throws IOException {
        Files.write(Paths.get(keyPath), key.toPrettyString().getBytes());
    }

    private void deleteKey(String keyName) throws IOException {
        keyService.delete(keyName).execute();

    }

    private static String getKeyName(String serviceAccountId, String keyId) {
        return format(KEY_NAME_FORMAT, serviceAccountId, keyId);
    }

}
