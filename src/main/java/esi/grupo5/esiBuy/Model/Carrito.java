package esi.grupo5.esiBuy.Model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "carritos")
public class Carrito {

    @Id
    private String id;

    private String clienteId;

}

