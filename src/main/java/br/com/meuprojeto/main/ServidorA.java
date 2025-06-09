// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServidorA {
    private static final int CLIENT_PORT = 2000;
    private static final String SERVER_B_HOST = "localhost";
    private static final int SERVER_B_PORT = 2001;
    private static final String SERVER_C_HOST = "localhost";
    private static final int SERVER_C_PORT = 2002;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);
        System.out.println("Servidor A (Coordenador) iniciado na porta " + CLIENT_PORT);
        while (true) {
            new Thread(() -> {
				try {
					handleClientRequest(serverSocket.accept());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (
            DataInputStream inFromClient = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String searchTerm = inFromClient.readUTF(); // Termo de busca é pequeno, UTF é ok.
            System.out.println("Servidor A: Recebido '" + searchTerm + "' do cliente.");

            CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> querySearchServer(SERVER_B_HOST, SERVER_B_PORT, searchTerm));
            CompletableFuture<String> futureC = CompletableFuture.supplyAsync(() -> querySearchServer(SERVER_C_HOST, SERVER_C_PORT, searchTerm));

            CompletableFuture.allOf(futureB, futureC).join();
            String responseB = futureB.get();
            String responseC = futureC.get();

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Artigo>>(){}.getType();
            List<Artigo> articlesB = gson.fromJson(responseB, listType);
            List<Artigo> articlesC = gson.fromJson(responseC, listType);
            
            List<Artigo> combinedList = new ArrayList<>();
            if (articlesB != null) combinedList.addAll(articlesB);
            if (articlesC != null) combinedList.addAll(articlesC);

            // **MUDANÇA CRÍTICA NO ENVIO PARA O CLIENTE**
            String finalJsonResponse = gson.toJson(combinedList);
            byte[] jsonBytes = finalJsonResponse.getBytes(StandardCharsets.UTF_8);
            
            outToClient.writeInt(jsonBytes.length);
            outToClient.write(jsonBytes);
            outToClient.flush();

            System.out.println("Servidor A: Enviando " + combinedList.size() + " resultados ("+jsonBytes.length+" bytes) para o cliente.");

        } catch (Exception e) {
            System.err.println("Erro no Servidor A: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String querySearchServer(String host, int port, String searchTerm) {
        try (
            Socket socket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            out.writeUTF(searchTerm);
            out.flush();

            // **MUDANÇA CRÍTICA NO RECEBIMENTO DE B/C**
            int length = in.readInt();
            if (length > 0) {
                byte[] messageBytes = new byte[length];
                in.readFully(messageBytes, 0, length);
                return new String(messageBytes, StandardCharsets.UTF_8);
            }
            return "[]";

        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o servidor em " + host + ":" + port + ". Ele está rodando?");
            return "[]";
        }
    }
}
