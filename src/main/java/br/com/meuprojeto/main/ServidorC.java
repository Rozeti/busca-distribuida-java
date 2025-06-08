// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServidorC {
    private static final int PORT = 8002; // Porta para o Servidor C
    private static final String JSON_FILE_PATH = "data/dados_servidor_c.json";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor C iniciado na porta " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleRequest(clientSocket)).start();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String searchTerm = in.readLine().toLowerCase(); // Recebe o termo e converte para minúsculas
            System.out.println("Servidor C: Recebido termo de busca '" + searchTerm + "'");

            KMPAlgorithm kmp = new KMPAlgorithm(); // Instancia o algoritmo de busca

            List<Artigo> allArticles = loadArticlesFromJson();
            List<Artigo> foundArticles = allArticles.stream()
                .filter(artigo -> {
                    String title = artigo.getTitle() != null ? artigo.getTitle().toLowerCase() : "";
                    String abstractText = artigo.getAbstractText() != null ? artigo.getAbstractText().toLowerCase() : "";
                    // Usa o algoritmo KMP para buscar no título e no resumo
                    return kmp.search(title, searchTerm) || kmp.search(abstractText, searchTerm);
                })
                .collect(Collectors.toList());

            String jsonResponse = new Gson().toJson(foundArticles);
            out.println(jsonResponse);
            System.out.println("Servidor C: Enviando " + foundArticles.size() + " artigos encontrados.");

        } catch (IOException e) {
            System.err.println("Erro no Servidor C ao tratar requisição: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<Artigo> loadArticlesFromJson() throws IOException {
        // Crie um arquivo JSON de exemplo se ele não existir
        File dataFile = new File(JSON_FILE_PATH);
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            String exampleJson = "[{\"title\":\"Algoritmos de Consenso: Paxos vs Raft\",\"abstract\":\"Uma comparação detalhada entre os algoritmos Paxos e Raft, focando em sua complexidade e aplicação prática em sistemas distribuídos.\"},{\"title\":\"Segurança em Microsserviços\",\"abstract\":\"Este trabalho aborda padrões de segurança, como autenticação e autorização, no contexto de uma arquitetura de microsserviços.\"} ]";
            Files.write(Paths.get(JSON_FILE_PATH), exampleJson.getBytes());
        }
        String jsonContent = new String(Files.readAllBytes(Paths.get(JSON_FILE_PATH)));
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Artigo>>(){}.getType();
        return gson.fromJson(jsonContent, listType);
    }
}
