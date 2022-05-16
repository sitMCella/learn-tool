# Learn Tool

## Google Authentication
Learn Tool application supports the Google authentication using JWT and Oauth2.

### Configure the Google API
Open the Google API console of your Google account:
https://console.cloud.google.com/apis/credentials

Create a new OAuth2.0 client ID and specify the following as authorized redirect URL:
```
http://localhost:8080/oauth2/callback/google
```

Configure application.yml to use the Google Client ID and secret in security.oauth2.client.registration.google

Register a new App in the Google OAuth consent screen page.
Define the test accounts in case that the App is not published.
