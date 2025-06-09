// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class Cliente {
    private static final String SERVER_A_HOST = "localhost";
    private static final int SERVER_A_PORT = 2000;

    public static void main(String[] args) {
        try (Scanner consoleScanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nDigite o termo de busca (ou 'sair' para encerrar): ");
                String searchTerm = consoleScanner.nextLine();

                if ("sair".equalsIgnoreCase(searchTerm)) break;
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    System.out.println("Erro: O termo de busca não pode ser vazio. Por favor, tente novamente.");
                    continue;
                }

                try (
                    Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream())
                ) {
                    out.writeUTF(searchTerm);
                    out.flush();
                    System.out.println("Buscando por '" + searchTerm + "'...");

                    // **MUDANÇA CRÍTICA NO RECEBIMENTO DO SERVIDOR A**
                    int length = in.readInt();
                    String jsonResponse = "[]";
                    if (length > 0) {
                        byte[] messageBytes = new byte[length];
                        in.readFully(messageBytes, 0, length);
                        jsonResponse = new String(messageBytes, StandardCharsets.UTF_8);
                    }

                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Artigo>>(){}.getType();
                    List<Artigo> foundArticles = gson.fromJson(jsonResponse, listType);

                    System.out.println("\n--- RESULTADOS DA BUSCA ---");
                    if (foundArticles == null || foundArticles.isEmpty()) {
                        System.out.println("Nenhuma correspondência encontrada para o termo pesquisado.");
                    } else {
                        System.out.println(foundArticles.size() + " artigo(s) encontrado(s):");
                        for (Artigo artigo : foundArticles) {
                            System.out.println("--------------------");
                            System.out.println(artigo);
                        }
                    }

                } catch (UnknownHostException e) {
                    System.err.println("Erro crítico: Não foi possível conectar ao servidor principal em " + SERVER_A_HOST + ".");
                } catch (IOException e) {
                    System.err.println("Ocorreu um erro de comunicação com o servidor: " + e.getMessage());
                }

                System.out.print("\nDeseja realizar uma nova busca? (s/n): ");
                String userDecision = consoleScanner.nextLine();
                if (!"s".equalsIgnoreCase(userDecision)) break;
            }
        }
        System.out.println("\nPrograma encerrado.");
    }
}
