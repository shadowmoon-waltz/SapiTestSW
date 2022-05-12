SapiTestSW
==========

These are test apps for developing a simple, but moderately secure, interface for sending and receiving messages through an app providing communication services. The first S in SapiTestSW refers to simple in this case.

The server app shows a list of apps that have the activity that handles a specific intent action, one that will be used to start the app and pass a token to the app allowing it to use the API. The client app will also have a chance to confirm the user's intent to connect this app to the API, hoping preventing some type of man in the middle vulnerabilities. When the server receives a message intended for the client, it will send a broadcast configured to only be sent to the client app, and the client will do the same when it wants to send a message through the server. There is a also a broadcast sent to the client if the user removes API access for the client.

Limitations in the example are that the server can only have one API connection to each client, and the client can only have one API connection, to one server. While I am not aware of any methods of spoofing a return when launching the client app's intent to send the token and verify the connection, the server out of an abundance of caution does require the client to send back a result status that I randomly generate (as onActivityResult doesn't seem to allow me to send back the token in the intent across app boundaries); even if such spoofing were possible, I don't believe such an app would be able to get the token in any case.

No permissions are required (although there are queries element in the manifest to allow API client and servers to find and communicate with each other).

License is GPLv3.

Build command is: gradle assembleRelease (requires Gradle and Android SDK installation).

If you don't have a signing key, follow the [command line signing instructions] (https://developer.android.com/studio/build/building-cmdline#sign_cmdline).

Uses a signing method where you add to (or create) the following lines in gradle.properties in the gradle user home directory (in .gradle in your home directory by default) (replace all `...` with the correct values)
```
keystoreFile=...
keystorePassword=...
keystoreAlias=...
keystoreAliasPassword=...
```
