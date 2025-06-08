// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServidorA {
    private static final int CLIENT_PORT = 8000; // Porta para o Cliente
    private static final String SERVER_B_HOST = "localhost";
    private static final int SERVER_B_PORT = 8001;
    private static final String SERVER_C_HOST = "localhost";
    private static final int SERVER_C_PORT = 8002;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);
        System.out.println("Servidor A (Coordenador) iniciado na porta " + CLIENT_PORT);
        System.out.println("Aguardando conexões de clientes...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClientRequest(clientSocket)).start();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String searchTerm = inFromClient.readLine();
            System.out.println("Servidor A: Recebido '" + searchTerm + "' do cliente.");

            // Dispara as buscas nos servidores B e C em paralelo
            CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> querySearchServer(SERVER_B_HOST, SERVER_B_PORT, searchTerm));
            CompletableFuture<String> futureC = CompletableFuture.supplyAsync(() -> querySearchServer(SERVER_C_HOST, SERVER_C_PORT, searchTerm));
            
            // Espera os dois terminarem
            CompletableFuture.allOf(futureB, futureC).join();

            String responseB = futureB.get();
            String responseC = futureC.get();
            
            System.out.println("Servidor A: Respostas recebidas dos servidores de busca.");

            // Combina os resultados
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Artigo>>(){}.getType();
            List<Artigo> articlesB = gson.fromJson(responseB, listType);
            List<Artigo> articlesC = gson.fromJson(responseC, listType);
            
            List<Artigo> combinedList = new ArrayList<>();
            if (articlesB != null) combinedList.addAll(articlesB);
            if (articlesC != null) combinedList.addAll(articlesC);

            String finalJsonResponse = gson.toJson(combinedList);
            outToClient.println(finalJsonResponse);
            System.out.println("Servidor A: Enviando " + combinedList.size() + " resultados combinados para o cliente.");

        } catch (Exception e) {
            System.err.println("Erro no Servidor A: " + e.getMessage());
            e.printStackTrace();
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
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(searchTerm);
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Erro ao comunicar com o servidor em " + host + ":" + port + " - " + e.getMessage());
            return "[]"; // Retorna uma lista JSON vazia em caso de erro
        }
    }
}