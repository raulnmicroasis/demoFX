package com.example.demofx.dtos;

public class Camion {
    private Long id;

    private String codigo;

    private String nombre;

    private int tipo;

    private int numcompartimento;

    private String proveedor_trans;

    private String marca;

    private String modelo;

    private Object matricula;

    private long idProve;

    public Camion() {
    }

    public long getIdProve() {
        return idProve;
    }

    public void setIdProve(long idProve) {
        this.idProve = idProve;
    }

    public Object getMatricula() {
        return matricula;
    }

    public void setMatricula(Object matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getNumcompartimento() {
        return numcompartimento;
    }

    public void setNumcompartimento(int numcompartimento) {
        this.numcompartimento = numcompartimento;
    }

    public String getProveedor_trans() {
        return proveedor_trans;
    }

    public void setProveedor_trans(String proveedor_trans) {
        this.proveedor_trans = proveedor_trans;
    }
}
