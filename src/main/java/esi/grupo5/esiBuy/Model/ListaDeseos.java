package esi.grupo5.esiBuy.Model;

import java.util.ArrayList;
import java.util.List;

public class ListaDeseos {

    private List<Producto> favoritos = new ArrayList<>();

    public List<Producto> getFavoritos() { return favoritos; }
    public void setFavoritos(List<Producto> favoritos) { this.favoritos = favoritos; }
}
