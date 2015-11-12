import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends JFrame {

    private JButton sendButton;
    private JTextField inputText;
    private JTextArea chat;
    
    Socket sock = new Socket();

    /**
     * Send button handler
     */
    private final ActionListener sendAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {            	
            	DataOutputStream out = new DataOutputStream(sock.getOutputStream());             
                out.writeUTF(inputText.getText());
                out.flush();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            inputText.setText("");
		}
    };

    public void init(String name) {

        initComponents();

        connect(name);
        
        startServerListener();
    }

    private void startServerListener() {
        (new Thread(new Runnable(){
			public void run() {
				try {
					while(true) {
						DataInputStream in = new DataInputStream(sock.getInputStream());
						addMessageFromServer(in.readUTF());
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		})).start();
    }

    /**
     * Adds String message to GUI component
     * @param message
     */
    private void addMessageFromServer(String message) {
        chat.append(message + "\n");
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setPreferredSize(new Dimension(300, 200));
        container.setLayout(null);
        setContentPane(container);

        JLabel inputLabel = new JLabel("Введите текст:");
        inputLabel.setSize(150, 20);
        inputLabel.setLocation(5, 105);
        container.add(inputLabel);

        sendButton = new JButton("Send");
        sendButton.setSize(80, 30);
        sendButton.setLocation(105, 125);
        container.add(sendButton);

        sendButton.addActionListener(sendAction);

        inputText = new JTextField();
        inputText.setSize(100, 20);
        inputText.setBorder(BorderFactory.createLineBorder(Color.black));
        inputText.setLocation(5, 128);
        inputText.addActionListener(sendAction);
        container.add(inputText);

        chat = new JTextArea();
        chat.setSize(200, 200);
        chat.setEditable(false);
        chat.setFont(new Font("Arial", Font.BOLD, 12));
        chat.setBorder(BorderFactory.createEtchedBorder(Color.green, Color.black));

        JScrollPane scroll = new JScrollPane(chat);
        scroll.setSize(200, 100);
        scroll.setLocation(5, 5);
        container.add(scroll);

        pack();
        setLocation(200, 150);
        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void connect(String name) {
    	String hostName = "localhost";
		int portNumber = 7777;
		Socket socket = null;
		DataOutputStream out;
		try {
			socket = new Socket(hostName, portNumber);
			sock = socket;
			out = new DataOutputStream(sock.getOutputStream());
			out.writeUTF(name);
	        out.flush();
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
    }

    public static void main(String... args) {
        ChatClient cc = new ChatClient();
        cc.init(args[0]);
    }
}
