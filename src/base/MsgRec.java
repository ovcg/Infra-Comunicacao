package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MsgRec implements Runnable {

	private int auxThread = 0;
	private String flag;

	public MsgRec() {
		
	}

	public void setAux(int auxThread) {
		this.auxThread = auxThread;
	}
	public String getFlag() {
		return this.flag;
	}
	public void setFlag(String flag) {
		this.flag=flag;
	}


	public void run() {
		OutputStream outputStream = null;
		InputStream inputStream = null;
		Socket socket = null;
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(3274);
			socket = serverSocket.accept();

			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();

			InputStreamReader input = new InputStreamReader(inputStream);
			BufferedReader buffer = new BufferedReader(input);
			long tempoRTT = 0;
			flag = "RTT\n";
			long tempoInicial;

			while (true) {

				while (!buffer.ready() && auxThread == 0);
				if (buffer.ready())
					buffer.readLine();
				tempoInicial = System.nanoTime();

				if (auxThread == 1) {
					break;
				}

				outputStream.write(flag.getBytes());
				outputStream.flush();

				while (!buffer.ready() && auxThread == 0);
				if (buffer.ready()) {
					if (buffer.readLine().equals("RTT2")) {
						tempoRTT = System.nanoTime() - tempoInicial;

					}
					else if(buffer.readLine().equals("CAN")) {
						setFlag("cancelar");
					}
				}
							

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
			serverSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
