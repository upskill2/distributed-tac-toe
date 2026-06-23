# **🦩Backend Home Assignment \- Distributed Tic Tac Toe Microservices**

## **📌Overview:**

In this assignment, you will build a distributed Tic Tac Toe application in which the game is played automatically by the microservices. The system is composed of **three** main components:

- **Game Engine Service:** Manages the core game logic, including board management, move validation, and determining game outcomes.  
- **Game Session Service:** Oversees game sessions and automates moves for both players by coordinating with the Game Engine Service.  
- **User Interface (UI):** Provides a visual presentation of the game board and displays the progress of the automated game as the microservices play against each other.

## **Project Requirements:**

### **1\. Game Engine Service**

**Objective:**  
Implement the core Tic Tac Toe logic to manage the game state.

**Responsibilities:**

- **Board Management:**  
  Maintain the current state of the game board.  
- **Move Validation:**  
  Ensure that each move is legal (e.g., the target cell is unoccupied).  
- **Game Outcome:**  
  Check for winning conditions or a draw after each move and report the current status.

**Key Endpoints:**

- **POST `/games/{gameId}/move`**  
  - **Purpose:** Receive a move request containing the player symbol and board position.  
  - **Functionality:**  
    - Validate the move.  
    - Update the game state.  
    - Determine and return the current game status (in progress, win, or draw).  
- **GET `/games/{gameId}`**  
  - **Purpose:** Retrieve the current state of the game (board and status).

**Technical Requirements:**

- Use an in-memory data structure or an H2 in-memory database for game state management.  
- Implement robust error handling for invalid moves and end-of-game scenarios.

---

### **2\. Game Session Service**

**Objective:**  
Manage game sessions and automate the gameplay by generating moves for both players.

**Responsibilities:**

- **Session Management:**  
  Create and manage game sessions along with session details.  
- **Automated Move Generation:**  
  Simulate moves for both players (e.g., using a simple random or rule-based algorithm) and forward these moves to the Game Engine Service.  
- **Move Coordination:**  
  Receive responses from the Game Engine Service and update session data accordingly.

**Key Endpoints:**

- **POST `/sessions`**  
  - **Purpose:** Create a new game session.  
  - **Functionality:**  
    - Generate a unique `sessionId` (which may also serve as the `gameId` for the Game Engine Service).  
    - Optionally initialize the game state in the Game Engine Service.  
- **POST `/sessions/{sessionId}/simulate`**  
  - **Purpose:** Trigger the automated simulation of a game.  
  - **Functionality:**  
    - Automatically generate and forward moves to the Game Engine Service (alternating between players).  
    - Continue simulation until the game concludes (win or draw).  
- **GET `/sessions/{sessionId}`**  
  - **Purpose:** Retrieve session details, including the current game state and move history.

**Technical Requirements:**

- Use an in-memory database (e.g., H2, Hashmap) for storing session and move data.  
- Ensure consistent communication with the Game Engine Service and robust error handling.

---

### **3\. User Interface (UI)**

**Objective:**  
Create a visual interface that displays the Tic Tac Toe board and presents the progress of the game as it is played automatically by the microservices.

**UI Responsibilities:**

- **Game Initialization:**  
  - Display a “Start Simulation” button that triggers the automated game simulation via the Game Session Service.  
- **Game Display:**  
  - Render a 3x3 Tic Tac Toe board showing the current game state.  
  - Update the board in real time as moves are made by the microservices.  
- **Status and Feedback:**  
  - Show the current game status (in progress, win, or draw).  
  - Display a move history or log for reference.  
- **Error Handling:**  
  - Present appropriate error messages in case of backend or communication issues.  
- **Real-Time Feedback (Optional):**  
  - Consider using WebSockets or Server-Sent Events (SSE) to push live updates to the UI.

## **Testing & Validation:**

- **Inter-Service Communication:**  
  Validate that the Game Session Service and Game Engine Service communicate effectively via REST APIs.  
- **State Management:**  
  Ensure that the game state is accurately maintained and updated across components.  
- **Error Handling:**  
  Implement graceful handling of invalid moves, simulation errors, and communication failures.  
- **Integration Testing:**  
  Provide tests that simulate a full automated game flow: session creation, move simulation, and game outcome determination.

## **Optional Enhancements:**

- **Concurrency Handling:**  
  Address scenarios where moves might be processed concurrently.  
- **Service Discovery / API Gateway:**  
  Integrate solutions like Spring Cloud Gateway or Eureka to simulate a production microservices environment.  
- **Data Persistence:**  
  Although an in-memory database is acceptable, consider strategies for persistent storage and data recovery.  
- **Real-Time Updates:**  
  Enhance the UI with real-time capabilities using WebSockets or SSE for a smoother experience.

## **✅ Submission Guidelines:**

- **Code Quality:**  
  Ensure your code is well-organized, commented, and adheres to Spring Boot best practices.  
- **Documentation:**  
  Include a README with detailed instructions on building, running, and testing your application.  
- **Testing:**  
  Provide comprehensive integration tests to validate the application’s functionality.  
- **Discussion:**  
  Optionally, include a brief discussion of potential improvements or alternative design approaches.

**Questions? Reach out to [Oleksandra](https://t.me/oleksandra_flamingo). Good luck and happy coding\! 🚀**

