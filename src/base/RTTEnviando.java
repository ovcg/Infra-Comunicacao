package base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JTextPane;

public class RTTEnviando implements Runnable {

	private String ip;
	private JTextPane showRTT;
	private int auxThread = 0;

	public RTTEnviando(String ip, JTextPane showRTT) {
		this.ip = ip;
		this.showRTT = showRTT;
	}

	public void setAux(int auxThread) {
		this.auxThread = auxThread;
	}

	public void setRTT(String t) {
		this.showRTT.setText(t);
	}

	public void run() {
		OutputStream outputStream = null;
		InputStream inputStream = null;
		Socket socket = null;
		try {
			socket = new Socket(ip, 3274);

			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();

			InputStreamReader input = new InputStreamReader(inputStream);
			BufferedReader buffer = new BufferedReader(input);
			long tempoRTT = 0;
			String flag = "rtt\n";
			long tempoInicial;

			System.out.println("Conectando-se para obter RTT...");
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
					flag="pause\n";
					outputStream.write(flag.getBytes());
					outputStream.flush();
					while(auxThread==2)showRTT.setText("Pause");
				}
				outputStream.write(flag.getBytes());
				outputStream.flush();

				double rttms = tempoRTT / 1000000;

				showRTT.setText(String.valueOf(rttms));

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
