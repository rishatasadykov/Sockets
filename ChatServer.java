import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	
	Object lock = new Object();
	
	List<String> his = new ArrayList<>();
	
	public static List<User> clients = new ArrayList<>();
	
	public void runThread(User user) {
		(new Thread(new Runnable() {
			public void run() {
				String inputLine = "";
				try {
					DataInputStream in = new DataInputStream(user.getSocket().getInputStream());
					clients.add(user);
					ChatServer cs = new ChatServer();
					sendAll("New client connected: " + user.username);
					synchronized(lock) {
						try {
							DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
							for (int i = 0; i < his.size(); i++) {
								out.writeUTF(his.get(i));
								out.flush();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					while(true) {
						try {
							inputLine = in.readUTF();
							his.add(user.username + " : " + inputLine);
							if (his.size() > 5)
								his.remove(0);
							cs.sendAll(user.username + " : " + inputLine);
						} catch (SocketException e) {
							clients.remove(user);
							cs.sendAll(user.username + " disconnected.");
							break;
						}						
					}				
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		})).start();
	}
	public void sendAll(String message) {
		for (int i = 0; i < clients.size(); i++) {
			try {
				DataOutputStream out = new DataOutputStream(clients.get(i).getSocket().getOutputStream());
				out.writeUTF(message);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void sockAcc(int portNumber) {
		ChatServer cs = new ChatServer();
		try (
				ServerSocket serverSocket = new ServerSocket(portNumber);
				) {
			System.out.println("Started Chat Server");
			while (true) {
				Socket client = null;
				while (client == null) {
        			client = serverSocket.accept();
        		}
        		DataInputStream in = new DataInputStream(client.getInputStream());
        		cs.runThread(new User(client, in.readUTF()));
			}			
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
    public static void main(String... args) {
    	int portNumber = 7777;
        ChatServer cs = new ChatServer();
        cs.sockAcc(portNumber);
    }
}

class User {
	Socket socket = null;
	String username = "";
	
	public User(Socket clientSocket, String name) {
		this.socket = clientSocket;
		this.username = name;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public String getUsername() {
		return this.username;
	}
}