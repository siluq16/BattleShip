package network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

public class NetworkManager {
    private Socket socket;
    private ServerSocket serverSocket;
    private DatagramSocket udpSocket; 
    private BufferedReader in;
    private PrintWriter out;
    private NetworkListener listener;
    private boolean isHost;
    private String roomCode;
    private volatile boolean isDisconnecting = false;

    public interface NetworkListener {
        void onConnected();
        void onMessageReceived(String msg);
        void onError(String err);
    }

    public NetworkManager(NetworkListener listener) {
        this.listener = listener;
    }

    public boolean isHost() { return isHost; }
    public String getRoomCode() { return roomCode; }

    public void hostGameWithCode() {
    	isDisconnecting = false;
        roomCode = String.format("%04d", new Random().nextInt(10000));
        
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345);
                
                startUDPListener();

                socket = serverSocket.accept(); 
                
                if (udpSocket != null && !udpSocket.isClosed()) udpSocket.close();
                isHost = true;
                setupStreams();
            } catch (Exception e) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    listener.onError("Lỗi tạo phòng: " + e.getMessage());
                }
            }
        }).start();
    }

    private void startUDPListener() {
        new Thread(() -> {
            try {
                udpSocket = new DatagramSocket(8888);
                byte[] receiveData = new byte[1024];
                while (!udpSocket.isClosed()) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    udpSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    
                    if (message.equals("FIND_ROOM:" + roomCode)) {
                        byte[] sendData = "HERE_I_AM".getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
                        udpSocket.send(sendPacket);
                    }
                }
            } catch (Exception e) { }
        }).start();
    }

    public void joinGameWithCode(String code) {
    	isDisconnecting = false;
        new Thread(() -> {
            try {
                DatagramSocket clientUdp = new DatagramSocket();
                clientUdp.setSoTimeout(3000); 
                clientUdp.setBroadcast(true); 

                String msg = "FIND_ROOM:" + code;
                byte[] sendData = msg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                clientUdp.send(sendPacket);

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                try {
                    clientUdp.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    
                    if (response.equals("HERE_I_AM")) {
                        String hostIP = receivePacket.getAddress().getHostAddress();
                        clientUdp.close();
                        
                        socket = new Socket(hostIP, 12345);
                        isHost = false;
                        setupStreams();
                        return;
                    }
                } catch (SocketTimeoutException timeout) {
                    listener.onError("Không tìm thấy phòng mã: " + code + "\n(Vui lòng kiểm tra lại mã hoặc chắc chắn 2 máy đang dùng chung 1 mạng WiFi)");
                    clientUdp.close();
                }

            } catch (Exception e) {
                listener.onError("Lỗi mạng: " + e.getMessage());
            }
        }).start();
    }

    private void setupStreams() throws Exception {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new PrintWriter(socket.getOutputStream(), true);
        listener.onConnected();

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    listener.onMessageReceived(line);
                }
            } catch (Exception e) {
            	if (!isDisconnecting) {
                    listener.onError("Mất kết nối với đối thủ!");
                }
            }
        }).start();
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    public void disconnect() {
        isDisconnecting = true;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            if (udpSocket != null && !udpSocket.isClosed()) udpSocket.close();

            if (in != null) in.close();
            if (out != null) out.close();
        } catch (Exception e) {
            System.err.println("Lỗi khi disconnect: " + e.getMessage());
        }
    }
}