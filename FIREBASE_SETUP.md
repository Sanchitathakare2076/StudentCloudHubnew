# Firebase Setup

1. Firebase Console -> Create project.
2. Add Android app with package `com.studentcloudhub`.
3. Download `google-services.json`.
4. Copy it to `app/google-services.json`.
5. Authentication -> Sign-in method -> enable Email/Password.
6. Firestore Database -> Create database.
7. Storage -> Get started.
8. Cloud Messaging -> configure as needed.
9. Apply `firebase/firestore.rules` and `firebase/storage.rules`.
10. Open the project in Android Studio, sync Gradle, and run.

Collections:
users, subjects, notes, assignments.
