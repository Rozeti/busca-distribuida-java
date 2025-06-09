// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServidorC {
    private static final int PORT = 2002;
    private static final String JSON_FILE_PATH = "data/dados_servidor_c.json";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor B iniciado na porta " + PORT);

        while (true) {
            new Thread(() -> {
				try {
					handleRequest(serverSocket.accept());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            // A busca do termo continua igual
            String searchTerm = in.readUTF().trim().toLowerCase();
            System.out.println("Servidor C: Recebido termo de busca '" + searchTerm + "'");

            List<Artigo> foundArticles;
            if (searchTerm.isEmpty()) {
                foundArticles = new ArrayList<>();
            } else {
                KMPAlgorithm kmp = new KMPAlgorithm();
                List<Artigo> allArticles = loadArticlesFromJson();
                foundArticles = allArticles.stream()
                    .filter(artigo -> {
                        String title = artigo.getTitle() != null ? artigo.getTitle().toLowerCase() : "";
                        String abstractText = artigo.getAbstractText() != null ? artigo.getAbstractText().toLowerCase() : "";
                        return kmp.search(title, searchTerm) || kmp.search(abstractText, searchTerm);
                    })
                    .collect(Collectors.toList());
            }

            // **MUDANÇA CRÍTICA NO ENVIO DA RESPOSTA**
            String jsonResponse = new Gson().toJson(foundArticles);
            byte[] jsonBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

            // 1. Envia o tamanho do array de bytes (4 bytes)
            out.writeInt(jsonBytes.length);
            // 2. Envia o array de bytes em si
            out.write(jsonBytes);
            out.flush();

            System.out.println("Servidor C: Enviando " + foundArticles.size() + " artigos (" + jsonBytes.length + " bytes).");

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
        String jsonContent = new String(Files.readAllBytes(Paths.get(JSON_FILE_PATH)), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Artigo>>(){}.getType();
        return gson.fromJson(jsonContent, listType);
    }
}
