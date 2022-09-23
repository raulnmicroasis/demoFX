package com.example.demofx.dtos;

public class Empresa {


   private Long id;
   private Boolean activo;
   private Long idgrupo;
   private String descripcion;
   private String observacion;
   private String direccion;
   private String tlf1;
   private String tlf2;
   private String tlf3;
   private String cod_pais;
   private String nif;
   private String nombre;
   private String residencia;
   private String nombre_persona_fisica;
   private String provincia;
   private String cod_postal;
   private String pais;
   private String codigo;
   private String web;
   private String descripcion_comercial;
   private Integer tipo_persona;
   private String razon_social;
   private String status;
   private String nif_pais;
   private Integer maxdigitocuenta;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getIdgrupo() {
        return idgrupo;
    }

    public void setIdgrupo(Long idgrupo) {
        this.idgrupo = idgrupo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTlf1() {
        return tlf1;
    }

    public void setTlf1(String tlf1) {
        this.tlf1 = tlf1;
    }

    public String getTlf2() {
        return tlf2;
    }

    public void setTlf2(String tlf2) {
        this.tlf2 = tlf2;
    }

    public String getTlf3() {
        return tlf3;
    }

    public void setTlf3(String tlf3) {
        this.tlf3 = tlf3;
    }

    public String getCod_pais() {
        return cod_pais;
    }

    public void setCod_pais(String cod_pais) {
        this.cod_pais = cod_pais;
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

    public String getResidencia() {
        return residencia;
    }

    public void setResidencia(String residencia) {
        this.residencia = residencia;
    }

    public String getNombre_persona_fisica() {
        return nombre_persona_fisica;
    }

    public void setNombre_persona_fisica(String nombre_persona_fisica) {
        this.nombre_persona_fisica = nombre_persona_fisica;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCod_postal() {
        return cod_postal;
    }

    public void setCod_postal(String cod_postal) {
        this.cod_postal = cod_postal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getDescripcion_comercial() {
        return descripcion_comercial;
    }

    public void setDescripcion_comercial(String descripcion_comercial) {
        this.descripcion_comercial = descripcion_comercial;
    }

    public Integer getTipo_persona() {
        return tipo_persona;
    }

    public void setTipo_persona(Integer tipo_persona) {
        this.tipo_persona = tipo_persona;
    }

    public String getRazon_social() {
        return razon_social;
    }

    public void setRazon_social(String razon_social) {
        this.razon_social = razon_social;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNif_pais() {
        return nif_pais;
    }

    public void setNif_pais(String nif_pais) {
        this.nif_pais = nif_pais;
    }

    public Integer getMaxdigitocuenta() {
        return maxdigitocuenta;
    }

    public void setMaxdigitocuenta(Integer maxdigitocuenta) {
        this.maxdigitocuenta = maxdigitocuenta;
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", activo=" + activo +
                ", idgrupo=" + idgrupo +
                ", descripcion='" + descripcion + '\'' +
                ", observacion='" + observacion + '\'' +
                ", direccion='" + direccion + '\'' +
                ", tlf1='" + tlf1 + '\'' +
                ", tlf2='" + tlf2 + '\'' +
                ", tlf3='" + tlf3 + '\'' +
                ", cod_pais='" + cod_pais + '\'' +
                ", nif='" + nif + '\'' +
                ", nombre='" + nombre + '\'' +
                ", residencia='" + residencia + '\'' +
                ", nombre_persona_fisica='" + nombre_persona_fisica + '\'' +
                ", provincia='" + provincia + '\'' +
                ", cod_postal='" + cod_postal + '\'' +
                ", pais='" + pais + '\'' +
                ", codigo='" + codigo + '\'' +
                ", web='" + web + '\'' +
                ", descripcion_comercial='" + descripcion_comercial + '\'' +
                ", tipo_persona=" + tipo_persona +
                ", razon_social='" + razon_social + '\'' +
                ", status='" + status + '\'' +
                ", nif_pais='" + nif_pais + '\'' +
                ", maxdigitocuenta=" + maxdigitocuenta +
                '}';
    }
}
