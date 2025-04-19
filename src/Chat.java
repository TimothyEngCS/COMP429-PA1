import java.io.*;
import java.net.*;
import java.util.*;

public class Chat {
    // List to keep track of active connections
    private static final List<Connection> connections = new ArrayList<>();
    private static int connectionCounter = 1;
    private static int listeningPort;
    private static String myIp;

    public static void main(String[] args) {
        // Validate command-line argument
        if (args.length != 1) {
            System.out.println("Usage: java Chat <listening port>");
            System.exit(1);
        }
        try {
            listeningPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number.");
            System.exit(1);
        }
        try {
            myIp = getMyIp();
        } catch (SocketException e) {
            System.out.println("Error obtaining local IP: " + e.getMessage());
            System.exit(1);
        }

        // Start the server thread to accept incoming connections
        ServerThread serverThread = new ServerThread(listeningPort);
        serverThread.start();

        // Command loop (acts like a simple shell)
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Chat Application. Type 'help' for available commands.");
        while (true) {
            System.out.print("chat> ");
            String line = scanner.nextLine();
            String[] tokens = line.split(" ", 3); // split into at most 3 parts
            if (tokens.length == 0)
                continue;
            String command = tokens[0];

            if (command.equalsIgnoreCase("help")) {
                printHelp();
            } else if (command.equalsIgnoreCase("myip")) {
                System.out.println("My IP: " + myIp);
            } else if (command.equalsIgnoreCase("myport")) {
                System.out.println("Listening on port: " + listeningPort);
            } else if (command.equalsIgnoreCase("connect")) {
                if (tokens.length < 3) {
                    System.out.println("Usage: connect <destination> <port>");
                } else {
                    String destination = tokens[1];
                    int port;
                    try {
                        port = Integer.parseInt(tokens[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number.");
                        continue;
                    }
                    connectToPeer(destination, port);
                }
            } else if (command.equalsIgnoreCase("list")) {
                listConnections();
            } else if (command.equalsIgnoreCase("terminate")) {
                if (tokens.length < 2) {
                    System.out.println("Usage: terminate <connection id>");
                } else {
                    int id;
                    try {
                        id = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid connection id.");
                        continue;
                    }
                    terminateConnection(id);
                }
            } else if (command.equalsIgnoreCase("send")) {
                if (tokens.length < 3) {
                    System.out.println("Usage: send <connection id> <message>");
                } else {
                    int id;
                    try {
                        id = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid connection id.");
                        continue;
                    }
                    String message = tokens[2];
                    sendMessage(id, message);
                }
            } else if (command.equalsIgnoreCase("exit")) {
                exitChat();
                break;
            } else {
                System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }
        scanner.close();
    }

    // Prints the available commands
    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help                         - Display this help message");
        System.out.println("myip                         - Display your IP address");
        System.out.println("myport                       - Display the listening port");
        System.out.println("connect <destination> <port> - Establish connection to a peer");
        System.out.println("list                         - List all active connections");
        System.out.println("terminate <connection id>    - Terminate a connection");
        System.out.println("send <connection id> <msg>   - Send a message to a connection");
        System.out.println("exit                         - Close all connections and exit");
    }

    // Connects to a peer at the specified destination and port
    private static void connectToPeer(String destination, int port) {
        // Check for duplicate connections
        synchronized (connections) {
            for (Connection conn : connections) {
                if (conn.remoteIp.equals(destination) && conn.remotePort == port) {
                    System.out.println("Error: Already connected to " + destination + ":" + port);
                    return;
                }
            }
        }
        try {
            Socket socket = new Socket(destination, port);
            Connection connection = new Connection(connectionCounter++, socket, destination, port);
            connection.startHandler();
            synchronized (connections) {
                connections.add(connection);
            }
            System.out.println("Connected to " + destination + " on port " + port);
        } catch (IOException e) {
            System.out.println("Error connecting to " + destination + ":" + port + " - " + e.getMessage());
        }
    }

    // Lists all active connections with an id, IP address, and port
    private static void listConnections() {
        synchronized (connections) {
            if (connections.isEmpty()) {
                System.out.println("No active connections.");
            } else {
                System.out.println("ID\tIP Address\tPort");
                for (Connection conn : connections) {
                    System.out.println(conn.id + "\t" + conn.remoteIp + "\t" + conn.remotePort);
                }
            }
        }
    }

    // Terminates a connection given its id
    private static void terminateConnection(int id) {
        Connection toTerminate = null;
        synchronized (connections) {
            for (Connection conn : connections) {
                if (conn.id == id) {
                    toTerminate = conn;
                    break;
                }
            }
            if (toTerminate != null) {
                connections.remove(toTerminate);
            }
        }
        if (toTerminate != null) {
            toTerminate.close();
            System.out.println("Connection " + id + " terminated.");
        } else {
            System.out.println("Error: No connection with id " + id);
        }
    }

    // Sends a message to the connection with the specified id
    private static void sendMessage(int id, String message) {
        Connection target = null;
        synchronized (connections) {
            for (Connection conn : connections) {
                if (conn.id == id) {
                    target = conn;
                    break;
                }
            }
        }
        if (target != null) {
            // Prefixing the message with sender details
            target.sendMessage("Message from " + myIp + ":\n" + message);
            System.out.println("Message sent to " + id);
        } else {
            System.out.println("Error: No connection with id " + id);
        }
    }

    // Closes all connections and exits the application
    private static void exitChat() {
        synchronized (connections) {
            for (Connection conn : connections) {
                conn.close();
            }
            connections.clear();
        }
        System.out.println("Exiting chat.");
        System.exit(0);
    }

    // Utility method to obtain the local (non-loopback) IP address
    private static String getMyIp() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()){
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp())
                continue;
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    return addr.getHostAddress();
                }
            }
        }
        return "127.0.0.1"; // fallback
    }

    // Inner class representing an active connection
    private static class Connection {
        int id;
        Socket socket;
        String remoteIp;
        int remotePort;
        PrintWriter out;
        ConnectionHandler handler;

        public Connection(int id, Socket socket, String remoteIp, int remotePort) throws IOException {
            this.id = id;
            this.socket = socket;
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        // Starts a new thread to listen for messages from this connection
        public void startHandler() {
            handler = new ConnectionHandler(this);
            handler.start();
        }

        // Sends a message to the connected peer
        public void sendMessage(String msg) {
            out.println(msg);
        }

        // Closes the socket
        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore exceptions on close
            }
        }
    }

    // Thread to handle incoming messages on a connection
    private static class ConnectionHandler extends Thread {
        private Connection connection;

        public ConnectionHandler(Connection connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("\nMessage received from " + connection.remoteIp);
                    System.out.println("Sender's Port: " + connection.remotePort);
                    System.out.println("Message: " + line);
                    System.out.print("chat> ");
                }
            } catch (IOException e) {
                System.out.println("Connection " + connection.id + " closed.");
            } finally {
                // Remove this connection from the list if it is closed
                synchronized (connections) {
                    connections.removeIf(c -> c.id == connection.id);
                }
            }
        }
    }

    // Server thread to accept incoming connections
    private static class ServerThread extends Thread {
        private int port;
        private ServerSocket serverSocket;

        public ServerThread(int port) {
            this.port = port;
        }

        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server listening on port " + port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    String clientIp = clientSocket.getInetAddress().getHostAddress();
                    int clientPort = clientSocket.getPort();
                    // Check for duplicate connections
                    boolean duplicate = false;
                    synchronized (connections) {
                        for (Connection conn : connections) {
                            if (conn.remoteIp.equals(clientIp) && conn.remotePort == clientPort) {
                                duplicate = true;
                                break;
                            }
                        }
                    }
                    if (duplicate) {
                        clientSocket.close();
                        continue;
                    }
                    // Create and add the new connection
                    Connection connection = new Connection(connectionCounter++, clientSocket, clientIp, clientPort);
                    connection.startHandler();
                    synchronized (connections) {
                        connections.add(connection);
                    }
                    System.out.println("Accepted connection from " + clientIp + ":" + clientPort);
                    System.out.print("chat> ");
                }
            } catch (IOException e) {
                System.out.println("Server error: " + e.getMessage());
            }
        }
    }
}
