package controle;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import base.MsgRec;
import base.RTTRecebendo;

public class Server implements Runnable {

	private int porta;
	private ServerSocket server;
	private JProgressBar progressBar;
	private JTextPane rttRec;
	private JTextField tempoEstimado;
	private JLabel lblIp;
	private JLabel nomeArqGui;

	public Server(int porta, JProgressBar progressBar, JTextPane rttRec, JTextField tempoEstimado, JLabel lblIp,
			JLabel nomeArqGui) {
		this.porta = porta;
		this.progressBar = progressBar;
		this.rttRec = rttRec;
		this.tempoEstimado = tempoEstimado;
		this.lblIp = lblIp;
		this.nomeArqGui = nomeArqGui;

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

			MsgRec msgrec = new MsgRec();
			Thread tMsg = new Thread(msgrec);
			tMsg.start();
			msgrec.setAux(0);

			// Nome do arquivo
			byte[] nomeArq = new byte[150];
			input.read(nomeArq);

			String nome = new String(pegarCorpo(nomeArq), StandardCharsets.UTF_16);

			nome = formataString(nome);
			System.out.println("Recebendo arquivo: " + nome);
			nomeArqGui.setText("Nome do arquivo: " + nome);
			output.write(prosseguir);

			tempoInicial = System.currentTimeMillis();
			atualizaTempo = tempoInicial;

			byte[] ipRecebido = new byte[150];
			input.read(ipRecebido);

			byte[] IpRecebidoAux = pegarCorpo((ipRecebido));
			String ipRec = new String(IpRecebidoAux, StandardCharsets.UTF_16);
			ipRec = formataString(ipRec);
			lblIp.setText("IP fonte: " + ipRec);
			output.write(prosseguir);

			// Recebendo tamanho do arquivo
			byte[] aux = new byte[400];
			input.read(aux);
			// ByteBuffer bufferTam = ByteBuffer.wrap(aux);
			// tamArq = bufferTam.getLong();
			byte[] tamRecAux = pegarCorpo((aux));
			String tamanhoArquivoString = new String(tamRecAux, StandardCharsets.UTF_16);
			tamanhoArquivoString = formataString(tamanhoArquivoString);
			tamArq = Long.parseLong(tamanhoArquivoString);
			output.write(prosseguir);

			System.out.println("Recebendo tamanho do arquivo: " + tamArq / 1000000 + " MB");

			File arquivo = new File("Recebidos" + File.separator + nome);
			fileOutput = new FileOutputStream(arquivo);
			data = new DataInputStream(input);

			while ((bytesLidos = data.read(buffer)) > 0) {// Recebendo o arquivo

				if (msgrec.getFlag().equalsIgnoreCase("cancelar")) {
					msgrec.setAux(1);
					tempoEstimado.setText("" + 0);
					rtt.setAux(1);
					rtt.setRTT("0");
					progressBar.setValue(0);
					progressBar.setString("0 %");
					progressBar.setStringPainted(true);
					break;

				} else if (msgrec.getFlag().equalsIgnoreCase("reiniciar")) {

					Files.deleteIfExists(arquivo.toPath());
					tempoEstimado.setText("" + 0);
					rtt.setAux(1);
					rtt.setRTT("0");
					progressBar.setValue(0);
					progressBar.setString("0 %");
					progressBar.setStringPainted(true);
					
					reinicio(bytesLidos, progressBar, rttRec, tempoEstimado,fileOutput,data);
					break;

				} else {

					fileOutput.write(buffer, 0, bytesLidos);
					fileOutput.flush();

					System.out.println(bytesLidos);
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

					if (arqRecebido == 100) {
						break;
					}
				}
			}

			msgrec.setAux(1);
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

	public void reinicio(long tamArq, JProgressBar progressBar, JTextPane rttRec,
			JTextField tempoEstimado,FileOutputStream fileOutput,DataInputStream data1) throws IOException {

		byte[] buffer1 = new byte[5000];// tam do pacote
		int bytesLidos = 0;
		long arqRecebido=0;
		long tempoInicial = 0;
		long atualizaTempo = 0;
		long duracao = 0;
		double vel = 0;
		double tempoRestante = 0;
		while ((bytesLidos = data1.read(buffer1)) > 0) {// Recebendo o arquivo

			fileOutput.write(buffer1, 0, bytesLidos);
			fileOutput.flush();

			System.out.println(bytesLidos);
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

			if (arqRecebido == 100) {
				break;
			}
		}

	}

	public byte[] pegarCorpo(byte[] a) {
		byte[] b = new byte[a.length - 3];
		int j = 0;
		for (int i = 3; i < a.length; i++) {
			b[j] = a[i];
			j++;
		}
		return b;
	}

	public String formataString(String in) {
		int pos = in.indexOf(0);
		if (pos != -1) {
			in = in.substring(0, pos);
		}

		return in;
	}

}
