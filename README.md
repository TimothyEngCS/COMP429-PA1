# Chat Application for Remote Message Exchange

## Project Overview
This project is a chat application that enables remote message exchange among peers using TCP sockets. The application integrates both client-side and server-side functionalities within a single program. It supports multiple simultaneous connections and provides a UNIX shell-like interface with the following commands:
- **help:** Display the list of available commands.
- **myip:** Show your machine’s IP address.
- **myport:** Show the port on which your program is listening.
- **connect \<destination\> \<port\>:** Establish a TCP connection to another peer.
- **list:** Display a numbered list of all active connections.
- **terminate \<connection id\>:** Terminate the specified connection.
- **send \<connection id\> \<message\>:** Send a message (up to 100 characters) to a peer.
- **exit:** Close all connections and exit the application.

## Member Contributions
- **Timothy Eng:**  
  - Developed the networking module using Java’s socket programming.
  - Implemented the server thread for accepting incoming connections.
  - Conducted unit testing for the connection management module.

- **Zaid Alabwaini:**  
  - Designed and implemented the command-line interface (CLI).
  - Handled command parsing and integration with the networking component.
  - Coordinated debugging sessions and contributed to error handling improvements.

## Prerequisites
- **Java Development Kit (JDK) 8 or above:**  
  Download from [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://openjdk.java.net/install/).
- **Terminal/Command Prompt:**  
  Needed for compiling and running the application.

## Building the Program

### Using the Terminal
1. Open the terminal and navigate to the directory containing the `Chat.java` file.
2. Compile the program by running:
This command creates the compiled `Chat.class` file.

cd /Users/tim/Desktop/Projects/COMP429-PA1
javac -d bin Chat.java
java -cp bin Chat 4322,4323,4324


### Using Visual Studio Code
1. Open the project folder in VSCode.
2. Ensure the Java extension is installed.
3. You can compile the program either using the built-in terminal (with the same `javac Chat.java` command) or by clicking the "Run" button provided by the extension.

## Running the Application

### Testing on a Single Machine
1. **Open multiple terminal windows or tabs:**  
Each instance of the application must run on a different port.

2. **Start the application in each terminal:**
- **Terminal 1:**
  ```
  java Chat 4322
  ```
- **Terminal 2:**
  ```
  java Chat 4323
  ```
- **Terminal 3:**
  ```
  java Chat 4324
  ```
