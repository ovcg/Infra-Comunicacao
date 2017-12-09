package controle;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import base.RTTRecebendo;

public class Server implements Runnable {

	private int porta;
	private ServerSocket server;
	private JProgressBar progressBar;
	private JTextPane rttRec;
	private JTextField tempoEstimado;
	private int cancelar = 0;
	private int reiniciar = 0;
	private JLabel lblIp;

	public Server(int porta, JProgressBar progressBar, JTextPane rttRec, JTextField tempoEstimado, JLabel lblIp) {
		this.porta = porta;
		this.progressBar = progressBar;
		this.rttRec = rttRec;
		this.tempoEstimado = tempoEstimado;
		this.lblIp = lblIp;

	}

	@Override
	public void run() {
		InputStream input = null;
		OutputStream output = null;
		DataInputStream data = null;
		FileOutputStream fileOutput = null;

		byte prosseguir = 1;// sinal para continuar a receber os dados
		byte[] buffer = new byte[5000];// tam do pacote

		int bytesLidos = 0;// bytes lidos
		long tamArq = 0;// recebe tam do arquivo
		long arqRecebido = 0;// variavel para calcular a porcentagem na progressbar
		long tempoInicial = 0;
		long atualizaTempo = 0;
		long duracao = 0;
		double vel = 0;
		double tempoRestante = 0;

		try {
			server = new ServerSocket(porta);
			System.out.println("Escutando na porta: " + server.getLocalPort());
			Socket socket = server.accept();

			input = socket.getInputStream();
			output = socket.getOutputStream();
			output.write(prosseguir);

			RTTRecebendo rtt = new RTTRecebendo(rttRec);
			Thread t = new Thread(rtt);
			t.start();
			rtt.setAux(0);

			
			// Nome do arquivo
			byte[] nomeArq = new byte[150];
			input.read(nomeArq);
			copia(nomeArq);			
			String nome = new String(nomeArq, StandardCharsets.UTF_16);

			nome = formataString(nome);
			System.out.println("Recebendo arquivo: " + nome);
			output.write(prosseguir);

			tempoInicial = System.currentTimeMillis();
			atualizaTempo = tempoInicial;

			byte[] ipRecebido = new byte[150];
			input.read(ipRecebido);

			String ipRec = new String(ipRecebido, StandardCharsets.UTF_16);
			ipRec = formataString(ipRec);
			lblIp.setText("IP fonte: " + ipRec);
			output.write(prosseguir);

			// Recebendo tamanho do arquivo
			byte[] aux = new byte[Long.BYTES];
			input.read(aux);
			ByteBuffer bufferTam = ByteBuffer.wrap(aux);
			tamArq = bufferTam.getLong();
			output.write(prosseguir);

			System.out.println("Recebendo tamanho do arquivo: " + tamArq / 1000000 + " MB");

			File arquivo = new File("Recebidos" + File.separator + nome);
			fileOutput = new FileOutputStream(arquivo);
			data = new DataInputStream(input);

			while ((bytesLidos = data.read(buffer)) > 0) {// Recebendo o arquivo

				fileOutput.write(buffer, 0, bytesLidos);
				fileOutput.flush();

				arqRecebido += bytesLidos;

				// Atualizando ProgessBar

				progressBar.setValue((int) ((arqRecebido * 100) / tamArq));
				progressBar.setString(Long.toString((arqRecebido * 100) / tamArq) + " %");
				progressBar.setStringPainted(true);

				if (arqRecebido > 10000 && (System.currentTimeMillis() - atualizaTempo) > 1000) {

					duracao = System.currentTimeMillis() - tempoInicial;
					long div = arqRecebido / duracao;
					vel = div * 1000;
					tempoRestante = (tamArq - arqRecebido) / vel;
					DecimalFormat dec = new DecimalFormat("#");
					String auxDec = "" + dec.format(tempoRestante);
					tempoEstimado.setText(auxDec);
					atualizaTempo = System.currentTimeMillis();
				}
			}
			if (arqRecebido != tamArq) {
				Files.delete(arquivo.toPath());
			}

			tempoEstimado.setText("" + 0);
			rtt.setAux(1);
			rtt.setRTT("0");
			fileOutput.close();
			data.close();
			socket.close();
			server.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}
	
	public void copia(byte[]a) {
		byte[]b=new byte[a.length-3];
		System.arraycopy(a,3,b,0,a.length);
		a=b;
	}

	public String formataString(String in) {
		int pos = in.indexOf(0);
		if (pos != -1) {
			in = in.substring(0, pos);
		}

		return in;
	}

}
