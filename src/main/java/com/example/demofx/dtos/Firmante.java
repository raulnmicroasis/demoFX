package com.example.demofx.dtos;

public class Firmante {

    private long id;

    private long id_punto_suministro;

    private String nif;

    private String nombre;

    private int nif_pais;

    private String parentesco;

    private String cod_cliente;

    private final String email = "Sin email";

    private final String tlf1 = "SinTélefono";

    public Firmante() {
    }

    public String getCod_cliente() {
        return cod_cliente;
    }

    public void setCod_cliente(String cod_cliente) {
        this.cod_cliente = cod_cliente;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId_punto_suministro() {
        return id_punto_suministro;
    }

    public void setId_punto_suministro(long id_punto_suministro) {
        this.id_punto_suministro = id_punto_suministro;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNif_pais() {
        return nif_pais;
    }

    public void setNif_pais(int nif_pais) {
        this.nif_pais = nif_pais;
    }

    public String getParentesco() {
        return parentesco;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }

    public String getEmail() {
        return email;
    }

    public String getTlf1() {
        return tlf1;
    }
}
