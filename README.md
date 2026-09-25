# Student Cloud Hub — Full Firebase Android App

Package: `com.studentcloudhub`

This project contains a functional Java/XML Android application with:
- Firebase Email/Password Authentication
- Registration and Firestore user profile
- Login and password reset
- Home dashboard
- Subjects CRUD/add and Firestore retrieval
- Notes add and Firestore retrieval
- Assignments add and Firestore retrieval
- Profile display and logout
- Bottom navigation
- Firebase-ready security rules and documentation

## Firebase setup
1. Create a Firebase project.
2. Add an Android app with package `com.studentcloudhub`.
3. Download `google-services.json`.
4. Put it at `app/google-services.json`.
5. Enable Authentication > Email/Password.
6. Create Firestore.
7. Enable Storage.
8. Enable Cloud Messaging if notifications are required.
9. Publish the supplied rules.

## Important
A real `google-services.json` is project-specific and cannot be generated safely without your Firebase project. Do not use fake credentials.
