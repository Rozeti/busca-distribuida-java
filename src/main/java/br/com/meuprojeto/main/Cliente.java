// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Cliente {
    private static final String SERVER_A_HOST = "localhost";
    private static final int SERVER_A_PORT = 8000;

    public static void main(String[] args) {
        try (
            Scanner consoleScanner = new Scanner(System.in)
        ) {
            System.out.print("Digite o termo de busca (título ou resumo): ");
            String searchTerm = consoleScanner.nextLine();

            try (
                Socket socket = new Socket(SERVER_A_HOST, SERVER_A_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                // 1. Enviar termo de busca para o Servidor A
                out.println(searchTerm);
                System.out.println("Buscando por '" + searchTerm + "'...");

                // 2. Receber a resposta (lista de artigos em JSON)
                String jsonResponse = in.readLine();

                // 3. Converter JSON para Lista de Artigos e exibir
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Artigo>>(){}.getType();
                List<Artigo> foundArticles = gson.fromJson(jsonResponse, listType);

                System.out.println("\n--- RESULTADOS DA BUSCA ---");
                if (foundArticles == null || foundArticles.isEmpty()) {
                    System.out.println("Nenhum artigo encontrado.");
                } else {
                    System.out.println(foundArticles.size() + " artigo(s) encontrado(s):");
                    for (Artigo artigo : foundArticles) {
                        System.out.println("--------------------");
                        System.out.println(artigo);
                    }
                }

            } catch (Exception e) {
                System.err.println("Erro de comunicação com o servidor: " + e.getMessage());
            }

        }
    }
}