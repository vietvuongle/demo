# Survey Management System - API Endpoints

## Authentication Routes

| Method | Endpoint  | Access        | Description   |
| ------ | --------- | ------------- | ------------- |
| GET    | `/login`  | Public        | Login page    |
| POST   | `/login`  | Public        | Process login |
| POST   | `/logout` | Authenticated | Logout        |

## Home & General Routes

| Method | Endpoint | Access        | Description        |
| ------ | -------- | ------------- | ------------------ |
| GET    | `/`      | Public        | Redirect to home   |
| GET    | `/home`  | Authenticated | Home dashboard     |
| GET    | `/403`   | Authenticated | Access denied page |

## Admin Routes (`/admin`)

### Dashboard

| Method | Endpoint | Access | Description                     |
| ------ | -------- | ------ | ------------------------------- |
| GET    | `/admin` | ADMIN  | Admin dashboard with statistics |

### Survey Management

| Method | Endpoint                        | Access | Description                 |
| ------ | ------------------------------- | ------ | --------------------------- |
| GET    | `/admin/surveys`                | ADMIN  | List all surveys            |
| GET    | `/admin/surveys/create`         | ADMIN  | Show create survey form     |
| POST   | `/admin/surveys/create`         | ADMIN  | Create new survey           |
| GET    | `/admin/surveys/{id}/edit`      | ADMIN  | Show edit survey form       |
| POST   | `/admin/surveys/{id}/edit`      | ADMIN  | Update survey               |
| GET    | `/admin/surveys/{id}/assign`    | ADMIN  | Show member assignment form |
| POST   | `/admin/surveys/{id}/assign`    | ADMIN  | Assign members to survey    |
| GET    | `/admin/surveys/{id}/approve`   | ADMIN  | Approve survey              |
| POST   | `/admin/surveys/{id}/delete`    | ADMIN  | Delete survey               |
| GET    | `/admin/surveys/{id}/reports`   | ADMIN  | View survey report          |
| GET    | `/admin/surveys/{id}/questions` | ADMIN  | Manage survey questions     |

### Question Management

| Method | Endpoint                                                      | Access | Description               |
| ------ | ------------------------------------------------------------- | ------ | ------------------------- |
| GET    | `/admin/surveys/{surveyId}/questions/create`                  | ADMIN  | Show create question form |
| POST   | `/admin/surveys/{surveyId}/questions/create`                  | ADMIN  | Create question           |
| GET    | `/admin/surveys/{surveyId}/questions/{questionId}/edit`       | ADMIN  | Show edit question form   |
| POST   | `/admin/surveys/{surveyId}/questions/{questionId}/edit`       | ADMIN  | Update question           |
| GET    | `/admin/surveys/{surveyId}/questions/{questionId}/delete`     | ADMIN  | Delete question           |
| POST   | `/admin/surveys/{surveyId}/questions/{questionId}/add-option` | ADMIN  | Add question option       |

### User Management

| Method | Endpoint            | Access | Description       |
| ------ | ------------------- | ------ | ----------------- |
| GET    | `/admin/users`      | ADMIN  | List all users    |
| GET    | `/admin/users/{id}` | ADMIN  | View user details |

## Member Routes (`/survey`)

| Method | Endpoint                    | Access | Description           |
| ------ | --------------------------- | ------ | --------------------- |
| GET    | `/survey`                   | MEMBER | List assigned surveys |
| GET    | `/survey/{surveyId}/start`  | MEMBER | Start survey page     |
| GET    | `/survey/{surveyId}/take`   | MEMBER | Take survey form      |
| POST   | `/survey/{surveyId}/submit` | MEMBER | Submit survey answers |
| GET    | `/survey/{surveyId}/view`   | MEMBER | View submitted survey |

## Database Models

### User

```json
{
    "id": "Long",
    "username": "String (unique)",
    "password": "String (encrypted)",
    "fullName": "String",
    "email": "String",
    "status": "UserStatus (ACTIVE/INACTIVE)",
    "roles": "Set<Role>",
    "createdAt": "LocalDateTime"
}
```

### Role

```json
{
    "id": "Long",
    "code": "RoleCode (ADMIN/PM/MEMBER/LEADER)",
    "name": "String",
    "users": "Set<User>"
}
```

### Survey

```json
{
    "id": "Long",
    "title": "String",
    "description": "String",
    "startDate": "LocalDateTime",
    "endDate": "LocalDateTime",
    "status": "SurveyStatus (DRAFT/PENDING/ACTIVE/APPROVED/CLOSED)",
    "createdBy": "User",
    "approvedBy": "User",
    "approvedAt": "LocalDateTime",
    "createdAt": "LocalDateTime",
    "questionGroups": "Set<QuestionGroup>",
    "assignRules": "Set<SurveyAssignRule>"
}
```

### QuestionGroup

```json
{
    "id": "Long",
    "code": "String (unique)",
    "name": "String",
    "description": "String",
    "targetRole": "RoleCode (optional)",
    "questions": "List<Question>"
}
```

### Question

```json
{
    "id": "Long",
    "group": "QuestionGroup",
    "content": "String",
    "type": "QuestionType (YES_NO/TEXT/OPTION)",
    "required": "boolean",
    "orderIndex": "Integer"
}
```

### QuestionOption

```json
{
    "id": "Long",
    "question": "Question",
    "content": "String"
}
```

