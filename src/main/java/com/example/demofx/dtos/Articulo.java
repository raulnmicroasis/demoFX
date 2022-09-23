package com.example.demofx.dtos;

public class Articulo {

    private Long id;

    private Long id_grupo;

    private String codigo;

    private Long id_superfamilia;

    private Long iva_tipo;

    private Long id_familia;

    private String nombre;

    private Long cod_ministerioindustria;

    public Articulo() {
    }

    public Long getId_grupo() {
        return id_grupo;
    }

    public void setId_grupo(Long id_grupo) {
        this.id_grupo = id_grupo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getId_superfamilia() {
        return id_superfamilia;
    }

    public void setId_superfamilia(Long id_superfamilia) {
        this.id_superfamilia = id_superfamilia;
    }

    public Long getIva_tipo() {
        return iva_tipo;
    }

    public void setIva_tipo(Long iva_tipo) {
        this.iva_tipo = iva_tipo;
    }

    public Long getId_familia() {
        return id_familia;
    }

    public void setId_familia(Long id_familia) {
        this.id_familia = id_familia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCod_ministerioindustria() {
        return cod_ministerioindustria;
    }

    public void setCod_ministerioindustria(Long cod_ministerioindustria) {
        this.cod_ministerioindustria = cod_ministerioindustria;
    }
}
