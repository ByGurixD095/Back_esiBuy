package esi.grupo5.esiBuy.Model;

/** Copia inmutable en el tiempo de la dirección indicada al realizar un pedido. */
public class DireccionEnvioPedido {

    private String nombreCompleto;
    private String direccion;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String pais;
    private String telefono;

    public DireccionEnvioPedido() {
    }

    public DireccionEnvioPedido(
            String nombreCompleto,
            String direccion,
            String ciudad,
            String provincia,
            String codigoPostal,
            String pais,
            String telefono) {
        this.nombreCompleto = nombreCompleto;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.codigoPostal = codigoPostal;
        this.pais = pais;
        this.telefono = telefono;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
