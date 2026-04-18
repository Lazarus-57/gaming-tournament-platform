# Gaming Tournament Management Platform

**OOAD Mini Project - UE23CS352B**
**Semester 6 | Academic Year 2025-26**

## Team Members
| Name | SRN |
|------|-----|
| Vinay V | PES1UG24CS840 |
| Suprateek Yawagal | PES1UG23CS614 |
| Srihari Iyer | PES1UG23CS596 |
| Joshua Daniel Andrews | PES1UG24CS808 |

## Tech Stack
- **Backend:** Java 17+ with Spring Boot 3.2 (MVC Architecture)
- **Frontend:** Thymeleaf + Bootstrap 5
- **Database:** H2 In-Memory Database
- **Security:** Spring Security with BCrypt password hashing
- **Build Tool:** Apache Maven

## Design Patterns Used
1. **MVC Pattern** - Model-View-Controller architecture (Spring MVC)
2. **Strategy Pattern** - Different bracket generation algorithms (Knockout vs Round Robin)
3. **Observer Pattern** - Notification system for tournament events
4. **Factory Pattern** - User creation based on role type

## How to Run

### Prerequisites
- Java 17 or higher installed
- Apache Maven 3.6+ installed (or use the included wrapper)

### Steps
```bash
# Navigate to the project directory
cd Project

# Compile and run
mvn spring-boot:run
```

The application will start at **http://localhost:8080**

### Default Login Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@tournament.com | admin123 |
| Organizer | organizer@tournament.com | org123 |
| Player | alice@tournament.com | player123 |
| Player | bob@tournament.com | player123 |
| Player | charlie@tournament.com | player123 |
| Player | diana@tournament.com | player123 |

## Features

### Player
- Register and login
- Browse and join tournaments (solo)
- View match schedules and brackets
- Track standings and leaderboards
- Receive notifications

### Organizer
- Create and configure tournaments (format, rules, prize pool)
- View registrations
- Generate match brackets (Knockout / Round Robin)
- Submit match results
- Declare tournament winners
- View tournament reports

### Admin
- View system dashboard with statistics
- Manage users (activate/suspend/delete)
- View all tournaments
- View reports and analytics

## Project Structure
```
src/main/java/com/tournament/
├── TournamentApplication.java          # Main entry point
├── config/
│   ├── SecurityConfig.java             # Spring Security configuration
│   └── DataInitializer.java            # Sample data seeder
├── model/                              # Entity classes (JPA)
│   ├── User.java (abstract)
│   ├── Admin.java, Organizer.java, Player.java
│   ├── Tournament.java, Bracket.java, Match.java
│   ├── Team.java, Result.java, Registration.java
│   ├── Notification.java
│   └── enums/                          # Enumeration types
├── repository/                         # Spring Data JPA repositories
├── service/                            # Business logic layer
│   ├── strategy/                       # Strategy Pattern
│   │   ├── BracketGenerationStrategy.java
│   │   ├── KnockoutBracketStrategy.java
│   │   └── RoundRobinBracketStrategy.java
│   ├── observer/                       # Observer Pattern
│   │   ├── TournamentObserver.java
│   │   └── NotificationObserver.java
│   └── factory/                        # Factory Pattern
│       └── UserFactory.java
└── controller/                         # MVC Controllers
    ├── AuthController.java
    ├── PlayerController.java
    ├── OrganizerController.java
    └── AdminController.java

src/main/resources/
├── application.properties
├── static/css/style.css
└── templates/                          # Thymeleaf HTML templates
    ├── login.html, register.html
    ├── fragments/layout.html
    ├── player/
    ├── organizer/
    └── admin/
```

## H2 Database Console
Available at **http://localhost:8080/h2-console** when the app is running.
- JDBC URL: `jdbc:h2:mem:tournamentdb`
- Username: `sa`
- Password: *(leave empty)*
