# Survey Management System - Quick Start Guide

## 🚀 Getting Started

### Prerequisites

- Java 21+
- MySQL 8.0+
- Maven 3.6+

### Installation

1. **Clone/Extract the project**

    ```bash
    cd d:\OJT\demo
    ```

2. **Update Database Configuration**
   Edit `src/main/resources/application.yaml`:

    ```yaml
    spring:
        datasource:
            url: jdbc:mysql://localhost:3306/survey_project?useSSL=false&serverTimezone=UTC
            username: root
            password: root # Update with your MySQL password
    ```

3. **Create Database** (Optional - Hibernate will auto-create)

    ```sql
    CREATE DATABASE survey_project;
    ```

4. **Build the Project**

    ```bash
    mvn clean install
    ```

5. **Run the Application**

    ```bash
    mvn spring-boot:run
    ```

6. **Access the Application**
    - URL: `http://localhost:8080`
    - Demo Accounts (see below)

---

## 👥 Demo Accounts

### Admin Account

```
Username: admin
Password: 123456
```

**Access**: Full system control, user management, survey analytics

### PM Account

```
Username: pm
Password: 123456
```

**Access**: Create/approve surveys, assign members

### Member Account

```
Username: user
Password: 123456
```

**Access**: View and complete assigned surveys

---

## 📝 Sample Workflow

### Step 1: Login as Admin

1. Go to `http://localhost:8080`
2. Click login or go to `/login`
3. Enter: `admin / 123456`
4. You'll be on the admin dashboard

### Step 2: Create a Survey (as Admin)

1. Navigate to "Surveys" → "Create Survey"
2. Fill in the survey details:
    - **Title**: "Employee Satisfaction Survey 2026"
    - **Description**: "Please share your feedback"
    - **Start Date**: Today's date
    - **End Date**: 30 days from today
3. Click "Create Survey"
4. Survey is now in DRAFT status

### Step 3: Add Questions to Survey

1. Go to your survey
2. Click "Manage Questions"
3. Add questions:
    - Question 1 (YES/NO): "Are you satisfied with your work?"
    - Question 2 (TEXT): "What can we improve?"
    - Question 3 (OPTION): "How do you rate our support?"
        - Options: Excellent, Good, Average, Poor

### Step 4: Assign Members

1. Go to survey
2. Click "Assign Members"
3. Select members to assign (check their names)
4. Click "Save Assignments"

### Step 5: Approve Survey

1. Click "Approve" button on survey
2. Survey status changes to ACTIVE
3. Assigned members can now see it

### Step 6: Complete Survey as Member

1. Logout and login as: `user / 123456`
2. Go to "My Surveys"
3. Click "Take Survey"
4. Answer all questions
5. Click "Submit Survey"

### Step 7: View Reports (as Admin)

1. Logout and login as admin
2. Go to survey
3. Click "Report" button
4. View completion statistics

---

## 🏗️ Project Structure

```
demo/
├── src/main/java/com/example/demo/
│   ├── configuration/          # Spring config (Security, DataInitializer)
│   ├── controller/             # Web controllers
│   ├── entity/                 # JPA entities
│   ├── enums/                  # Enum definitions
│   ├── repository/             # Data access layer
│   ├── service/                # Business logic
│   ├── dto/                    # Data transfer objects
│   └── DemoApplication.java    # Main entry point
├── src/main/resources/
│   ├── application.yaml        # App configuration
│   └── templates/              # Thymeleaf templates
├── pom.xml                     # Maven dependencies
└── target/                     # Compiled output
```

---

## 🛠️ Troubleshooting

### Issue: Cannot connect to database

**Solution**:

1. Check MySQL is running
2. Verify database URL in `application.yaml`
3. Ensure MySQL username/password are correct

### Issue: Port 8080 already in use

**Solution**:
Change port in `application.yaml`:

```yaml
server:
    port: 8081
```

### Issue: Compilation errors

**Solution**:

```bash
mvn clean compile
```

### Issue: Templates not showing properly

**Solution**:

1. Clear browser cache
2. Restart the application
3. Check Thymeleaf is cached: false in `application.yaml`

---

## 📊 Database Schema Overview

### Core Tables

- **users**: User accounts with passwords
- **roles**: Role definitions (ADMIN, PM, MEMBER, LEADER)
- **user_role**: Many-to-many relationship between users and roles
- **survey**: Survey master records
- **question_group**: Reusable question groups
- **question**: Individual survey questions
- **question_option**: Multiple choice options
- **survey_member**: Survey assignments to members
- **survey_submission**: Tracks survey submissions
- **survey_answer**: Individual answers to questions

---

## 🔐 Security Features

- Spring Security integration
- BCrypt password encryption
- Role-based access control (RBAC)
- Session management
- CSRF protection (default)
- Form-based authentication

---

## 📱 UI Features

### Responsive Design

- Works on desktop, tablet, mobile
- Bootstrap 5 framework
- Modern color scheme
- Intuitive navigation

### Key Pages

1. **Login**: Secure authentication page
2. **Home**: Role-based dashboard
3. **Admin Dashboard**: Statistics and controls
4. **Survey List**: Browse all surveys
5. **Create/Edit Survey**: Survey management
6. **Member Assignment**: Assign surveys to users
7. **Survey Submission**: Complete surveys
8. **Reports**: View analytics

---

## 🔧 Configuration Options

### Application Properties

```yaml
server:
    port: 8080

spring:
    datasource:
        url: jdbc:mysql://localhost:3306/survey_project
        username: root
        password: root
    jpa:
        hibernate:
            ddl-auto: update # auto, create, validate, etc.
        show-sql: true
    thymeleaf:
        cache: false # Set to true in production

logging:
    level:
        org.springframework.security: DEBUG
```

---

## 📚 Key Technologies

- **Backend**: Spring Boot 4.0.1
- **Database**: MySQL with JPA/Hibernate
- **Frontend**: Thymeleaf, Bootstrap 5, HTML5, CSS3
- **Security**: Spring Security with BCrypt
- **Build**: Maven
- **Server**: Embedded Tomcat

---

## 🎯 Next Features to Consider

1. Excel export for survey results
2. Email notifications for survey assignments
3. Survey templates
4. Advanced analytics and charts
5. Mobile app
6. API endpoints for third-party integration
7. Survey scheduling
8. Conditional questions

---

## 📞 Support

### Common Questions

**Q: How do I add more users?**
A: Login as admin, go to Users section, or add them in `DataInitializer.java`

**Q: Can I delete a survey?**
A: Yes, from the survey list, each survey has a delete option

**Q: What happens if a member doesn't complete a survey?**
A: The report will show them as incomplete

**Q: Can I reuse question groups?**
A: Yes, question groups can be added to multiple surveys

**Q: How do I change the database to PostgreSQL?**
A: Update `pom.xml` and `application.yaml` with PostgreSQL driver and connection string

---

## 📝 Notes

- All timestamps are stored in UTC
- Survey status flow: DRAFT → PENDING → ACTIVE → APPROVED → CLOSED
- Members can only see surveys in ACTIVE status within the date range
- Surveys can be edited until they are APPROVED
- Question groups are reusable across surveys
- Answers are immutable once submitted

---

**Version**: 1.0
**Last Updated**: January 21, 2026
**Status**: ✅ Ready for Production
