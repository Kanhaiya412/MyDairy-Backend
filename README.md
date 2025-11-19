# 🐄 MyDairy Backend  
### A complete Dairy Management Backend built with Spring Boot, Kafka, Docker & MySQL

MyDairy Backend is the core server-side application that powers the **MyDairy mobile app**, enabling farmers and dairy owners to manage cattle, milk entries, expenses, and user operations efficiently.

This backend provides secure authentication, real-time event processing using Kafka, and REST APIs for the mobile frontend.

---

## 🚀 Tech Stack

| Component | Technology |
|----------|------------|
| Backend Framework | **Spring Boot 3** |
| Security | **JWT Authentication, Spring Security** |
| Database | **MySQL** |
| Messaging | **Apache Kafka** |
| Build Tool | **Maven** |
| Containerization | **Docker & Docker Compose** |
| Documentation | **Swagger / OpenAPI** |

---

## 🧩 Main Features

- 👤 **User Authentication**
  - Register, Login
  - Role-based access (Admin, Farmer)

- 🐄 **Cattle Management**
  - Add cattle
  - Update categories, breed, status

- 🥛 **Milk Entry Management**
  - Add milk records
  - Filter entries by month/year

- 💰 **Expense Management**
  - Track expenses
  - Filter by date

- 📩 **Kafka Event System**
  - User events, milk events, expense events published
  - Consumer for real-time event handling

- 📦 **Docker Support**
  - MySQL container
  - Kafka + Zookeeper container
  - Backend container

---

## 📁 Project Structure

MyDairy-Backend/
├── src/
│ ├── main/java/com/MyFarmerApp/MyFarmer/
│ │ ├── config/ # Security, JWT, Swagger
│ │ ├── controller/ # REST controllers
│ │ ├── dto/ # Request/response payloads
│ │ ├── entity/ # JPA entities
│ │ ├── enums/ # Static enums
│ │ ├── repository/ # JPA Repositories
│ │ ├── service/ # Business logic
│ │ └── util/ # Event payloads
│ └── main/resources/
│ └── application.properties
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md

yaml
Copy code

---

## 🔐 Authentication Flow (JWT)

1. User registers → stored in DB  
2. User logs in → server generates **JWT token**  
3. React Native app sends JWT in headers:

Authorization: Bearer <token>

yaml
Copy code

4. All secured endpoints validate token automatically

---

## 📡 REST API Endpoints

### 🔹 **Auth APIs**
POST /api/auth/register
POST /api/auth/login

markdown
Copy code

### 🔹 **Milk Entry APIs**
POST /api/milk/add
GET /api/milk/user/{userId}?month=11&year=2025

markdown
Copy code

### 🔹 **Cattle APIs**
POST /api/cattle/add
GET /api/cattle/user/{userId}

markdown
Copy code

### 🔹 **Expense APIs**
POST /api/expense/add
GET /api/expense/user/{userId}

arduino
Copy code

Swagger UI available at:

http://localhost:8080/swagger-ui/index.html

yaml
Copy code

---

## 🐳 Run with Docker

Build and run all containers:

docker-compose up --build

yaml
Copy code

This will start:
- MySQL
- Zookeeper
- Kafka
- Backend server

---

## ⚙️ Run Locally (Without Docker)

### 1. Configure `application.properties`

spring.datasource.url=jdbc:mysql://localhost:3306/mydairy
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
springdoc.api-docs.enabled=true

shell
Copy code

### 2. Start MySQL  
### 3. Run the Backend

mvn spring-boot:run

yaml
Copy code

---

## 📬 Kafka Topics Used

| Topic Name | Purpose |
|------------|---------|
| `user_events` | User related notifications |
| `milk_events` | Milk entry notifications |
| `expense_events` | Expense tracking |

---

## 🛠️ Future Improvements

- AI-based cattle disease prediction  
- Milk production analytics  
- Push notifications  
- Multi-dairy owner support  
- Full admin panel dashboard  

---

## 🤝 Contributing
Feel free to submit PRs or open issues for improvements!

---

## 📄 License
This project is licensed under the MIT License.

---

# ✨ Author  
**Kanhaiya Patidar**  
Creator of **MyDairy App**
