// Pacote: br.com.meuprojeto.main
package br.com.meuprojeto.main;

import com.google.gson.annotations.SerializedName;

public class Artigo {
    private String title;

    @SerializedName("abstract")  // Faz o mapeamento correto do campo JSON
    private String abstractText;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    @Override
    public String toString() {
        return "Título: " + title + "\nResumo: " + abstractText + "\n";
    }
}
