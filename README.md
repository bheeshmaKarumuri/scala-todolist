# Scala TODO List App
Very simplistic Todo List application.

## Description
This application implements a simple TODO list.
A few highlights include:
- Uses the latest stable version of Play Scala (2.6)
- Stores its data in MongoDB
- Tasks can be created, edited, and deleted
- Each task has a completion checkbox
- Each task can have comments
- The application also includes a suite of Scala Tests
- Both a web front-end and a JSON API are provided with the application

## MongoDB Migration
This application has been migrated from PostgreSQL to MongoDB. If you have existing data in PostgreSQL that you want to migrate, follow these steps:

1. Make sure both PostgreSQL and MongoDB are running
2. Update the database credentials in the migration script if needed:
   - Open `/scripts/migrate_to_mongodb.scala`
   - Update the PostgreSQL connection settings (pgUrl, pgUser, pgPassword)
   - Update the MongoDB connection URI if needed
3. Run the migration script using the Scala REPL or as a standalone application

## Setup Instructions
1. Install MongoDB if you haven't already
2. Start MongoDB service
3. Update the MongoDB connection URI in `conf/application.conf` if needed
4. Run the application using `sbt run`


 
