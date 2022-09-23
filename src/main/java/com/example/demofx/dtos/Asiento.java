package com.example.demofx.dtos;

public class Asiento {

    private Long id;

    private String cta;

    private Double importedebe;

    private Double importehaber;

    private String numfactura;

    private String concepto;

    private Long idfactura;

    private int apunte;

    private Integer anocontable;

    private String observaciones;

    private Double total;

    private Double descuadre;

    private String codnc;

    private int idremesa;

    private String numrecibo;

    private boolean transito;

    public Asiento() {
    }

    public int isDebeHaber () {
        if (importedebe == 0.0) {
            // Cuenta haber
            return 1;
        } else if (importehaber == 0.0){
            // Cuenta debe
            return 2;
        } else {
            // Datos erroneos
            return 3;
        }
    }

    public Integer getAnocontable() {
        return anocontable;
    }

    public void setAnocontable(Integer anocontable) {
        this.anocontable = anocontable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCta() {
        return cta;
    }

    public void setCta(String cta) {
        this.cta = cta;
    }

    public Double getImportedebe() {
        return importedebe;
    }

    public void setImportedebe(Double importedebe) {
        this.importedebe = importedebe;
    }

    public Double getImportehaber() {
        return importehaber;
    }

    public void setImportehaber(Double importehaber) {
        this.importehaber = importehaber;
    }

    public String getNumfactura() {
        return numfactura;
    }

    public void setNumfactura(String numfactura) {
        this.numfactura = numfactura;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Long getIdfactura() {
        return idfactura;
    }

    public void setIdfactura(Long idfactura) {
        this.idfactura = idfactura;
    }

    public int getApunte() {
        return apunte;
    }

    public void setApunte(int apunte) {
        this.apunte = apunte;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getDescuadre() {
        return descuadre;
    }

    public void setDescuadre(Double descuadre) {
        this.descuadre = descuadre;
    }

    public String getCodnc() {
        return codnc;
    }

    public void setCodnc(String codnc) {
        this.codnc = codnc;
    }

    public int getIdremesa() {
        return idremesa;
    }

    public void setIdremesa(int idremesa) {
        this.idremesa = idremesa;
    }

    public String getNumrecibo() {
        return numrecibo;
    }

    public void setNumrecibo(String numrecibo) {
        this.numrecibo = numrecibo;
    }

    public boolean isTransito() {
        return transito;
    }

    public void setTransito(boolean transito) {
        this.transito = transito;
    }

    @Override
    public String toString() {
        return "Asiento{" +
                "cta='" + cta + '\'' +
                ", importedebe=" + importedebe +
                ", importehaber=" + importehaber +
                ", numfactura='" + numfactura + '\'' +
                ", concepto='" + concepto + '\'' +
                ", idfactura=" + idfactura +
                ", apunte=" + apunte +
                ", anocontable=" + anocontable +
                '}';
    }
}
