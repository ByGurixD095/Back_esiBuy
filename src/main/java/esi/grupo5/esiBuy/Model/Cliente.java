package esi.grupo5.esiBuy.Model;

import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {
    private String tipoCliente;
    private List<String> direcciones = new ArrayList<>();
    private List<MetodoPago> metodosPago = new ArrayList<>();
    private List<Pedido> pedidos = new ArrayList<>();          // realiza
    private List<Valoracion> valoraciones = new ArrayList<>(); // crea
    private ListaDeseos listaDeseos = new ListaDeseos();       // tiene
    private CarritoCompra carritoCompra = new CarritoCompra(); // tiene

    public Cliente(String username, String password, String tipoCliente) {
        super(username, password);
        this.tipoCliente = tipoCliente;
    }

    //------ SETTERS & GETERS --------
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public List<String> getDirecciones() { return direcciones; }
    public List<MetodoPago> getMetodosPago() { return metodosPago; }
    public List<Pedido> getPedidos() { return pedidos; }
    public List<Valoracion> getValoraciones() { return valoraciones; }
    public ListaDeseos getListaDeseos() { return listaDeseos; }
    public CarritoCompra getCarritoCompra() { return carritoCompra; }


    //------ METHODS --------
    public List<Producto> buscarProducto(List<Producto> catalogo, String texto) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : catalogo) {
            if (p.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public void gestionarDirecciones() {
        // TODO: alta, baja y modificación de direcciones
    }

    public void gestionarMetodosPago() {
        // TODO: alta, baja y modificación de métodos de pago
    }
}
