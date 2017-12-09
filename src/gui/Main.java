package gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import controle.Cliente;
import controle.Server;

import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class Main extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2622910333830375766L;
	private JPanel contentPane;
	private JTextField textFieldIp;
	private JTextField textFieldPort;
	private JLabel lblNomeDoArquivo;
	private JLabel lblEndereoDestino;
	private JLabel lblPortaDestino;
	private JLabel lblTamanhoDoArquivo;
	private JLabel lblIp;
	private JButton buttonParar;
	private JButton buttonReiniciar;
	private JButton buttonCancelar;
	private JTextPane rttEnv;
	private JTextPane rttRec;
	private JTextField textFieldTempoEnv;
	private JTextField textFieldTempoRec;
	private JProgressBar progressBarRecebendo;
	private JProgressBar progressBar;
	private String path;
	private String nomeArquivo;
	private File file;
	private Server server;
	private Cliente cliente;
	private int enviar = 0;

	private Thread t;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main frame = new Main();
					frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public Main() throws IOException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 666, 425);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		textFieldIp = new JTextField();
		textFieldIp.setColumns(10);
		textFieldIp.setBounds(88, 15, 117, 19);
		contentPane.add(textFieldIp);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(163, 160, 234, 25);
		contentPane.add(progressBar);
		JLabel lblRecebendo = new JLabel("Recebendo:");
		lblRecebendo.setBounds(48, 244, 108, 15);
		contentPane.add(lblRecebendo);

		JLabel label_1 = new JLabel("RTT:");
		label_1.setBounds(415, 244, 70, 15);
		contentPane.add(label_1);

		JLabel lblTempoEstimado_1 = new JLabel("Tempo Estimado:");
		lblTempoEstimado_1.setBounds(48, 297, 124, 15);
		contentPane.add(lblTempoEstimado_1);

		rttRec = new JTextPane();
		rttRec.setBounds(454, 244, 114, 19);
		contentPane.add(rttRec);

		rttEnv = new JTextPane();
		rttEnv.setBounds(454, 163, 114, 19);
		contentPane.add(rttEnv);

		textFieldTempoEnv = new JTextField();
		textFieldTempoEnv.setColumns(10);
		textFieldTempoEnv.setBounds(173, 197, 114, 19);
		contentPane.add(textFieldTempoEnv);

		textFieldTempoRec = new JTextField();
		textFieldTempoRec.setColumns(10);
		textFieldTempoRec.setBounds(176, 295, 114, 19);
		contentPane.add(textFieldTempoRec);

		JLabel lblMs = new JLabel("segundos");
		lblMs.setBounds(305, 199, 70, 15);
		contentPane.add(lblMs);

		JLabel lblSegundos = new JLabel("segundos");
		lblSegundos.setBounds(295, 299, 70, 15);
		contentPane.add(lblSegundos);
		progressBarRecebendo = new JProgressBar();
		progressBarRecebendo.setStringPainted(true);
		progressBarRecebendo.setBounds(163, 243, 234, 25);
		contentPane.add(progressBarRecebendo);

		textFieldPort = new JTextField();
		textFieldPort.setColumns(10);
		textFieldPort.setBounds(310, 15, 75, 19);
		contentPane.add(textFieldPort);

		lblNomeDoArquivo = new JLabel("Nome do arquivo: ");
		lblNomeDoArquivo.setBounds(226, 66, 348, 15);
		contentPane.add(lblNomeDoArquivo);

		lblEndereoDestino = new JLabel("IP:");
		lblEndereoDestino.setBounds(52, 17, 29, 15);
		contentPane.add(lblEndereoDestino);

		lblPortaDestino = new JLabel("Porta:");
		lblPortaDestino.setBounds(246, 17, 75, 15);
		contentPane.add(lblPortaDestino);

		lblTamanhoDoArquivo = new JLabel("Tam. do Arquivo:");
		lblTamanhoDoArquivo.setBounds(48, 102, 522, 15);
		contentPane.add(lblTamanhoDoArquivo);

		JLabel lblEnviando = new JLabel("Enviando:");
		lblEnviando.setBounds(49, 170, 108, 15);
		contentPane.add(lblEnviando);

		JLabel label_3 = new JLabel("RTT:");
		label_3.setBounds(413, 165, 70, 15);
		contentPane.add(label_3);

		JLabel lblTempoEstimado = new JLabel("Tempo Estimado:");
		lblTempoEstimado.setBounds(48, 197, 136, 15);
		contentPane.add(lblTempoEstimado);

		JLabel label_2 = new JLabel("ms");
		label_2.setBounds(572, 244, 70, 15);
		contentPane.add(label_2);

		JLabel label_4 = new JLabel("ms");
		label_4.setBounds(572, 165, 70, 15);
		contentPane.add(label_4);

		lblIp = new JLabel("IP:");
		lblIp.setBounds(48, 129, 353, 15);
		contentPane.add(lblIp);

		JButton btnEscutar = new JButton("Escutar");
		btnEscutar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int porta = Integer.parseInt(textFieldPort.getText());
				server = new Server(porta, progressBarRecebendo, rttRec, textFieldTempoRec, lblIp);

				Thread serverThread = new Thread(server);
				serverThread.start();

			}
		});
		btnEscutar.setBounds(413, 12, 117, 25);
		contentPane.add(btnEscutar);

		JButton btnNewButton = new JButton("Escolher Arquivo");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();

				int abrir = fc.showOpenDialog(null);

				if (abrir == JFileChooser.APPROVE_OPTION) {
					path = fc.getSelectedFile().getAbsolutePath();
					file = new File(path);
					nomeArquivo = file.getName();
					lblNomeDoArquivo.setText("Nome do Arquivo: " + nomeArquivo);
					lblTamanhoDoArquivo.setText("Tamanho do Arquivo: " + file.length() / 1000000 + " MB");

				}

			}
		});
		btnNewButton.setBounds(48, 61, 163, 25);
		contentPane.add(btnNewButton);

		JButton btnIniciar = new JButton("Iniciar");
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String ip = textFieldIp.getText();
				int porta = Integer.parseInt(textFieldPort.getText());
				if (enviar == 0) {
					enviar = 1;
					cliente = new Cliente(ip, porta, nomeArquivo, path, enviar, progressBar, rttEnv, textFieldTempoEnv);
					t = new Thread(cliente);
					t.start();
				} else if(cliente.getReiniciar()==1) {
					enviar = 1;
					cliente.pararEnvio(0);
				}

			}
		});
		btnIniciar.setBounds(48, 357, 117, 25);
		contentPane.add(btnIniciar);

		buttonParar = new JButton("Parar");// botão para parar
		buttonParar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cliente.pararEnvio(1);
				cliente.iniciar(0);
				JOptionPane.showMessageDialog(null, "Transferência parada!");
			}
		});
		buttonParar.setBounds(202, 357, 117, 25);
		contentPane.add(buttonParar);

		buttonReiniciar = new JButton("Reiniciar");// botão para recomecar transferencia
		buttonReiniciar.setBounds(497, 357, 117, 25);
		contentPane.add(buttonReiniciar);

		buttonReiniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cliente.reiniciar(1);
				JOptionPane.showMessageDialog(null, "Transferência reiniciada!");
				
				t.destroy();
			}
		});

		buttonCancelar = new JButton("Cancelar");// botão para cancelar
		buttonCancelar.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (enviar == 1) {
					cliente.cancelarEnvio(1);
					cliente.iniciar(0);
					enviar = 0;
					JOptionPane.showMessageDialog(null, "Transfer cancelada!");
				}
			}
		});
		buttonCancelar.setBounds(352, 357, 117, 25);
		contentPane.add(buttonCancelar);

	}
}
