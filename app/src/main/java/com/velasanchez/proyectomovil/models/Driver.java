package com.velasanchez.proyectomovil.models;

public class Driver {

    String id;
    String nombre;
    String email;
    String marcaVehiculo;
    String placaVehiculo;
    String image;

    public Driver() {
    }

    public Driver(String id, String nombre, String email, String marcaVehiculo, String placaVehiculo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.marcaVehiculo = marcaVehiculo;
        this.placaVehiculo = placaVehiculo;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }

    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }
}
