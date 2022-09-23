package com.example.demofx.dtos;

public class Proveedor {

    private Long id;

    private Long id_empresa;

    private String telefono;

    private String codigo;

    private String nombrecomercial;

    private String nif;

    private Object nif_pais;

    private Integer tipo;

    private Object razonsocial;

    private Direccion direccion;

    private String cuentacontable;

    public Proveedor() {
        direccion = new Direccion();
    }

    public String getCuentacontable() {
        return cuentacontable;
    }

    public void setCuentacontable(String cuentacontable) {
        this.cuentacontable = cuentacontable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(Long id_empresa) {
        this.id_empresa = id_empresa;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombrecomercial() {
        return nombrecomercial;
    }

    public void setNombrecomercial(String nombrecomercial) {
        this.nombrecomercial = nombrecomercial;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public Object getNif_pais() {
        return nif_pais;
    }

    public void setNif_pais(Object nif_pais) {
        this.nif_pais = nif_pais;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public Object getRazonsocial() {
        return razonsocial;
    }

    public void setRazonsocial(Object razonsocial) {
        this.razonsocial = razonsocial;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }
}
