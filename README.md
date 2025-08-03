# Workforce Management - Starter Project


This is a Spring Boot application for the Backend Engineer take-home assignment.


## How to Run


1.  Ensure you have Java 17 and Gradle installed.
2.  Open the project in your favorite IDE (IntelliJ, VSCode, etc.).
3.  Run the main class `com.railse.hiring.workforcemgmt.Application`.
4.  The application will start on `http://localhost:8080`.


## API Endpoints


Here are some example `cURL` commands to interact with the API.


### Get a single task
```bash
curl --location 'http://localhost:8080/task-mgmt/1'
```


### Create a new task
```bash
curl --location 'http://localhost:8080/task-mgmt/create' \
--header 'Content-Type: application/json' \
--data '{
   "requests": [
       {
           "reference_id": 105,
           "reference_type": "ORDER",
           "task": "CREATE_INVOICE",
           "assignee_id": 1,
           "priority": "HIGH",
           "task_deadline_time": 1728192000000
       }
   ]
}'
```


### Update a task's status
```bash
curl --location 'http://localhost:8080/task-mgmt/update' \
--header 'Content-Type: application/json' \
--data '{
   "requests": [
       {
           "task_id": 1,
           "task_status": "STARTED",
           "description": "Work has been started on this invoice."
       }
   ]
}'
```


### Assign tasks by reference (Bug #1 is here)
This assigns all tasks for `reference_id: 201` to `assignee_id: 5`.
```bash
curl --location 'http://localhost:8080/task-mgmt/assign-by-ref' \
--header 'Content-Type: application/json' \
--data '{
   "reference_id": 201,
   "reference_type": "ENTITY",
   "assignee_id": 5
}'
```


### Fetch tasks by date (Bug #2 is here)
This fetches tasks for assignees 1 and 2. It incorrectly includes cancelled tasks.
```bash
curl --location 'http://localhost:8080/task-mgmt/fetch-by-date/v2' \
--header 'Content-Type: application/json' \
--data '{
   "start_date": 1672531200000,
   "end_date": 1735689599000,
   "assignee_ids": [1, 2]
}'
```
*/




// =====================================================================================
// FILE: SUBMISSION.md
// =====================================================================================
/*
# Submission


### 1. Link to your Git Repository Branch
https://github.com/samarth350/workforceMgmt


### 2. Link to your Video Demonstration
https://drive.google.com/file/d/1IdlN7Oo1IQCZPMdIykiwcxV11fpxUxt0/view?usp=sharing
