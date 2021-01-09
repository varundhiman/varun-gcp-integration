package com.varun.cloud.integrations;

import static java.lang.String.format;
import static java.time.Instant.parse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts.Keys;
import com.google.api.services.iam.v1.model.CreateServiceAccountKeyRequest;
import com.google.api.services.iam.v1.model.ServiceAccountKey;
import com.google.api.services.sheets.v4.Sheets;

@Component
public class RefreshKey {

    private static final String SERVICE_ACCOUNT_NAME =
            "projects/gcp-gfs-transportation-sandbox/serviceAccounts/116763978621484921311";

    private static final String KEY_NAME_FORMAT = "projects/gcp-gfs-transportation-sandbox/serviceAccounts/%s/keys/%s";

    @Autowired
    public Keys keyService;

    @Autowired
    Sheets sheetsServiceAcc;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    Credential geServiceAccounttCredential;

    public void refreshKey() throws IOException, GeneralSecurityException {

        showCurrentKey();
        createNewKey();
        listAllKeys();
        deleteLatestKey();
        listAllKeys();

    }

    private void showCurrentKey() throws IOException {
        final GoogleCredential creds = CloudConfig.geServiceAccountCredentialForKeys();
        // final GoogleCredentials newCreds = CloudConfig.geServiceAccountCredentialWithKey();
        final String currentKeyName = getKeyName(creds.getServiceAccountId(), creds.getServiceAccountPrivateKeyId());
        final ServiceAccountKey key = keyService.get(currentKeyName).execute();
        System.out.println(key.toPrettyString());

    }

    private static String getKeyName(String serviceAccountId, String keyId) {
        return format(KEY_NAME_FORMAT, serviceAccountId, keyId);
    }

    private void createNewKey() throws IOException {
        final CreateServiceAccountKeyRequest requestBody = new CreateServiceAccountKeyRequest();
        final Iam.Projects.ServiceAccounts.Keys.Create request = keyService.create(SERVICE_ACCOUNT_NAME, requestBody);

        final ServiceAccountKey newKey = request.execute();
        // save to a file
        System.out.println(newKey.toPrettyString());
        System.out.println(newKey.getName());
        publisher.publishEvent(geServiceAccounttCredential);

    }

    private void listAllKeys() throws IOException {
        final List<ServiceAccountKey> keys = keyService.list(SERVICE_ACCOUNT_NAME).execute().getKeys();
        keys.forEach(key -> System.out.println(key.getValidBeforeTime() + "\t" + key.getKeyType() + " - "
                + key.getName()));
    }

    private void deleteLatestKey() throws IOException {
        final List<ServiceAccountKey> keys = keyService.list(SERVICE_ACCOUNT_NAME).execute().getKeys();
        final ServiceAccountKey newestKey = keys.stream()
                                                .max((k1, k2) -> parse(k1.getValidAfterTime())
                                                                                              .compareTo(parse(k2.getValidAfterTime())))
                                                .get();
        System.out.println("\n\n" + newestKey.getName() + "\t" + newestKey.getValidAfterTime() + " - "
                + newestKey.getValidBeforeTime() + "\n");

        keyService.delete(newestKey.getName()).execute();

    }

}