### SurveyMember

```json
{
    "id": "SurveyMemberId",
    "survey": "Survey",
    "user": "User",
    "assignedAt": "LocalDateTime"
}
```

### SurveySubmission

```json
{
    "id": "Long",
    "survey": "Survey",
    "user": "User",
    "submittedAt": "LocalDateTime"
}
```

### SurveyAnswer

```json
{
    "id": "Long",
    "submission": "SurveySubmission",
    "question": "Question",
    "answerText": "String",
    "option": "QuestionOption (optional)"
}
```

## Enums

### UserStatus

- ACTIVE
- INACTIVE

### RoleCode

- ADMIN
- PM
- MEMBER
- LEADER

### SurveyStatus

- DRAFT
- PENDING
- ACTIVE
- APPROVED
- CLOSED

### QuestionType

- YES_NO
- TEXT
- OPTION

### RuleType

- ALL
- ROLE
- DEPARTMENT
- GROUP

## Service Methods

### UserService

```java
Optional<User> findByUsername(String username)
List<User> findAll()
User createUser(String username, String password, String fullName, String email, RoleCode roleCode)
User updateUser(Long id, String fullName, String email, UserStatus status)
void deleteUser(Long id)
Optional<User> findById(Long id)
```

### SurveyService

```java
Survey createSurvey(String title, String description, LocalDateTime startDate, LocalDateTime endDate, Long createdById)
Optional<Survey> findById(Long id)
List<Survey> findAll()
List<Survey> findByCreatedBy(Long userId)
List<Survey> findByStatus(SurveyStatus status)
Survey updateSurvey(Long id, String title, String description, LocalDateTime startDate, LocalDateTime endDate)
Survey approveSurvey(Long id, Long approvedById)
void assignMembers(Long surveyId, List<Long> userIds)
List<User> findAssignedMembers(Long surveyId)
List<Survey> findAssignedSurveys(Long userId)
boolean isUserAssignedToSurvey(Long userId, Long surveyId)
void deleteSurvey(Long id)
```

### QuestionGroupService

```java
QuestionGroup createQuestionGroup(String code, String name, String description, RoleCode targetRole)
Optional<QuestionGroup> findById(Long id)
Optional<QuestionGroup> findByCode(String code)
List<QuestionGroup> findAll()
QuestionGroup updateQuestionGroup(Long id, String name, String description, RoleCode targetRole)
void deleteQuestionGroup(Long id)
```

### QuestionService

```java
Question createQuestion(Long groupId, String content, QuestionType type, boolean required, Integer orderIndex)
Optional<Question> findById(Long id)
List<Question> findByGroupId(Long groupId)
Question updateQuestion(Long id, String content, QuestionType type, boolean required, Integer orderIndex)
void deleteQuestion(Long id)
QuestionOption addOption(Long questionId, String content)
List<QuestionOption> findOptionsByQuestionId(Long questionId)
void deleteOption(Long optionId)
```

### SurveySubmissionService

```java
Optional<SurveySubmission> findExistingSubmission(Long surveyId, Long userId)
SurveySubmission createSubmission(Long surveyId, Long userId)
Optional<SurveySubmission> findById(Long id)
List<SurveySubmission> findBySurveyId(Long surveyId)
SurveyAnswer saveAnswer(Long submissionId, Long questionId, String answerText, Long optionId)
List<SurveyAnswer> findAnswersBySubmissionId(Long submissionId)
int getCompletedCount(Long surveyId)
```

## DTOs

### UserDTO

```json
{
    "id": "Long",
    "username": "String",
    "fullName": "String",
    "email": "String",
    "status": "String",
    "createdAt": "LocalDateTime",
    "roles": "Set<String>"
}
```

### SurveyDTO

```json
{
    "id": "Long",
    "title": "String",
    "description": "String",
    "startDate": "LocalDateTime",
    "endDate": "LocalDateTime",
    "status": "String",
    "createdByName": "String",
    "createdAt": "LocalDateTime",
    "approvedByName": "String",
    "approvedAt": "LocalDateTime",
    "totalMembers": "Integer",
    "completedMembers": "Integer"
}
```

### QuestionGroupDTO

```json
{
    "id": "Long",
    "code": "String",
    "name": "String",
    "description": "String",
    "targetRole": "String",
    "questions": "List<QuestionDTO>"
}
```

### QuestionDTO

```json
{
    "id": "Long",
    "content": "String",
    "type": "String",
    "required": "boolean",
    "orderIndex": "Integer",
    "options": "List<QuestionOptionDTO>"
}
```

### QuestionOptionDTO

```json
{
    "id": "Long",
    "content": "String"
}
```

## Security Configuration

### Role-Based Access Control

- `/`: Public redirect
- `/login`, `/register`, `/css/**`, `/js/**`, `/img/**`: Permit all
- `/admin/**`: Requires ADMIN authority
- `/pm/**`: Requires PM authority
- `/survey/**`: Requires MEMBER, ADMIN, or PM authority
- All other routes: Require authentication

### Form Login

- Login page: `/login`
- Login processing: `/login` (POST)
- Default success URL: `/home`
- Failure URL: `/login?error`

### Logout

- Logout URL: `/logout` (POST)
- Success URL: `/login?logout`

---

**Documentation Version**: 1.0
**Last Updated**: January 21, 2026
