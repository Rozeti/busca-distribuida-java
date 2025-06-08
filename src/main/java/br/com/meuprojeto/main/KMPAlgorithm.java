// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

public class KMPAlgorithm {

    /**
     * Busca por um padrão em um texto usando o algoritmo Knuth-Morris-Pratt.
     * @param text O texto onde a busca será realizada.
     * @param pattern O padrão a ser encontrado.
     * @return true se o padrão for encontrado, false caso contrário.
     */
    public boolean search(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty()) {
            return false;
        }
        
        int n = text.length();
        int m = pattern.length();
        int[] lps = computeLPSArray(pattern); // Array de "Longest Proper Prefix which is also Suffix"
        
        int i = 0; // ponteiro para o texto
        int j = 0; // ponteiro para o padrão

        while (i < n) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }
            if (j == m) {
                // Padrão encontrado!
                return true; 
            } else if (i < n && pattern.charAt(j) != text.charAt(i)) {
                // Mismatch após j matches.
                // Não precisamos voltar o ponteiro do texto (i).
                // Apenas o ponteiro do padrão (j) é atualizado usando o array lps.
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    /**
     * Calcula o array LPS (Longest Proper Prefix which is also Suffix).
     * Este array nos ajuda a evitar comparações redundantes no texto.
     * @param pattern O padrão a ser pré-processado.
     * @return O array LPS.
     */
    private int[] computeLPSArray(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int length = 0; // Comprimento do maior prefixo-sufixo anterior
        int i = 1;
        lps[0] = 0; // lps[0] é sempre 0

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else {
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }
}
