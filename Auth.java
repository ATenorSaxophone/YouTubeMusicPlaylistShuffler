package com.lonestarbandit;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

import java.io.File;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class Auth {

    private static final String CLIENT_SECRETS= "/credentials.json";  // Make sure this is in `resources`
    private static final List<String> SCOPES = Collections.singletonList(YouTubeScopes.YOUTUBE);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static YouTube authorize() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load client secrets
        InputStream in = Auth.class.getResourceAsStream(CLIENT_SECRETS);
        if (in == null) {
            throw new RuntimeException("Resource not found: " + CLIENT_SECRETS);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("YouTube API Java Quickstart")
                .build();
    }
}
