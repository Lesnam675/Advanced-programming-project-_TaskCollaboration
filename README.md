# Advanced Programming Project – Task Collaboration System

## Overview

Task Collaboration System is a Java-based collaborative task management platform that allows multiple users to create, assign, manage, and track tasks in real time through a Kanban-style interface.

The system combines:

* Spring Boot backend
* JavaFX desktop client
* MySQL database
* Socket-based real-time synchronization

Users can create tasks, assign tasks to team members, move tasks between workflow stages, edit task details, and receive live updates from other connected users.

---

# Features

## User Management

* User registration
* User login and authentication
* Session management
* Creator ownership tracking

## Task Management

* Create tasks
* Edit tasks
* Delete tasks
* Assign tasks to users
* Set task priorities
* Set due dates
* Track task ownership

## Kanban Board

* TO DO column
* DOING column
* DONE column

Tasks can be moved across workflow stages using the user interface.

## Real-Time Collaboration

* Socket-based communication
* Instant task updates
* Live synchronization between connected clients
* Real-time task movement notifications

## Task Metadata

Each task contains:

* Title
* Description
* Status
* Priority
* Due Date
* Assigned User
* Created By

## Filtering and Search

* Filter by status
* Filter by priority
* Filter by assigned user
* Search tasks by title

## User Interface

* Modern Kanban board layout
* Responsive columns
* Task counters
* User avatars
* Hover effects
* Professional styling
* Empty-state indicators

---

# Technology Stack

## Backend

* Java
* Spring Boot
* Spring Data JPA
* Hibernate

## Frontend

* JavaFX

## Database

* MySQL

## Real-Time Communication

* Java Socket Programming

## Build Tool

* Gradle

---

# Project Structure

```text
src/main/java/com/aastu/taskmanagersystem

backend/
├── TaskManagerSystemApplication.java
├── controller/
├── service/
├── repository/
├── model/
├── socket/
└── database/

client/
├── api/
├── model/
├── socket/
└── ui/
    ├── components/
    ├── dialogs/
    ├── filter/
    ├── permissions/
    └── styles/
```

---

# System Architecture

Client (JavaFX)
↓
REST API Calls
↓
Spring Boot Backend
↓
MySQL Database

Client (JavaFX)
↕
Socket Server
↕
Other Connected Clients

---

# Running the Application

## Step 1 – Start MySQL

Ensure MySQL server is running and the required database has been created.

---

## Step 2 – Start Backend

Run:

```java
TaskManagerSystemApplication.java
```

Expected result:

* Spring Boot starts successfully
* API becomes available on port 8080

---

## Step 3 – Start Socket Server

Run:

```java
TaskServer.java
```

Expected result:

* Socket server starts successfully
* Real-time communication enabled

---

## Step 4 – Start Client

Run:

```java
LoginScreen.java
```

or

```java
Main.java
```

Expected result:

* Login window opens
* Users can authenticate and access the task board

---

# Usage

## Create Task

1. Enter task information.
2. Select priority.
3. Select assignee.
4. Set due date.
5. Click Add Task.

## Move Task

Click a task to move it through:

TO DO → DOING → DONE

## Edit Task

Double-click a task to edit its details.

## Delete Task

Use the delete action available on the task card.

## View Task Details

Open task details to inspect metadata and ownership information.

---

# Permission Rules

Task ownership is enforced.

Only the creator of a task can:

* Edit the task
* Move the task
* Delete the task

Other users can view the task but cannot modify it unless permission rules are extended.

---

# Team Responsibilities

## Selman

Backend Architecture

* TaskManagerSystemApplication
* Controllers
* Services
* Repositories
* Entities

## Seyfadin

Client Logic

* APIs
* Models
* Filtering
* Data Management

## Sington

Real-Time Collaboration

* Socket Server
* Client Socket Communication
* Synchronization Logic
* UI Components

---

# Future Improvements

* Comments system
* Subtasks
* Notifications
* Activity history
* User profile management
* Dark mode
* Drag-and-drop task movement
* Role-based permissions
* WebSocket migration
* Dashboard analytics

---

# Authors

Advanced Programming Project

Developed by:

* Selman
* Seyfadin
* Singitan

Academic Year: 2025/2026
