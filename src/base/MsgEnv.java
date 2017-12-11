package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JTextPane;

public class MsgEnv implements Runnable {

	private String ip;
	private String flag;	
	private int auxThread = 0;

	public MsgEnv(String ip) {
		this.ip = ip;
	
	}

	public void setAux(int auxThread) {
		this.auxThread = auxThread;
	}

	public void setFlag(String flag) {
		this.flag=flag;
	}

	public void run() {
		OutputStream outputStream = null;
		InputStream inputStream = null;
		Socket socket = null;
		try {
			socket = new Socket(ip, 3270);

			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();

			InputStreamReader input = new InputStreamReader(inputStream);
			BufferedReader buffer = new BufferedReader(input);
			long tempoRTT = 0;
			flag = "RTT\n";
			long tempoInicial;

			System.out.println("Conectando-se para enviar arquivo...");
			while (true) {

				flag = "RTT\n";
				tempoInicial = System.nanoTime();
			
				outputStream.write(flag.getBytes());
				outputStream.flush();
				
				while (!buffer.ready() && auxThread == 0);
				if (buffer.ready()) {
					if (buffer.readLine().equals("RTT")) {
						tempoRTT = System.nanoTime() - tempoInicial;
					}
				}

				flag = "RTT2\n";
			
				if (auxThread == 1) {
					break;
				}
				if(auxThread==2) {
					flag="CAN\n";									
				}
				if(auxThread==3) {
					flag="REI\n";									
				}
				outputStream.write(flag.getBytes());
				outputStream.flush();

				if (auxThread == 1) {
					break;
				}

				Thread.sleep(1000);

				if (auxThread == 1) {
					break;
				}

			}

			inputStream.close();
			outputStream.close();

			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
