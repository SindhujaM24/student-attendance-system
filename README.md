# Student Attendance Management System

A web application that lets college teachers/admins manage students, subjects and daily attendance digitally, and lets students check their own attendance. Built as a beginner-friendly full-stack project with **Java, Spring Boot, MySQL and Bootstrap** - no AI/ML, no complicated frameworks.

## Features

**Admin / Teacher**
- Login, dashboard (total students, total subjects, today's attendance, students below 75%)
- Add, edit, delete, view and search students
- Add, edit, delete and view subjects
- Mark attendance (date + subject + year + section, Present/Absent) and edit it later
- Attendance report (total classes, present, absent, percentage) with subject / name filters
- Low attendance list (below 75%) with warning indicator

**Student**
- Login, personal profile
- Overall attendance %, subject-wise attendance, recent attendance records
- Warning when attendance is below 75%

**Validation:** required fields, valid email, 10-digit phone, duplicate Student ID, duplicate email, empty attendance submission, no future-dated attendance.

## Technology Stack
| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript (fetch API), Bootstrap 5 (CDN) |
| Backend | Java 17, Spring Boot 3.3, Spring MVC, REST APIs, Spring Data JPA |
| Database | MySQL 8 |
| Tools | Maven, IntelliJ IDEA / Eclipse / STS, MySQL Workbench, Git & GitHub |

## Architecture
```
Browser (HTML + Bootstrap + JS)
      |  fetch() JSON over HTTP
      v
controller  ->  service  ->  repository  ->  MySQL
 (REST API)    (rules, %)    (Spring Data JPA)
```
- The HTML/CSS/JS files are served by Spring Boot itself from `src/main/resources/static`, so frontend and backend run on the same port (no CORS setup).
- Login uses an HTTP session. An interceptor protects `/api/**`: admin APIs need role `ADMIN`, `/api/me/**` needs role `STUDENT`.
- Passwords are stored as BCrypt hashes (only the `spring-security-crypto` jar is used, not the full Spring Security).

## Project Structure
```
student-attendance-system/
├── database/
│   ├── schema.sql                 # CREATE DATABASE + tables
│   └── sample-data.sql            # users, students, subjects, attendance
├── src/main/java/com/example/attendance/
│   ├── AttendanceApplication.java
│   ├── controller/   Auth, Student, Subject, Attendance, Report, Me controllers
│   ├── service/      business logic + attendance % calculation
│   ├── repository/   Spring Data JPA interfaces
│   ├── entity/       User, Student, Subject, Attendance, Role, AttendanceStatus
│   ├── dto/          request/response objects (Java records)
│   ├── config/       BCrypt bean, login interceptor, web config
│   └── exception/    ApiException + global error handler
├── src/main/resources/
│   ├── application.properties
│   └── static/
│       ├── login.html, dashboard.html, students.html, add-student.html,
│       │   subjects.html, attendance.html, attendance-report.html, student-dashboard.html
│       ├── css/style.css
│       └── js/       common.js + one script per page
├── pom.xml
└── README.md
```

## Database Design
| Table | Purpose | Key columns |
|---|---|---|
| `users` | Login accounts | `id` PK, `username` UNIQUE, `password` (BCrypt), `role` (ADMIN/STUDENT) |
| `students` | Student details | `id` PK, `student_code` UNIQUE (ST001), `email` UNIQUE, `user_id` FK -> users |
| `subjects` | Subject details | `id` PK, `subject_code` UNIQUE (CS301), `department`, `semester` |
| `attendance` | Daily attendance | `id` PK, `student_id` FK, `subject_id` FK, `attendance_date`, `status` |

**Relationships**
- `users` 1 : 1 `students` - every student has one login account.
- `students` 1 : N `attendance` - a student has many attendance rows.
- `subjects` 1 : N `attendance` - a subject has many attendance rows.
- `students` and `subjects` are linked through `attendance` (many-to-many with the date/status as extra data).
- `UNIQUE (student_id, subject_id, attendance_date)` prevents duplicate attendance; saving again simply updates the row (this is how "edit attendance" works).

**Attendance % = Present / Total classes x 100**, calculated in `ReportService` (JPQL `GROUP BY` query + Java).

## REST API
| Method | URL | Who | Purpose |
|---|---|---|---|
| POST | `/api/auth/login` | all | Login (`username`, `password`, `role`) |
| POST | `/api/auth/logout` | all | Logout |
| GET | `/api/auth/me` | all | Current user |
| GET | `/api/students?search=` | admin | List / search students |
| GET/PUT/DELETE | `/api/students/{id}` | admin | Get / update / delete student |
| POST | `/api/students` | admin | Add student |
| GET/POST | `/api/subjects` | admin | List / add subject |
| PUT/DELETE | `/api/subjects/{id}` | admin | Update / delete subject |
| GET | `/api/attendance/sheet?subjectId&date&year&section` | admin | Load attendance sheet (with saved status) |
| POST | `/api/attendance` | admin | Save / edit attendance |
| GET | `/api/reports/dashboard` | admin | Dashboard numbers |
| GET | `/api/reports/attendance?subjectId&search` | admin | Attendance report |
| GET | `/api/reports/low-attendance` | admin | Students below 75% |
| GET | `/api/me/profile` | student | Own profile |
| GET | `/api/me/attendance` | student | Own overall, subject-wise and recent attendance |

## Setup

### 1. Prerequisites
- JDK 17 or newer
- MySQL 8 + MySQL Workbench
- Eclipse / STS / IntelliJ IDEA (Maven is built into these IDEs; the `mvn` command is only needed for the terminal)
- Internet on first run (Maven downloads libraries; Bootstrap is loaded from a CDN)

### 2. Create the database
1. Open MySQL Workbench and connect to your local server.
2. Open `database/schema.sql` and run it (creates `attendance_db` and the 4 tables).
3. Open `database/sample-data.sql` and run it (adds 1 admin, 8 students, 4 subjects and attendance).

### 3. Configure the application
Edit `src/main/resources/application.properties` and set your MySQL username and password:
```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### 4. Import the project
- **IntelliJ:** File > Open > select `pom.xml` > Open as Project.
- **Eclipse / STS:** File > Import > Maven > Existing Maven Projects > select the project folder.

### 5. Install dependencies & run
- The IDE downloads the Maven dependencies automatically (wait for the progress bar to finish).
- Run `AttendanceApplication.java` (Run as Java Application / Spring Boot App), or in a terminal:
  ```bash
  mvn spring-boot:run
  ```
- Wait for `Started AttendanceApplication` in the console.

### 6. Open the application
Go to **http://localhost:8080** in your browser.

## Login Credentials
| Role | Username | Password |
|---|---|---|
| Admin / Teacher | `admin` | `admin123` |
| Student | `arun.kumar@college.edu` | `student123` |
| Student (has low attendance) | `karthik.raja@college.edu` | `student123` |

Other sample students: `priya.sharma@`, `divya.lakshmi@`, `suresh.babu@`, `meena.devi@`, `vignesh.selvam@`, `nandhini.raj@` (all `@college.edu`, password `student123`). New students added by the admin get the same default password (`student123`).

Sample data: 8 CSE Year-3 students (Section A: ST001-ST006, Section B: ST007-ST008), subjects CS301-CS304, 16 classes per subject. Arun's Java attendance is 14/16 = **87.5%**; Karthik (Java 62.5%), Priya (DBMS 68.8%) and Suresh (DBMS 56.3%) show up in the low attendance list.

## Testing Checklist
Tick each item while testing in the browser.

**Login**
- [ ] Admin login with `admin` / `admin123` opens the dashboard
- [ ] Student login (select "Student" tab) opens the student dashboard
- [ ] Wrong password shows "Invalid username or password"
- [ ] Empty username/password shows validation messages
- [ ] Admin credentials with the "Student" tab selected are rejected
- [ ] Opening `dashboard.html` after logout redirects to login

**Students**
- [ ] Add a student (ST009) - appears in the table
- [ ] Adding the same Student ID again shows "already exists"
- [ ] Adding an existing email shows "already registered"
- [ ] Invalid email / phone (e.g. 12345) shows an error under the field
- [ ] Edit a student's department/section and save
- [ ] Search by name / ID works
- [ ] Delete a student - removed from the table (and their attendance)
- [ ] New student can log in with email + `student123`

**Subjects**
- [ ] Add subject, edit it, delete it
- [ ] Duplicate Subject ID shows an error

**Attendance**
- [ ] Select today, a subject, Year 3, Section A, click Load students
- [ ] Mark one student Absent, Save - success message
- [ ] Load the same class again - saved statuses are shown with the "already saved" notice
- [ ] Change a status, Save again (edit) - report numbers change
- [ ] Section with no students shows a warning
- [ ] Future date cannot be selected

**Reports**
- [ ] Attendance report shows Total / Present / Absent / % (Arun - Java = 87.5%)
- [ ] Subject filter and search work
- [ ] Low attendance table lists records below 75% with the warning badge
- [ ] Dashboard "below 75%" count matches the low attendance list

**Student dashboard**
- [ ] Profile, overall %, subject-wise table, recent records are shown
- [ ] Low-attendance student (Karthik) sees the warning message
- [ ] Student cannot open admin pages (redirected)

## Troubleshooting
| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | Wrong password in `application.properties` |
| `Unknown database 'attendance_db'` | Run `database/schema.sql` first |
| `Communications link failure` | MySQL server is not running / wrong port |
| Port 8080 already in use | Set `server.port=8081` in `application.properties` |
| Page looks unstyled | Bootstrap loads from a CDN - check internet connection |
| Login always fails | Run `sample-data.sql` (it contains the users) |

## Screenshots
_Add screenshots here after running the project (put images in a `screenshots/` folder)._

| Login | Admin Dashboard |
|---|---|
| ![Login](screenshots/login.png) | ![Dashboard](screenshots/dashboard.png) |

| Mark Attendance | Attendance Report |
|---|---|
| ![Attendance](screenshots/attendance.png) | ![Report](screenshots/report.png) |

## Future Improvements
- Spring Security with JWT, password change / reset
- Export reports to PDF / Excel
- Email or SMS alerts for low attendance
- Teacher accounts limited to their own subjects
- Pagination for large student lists
- Unit and integration tests, Docker setup
