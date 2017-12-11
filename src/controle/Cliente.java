package controle;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import base.MsgEnv;
import base.RTTEnviando;

public class Cliente implements Runnable {

	private String ip;
	private int porta = 0;
	private String nomeArq;
	private String path;
	private volatile int enviar;
	private JProgressBar progressbar;
	private JTextPane rttEnv;
	private JTextField tempoEstimado;
	private volatile boolean pausar = false;
	private volatile boolean cancelar = false;
	private volatile boolean reiniciar = false;
	private final Object pauseLock = new Object();

	public Cliente(String ip, int porta, String nomeArq, String path, int enviar, JProgressBar progress,
			JTextPane rttEnv, JTextField tempoEstimado) {
		this.ip = ip;
		this.porta = porta;
		this.nomeArq = nomeArq;
		this.path = path;
		this.enviar = enviar;
		this.progressbar = progress;
		this.rttEnv = rttEnv;
		this.tempoEstimado = tempoEstimado;

	}

	@Override
	public void run() {

		Socket socket = null;
		FileInputStream fileInput = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		DataOutputStream out = null;

		try {
			System.out.println("Conectando-se na porta: " + porta + " e IP: " + ip);
			socket = new Socket(ip, porta);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			inputStream.read();

			byte[] buffer = new byte[5000];
			int bytesLidos = 0;
			long tamArq = 0;
			long arqEnviado = 0;
			long tempoInicial = 0;
			long atualizaTempo = 0;
			long duracao = 0;
			double vel = 0;
			double tempoRestante = 0;

			RTTEnviando rtt = new RTTEnviando(ip, rttEnv);
			MsgEnv msgenv = new MsgEnv(ip);

			Thread t = new Thread(rtt);
			t.start();
			rtt.setAux(0);

			Thread tmsg = new Thread(msgenv);
			tmsg.start();
			msgenv.setAux(0);
			msgenv.setFlag("RTT\n");

			if (enviar == 1) {

				File file = new File(path);
				tamArq = file.length();
				nomeArq = file.getName();

				byte[] cabecalho = new byte[3];
				cabecalho[0] = 0;
				cabecalho[1] = 1;
				cabecalho[2] = 1;
				byte[] corpo = nomeArq.getBytes("UTF_16");
				byte[] pacoteNome;
				pacoteNome = copiarArray(cabecalho, corpo);

				System.out.println("Cliente enviando nome do arquivo:" + nomeArq);
				outputStream.write(pacoteNome); // Enviando nome do arquivo
				inputStream.read();

				cabecalho[0] = 1;
				cabecalho[1] = 0;
				cabecalho[2] = 0;

				String ipEnv = InetAddress.getLocalHost().getHostAddress();// Enviando IP
				byte[] pacoteip;
				byte[] corpoIP = ipEnv.getBytes("UTF_16");
				pacoteip = copiarArray(cabecalho, corpoIP);
				outputStream.write(pacoteip);
				inputStream.read();
				System.out.println("Cliente enviando IP: " + ipEnv);

				int mega = 1000000;
				System.out.println("Cliente enviando tamanho do arquivo: " + tamArq / mega + " MB");

				// Envia tamanho do arquivo
				cabecalho[0] = 1;
				cabecalho[1] = 0;
				cabecalho[2] = 1;

				String a = "" + tamArq;
				byte pacoteTamArq[];
				byte[] corpoTam = a.getBytes("UTF-16");
				pacoteTamArq = copiarArray(cabecalho, corpoTam);

				outputStream.write(pacoteTamArq);
				inputStream.read();

				tempoInicial = System.currentTimeMillis();
				atualizaTempo = tempoInicial;

				fileInput = new FileInputStream(file);
				out = new DataOutputStream(outputStream);

				while ((bytesLidos = fileInput.read(buffer)) > 0) {// Enviando arquivo
					int bytes = bytesLidos;
					synchronized (pauseLock) {

						if (pausar) {
							out.write(buffer, 0, bytes);
							out.flush();
							arqEnviado += bytes;
							tempoEstimado.setText("" + 0);
							enviar = 0;
							
							pauseLock.wait();
						} else if (cancelar) {
							msgenv.setAux(2);
							tempoEstimado.setText("" + 0);
							enviar = 0;
							rtt.setAux(1);
							rtt.setRTT("0");

						} else if (reiniciar) {
							msgenv.setAux(3);
							msgenv.setFlag("cancelar");
							tempoEstimado.setText("" + 0);
							enviar = 0;
							rtt.setAux(1);
							rtt.setRTT("0");
							reinicio(tamArq, progressbar, rttEnv, tempoEstimado, file,outputStream,socket);
							break;

						} else if (arqEnviado == 100) {
							System.out.println("Transfer finalizada!");
							break;
						} else {

							out.write(buffer, 0, bytesLidos);
							out.flush();
							arqEnviado += bytesLidos;

							// Atualizando ProgessBar
							progressbar.setValue((int) ((arqEnviado * 100) / tamArq));
							progressbar.setString(Long.toString(((arqEnviado * 100) / tamArq)) + " %");
							progressbar.setStringPainted(true);

							if (arqEnviado > 10000 && (System.currentTimeMillis() - atualizaTempo) > 1000) {
								duracao = System.currentTimeMillis() - tempoInicial;
								vel = 1000 * (arqEnviado / duracao);
								tempoRestante = (tamArq - arqEnviado) / vel;
								tempoEstimado.setText(String.valueOf(new DecimalFormat("#").format(tempoRestante)));
								atualizaTempo = System.currentTimeMillis();
							}
							rtt.setAux(0);
						}

					}

				}
			}
			msgenv.setAux(1);
			tempoEstimado.setText("" + 0);
			enviar = 0;
			rtt.setAux(1);
			rtt.setRTT("0");
			inputStream.close();
			outputStream.close();
			fileInput.close();
			out.close();
			socket.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void reinicio(long tamArq,JProgressBar progressBar, JTextPane rttRec,
			JTextField tempoEstimado,File file,OutputStream outputstream,Socket socket)  {
		outputstream=null;
		try {
		outputstream=socket.getOutputStream();	
		FileInputStream fileInput=new FileInputStream(file);
		DataOutputStream out=new DataOutputStream(outputstream);
		byte[] buffer1 = new byte[5000];
		long arqEnviado = 0;
		int bytesLidos1 = 0;
		long tempoInicial = 0;
		long atualizaTempo = 0;
		long duracao = 0;
		double vel = 0;
		double tempoRestante = 0;

		while ((bytesLidos1 = fileInput.read(buffer1)) > 0) {// Enviando arquivo

			out.write(buffer1, 0, bytesLidos1);
			out.flush();
			arqEnviado += bytesLidos1;

			// Atualizando ProgessBar
			progressbar.setValue((int) ((arqEnviado * 100) / tamArq));
			progressbar.setString(Long.toString(((arqEnviado * 100) / tamArq)) + " %");
			progressbar.setStringPainted(true);

			if (arqEnviado > 10000 && (System.currentTimeMillis() - atualizaTempo) > 1000) {
				duracao = System.currentTimeMillis() - tempoInicial;
				vel = 1000 * (arqEnviado / duracao);
				tempoRestante = (tamArq - arqEnviado) / vel;
				tempoEstimado.setText(String.valueOf(new DecimalFormat("#").format(tempoRestante)));
				atualizaTempo = System.currentTimeMillis();
			}
			
		}
		fileInput.close();
		}catch(IOException e) {
			e.getStackTrace();
		}

	}

	public byte[] copiarArray(byte[] a, byte[] b) {
		byte[] c = new byte[b.length + 3];
		System.arraycopy(a, 0, c, 0, 3);
		System.arraycopy(b, 0, c, 3, b.length);
		return c;
	}

	public int getEnviar() {
		return this.enviar;
	}

	public void iniciar(int enviar) {
		this.enviar = enviar;
	}

	public void resume() {
		synchronized (pauseLock) {
			pausar = false;
			pauseLock.notifyAll();
		}
	}

	public void pausar() {
		pausar = true;
	}

	public void cancelar() {

		cancelar = true;
	}

	public void reiniciar() {

		this.reiniciar = true;
	}

}
