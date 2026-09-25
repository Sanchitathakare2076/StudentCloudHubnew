# Project Documentation

## Title
Student Cloud Hub

## Topic
Cloud Database and Cloud Services using Firebase

## Objective
Demonstrate Firebase Authentication, Cloud Firestore, Firebase Storage and Firebase Cloud Messaging in an Android student application.

## Modules
Authentication, Home, Subjects, Notes, Assignments, Profile.

## Architecture
Android Java/XML client -> Firebase Authentication, Cloud Firestore, Storage and FCM.

## Database
users: name, email, mobile, college, branch, createdAt
subjects: userId, subjectName, subjectCode, description, semester
notes: userId, title, subject, description, date
assignments: userId, title, subject, description, status

## Future Scope
File attachments, profile image upload, advanced search/filtering, scheduled assignment reminders, and richer FCM notification administration.
