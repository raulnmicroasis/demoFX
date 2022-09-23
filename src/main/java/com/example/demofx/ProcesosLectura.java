package com.example.demofx;

import com.example.demofx.dtos.*;
import org.apache.poi.ss.usermodel.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcesosLectura {

    boolean lecturaArticulo(Workbook workbook, Database db) {
        String reformatCell;

        String camposParaArticulo = "idgrupo, activo, baja, codigo, iva_tipo, cod_ministerioindustria, nombre, idsuperfamilia, idfamilia";
        String queryArticulo = "SELECT id FROM gasocentro.articulo WHERE codigo = ? and idgrupo = ?";
        String query = "INSERT INTO gasocentro.articulo (" + camposParaArticulo + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Sheet sheet = workbook.getSheetAt(0);
        Object cellValue = null;
        Cell cell;

        ArrayList<Long> idsFamilias = db.consultaFamilias();

        PreparedStatement statementInsert;
        try {
            statementInsert = db.connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            Articulo articulo = new Articulo();
            for (int cn = 0; cn < 3; cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();

                switch (cn) {
                    case 0 -> {
                        // Codigo
                        cellValue = codigo4Digitos(reformatCell, cellValue);
                        articulo.setCodigo((String) cellValue);
                    }
                    case 2 -> {
                        // Nombre y codigo del ministerio de industria
                        articulo.setNombre(reformatCell);
                        reformatCell = reformatCell.toUpperCase();
                        if (reformatCell.contains("GOA") || reformatCell.contains("GASOLEO A") || reformatCell.contains("GÁSOLEO A")) {
                            articulo.setCod_ministerioindustria(1L);
                        } else if (reformatCell.contains("GOB") || reformatCell.contains("GASOLEO B") || reformatCell.contains("GÁSOLEO B")) {
                            articulo.setCod_ministerioindustria(2L);
                        } else if (reformatCell.contains("GOC") || reformatCell.contains("GASOLEO C") || reformatCell.contains("GÁSOLEO C")) {
                            articulo.setCod_ministerioindustria(3L);
                        } else if (reformatCell.contains("95")) {
                            articulo.setCod_ministerioindustria(4L);
                        } else if (reformatCell.contains("98")) {
                            articulo.setCod_ministerioindustria(5L);
                        } else {
                            articulo.setCod_ministerioindustria(null);
                        }
                    }
                    case 1 -> {
                        // Tipo de iva
                        reformatCell = reformatCell.trim();
                        if (reformatCell.contains("21")) {
                            articulo.setIva_tipo(1L);
                        } else if (reformatCell.contains("10")) {
                            articulo.setIva_tipo(2L);
                        } else if (reformatCell.contains("4")) {
                            articulo.setIva_tipo(3L);
                        } else if (reformatCell.contains("exento") || reformatCell.contains("0") || reformatCell.equals("")) {
                            articulo.setIva_tipo(4L);
                        } else {
                            articulo.setIva_tipo(null);
                        }
                    }
                }
                // Id de grupo
                articulo.setId_grupo(db.idGrupo);
                // Ids de superfamilia + familia
                articulo.setId_superfamilia(idsFamilias.get(0));
                articulo.setId_familia(idsFamilias.get(1));
            }

            if (articulo.getNombre().matches("\\s*")) {
                continue;
            }


            try {
                PreparedStatement statement = db.connection.prepareStatement(queryArticulo);
                statement.setString(1, articulo.getCodigo());
                statement.setLong(2, articulo.getId_grupo());
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) {
                    statementInsert.setLong(1, articulo.getId_grupo());
                    statementInsert.setBoolean(2, true);
                    statementInsert.setBoolean(3, false);
                    statementInsert.setString(4, articulo.getCodigo());
                    statementInsert.setObject(5, articulo.getIva_tipo());
                    statementInsert.setObject(6, articulo.getCod_ministerioindustria());
                    statementInsert.setString(7, articulo.getNombre());
                    statementInsert.setLong(8, articulo.getId_superfamilia());
                    statementInsert.setLong(9, articulo.getId_familia());
                    statementInsert.addBatch();
                }
                statement.close();
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(new JFrame(), "Error insertando articulos:  \n" + e.getMessage(), "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }


        try {
            statementInsert.executeBatch();
            statementInsert.close();
            workbook.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando articulos:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    boolean lecturaPSuministro(Workbook workbook, Database db) {

        String reformatCell;
        String insertObra = "insert into gasocentro.cliente_puntosuministro (idcliente, principal, tipoinstalacion, codigo, iva_regimen, iva_tipo, " +
                "formapago, fecha_alta, fecha_modificacion, nombre_comercial, razonsocial, nif, cuentacontable, direccion, " +
                "cod_postal, municipio, provincia, ccaa, pais, nif_pais, localidad, tlf1, activo, tipo) " +
                "select cli.id, false, 3, lpad(cast(psum.codigo::integer + 1 as text), 6, '0'), 1, 1, cli.formapago, current_date, current_date, " +
                "case when ? is null then cli.nombre_comercial " +
                "else ? end, " +
                "case when ? is null then cli.razonsocial " +
                "else ? end, " +
                "cli.nif, cli.cuentacontable, ?, ?, ?, ?, ?, ?, cli.nif_pais, ?, " +
                "cli.tlf1, true, cli.tipo_persona " +
                "from gasocentro.cliente cli left join gasocentro.cliente_puntosuministro psum on cli.id = psum.idcliente " +
                "where cli.idempresa = ? and cli.codigo = ? " +
                "order by psum.codigo desc limit 1 ";

//        String consultaNumerador = "select ultnumero from gasocentro.series_numerador where idgrupo = ? and idempresa = ? and idlocal = 0 and tipodocumento = 110";

        Sheet sheet = workbook.getSheetAt(0);
        Object cellValue = null;
        Cell cell;
//        int ultnumero_series = 0;

        PreparedStatement statementInsert;
        try {
            statementInsert = db.connection.prepareStatement(insertObra);
//            PreparedStatement queryNumerador = db.connection.prepareStatement(consultaNumerador);
//            queryNumerador.setLong(1, db.idGrupo);
//            queryNumerador.setLong(2, db.idEmpresa);
//            ResultSet resultSet = queryNumerador.executeQuery();
//            if (resultSet.next()) {
//                ultnumero_series = resultSet.getInt(1);
//            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando puntos de suministro:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }


        for (Row row : sheet) {
            PuntoSuministro obra = new PuntoSuministro();
            for (int cn = 0; cn < 5; cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();

                if (row.getRowNum() == 0) {
                    continue;
                }

                switch (cn) {
                    case 0 -> {
                        // Codigo cliente
                        cellValue = codigo6Digitos(reformatCell, cellValue);
                        obra.setCodCliente(String.valueOf(cellValue));
                    }
//                    case 1 -> {} // Codigo obra ?
                    case 2 -> {
                        if (!reformatCell.matches("\\s*")) obra.setNombre_comercial(reformatCell);
                    }
                    case 3 -> obra.getDireccion().setDireccion(reformatCell); // Direccion
                    case 4 -> {
                        // Poblacion
                        try {
                            if (!reformatCell.matches("\\s*")) {
                                obra.setDireccion(db.controlMunicipioCodigoPostalConObjeto(obra.getDireccion(), reformatCell));
                            } else {
                                obra.getDireccion().setCod_postal("SinCodPostal");
                                obra.getDireccion().setLocalidad("Sin localidad");
                                obra.getDireccion().setCcaa("Sin comunidad");
                                obra.getDireccion().setPais("Sin país");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            obra.getDireccion().setCod_postal("SinCodPostal");
                            obra.getDireccion().setLocalidad("Sin localidad");
                            obra.getDireccion().setCcaa("Sin comunidad");
                            obra.getDireccion().setPais("Sin país");
                        }
                    }
                }
            }

//            ultnumero_series++;
//            obra.setCodigo(String.valueOf(codigo6Digitos(String.valueOf(ultnumero_series), cellValue)));

            try {

                statementInsert = db.connection.prepareStatement(insertObra);
                statementInsert.setString(1, obra.getNombre_comercial());
                statementInsert.setString(2, obra.getNombre_comercial());
                statementInsert.setString(3, obra.getNombre_comercial());
                statementInsert.setString(4, obra.getNombre_comercial());
                statementInsert.setString(5, obra.getDireccion().getDireccion());
                statementInsert.setString(6, obra.getDireccion().getCod_postal());
                statementInsert.setString(7, obra.getDireccion().getMunicipio());
                statementInsert.setString(8, obra.getDireccion().getProvincia());
                statementInsert.setString(9, obra.getDireccion().getCcaa());
                statementInsert.setString(10, obra.getDireccion().getPais());
                statementInsert.setLong(12, db.idEmpresa);
                statementInsert.setString(11, obra.getDireccion().getLocalidad());
                statementInsert.setString(13, obra.getCodCliente());
                System.out.println(statementInsert);
                statementInsert.execute();

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(new JFrame(), "Error insertando puntos de suministro:  \n" + e.getMessage(), "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        try {
            workbook.close();
            statementInsert.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando puntos de suministro:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }


    boolean lecturaFirmante(Workbook workbook, Database db) {
        String reformatCell;

        Sheet sheet = workbook.getSheetAt(0);
        Object cellValue = null;
        Cell cell;
        Firmante firmante;
        boolean insert;

        String queryInsert = "INSERT INTO gasocentro.cliente_puntosuministro_contacto (idpuntosuministro, departamento, nombre_completo, nif, copiadni, parentesco, email, " +
                "tlf1, nif_pais, es_firmante_nif, es_firmante_declaracion, es_firmante, rgpd_firmante, dcf_firmante) select psum.id, 0, ?, ?, false, ?, ?, ?, ?, false, false, true, false, false " +
                "from gasocentro.cliente_puntosuministro psum inner join gasocentro.cliente cli on psum.idcliente = cli.id where cli.idempresa = ? and cli.codigo = ?";
        PreparedStatement statementInsert;

        try {
            statementInsert = db.connection.prepareStatement(queryInsert);
            statementInsert.setLong(7, db.idEmpresa);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando firmantes:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            firmante = new Firmante();
            insert = true;
            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();

                switch (cn) {
                    case 0 -> cellValue = codigo6Digitos(reformatCell, cellValue); // Codigo Cliente
                    case 1 -> firmante.setNombre(reformatCell); // Nombre firmante
                    case 2 -> {
                        // Nif
                        ArrayList<Object> datosNif = validarNifConObjeto(cellValue);
                        firmante.setNif(String.valueOf(datosNif.get(0)));
                        firmante.setNif_pais((Integer) datosNif.get(1));
                    }
                    case 3 -> {
                        if (reformatCell.trim().equals("DUENO") || reformatCell.trim().equals("DUENA"))
                            firmante.setParentesco(reformatCell.replace("N", "Ñ"));
                        else firmante.setParentesco(reformatCell);
                    }
                }
            }

            if (firmante.getNombre().matches("\\s*")) {
                continue;
            }

            if (insert) {
                try {
                    statementInsert.setString(1, firmante.getNombre());
                    statementInsert.setString(2, firmante.getNif());
                    statementInsert.setString(3, firmante.getParentesco());
                    statementInsert.setString(4, firmante.getEmail());
                    statementInsert.setString(5, firmante.getTlf1());
                    statementInsert.setInt(6, firmante.getNif_pais());
                    statementInsert.setString(8, firmante.getCod_cliente());
                    statementInsert.addBatch();
                    System.out.println(statementInsert);

                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(new JFrame(), "Error insertando firmantes:  \n" + e.getMessage(), "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }


        try {
            statementInsert.executeBatch();
            statementInsert.close();
            workbook.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando clientes:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    boolean lecturaProv(Workbook workbook, Database db) {


        CallableStatement insert = null;
        try {
            insert = db.connection.prepareCall("{ call gasocentro.insert_prov_direccion(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            insert.setLong(1, db.idEmpresa);
            insert.setLong(2, db.idGrupo);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        int maxDigCuenta = 0;
        try {
            maxDigCuenta = db.consultaMaximoDigitosCuenta();
        } catch (Exception e) {
            return false;
        }


        Sheet sheet = workbook.getSheetAt(0);
        Object cellValue = null;
        String reformatCell;
        int ultimoCodigo = 0;


        ArrayList<Object> datosNif;

        for (Row row : sheet) {
            Proveedor prov = new Proveedor();
            if (row.getRowNum() == 0) {
                continue;
            }

            if (getCellValue(row.getCell(0)).equals("\u001A") || getCellValue(row.getCell(0)).equals("&")) break;

            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();

                switch (cn) {
                    case 0 -> {
                        // Codigo
                        cellValue = codigo4Digitos(reformatCell, cellValue);
                        prov.setCodigo(String.valueOf(cellValue));
                    }
                    case 1 -> {
                        // Nombre
                        if (reformatCell.matches("\\s*")) {
                            prov.setNombrecomercial("Sin nombre");
                        } else {
                            prov.setNombrecomercial(reformatCell);
                        }
                    }
                    case 5 -> {
                        // CIF
                        datosNif = validarNifConObjeto(cellValue);
                        prov.setNif(String.valueOf(datosNif.get(0)));
                        prov.setNif_pais(datosNif.get(1));
                        if (datosNif.get(2) != null) {
                            prov.setRazonsocial(prov.getNombrecomercial());
                        } else {
                            prov.setRazonsocial(datosNif.get(2));
                        }
                        prov.setTipo((Integer) datosNif.get(3));
                    }
                    case 2 -> {
                        // Direccion
                        if (reformatCell.matches("\\s*")) {
                            prov.getDireccion().setDireccion("Sin dirección");
                        } else {
                            prov.getDireccion().setDireccion(reformatCell);
                        }
                    }
                    case 3 -> {
                        try {
                            // Poblacion + código postal
                            if (!reformatCell.matches("\\s*")) {
                                prov.setDireccion(db.controlMunicipioCodigoPostalConObjeto(prov.getDireccion(), reformatCell));
                            } else {
                                prov.getDireccion().setCod_postal("SinCodPostal");
                                prov.getDireccion().setLocalidad("Sin localidad");
                                prov.getDireccion().setMunicipio("Sin municipio");
                                prov.getDireccion().setCcaa("Sin comunidad");
                                prov.getDireccion().setPais("Sin país");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            prov.getDireccion().setCod_postal("SinCodPostal");
                            prov.getDireccion().setLocalidad("Sin localidad");
                            prov.getDireccion().setMunicipio("Sin municipio");
                            prov.getDireccion().setCcaa("Sin comunidad");
                            prov.getDireccion().setPais("Sin país");
                        }
                    }
                    case 4 -> {
                        // Provincia
                        if (!reformatCell.matches("\\s*")) {
                            prov.getDireccion().setProvincia(reformatCell);
                        } else {
                            prov.getDireccion().setProvincia("Sin provincia");
                        }
                    }
                }
            }

            if (row.getRowNum() == sheet.getLastRowNum()) {
                ultimoCodigo = Integer.parseInt(prov.getCodigo());
            }

            reformatCell = "400" + prov.getCodigo();
            prov.setCuentacontable(String.valueOf(formatCuenta(reformatCell, cellValue, maxDigCuenta, "400")));

            if (prov.getDireccion().getDireccion().equals("Sin dirección") && prov.getDireccion().getMunicipio().equals("Sin municipio") && prov.getDireccion().getProvincia().equals("Sin provincia")) {
                prov.setDireccion(new Direccion());
            }

            // Id de empresa
            prov.setId_empresa(db.idEmpresa);


            if (prov.getNombrecomercial() == null) continue;


            try {
                insert.setString(3, prov.getNombrecomercial());
                insert.setString(4, prov.getNif());
                insert.setObject(5, prov.getNif_pais());
                insert.setObject(6, prov.getRazonsocial());
                insert.setInt(7, prov.getTipo());
                insert.setString(8, prov.getDireccion().getDireccion());
                insert.setString(9, prov.getDireccion().getCod_postal());
                insert.setString(10, prov.getDireccion().getLocalidad());
                insert.setString(11, prov.getDireccion().getMunicipio());
                insert.setString(12, prov.getDireccion().getProvincia());
                insert.setString(13, prov.getDireccion().getCcaa());
                insert.setString(14, prov.getDireccion().getPais());
                insert.setString(15, prov.getCodigo());
                insert.setString(16, prov.getCuentacontable());
                System.out.println(insert);
                insert.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(new JFrame(), "Error insertando proveedores:  \n" + e.getMessage(), "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        try {

//            String updateSeriesNumerador = "UPDATE gasocentro.series_numerador set ultnumero = ? where idempresa = ? and tipodocumento = 107";
//            PreparedStatement statement = db.connection.prepareStatement(updateSeriesNumerador);
//            statement.setInt(1, ultimoCodigo);
//            statement.setLong(2, db.idEmpresa);
//            statement.executeUpdate();

            workbook.close();
            insert.executeBatch();
            insert.close();
//            statement.close();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando proveedores:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
        return true;
    }

    boolean lecturaFactura(Workbook workbook, Database db) {

        int x = 0;
        Sheet sheet = workbook.getSheetAt(x);
        String reformatCell;

        Object cellValue = null;
        Factura factura;
        Cell cell;

        CallableStatement insert = null;
        try {
            insert = db.connection.prepareCall("{ call gasocentro.insert_fac_mas_albaran(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage() + "\n" + e.getNextException());
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando facturas:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        for (Row row : sheet) {

            factura = new Factura();
            if (row.getRowNum() == 0) {
                continue;
            }

            if (getCellValue(row.getCell(0)).equals("\u001A") || getCellValue(row.getCell(0)).equals("&")) break;

            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                switch (cn) {
                    case 0 -> {
                        // Número factura
                        if (reformatCell.matches("\\s*")) {
                            break;
                        }
                        if (cellValue instanceof Double) {
                            reformatCell = reformatCell.replace(".0", "");
                        }
                        factura.setNumfactura(reformatCell.replace("-", "").trim());
                        factura.setTipofactura(factura.getNumfactura().matches("[A-Z]\\d*") ? 1 : 2);

                    }
                    case 1 -> {
                        // Fecha emision + anocontable
                        reformatCell = getFecha(reformatCell, cellValue);
                        cellValue = stringToDate(reformatCell);
                        if (((LocalDate) cellValue).getYear() < 2017) {
                            factura.setAnocontable(null);
                            factura.setFechaemision(null);
                        } else {
                            factura.setAnocontable(((LocalDate) cellValue).getYear());
                            factura.setFechaemision((LocalDate) cellValue);
                        }
                    }
                    case 2 -> {
                        // Fecha albaran
                        if (cellValue instanceof Double) {
                            reformatCell = reformatCell.replace(".0", "");
                        }

                        if (!reformatCell.matches("\\s*")) {
                            reformatCell = getFecha(reformatCell, cellValue);
                            cellValue = stringToDate(reformatCell);
                            factura.getAlbaran().setFecha((LocalDate) cellValue);
                        }
                    }
                    case 3 -> {
                        // Número albarán
                        if (cellValue instanceof Double) {
                            reformatCell = reformatCell.replace(".0", "");
                        }
                        factura.getAlbaran().setNumero(reformatCell.replace("-", "").trim());
                    }
                    case 4 -> {
                        // Codigo punto suministro
                        if (reformatCell.matches("^\\d{0,6}")) {
                            if (reformatCell.matches("\\s*")) {
                                factura.getPuntoSuministro().setPrincipal(true);
                            } else {
                                factura.getPuntoSuministro().setPrincipal(false);
                                factura.getPuntoSuministro().setCodigo(String.valueOf(codigo6Digitos(reformatCell, cellValue)));
                            }
                        }
                    }
                    case 5 -> {
                        // Código cliente
                        if (reformatCell.matches("\\s*")) {
                            break;
                        }
                        cellValue = codigo6Digitos(reformatCell, cellValue);
                        factura.getCliente().setCodigo(String.valueOf(cellValue));
                        if (factura.getPuntoSuministro().isPrincipal())
                            factura.getPuntoSuministro().setCodigo(factura.getCliente().getCodigo());
                    }
                    case 7 -> {
                        // Codigo articulo
                        if (reformatCell.matches("\\s*")) {
                            break;
                        }
                        cellValue = codigo4Digitos(reformatCell, cellValue);
                        if (cellValue.equals("00GB")) {
                            factura.getAlbaran().setCod_producto("0001");
                        } else if (cellValue.equals("00GC")) {
                            factura.getAlbaran().setCod_producto("0002");
                        }
                    }
                    case 8 -> {
                        // Cantidad
                        reformatCell = reformatCell.replaceAll("\\s*", "").replace(",", ".");
                        factura.getAlbaran().setCanttempamb(Double.parseDouble(reformatCell));
                    }
                    case 9 -> {
                        // Precio unitario
                        reformatCell = reformatCell.replace(",", ".");
                        factura.getAlbaran().setPrecioUnidad(Double.parseDouble(reformatCell));
                    }
                    case 10 -> {
                        // Descuento
                        reformatCell = reformatCell.replace("-", "");
                        try {
                            factura.getAlbaran().setDescuento(Double.parseDouble(reformatCell));
                        } catch (Exception ex) {
                            factura.getAlbaran().setDescuento(0.0);
                        }
                    }
                    case 11 -> {
                        // Tipo iva
                        if (reformatCell.contains("21")) {
                            factura.getAlbaran().setTipoiva(1);
                            factura.getAlbaran().setIva(Integer.parseInt(reformatCell.replace(".0", "")));
                        } else if (reformatCell.contains("10")) {
                            factura.getAlbaran().setTipoiva(2);
                            factura.getAlbaran().setIva(Integer.parseInt(reformatCell.replace(".0", "")));
                        } else if (reformatCell.contains("4")) {
                            factura.getAlbaran().setTipoiva(3);
                            factura.getAlbaran().setIva(Integer.parseInt(reformatCell.replace(".0", "")));
                        } else {
                            factura.getAlbaran().setTipoiva(4);
                            factura.getAlbaran().setIva(0);
                        }
                    }
                }
            }
            if (factura.getAnocontable() != null && factura.getAlbaran().getFecha() != null) {
                try {
                    insert.setLong(1, db.idGrupo);
                    insert.setLong(2, db.idEmpresa);
                    insert.setLong(3, db.idLocal);
                    insert.setString(4, db.grupo);
                    insert.setString(5, db.empresa);
                    insert.setString(6, db.local);
                    insert.setString(7, factura.getNumfactura());
                    insert.setInt(8, factura.getAnocontable());
                    insert.setString(9, String.valueOf(factura.getFechaemision()));
                    insert.setString(10, factura.getPuntoSuministro().getCodigo());
                    insert.setString(11, factura.getCliente().getCodigo());
                    insert.setString(12, String.valueOf(factura.getAlbaran().getFecha()));
                    insert.setString(13, factura.getAlbaran().getNumero());
                    insert.setDouble(14, factura.getAlbaran().getCanttempamb());
                    insert.setInt(15, factura.getAlbaran().getTipoiva());
                    insert.setDouble(16, factura.getAlbaran().getImporte());
                    insert.setString(17, factura.getAlbaran().getCod_producto());
                    insert.setDouble(18, factura.getAlbaran().getPrecioUnidad());
                    insert.setDouble(19, factura.getAlbaran().getDescuento());
                    insert.setDouble(20, factura.getAlbaran().getImporte_iva());
                    insert.setString(21, db.codlocal);
                    insert.setInt(22, factura.getTipofactura());
                    System.out.println(insert);
                    insert.addBatch();
                } catch (SQLException e) {
                    System.out.println("ERROR: " + e.getMessage() + "\n" + e.getNextException());
                    JOptionPane.showMessageDialog(new JFrame(), "Error insertando facturas:  \n" + e.getMessage(), "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        try {
            insert.executeBatch();
            insert.close();
            workbook.close();
            String updateTotales = "update gasocentro.tesoreria_factura fac " +
                    "set total = (select sum(importe) from gasocentro.tesoreria_factura_albaran tfa where tfa.idfactura = fac.id) " +
                    "where fac.idlocal = ?";
            PreparedStatement statement = db.connection.prepareStatement(updateTotales);
            statement.setLong(1, db.idLocal);
            statement.executeUpdate();
            statement.close();
            statement.close();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), "Error insertando facturas:  \n" + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    boolean updateBanco(Workbook workbook, Database db) {

        String dropTriggers = "DROP TRIGGER IF EXISTS update_client_func ON gasocentro.cliente_banco;" +
                "DROP TRIGGER IF EXISTS update_client_func ON gasocentro.cliente_facturacion;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_banco;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_facturacion;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_finalidad;";

        try {
            db.connection.createStatement().execute(dropTriggers);
        } catch (SQLException e) {
            db.createTriggers();
            e.printStackTrace();
        }

        Sheet sheet = workbook.getSheetAt(0);
        // Dato de la celda
        Object cellValue = null;
        Cell cell;
        String reformatCell, bic = null, entidad = null;

        String updateBancosCliente = "update gasocentro.cliente_banco ban set bic = ?  " +
                "where ban.cod_entidad = ?;";
        String updateBancosEmpresa = "update gasocentro.empresa_banco ban set bic = ? " +
                "where ban.idempresa = ? and ban.cod_entidad = ?;";
        String updateBancosPSum = "update gasocentro.cliente_puntosuministro_banco ban set bic = ?  " +
                "where ban.cod_entidad = ?;";

        PreparedStatement updateCliente = null;
        PreparedStatement updateEmpresa = null;
        PreparedStatement updatePSum = null;

        try {
            updateCliente = db.connection.prepareStatement(updateBancosCliente);
            updateEmpresa = db.connection.prepareStatement(updateBancosEmpresa);
            updatePSum = db.connection.prepareStatement(updateBancosPSum);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            for (Row row : sheet) {

                bic = null;
                entidad = null;

                for (int cn = 0; cn < 2; cn++) {
                    cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cellValue = getCellValue(cell);
                    reformatCell = cellValue.toString();

                    switch (cn) {
                        case 0 -> entidad = String.valueOf(codigo4Digitos(reformatCell.trim(), cellValue));
                            //Cod entidad
                        case 1 -> bic = reformatCell.trim();
                            //Cod bic
                    }
                }

                if (bic != null && entidad != null) {
                    updateCliente.setString(1, bic);
                    updateCliente.setString(2, entidad);
                    updateEmpresa.setString(1, bic);
                    updateEmpresa.setLong(2, db.idEmpresa);
                    updateEmpresa.setString(3, entidad);
                    updatePSum.setString(1, bic);
                    updatePSum.setString(2, entidad);
                    System.out.println(updateCliente);
                    System.out.println(updateEmpresa);
                    System.out.println(updatePSum);
                    updateEmpresa.addBatch();
                    updateCliente.addBatch();
                    updatePSum.addBatch();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            updateEmpresa.executeBatch();
            updateCliente.executeBatch();
            updatePSum.executeBatch();
            updateEmpresa.close();
            updateCliente.close();
            updatePSum.close();
            db.createTriggers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    boolean updateCliente(Workbook workbook, Database db) {

        Cliente cliente;

        Sheet sheet = workbook.getSheetAt(0);
        // Dato de la celda
        Object cellValue = null;
        Cell cell;
        String reformatCell;

        String queryBusquedaMunicipio = "SELECT municipio, municipio_codigo FROM gasocentro.busca_direccion_vw WHERE codigo_postal = ?";
        String queryUpdate = "update gasocentro.cliente set municipio = ?, localidad = ? where idempresa = ? and codigo = ?";

        PreparedStatement update = null;
        PreparedStatement queryDireccion = null;
        ResultSet resultSet = null;

        try {
            update = db.connection.prepareStatement(queryUpdate);
            queryDireccion = db.connection.prepareStatement(queryBusquedaMunicipio);
            update.setLong(3, db.idEmpresa);
        } catch (SQLException e) {
            e.printStackTrace();
            db.createTriggers();
            return false;
        }

        try {
            for (Row row : sheet) {
                cliente = new Cliente();

                if (row.getRowNum() == 0) {
                    continue;
                }

                for (int cn = 0; cn < 2; cn++) {
                    cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cellValue = getCellValue(cell);
                    reformatCell = cellValue.toString();
                    switch (cn) {
                        case 0 -> {
                            //Codigo
                            cliente.setCodigo(String.valueOf(codigo6Digitos(reformatCell, cellValue)));
                        }
                        case 1 -> {
                            //Codigo postal
                            cliente.setDireccion(db.controlMunicipioCodigoPostalConObjeto(cliente.getDireccion(), reformatCell));
                        }
                    }
                }
                queryDireccion.setString(1, cliente.getDireccion().getCod_postal());
                resultSet = queryDireccion.executeQuery();
                if (resultSet.next()) {
                    cliente.getDireccion().setMunicipio(resultSet.getString(2) + " - " + resultSet.getString(1));
                } else {
                    cliente.getDireccion().setMunicipio("Sin Municipio");
                    cliente.getDireccion().setLocalidad("Sin Localidad");
                }
                update.setString(1, cliente.getDireccion().getMunicipio());
                update.setString(2, cliente.getDireccion().getLocalidad());
                update.setString(4, cliente.getCodigo());
                System.out.println(update);
                update.addBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            update.executeBatch();
            update.close();
            queryDireccion.close();
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    boolean lecturaBancos(Workbook workbook, Database db) {
        Banco banco;
        String iban, cod_entidad, cod_sucursal, dc, numero_cuenta, reformatCell;

        Sheet sheet = workbook.getSheetAt(0);
        // Dato de la celda
        Object cellValue = null;
        Cell cell;
        int maxDigCuenta = 0;
        try {
            maxDigCuenta = db.consultaMaximoDigitosCuenta();
        } catch (Exception e) {
            return false;
        }

        String insertBanco = "insert into gasocentro.empresa_banco (idempresa, cod_entidad, cod_sucursal, dc, numero_cuenta, iban, bic,   " +
                "    cuenta_contable, direccion, cp, poblacion, provincia, principal, es_caja, denominacion, ctariesgo, observaciones, efectoscartera, efectosgestion, efectosdescontados, efectosimpagados)   " +
                "    select ?, ?, ?, ?, ?, ?,  " +
                "    case when (select bic from gasocentro.lista_bic_swift where nrbe = ?) is null then 'SinDato' " +
                "    else (select bic from gasocentro.lista_bic_swift where nrbe = ?) end, " +
                "    ?, ? , ?, ?, ?, ?, ?, " +
                "    case when (select denominacion from gasocentro.lista_bic_swift where nrbe = ?) is null then ? " +
                "    else (select denominacion from gasocentro.lista_bic_swift where nrbe = ?) end, " +
                "    ?, ?, '4310', '4312', '4311', '4315'";


        PreparedStatement insert;
        try {
            insert = db.connection.prepareStatement(insertBanco);
            insert.setLong(1, db.idEmpresa);
            insert.setBoolean(14, false);
            insert.setBoolean(15, false);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            for (Row row : sheet) {
                banco = new Banco();

                if (row.getRowNum() == 0) {
                    continue;
                }

                for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                    cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cellValue = getCellValue(cell);
                    reformatCell = cellValue.toString();

                    if (reformatCell.equals("\u0003")) break;

                    switch (cn) {
                        case 0 -> {
                            //Código banco
                            banco.setObservacion("Código interno de banco: " + reformatCell.trim().replace(".0", "") + ". ");
                        }
                        case 1 -> {
                            // Nombre
                            if (reformatCell.matches("\\s*")) {
                                banco.setDenominacion("Sin nombre de banco");
                            } else {
                                banco.setDenominacion(reformatCell);
                            }
                        }
                        case 2 -> {
                            // Direccion
                            if (reformatCell.matches("\\s*")) {
                                banco.getDireccion().setDireccion("Sin dirección");
                            } else {
                                banco.getDireccion().setDireccion(reformatCell);
                            }
                        }
                        case 3 -> {
                            // Poblacion + codigo postal
                            if (!reformatCell.matches("\\s*")) {
                                banco.setDireccion(db.controlMunicipioCodigoPostalConObjeto(banco.getDireccion(), reformatCell));
                                banco.getDireccion().setProvincia(banco.getDireccion().getLocalidad());
                            } else {
                                banco.getDireccion().setCod_postal("SinCodPostal");
                                banco.getDireccion().setLocalidad("Sin localidad");
                                banco.getDireccion().setCcaa("Sin comunidad");
                                banco.getDireccion().setPais("Sin país");
                            }

                        }
                        case 5 -> {
                            // Cuenta contable
                            reformatCell = new BigDecimal(cellValue.toString().replace(".0", "")).toPlainString();
                            if (reformatCell.startsWith("572")) { // Cuenta contable
                                cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, "572");
                                banco.setCuentacontable(String.valueOf(cellValue));
                                cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, "520");
                                banco.setCuentariesgo(String.valueOf(cellValue));
                            } else if (reformatCell.startsWith("520")){ // Cuenta riesgo
                                cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, "520");
                                banco.setCuentariesgo(String.valueOf(cellValue));
                                banco.setCuentacontable(String.valueOf(cellValue));
                            } else if (reformatCell.startsWith("570")) { // Cuenta contable
                                cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, "570");
                                banco.setCuentacontable(String.valueOf(cellValue));
                                cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, "520");
                                banco.setCuentariesgo(String.valueOf(cellValue));
                            } else { // Número sin prefijo
                                cellValue = formatCuenta("572" + reformatCell, cellValue, maxDigCuenta, "572");
                                banco.setCuentacontable(String.valueOf(cellValue));
                                cellValue = formatCuenta("520" + reformatCell, cellValue, maxDigCuenta, "520");
                                banco.setCuentariesgo(String.valueOf(cellValue));
                            }

                        }
                        case 6 -> {
                            // CCC
                            reformatCell = reformatCell.toUpperCase().replace(" ", "").replace("-", "");
                            if (reformatCell.length() == 24) {
                                iban = reformatCell.substring(0, 4);
                                cod_entidad = reformatCell.substring(4, 8);
                                cod_sucursal = reformatCell.substring(8, 12);
                                dc = reformatCell.substring(12, 14);
                                numero_cuenta = reformatCell.substring(14, 24);
                                banco.setIban(iban);
                                banco.setCod_entidad(cod_entidad);
                                banco.setCod_sucursal(cod_sucursal);
                                banco.setDc(dc);
                                banco.setNumero_cuenta(numero_cuenta);
                            } else {
                                banco.setIban("SinI");
                                banco.setCod_entidad("SinE");
                                banco.setCod_sucursal("SinS");
                                banco.setDc("SN");
                                banco.setNumero_cuenta("SinNCuenta");
                                banco.setObservacion(banco.getObservacion() + "Datos de banco erróneos: " + reformatCell + ". ");
                            }
                        }
                    }
                }

                try {
                    insert.setString(2, banco.getCod_entidad());
                    insert.setString(3, banco.getCod_sucursal());
                    insert.setString(4, banco.getDc());
                    insert.setString(5, banco.getNumero_cuenta());
                    insert.setString(6, banco.getIban());
                    insert.setString(7, banco.getCod_entidad());
                    insert.setString(8, banco.getCod_entidad());
                    insert.setObject(9, banco.getCuentacontable());
                    insert.setString(10, banco.getDireccion().getDireccion());
                    insert.setString(11, banco.getDireccion().getCod_postal());
                    insert.setString(12, banco.getDireccion().getLocalidad());
                    insert.setString(13, banco.getDireccion().getProvincia());
                    insert.setString(16, banco.getCod_entidad());
                    insert.setString(17, banco.getDenominacion());
                    insert.setString(18, banco.getCod_entidad());
                    insert.setObject(19, banco.getCuentariesgo());
                    insert.setObject(20, banco.getObservacion());
                    System.out.println(insert);
                    insert.addBatch();

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            try {

                workbook.close();
                insert.executeBatch();
                insert.close();

            } catch (IOException | SQLException e) {
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    boolean lecturaCliente(Workbook workbook, Database db) {

        String dropTriggers = "DROP TRIGGER IF EXISTS update_client_func ON gasocentro.cliente_banco;" +
                "DROP TRIGGER IF EXISTS update_client_func ON gasocentro.cliente_facturacion;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_banco;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_facturacion;" +
                "DROP TRIGGER IF EXISTS update_psuministro_func ON gasocentro.cliente_puntosuministro_finalidad;";

        try {
            db.connection.createStatement().execute(dropTriggers);
        } catch (SQLException e) {
            db.createTriggers();
            e.printStackTrace();
        }

        Cliente cliente;
        String iban, cod_entidad, cod_sucursal, dc, numero_cuenta, reformatCell;
        String tlf;
        boolean havecuenta;

        Sheet sheet = workbook.getSheetAt(0);
        // Dato de la celda
        Object cellValue = null;
        ArrayList<Object> datosNif;
        Cell cell;

        int maxDigCuenta;
        CallableStatement insert;

        try {
            maxDigCuenta = db.consultaMaximoDigitosCuenta();
            insert = db.connection.prepareCall("{ call gasocentro.insert_cliente_mas_puntosuministro_banco(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            insert.setLong(1, db.idEmpresa);
        } catch (Exception e) {
            db.createTriggers();
            return false;
        }

        sheet = workbook.getSheetAt(0);
        Row lastRow = sheet.getRow(sheet.getLastRowNum() - 1);
        Cell lastCell = lastRow.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        Object lastCellValue = null;
        lastCellValue = getCellValue(lastCell);
        int lastValueOnString = Integer.parseInt(lastCellValue.toString().replace(".0", ""));


        ArrayList<Integer> codigos = new ArrayList<>();

        try {
            for (Row row : sheet) {
                cliente = new Cliente();
                havecuenta = true;
                Integer vto1 = null;

                if (row.getRowNum() == 0) {
                    continue;
                }

                for (int cn = 0; cn < 29; cn++) {
                    cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cellValue = getCellValue(cell);
                    reformatCell = String.valueOf(cellValue);

                    if (reformatCell.equals("\u001A") || reformatCell.equals("&")) break;

                    switch (cn) {
                        case 0 -> {
                            // Codigo cliente
                            if (reformatCell.matches("\\s*")) {
                                continue;
                            }
                            if (codigos.contains(Integer.parseInt(String.valueOf(cellValue).replace(".0", "")))) {
                                lastValueOnString++;
                                cellValue = String.format("%6s", lastValueOnString).replace(" ", "0");
                            } else {
                                codigos.add(Integer.parseInt(String.valueOf(cellValue).replace(".0", "")));
                                cellValue = codigo6Digitos(reformatCell, cellValue);
                            }
                            cliente.setCodigo(String.valueOf(cellValue));
                        }
                        case 1 -> {
                            // Cuenta contable
                            reformatCell = new BigDecimal(cellValue.toString().replace(".0", "")).toPlainString();
                            if (reformatCell.startsWith("43")) {
                                cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, "430");
                            } else {
                                cellValue = null;
                            }
                            cliente.setCuentacontable(String.valueOf(cellValue));
                        }
                        case 2 -> {
                            // Nombre fiscal
                            if (reformatCell.matches("\\s*")) {
                                cliente.setRazonsocial("Sin nombre referenciado");
                            } else {
                                cliente.setRazonsocial(reformatCell);
                            }
                        }
                        case 3 -> {
                            // Nombre comercial
                            if (reformatCell.matches("\\s*")) {
                                cliente.setNombre_comercial(null);
                            } else {
                                cliente.setNombre_comercial(reformatCell);
                            }
                        }
                        case 4 -> {
                            // Direccion
                            if (reformatCell.matches("\\s*")) {
                                cliente.getDireccion().setDireccion("Sin dirección");
                            } else {
                                cliente.getDireccion().setDireccion(reformatCell);
                            }
                        }
                        case 5 -> {
                            // Poblacion + codigo postal
                            if (!reformatCell.matches("\\s*")) {
                                cliente.setDireccion(db.controlMunicipioCodigoPostalConObjeto(cliente.getDireccion(), reformatCell));
                            } else {
                                cliente.getDireccion().setCod_postal("SinCodPostal");
                                cliente.getDireccion().setLocalidad("Sin localidad");
                                cliente.getDireccion().setCcaa("Sin comunidad");
                                cliente.getDireccion().setPais("Sin país");
                            }
                        }
                        case 6 -> {
                            // Provincia
                            if (!reformatCell.matches("\\s*")) {
                                cliente.getDireccion().setProvincia(reformatCell);
                            } else {
                                cliente.getDireccion().setProvincia("Sin provincia");
                            }

                        }
                        case 7 -> {
                            // Cif
                            datosNif = validarNifConObjeto(cellValue);
                            cliente.setNif(String.valueOf(datosNif.get(0)));
                            cliente.setNif_pais((Integer) datosNif.get(1));
                            if (datosNif.get(2) != null) {
                                if (cliente.getNombre_comercial() == null) cliente.setNombre_comercial(cliente.getRazonsocial());
                            } else {
                                cliente.setNombre_comercial(cliente.getRazonsocial());
                            }
                            cliente.setTipoPersona((Integer) datosNif.get(3));
                        }
                        case 9 -> {
                            // Banco
                            if (!reformatCell.matches("\\s*")) {
                                cliente.getBanco().setDenominacion(reformatCell);
                            } else {
                                cliente.getBanco().setDenominacion("Sin nombre de banco");
                            }
                        }
                        case 8 -> {
                            // Telefono
                            if (reformatCell.matches("\\s*")) {
                                cliente.setTlf1("SinTélefono");
                            } else {
                                if (cellValue instanceof Double) reformatCell = new BigDecimal(cellValue.toString()).toPlainString().replace(".0", "");
                                reformatCell = reformatCell.replace("-", "").replace(".", "").replaceAll("^\\s", "");
                                String[] telefonos = reformatCell.split("\\s");
                                boolean breakFor = false;

                                for (int i = 0; i < telefonos.length; i++) {
                                    telefonos[i] = telefonos[i].trim();
                                    if (telefonos[i].length() == 9) {
                                        tlf = telefonos[i].substring(0, 3) + " " + telefonos[i].substring(3, 6) + " " + telefonos[i].substring(6, 9);
                                    } else if (telefonos[i].length() == 6) {
                                        tlf = "920" + telefonos[i];
                                        tlf = tlf.substring(0, 3) + " " + tlf.substring(3, 6) + " " + tlf.substring(6, 9);
                                    } else {
                                        tlf = "SinTélefono";
                                    }
                                    switch (i) {
                                        case 0 -> cliente.setTlf1(tlf);
                                        case 1 -> cliente.setTlf2(tlf);
                                        case 2 -> cliente.setTlf3(tlf);
                                        default -> breakFor = true;
                                    }
                                    if (breakFor) break;
                                }
                            }

                        }
                        case 10 -> {
                            // Sucursal
                            if (!reformatCell.matches("\\s*")) {
                                cliente.getBanco().setObservacion("Sucursal: " + reformatCell + ". ");
                            }
                        }
                        case 11 -> {
                            // CCC
                            reformatCell = reformatCell.toUpperCase();
                            if (reformatCell.matches("^[A-Z]{2}\\d{6}-\\d{4}-\\d{2}-\\d{10}")) {
                                String[] parts = reformatCell.split("-");
                                iban = parts[0].substring(0, 4);
                                cod_entidad = parts[0].substring(4);
                                cod_sucursal = parts[1];
                                dc = parts[2];
                                numero_cuenta = parts[3];
                                cliente.getBanco().setIban(iban);
                                cliente.getBanco().setCod_entidad(cod_entidad);
                                cliente.getBanco().setCod_sucursal(cod_sucursal);
                                cliente.getBanco().setDc(dc);
                                cliente.getBanco().setNumero_cuenta(numero_cuenta);
                                havecuenta = true;
                            } else if (reformatCell.contains("-")) {
                                cliente.getBanco().setIban("SinI");
                                cliente.getBanco().setCod_entidad("SinE");
                                cliente.getBanco().setCod_sucursal("SinS");
                                cliente.getBanco().setDc("SN");
                                cliente.getBanco().setNumero_cuenta("SinNCuenta");
                                cliente.getBanco().setObservacion(cliente.getBanco().getObservacion() + "Datos de banco erróneos: " + reformatCell + ". ");
                                havecuenta = true;
                            } else {
                                cliente.getBanco().setIban("SinI");
                                cliente.getBanco().setCod_entidad("SinE");
                                cliente.getBanco().setCod_sucursal("SinS");
                                cliente.getBanco().setDc("SN");
                                cliente.getBanco().setNumero_cuenta("SinNCuenta");
                                cliente.getBanco().setObservacion(cliente.getBanco().getObservacion() + "Sin datos de banco. ");
                                havecuenta = false;
                            }
                        }
                        case 12 -> cliente.setObservacionPuntoSuministro(switch (Integer.parseInt(reformatCell.replace(".0", ""))) {
                            case 1 -> "Cliente de Gasóleo A. ";
                            case 2 -> "Cliente de Agricultura. ";
                            case 3 -> "Cliente de Calefacción. ";
                            case 4 -> "Cliente Resto. ";
                            case 5 -> "Cliente Venta Fin de Mes. ";
                            default -> "";
                        });
                        case 13 -> {
                            try {
                                reformatCell = getFecha(reformatCell, cellValue);
                                cellValue = stringToDate(reformatCell);
                                cliente.getBanco().setFechaautoriza_core((LocalDate) cellValue);
                            } catch (Exception e) {
                                cliente.getBanco().setFechaautoriza_core(LocalDate.of(2009, 10, 31));
                            }
                        }
                        case 14 -> {
                            if (reformatCell.matches("\\s*")) {
                                if (cliente.getObservacionPuntoSuministro().equals("")) cliente.setObservacionPuntoSuministro(null);
                            } else if (reformatCell.matches("\\d+")){
                                reformatCell = new BigDecimal(cellValue.toString().trim().replace("-", "").replace(".0", "")).toPlainString();
                                cliente.setObservacionPuntoSuministro(cliente.getObservacionPuntoSuministro() + "Venta por Agencia " + reformatCell + ". ");
                            } else {
                                cliente.setObservacionPuntoSuministro(cliente.getObservacionPuntoSuministro() + "Venta por Agencia " + reformatCell + ". ");
                            }
                        }
                        case 16, 17, 18, 19, 20, 21 -> {
                            // Otros
                            if (!reformatCell.matches("\\s*")) {
                                cliente.setObservacion(cliente.getObservacion() + reformatCell + ". ");
                            }
                        }
//                        case 24 -> {
//                            try {
//                                vto1 = Integer.parseInt(reformatCell.replace(".0", ""));
//                            } catch (Exception e) {
//                                vto1 = null;
//                            }
//                        }
//                        case 25 -> {
//                            try {
//                                vto2 = Integer.parseInt(reformatCell.replace(".0", ""));
//                            } catch (Exception e) {
//                                vto2 = null;
//                            }
//                        }
//                        case 26 -> {
//                            try {
//                                vto3 = Integer.parseInt(reformatCell.replace(".0", ""));
//                            } catch (Exception e) {
//                                vto3 = null;
//                            }
//                        }
                        case 27 -> {
                            try {
                                cliente.setDiapago1(Integer.parseInt(reformatCell.replace(".0", "")));
                            } catch (Exception e) {
                                cliente.setDiapago1(0);
                            }
                        }
                        case 28 -> {
                            try {
                                cliente.setDiapago2(Integer.parseInt(reformatCell.replace(".0", "")));
                            } catch (Exception e) {
                                cliente.setDiapago2(0);
                            }
                        }
                    }
                }

                if (cliente.getNombre_comercial() == null) continue;

                if (cliente.getBanco().getDenominacion().equals("Sin nombre de banco") && !havecuenta) {
                    cliente.setBanco(new Banco());
                } else {
                    cliente.getBanco().setRefmandato_core(cliente.getNif() + "CORE001");
                    cliente.getBanco().setRefmandato_b2b(cliente.getNif() + "B2B0001");
                }

//                if (vto1 == null) {
//                    vto1 = 0;
//                    cliente.setNumero_vencimientos(1);
//                } else if (vto2 == null) {
//                    cliente.setVencimiento(vto1);
//                    cliente.setNumero_vencimientos(1);
//                } else if (vto3 == null) {
//                    cliente.setVencimiento(vto2);
//                    cliente.setNumero_vencimientos(2);
//                } else {
//                    cliente.setVencimiento(vto1);
//                    cliente.setNumero_vencimientos(3);
//                }
//                if (vto1 == null) {
//                    cliente.setVencimiento(0);
//                    cliente.setTipo_formapago(19);
//                }
//                else if (vto1 <= 4 ) {
//                    cliente.setVencimiento(2);
//                    cliente.setTipo_formapago(2);
//                }
//                else if (vto1 < 90) {
//                    cliente.setVencimiento(15);
//                    cliente.setTipo_formapago(2);
//                }
//                else {
//                    cliente.setVencimiento(90);
//                    cliente.setTipo_formapago(2);
//                }

                try {
                    insert.setString(2, cliente.getCodigo());
                    insert.setString(3, cliente.getCuentacontable());
                    insert.setString(4, cliente.getNombre_comercial());
                    insert.setString(5, cliente.getDireccion().getDireccion());
                    insert.setString(6, cliente.getDireccion().getCod_postal());
                    insert.setString(7, cliente.getDireccion().getLocalidad());
                    insert.setString(8, cliente.getDireccion().getMunicipio());
                    insert.setString(9, cliente.getDireccion().getCcaa());
                    insert.setString(10, cliente.getDireccion().getPais());
                    insert.setString(11, cliente.getDireccion().getProvincia());
                    insert.setString(12, cliente.getNif());
                    insert.setObject(13, cliente.getNif_pais());
                    insert.setObject(14, cliente.getRazonsocial());
                    insert.setInt(15, cliente.getTipoPersona());
                    insert.setString(16, cliente.getTlf1());
                    insert.setString(17, cliente.getBanco().getDenominacion());
                    insert.setString(18, cliente.getBanco().getIban());
                    insert.setString(19, cliente.getBanco().getCod_entidad());
                    insert.setString(20, cliente.getBanco().getCod_sucursal());
                    insert.setString(21, cliente.getBanco().getDc());
                    insert.setString(22, cliente.getBanco().getNumero_cuenta());
                    insert.setString(23, cliente.getBanco().getObservacion());
                    insert.setString(24, cliente.getBanco().getRefmandato_core());
                    insert.setString(25, cliente.getBanco().getRefmandato_b2b());
                    insert.setObject(26, cliente.getBanco().getFechaautoriza_core());
                    insert.setObject(27, cliente.getTlf2());
                    insert.setObject(28, cliente.getTlf3());
                    insert.setInt(29, 1);
                    insert.setInt(30, 1); // Numero_vencimientos
                    insert.setString(31, cliente.getObservacion());
                    insert.setInt(32, 3); // Tipo forma pago
                    insert.setObject(33, cliente.getObservacionPuntoSuministro());
                    insert.setObject(34, cliente.getDiapago1());
                    insert.setObject(35, cliente.getDiapago2());
                    System.out.println(insert);
                    insert.addBatch();

                } catch (SQLException e) {
                    e.printStackTrace();
                    db.createTriggers();
                    return false;
                }
            }


            try {

                workbook.close();
                insert.executeBatch();
                insert.close();

            } catch (IOException | SQLException e) {
                e.printStackTrace();
                db.createTriggers();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            db.createTriggers();
            return false;
        }

        db.createTriggers();
        return true;
    }

    boolean lecturaAsientos(@NotNull Workbook workbook, Database db) {
        String camposAsientosDebe = "idfactura, ctadebe, concepto, numfactura, importedebe";
        String camposAsientosHaber = "idfactura, ctahaber, concepto, numfactura, importehaber";
        String insertAsientoDebe = "INSERT INTO gasocentro.tesoreria_asiento (" + camposAsientosDebe + ") VALUES (?, ?, ?, ?, ?)";
        String insertAsientoHaber = "INSERT INTO gasocentro.tesoreria_asiento (" + camposAsientosHaber + ") VALUES (?, ?, ?, ?, ?)";
        String queryFac = "SELECT id from gasocentro.tesoreria_factura WHERE numfactura = ? and anocontable = ? and idlocal = ?";


        Asiento asiento;
        String reformatCell;
        Sheet sheet = workbook.getSheetAt(0);
        Object cellValue = null;
        Cell cell;
        Pattern pattern = Pattern.compile("(FRA\\.?\\s*[A-Z]?\\d{1,4})");
        Matcher matcher;
        int maxDigCuenta = 0;
        try {
            maxDigCuenta = db.consultaMaximoDigitosCuenta();
        } catch (Exception e) {
            return false;
        }


        for (Row row : sheet) {
            asiento = new Asiento();
            if (row.getRowNum() == 0) {
                continue;
            }
            for (int cn = 0; cn < 7; cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                switch (cn) {
                    case 0 -> {
                        // Fecha
                        reformatCell = getFecha(reformatCell, cellValue);
                        if (reformatCell.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            cellValue = stringToDate(reformatCell);
                            cellValue = ((LocalDate) cellValue).getYear();
                            asiento.setAnocontable((Integer) cellValue);
                        }
                    }
                    case 1 -> {
                        // Nasiento
                    }
                    case 2 -> {
                        // Cuenta
                        if (reformatCell.matches("\\s*")) {
                            cellValue = "SinCuenta";
                        } else {
                            cellValue = formatCuenta(reformatCell, cellValue, maxDigCuenta, null);
                        }
                        asiento.setCta(String.valueOf(cellValue));
                    }
                    case 3 -> asiento.setConcepto(reformatCell); // Descripcion = concepto
                    case 4 -> {
                        // Concepto = nFactura
                        matcher = pattern.matcher(reformatCell);
                        if (matcher.find()) {
                            reformatCell = matcher.group(1);
                            reformatCell = reformatCell.trim().replaceAll("^FRA\\.?\\s*", "");
                            asiento.setNumfactura(reformatCell);
                        } else {
                            cn = 7;
                        }
                    }
                    case 5 -> {
                        // Importe debe
                        if (!reformatCell.matches("\\s*")) {
                            reformatCell = reformatCell.replace(",", ".");
                            asiento.setImportedebe(Double.parseDouble(reformatCell));
                        } else {
                            asiento.setImportedebe(0.0);
                        }
                    }
                    case 6 -> {
                        // Importe haber
                        if (!reformatCell.matches("\\s*")) {
                            reformatCell = reformatCell.replace(",", ".");
                            asiento.setImportehaber(Double.parseDouble(reformatCell));
                        } else {
                            asiento.setImportehaber(0.0);
                        }
                    }
                }
            }
            if (asiento.getNumfactura() != null && asiento.getAnocontable() != null) {
                try {
                    PreparedStatement statementInsertHaber = db.connection.prepareStatement(insertAsientoHaber);
                    PreparedStatement statementInsertDebe = db.connection.prepareStatement(insertAsientoDebe);


                    PreparedStatement statement = db.connection.prepareStatement(queryFac);
                    statement.setString(1, asiento.getNumfactura());
                    statement.setInt(2, asiento.getAnocontable());
                    statement.setLong(3, db.idLocal);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        asiento.setIdfactura(resultSet.getLong(1));
                        if (asiento.isDebeHaber() == 1) {
                            // Cuenta haber -> importedebe = 0.0
                            statementInsertHaber.setLong(1, asiento.getIdfactura());
                            statementInsertHaber.setString(2, asiento.getCta());
                            statementInsertHaber.setString(3, asiento.getConcepto());
                            statementInsertHaber.setString(4, asiento.getNumfactura());
                            statementInsertHaber.setDouble(5, asiento.getImportehaber());
                            statementInsertHaber.addBatch();

                        } else if (asiento.isDebeHaber() == 2) {
                            // Cuenta debe -> importehaber = 0.0
                            statementInsertDebe.setLong(1, asiento.getIdfactura());
                            statementInsertDebe.setString(2, asiento.getCta());
                            statementInsertDebe.setString(3, asiento.getConcepto());
                            statementInsertDebe.setString(4, asiento.getNumfactura());
                            statementInsertDebe.setDouble(5, asiento.getImportedebe());
                            statementInsertDebe.addBatch();
                        }
                    }
                    statement.close();
                    resultSet.close();
                    statementInsertHaber.executeBatch();
                    statementInsertDebe.executeBatch();
                    statementInsertHaber.close();
                    statementInsertDebe.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    boolean lecturaRecibos(Workbook workbook, Database db) {
        String reformatCell, numRecibo, codlocal;
        int contadorRecibo = 1;
        Sheet sheet = workbook.getSheetAt(0);
        // Dato de la celda
        Object cellValue = null;
        Cell cell;

        PreparedStatement statementInsert;
        PreparedStatement statement;
        ResultSet resultSet;

        String queryLocal = "SELECT codigo from gasocentro.local where id = ?";

        String queryInsert = "insert into gasocentro.tesoreria_factura_recibo (fechacreacion, fechamodificacion, idfactura, idgrupo, idempresa, idlocal, grupo, " +
                "empresa, local, tiporecibo, numrecibo, idformapago, idagrupaformapago, formapago, agrupaformapago, gastoscomision, " +
                "gastosdevolucion, gastosrenegociacion, docnuestropoder, fusionado, idcliente, idpuntosuministro, puntosuministro, " +
                "cliente, codcliente, codpuntosuministro, nifcliente, nifpuntosuministro, nifpaiscliente, nifpaispuntosuministro, " +
                "razonsocial, ctacontablepuntosuministro, ctacontablecliente, clientedireccion, clientecodpostal, clientelocalidad, " +
                "clientemunicipio, clienteprovincia, clientecomarca, clientepais, clienteca, gestionrecibo, adjuntarfactura, " +
                "adjuntaralbaran, importe, estado, ctafacturacion, remitido, anocontable, numfactura, tipofactura, " +
                "fechafactura, importefactura, procesado, esrecibo, codlocal, nombrebancoprincipal, ibanbancoprincipal, " +
                "entidadbancoprincipal, sucursalbancoprincipal, dcbancoprincipal, numcuentabancoprincipal, swiftbicbancoprincipal, " +
                "authcoremandato, refmandatocore, authb2bmandato, refmandatob2b, fechamandatocore, fechamandatob2b, requierebanco, giro, fechavencimiento) " +
                "select ?, ?, fac.id, ?, ?, ?, ?, ?, ?, 1, ?, cli.formapago, grufp.id, fp.descripcion, grufp.descripcion, " +
                "0, 0, 0, false, false, cli.id, psum.id, cli.nombre_comercial, psum.nombre_comercial, cli.codigo, psum.codigo, " +
                "cli.nif, psum.nif, cli.nif_pais, psum.nif_pais, cli.razonsocial, psum.cuentacontable, cli.cuentacontable, " +
                "cli.direccion, cli.cod_postal, cli.localidad, cli.municipio, cli.provincia, cli.comarca, cli.pais, cli.ccaa, " +
                "0, false, false, ?, 10, cli.cuentacontable, false, fac.anocontable, fac.numfactura, fac.tipofactura, fac.fechaalta, " +
                "fac.totalapagar, false, true, ?, ban.denominacion, ban.iban, ban.cod_entidad, ban.cod_sucursal, " +
                "ban.dc, ban.numero_cuenta, ban.bic, ban.autorizapago_core, ban.refmandato_core, ban.autorizapago_b2b, " +
                "ban.refmandato_b2b, ban.fechaautoriza_core, ban.fechaautoriza_b2b, true, " +
                "(select count(id)+1 from gasocentro.tesoreria_factura_recibo where idfactura = fac.id) as numgiro, ?" +
                "from gasocentro.tesoreria_factura fac, gasocentro.cliente cli inner join gasocentro.cliente_puntosuministro psum " +
                "on cli.id = psum.idcliente inner join gasocentro.cliente_banco ban on cli.id = ban.idcliente inner join " +
                "gasocentro.empresa_formas_pago fp on fp.id = cli.formapago inner join gasocentro.empresa_grupo_formas_pago grufp " +
                "on grufp.id = fp.nombre_agrupacion " +
                "where cli.idempresa = ? and cli.codigo = ? and fac.idlocal = ? and fac.numfactura = ? and fac.anocontable = ?";
        try {
            statementInsert = db.connection.prepareStatement(queryInsert);
            statementInsert.setDate(1, Date.valueOf(LocalDate.now()));
            statementInsert.setDate(2, Date.valueOf(LocalDate.now()));
            statementInsert.setLong(3, db.idGrupo);
            statementInsert.setLong(4, db.idEmpresa);
            statementInsert.setLong(5, db.idLocal);
            statementInsert.setString(6, db.grupo);
            statementInsert.setString(7, db.empresa);
            statementInsert.setString(8, db.local);
            statementInsert.setLong(13, db.idEmpresa);
            statementInsert.setLong(15, db.idLocal);

            statement = db.connection.prepareStatement(queryLocal);
            statement.setLong(1, db.idLocal);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                codlocal = resultSet.getString(1);
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


        for (Row row : sheet) {
            boolean shouldBreak = false;
            Recibo recibo = new Recibo();

            if (row.getRowNum() == 0) {
                continue;
            }

            if (getCellValue(row.getCell(0)).equals("\u001A") || getCellValue(row.getCell(0)).equals("&")) break;

            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                try {
                    switch (cn) {
                        case 0 ->
                                recibo.setCodCliente(String.valueOf(codigo6Digitos(reformatCell, cellValue))); // Cliente
                        case 1 -> {
                            // Fecha vto
                            reformatCell = getFecha(reformatCell, cellValue);
                            cellValue = stringToDate(reformatCell);

                            recibo.setAnocontable(((LocalDate) cellValue).getYear());
                            recibo.setFecha((LocalDate) cellValue);
                        }
                        case 2 ->
                                recibo.setNumfactura(reformatCell.replace(".0", "").replace("-", "").trim()); // Factura
                        case 3 -> recibo.setImporte(Double.parseDouble(reformatCell.replace(",", "."))); // Importe
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: " + e.getMessage());
                    shouldBreak = true;
                }
                if (shouldBreak) break;


            }

            numRecibo = String.valueOf(recibo.getAnocontable()).substring(2) + "CC" + codlocal + codigo4Digitos(String.valueOf(contadorRecibo), cellValue);
            recibo.setNumrecibo(numRecibo);
            contadorRecibo++;

            if (recibo.getFecha() == null) continue;

            try {
                statementInsert.setString(9, recibo.getNumrecibo());
                statementInsert.setDouble(10, recibo.getImporte());
                statementInsert.setString(11, codlocal);
                statementInsert.setDate(12, Date.valueOf(recibo.getFecha()));
                statementInsert.setString(14, recibo.getCodCliente());
                statementInsert.setString(16, recibo.getNumfactura());
                statementInsert.setInt(17, recibo.getAnocontable());
                System.out.println(statementInsert);
                statementInsert.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }

        }

        try {
            String updateOrden = "update gasocentro.tesoreria_factura_recibo tfa set orden = concat(tfa.giro,'/',(select count(idfactura) from gasocentro.tesoreria_factura_recibo where idlocal = ? and idfactura = tfa.idfactura group by idfactura)) where idlocal = ?";
            statement = db.connection.prepareStatement(updateOrden);
            statement.setLong(1, db.idLocal);
            statement.setLong(2, db.idLocal);
            statement.executeUpdate();

            statementInsert.close();
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    boolean lecturaCompras(Workbook workbook, Database db) {

        Camion camionGenerico = db.consultaCamiones("");

        String insertDirectas = "insert into gasocentro.venta_directa (idempresa, idlocal, fechavalidacion, fecharecepcion, fechapreparacion, horavalidacion, " +
                "horarecepcion, horapreparacion, idtipoorigen, reflocal, idtipodocuentrega, idmodotransporte, idtipodestino, idtipotransporte, idtipoorganizador, idtransportista, " +
                "idvehiculo, kmsinicio, totalizainicio, kmsfinal, totalizafinal, idpuntosuministro, puntosuministro, idformapagopuntosuministro, nifpuntosuministro, " +
                "caepuntosuministro, direccionpuntosuministro, cppuntosuministro, provinciapuntosuministro, paispuntosuministro, nifcliente, " +
                "nombrecliente, idformapagocliente, direccioncliente, cpcliente, municipiocliente, provinciacliente, cacliente, paiscliente, " +
                "estado, fechafactura, fechaexpedicion, horaexpedicion, autoridadfiscal, nombrelocal, caelocal, direccionlocal, cplocal, provincialocal, " +
                "nifpaislocal, niflocal, nombretransportista, nifpaistransportista, niftransportista, ctatransportista, direcciontransportista, cptransportista, " +
                "ciudadtransportista, paistransportista, nombrevehiculo, duraciondias, duracionhoras, nifpaispuntosuministro, nifpaiscliente, codlocal, " +
                "nombreemisor, emisormensaje, regfiscal, fechaalta, presentaciondiferida, altaasientos, origen, bonificado, identificadorvehiculo) " +
                "select ?, ?, ?, ?, ?, '12:00:00', '12:00:00', '12:00:00', 1, ?, 2, 3, 2, 3, 1, prove.id, ?, 0, 0, 0, 0, psum.id, psum.nombre_comercial, psum.formapago, " +
                "psum.nif, psum.cae, psum.direccion, psum.cod_postal, psum.provincia, psum.pais, cli.nif, cli.nombre_comercial, cli.formapago, cli.direccion, cli.cod_postal, " +
                "cli.municipio, cli.provincia, cli.ccaa, cli.pais, 1, ?, ?, '12:00:00', ofi.autoridad, loc.nombre_local, loc.cae, loc.direccion, loc.cod_postal, loc.provincia, " +
                "loc.nif_pais, loc.nif, prove.nombrecomercial, prove.nif_pais, prove.nif, prove.cuentacontable, dir.direccion, dir.cod_postal, dir.localidad, dir.pais, " +
                "?, 1, 24, 'ES', 'ES', loc.codigo, emi.representante, emi.nif, 'F', ?, false, false, 1, false, ? " +
                "from gasocentro.empresa_certificado emi, gasocentro.local loc, " +
                "gasocentro.local_firmante fir inner join gasocentro.lista_oficina_gestora ofi on fir.idgestora = ofi.id, " +
                "gasocentro.proveedor prove inner join gasocentro.proveedor_direccion dir on dir.idproveedor = prove.id, " +
                "gasocentro.cliente cli inner join gasocentro.cliente_puntosuministro psum on psum.idcliente = cli.id " +
                "where cli.codigo = ? and psum.principal = true and loc.id = ? and fir.idlocal = ? and prove.id = ? and cli.idempresa = ? and dir.tipo = 11 " +
                "limit 1 returning id";

        String insertPartidaVenta = "insert into gasocentro.venta_directa_partida (idventa, numpartida, idarticulo, idmarcafiscal, idcompartimento, estado, epigrafe, " +
                "codnc, regfiscal, marcador, ieg, iee, canttempamb, canttempquince, undfiscales, tembalaje, codembalaje, denstempamb, denstempquince, " +
                "tempamb, numonu, codarticulo, nombrearticulo, precio, descuento, portes, proddensmin, proddensmax, importetotal, facturado, baseimponible, importeiva, " +
                "totalbonificacion, totalapagar, bonificado, fechafactura) " +
                "select ?, 1, art.id, ?, 1, 1, " +
                "case when art.cod_aduanero = 3 then 'B3' " +
                "when art.cod_aduanero = 4 then 'B4' " +
                "end, " +
                "case when art.cod_nc = 1 then '27102011' " +
                "when art.cod_nc = 2 then '27101943' " +
                "when art.cod_nc = 3 then '27101947' " +
                "when art.cod_nc = 4 then '27101245' " +
                "when art.cod_nc = 5 then '27101249' " +
                "end, " +
                "'F', " +
                "art.trazador, art.impuestoespecial_general, art.impuestoespecial_estatal, ?, ?, 'LTR', " +
                "'Líquido, a granel', 'VL', " +
                "round ((case when art.densidad15grados_min = 1 then 0.820 " +
                "when art.densidad15grados_min = 2 then 0.720 end " +
                "+ " +
                "case when art.densidad15grados_max = 1 then 0.845 " +
                "when art.densidad15grados_max = 2 then 0.880 " +
                "when art.densidad15grados_max = 3 then 0.900 " +
                "when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                "round ((case when art.densidad15grados_min = 1 then 0.820 " +
                "when art.densidad15grados_min = 2 then 0.720 end " +
                "+ " +
                "case when art.densidad15grados_max = 1 then 0.845 " +
                "when art.densidad15grados_max = 2 then 0.880 " +
                "when art.densidad15grados_max = 3 then 0.900 " +
                "when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                "15, art.onua1_num_valor, art.codigo, art.nombre, 0, 0, 0, art.densidad15grados_min, art.densidad15grados_max, " +
                "0, true, 0, 0, 0, 0, false, ? " +
                "from gasocentro.articulo art where art.codigo = ? and art.idgrupo = ?";

        String insertCompras = "insert into gasocentro.compra (idempresa, idlocal, idsuministrador, factura, idformapago, fecharecepcion, horarecepcion, idparquecarga, " +
                "idtipoorigen, idmodotransporte, idtipodestino, idtipotransporte, idvehiculo, estado, nifpaisparquecarga, nifparquecarga, " +
                "caeparquecarga, numsalidadf, fechaexpedicion, horaexpedicion, nombrelocal, caelocal, direccionlocal, cplocal, " +
                "provincialocal, nifpaislocal, niflocal, paislocal, nombreparquecarga, nombretransportista, nifpaistransportista, niftransportista, " +
                "ctatransportista, direcciontransportista, cptransportista, ciudadtransportista, paistransportista, nombrevehiculo, " +
                "duraciondias, duracionhoras, anoordencarga, codlocal, altaasientos, origen, ctasuministrador, idfpagosuministador, identificadorvehiculo, arc, " +
                "arcsecuencia, idtipodocuentrega, idtipoorganizador, suministrador, idalmacenista) " +
                "select ?, ?, prove.id, 2, prove.formapago, ?, '12:00:00', parque.id, 1, 3, 2, 3, fve.id, 1, " +
                "case when parque.nif_pais = 6 then 'ES' " +
                "when parque.nif_pais <> 6 then null end, " +
                "parque.nif, " +
                "parque.cae, ?, ?, '12:00:00', loc.nombre_local, loc.cae, loc.direccion, loc.cod_postal, loc.provincia, loc.nif_pais, loc.nif, loc.pais, " +
                "parque.nombre, prove.nombrecomercial, " +
                "case when prove.nif_pais = 6 then 'ES' " +
                "when prove.nif_pais <> 6 then null end, " +
                "prove.nif, prove.cuentacontable, " +
                "(select direccion from gasocentro.proveedor_direccion where idproveedor = prove.id and tipo = 11 ), " +
                "(select cod_postal from gasocentro.proveedor_direccion where idproveedor = prove.id and tipo = 11 ), " +
                "(select municipio from gasocentro.proveedor_direccion where idproveedor = prove.id and tipo = 11 ), " +
                "(select pais from gasocentro.proveedor_direccion where idproveedor = prove.id and tipo = 11 ), " +
                "concat(fve.codigo, ' ', fve.marca, ' ', fve.modelo), " +
                "1, 24, ?, loc.codigo, false, 1, prove.cuentacontable, prove.formapago, fve.matricula, ?, 1, 2, 4, prove.nombrecomercial, ? " +
                "from gasocentro.flota_vehiculo fve, gasocentro.local loc, gasocentro.parquecarga parque, " +
                "gasocentro.proveedor prove " +
                "where prove.codigo = ? and prove.idempresa = ? and parque.codigo = ? and parque.idempresa = ? and loc.id = ? and fve.id = ? " +
                "limit 1 returning id";

        String insertPartida = "insert into gasocentro.compra_partida (idcompra, numpartida, idarticulo, idmarcafiscal, idcompartimento, estado, epigrafe, " +
                "codnc, regfiscal, marcador, impegeneral, impeestatal, canttempamb, canttempquince, undfiscales, tembalaje, codembalaje, denstempamb, denstempquince, " +
                "tempamb, numonu, codarticulo, nombrearticulo, precio, descuento, portes, proddensmin, proddensmax, importetotal, prodiva, baseimponible, importeiva) " +
                "select ?, 1, art.id, ?, 1, 1, " +
                "case when art.cod_aduanero = 3 then 'B3' " +
                "when art.cod_aduanero = 4 then 'B4' " +
                "end, " +
                "case when art.cod_nc = 1 then '27102011' " +
                "when art.cod_nc = 2 then '27101943' " +
                "when art.cod_nc = 3 then '27101947' " +
                "when art.cod_nc = 4 then '27101245' " +
                "when art.cod_nc = 5 then '27101249' " +
                "end, " +
                "'F', " +
                "art.trazador, art.impuestoespecial_general, art.impuestoespecial_estatal, ?, ?, 'LTR', " +
                "'Líquido, a granel', 'VL', " +
                "round((case when art.densidad15grados_min = 1 then 0.820 " +
                "when art.densidad15grados_min = 2 then 0.720 end " +
                "+ " +
                "case when art.densidad15grados_max = 1 then 0.845 " +
                "when art.densidad15grados_max = 2 then 0.880 " +
                "when art.densidad15grados_max = 3 then 0.900 " +
                "when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                "round((case when art.densidad15grados_min = 1 then 0.820 " +
                "when art.densidad15grados_min = 2 then 0.720 end " +
                "+ " +
                "case when art.densidad15grados_max = 1 then 0.845 " +
                "when art.densidad15grados_max = 2 then 0.880 " +
                "when art.densidad15grados_max = 3 then 0.900 " +
                "when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                "15, art.onua1_num_valor, art.codigo, art.nombre, 0, 0, 0, art.densidad15grados_min, art.densidad15grados_max, " +
                "0, 0, 0, 0 " +
                "from gasocentro.articulo art where art.codigo = ? and art.idgrupo = ?";

        String insertMermas = "insert into gasocentro.silicie (idempresa, idlocal, fechaalta, fechamodificacion, establecimientocae, establecimientonif, " +
                "fechamovimiento, fecharegistrocontable, tipomovimiento, diferenciademenos, tipojustificante, prodcodnc, prodcantidad, " +
                "produnidad, proddescripcion, proddensidad, estado, codlocal, tipooperacion, prodcodepigrafe, fechapresentacion, " +
                "prodmarcador, ieg, iee, proddensidadfiscal, prodcantidadfiscal, prodregfiscal, precio, prodcantidadconsigno, signo, nombrelocal, " +
                "nifpaislocal, prodtemp) select ?, ?, current_date, current_date, loc.cae, loc.nif, ?, ?, 'H26', 'HI15', " +
                "'J11', " +
                "case when art.cod_nc = 1 then '27102011' " +
                "when art.cod_nc = 2 then '27101943' " +
                "when art.cod_nc = 3 then '27101947' " +
                "when art.cod_nc = 4 then '27101245' " +
                "when art.cod_nc = 5 then '27101249' " +
                "end, " +
                "?, 'LTR', art.nombre, " +
                "round((case when art.densidad15grados_min = 1 then 0.820 " +
                "when art.densidad15grados_min = 2 then 0.720 end " +
                "+ " +
                "case when art.densidad15grados_max = 1 then 0.845 " +
                "when art.densidad15grados_max = 2 then 0.880 " +
                "when art.densidad15grados_max = 3 then 0.900 " +
                "when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                "1, loc.codigo, 2, " +
                "case when art.cod_aduanero = 3 then 'B3' " +
                "when art.cod_aduanero = 4 then 'B4' " +
                "end, " +
                "?, art.trazador, art.impuestoespecial_general, art.impuestoespecial_estatal, " +
                "round((case when art.densidad15grados_min = 1 then 0.820 " +
                "when art.densidad15grados_min = 2 then 0.720 end " +
                "+ " +
                "case when art.densidad15grados_max = 1 then 0.845 " +
                "when art.densidad15grados_max = 2 then 0.880 " +
                "when art.densidad15grados_max = 3 then 0.900 " +
                "when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                "?, " +
                "case when art.cod_nc = 1 OR art.cod_nc = 4 OR art.cod_nc = 5 then 'P' " +
                "when art.cod_nc = 2 OR art.cod_nc = 3 then 'V' " +
                "end, " +
                "0, ?, '-', loc.nombre_local, loc.nif_pais, 15 " +
                "from gasocentro.articulo art, gasocentro.local loc where art.codigo = ? and art.idgrupo = ? and loc.id = ?";

        PreparedStatement statement;
        PreparedStatement statementCompras;
        PreparedStatement statementDirectas;
        PreparedStatement statementPartida;
        PreparedStatement statementPartidaVenta;
        ResultSet resultSet;

        try {
            statement = db.connection.prepareStatement(insertMermas);
            statement.setLong(1, db.idEmpresa);
            statement.setLong(2, db.idLocal);
            statement.setLong(10, db.idGrupo);
            statement.setLong(11, db.idLocal);

            statementCompras = db.connection.prepareStatement(insertCompras);
            statementCompras.setLong(1, db.idEmpresa);
            statementCompras.setLong(2, db.idLocal);
            statementCompras.setLong(10, db.idEmpresa);
            statementCompras.setLong(12, db.idEmpresa);
            statementCompras.setLong(13, db.idLocal);

            statementDirectas = db.connection.prepareStatement(insertDirectas);
            statementDirectas.setLong(1, db.idEmpresa);
            statementDirectas.setLong(2, db.idLocal);
            statementDirectas.setLong(7, camionGenerico.getId());
            statementDirectas.setString(10, camionGenerico.getCodigo() + " " + camionGenerico.getMarca() + " " + camionGenerico.getModelo());
            statementDirectas.setString(12, "");
            statementDirectas.setLong(14, db.idLocal);
            statementDirectas.setLong(15, db.idLocal);
            statementDirectas.setLong(16, camionGenerico.getIdProve());
            statementDirectas.setLong(17, db.idEmpresa);

            statementPartida = db.connection.prepareStatement(insertPartida);
            statementPartida.setLong(6, db.idGrupo);

            statementPartidaVenta = db.connection.prepareStatement(insertPartidaVenta);
            statementPartidaVenta.setLong(7, db.idGrupo);


        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        Sheet sheet = workbook.getSheetAt(0);
        String reformatCell;

        // Dato de la celda

        Object cellValue = null;
        Cell cell;
        int tipo_movimiento = 0;
        Compra compra;


        for (Row row : sheet) {

            boolean shouldBreak = false;
            compra = new Compra();

            if (row.getRowNum() == 0) {
                continue;
            }

            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                try {
                    switch (cn) {
                        case 0 -> {
                            reformatCell = getFecha(reformatCell.replace("/", ""), cellValue);
                            compra.setFecha(LocalDate.parse(reformatCell)); // Fecha
                        }
                        case 2 -> tipo_movimiento = switch (reformatCell) {
                            case "MERMAS" -> 3;
                            case "VENTA DIRECTA" -> 2;
                            case "COMPRA" -> 1;
                            default -> 0;
                        }; // Movimiento
                        case 3 ->
                                compra.setNota_entrega(reformatCell.trim()); // Nota Entrega, solo caso movimiento = venta ruta(2)
                        case 4 -> {
                            if (tipo_movimiento == 2)
                                compra.setCod_cliprov(String.valueOf(codigo6Digitos(reformatCell, cellValue)));
                        }
                        case 5 -> {
                            if (tipo_movimiento != 2)
                                compra.setCod_cliprov(String.valueOf(codigo4Digitos(reformatCell, cellValue))); // Cli/Prov, solo caso movimiento = compra(1)
                        }
                        case 6 -> {
                            // Articulo
                            if (reformatCell.trim().equals("GA")) {
                                compra.setCod_articulo("0001");
                            } else if (reformatCell.trim().equals("GB")) {
                                compra.setCod_articulo("0002");
                            }
                        }
                        case 7 -> compra.setLitros(Double.parseDouble(reformatCell.replace("-", ""))); // Litros
                        case 8 -> compra.setLitros_15(Double.parseDouble(reformatCell.replace("-", ""))); // Litros 15
                        case 9 -> compra.setDocumento_compra(reformatCell); // Documento compra
                        case 10 -> compra.setCae(reformatCell); // Cae
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    shouldBreak = true;
                }

                if (shouldBreak) break;


            }

            compra.setCod_parque("0001");

            if (tipo_movimiento == 1) {
                try {
                    statementCompras.setDate(3, Date.valueOf(compra.getFecha()));
                    if (compra.getCod_articulo().equals("0001")) {
                        statementCompras.setString(4, compra.getDocumento_compra());
                        statementCompras.setObject(7, null);
                    } else {
                        statementCompras.setObject(4, null);
                        statementCompras.setString(7, compra.getDocumento_compra());
                    }
                    statementCompras.setDate(5, Date.valueOf(compra.getFecha()));
                    statementCompras.setInt(6, compra.getFecha().getYear());
                    statementCompras.setInt(8, 1);
                    statementCompras.setString(9, "0037");
                    statementCompras.setString(11, compra.getCod_parque());
                    statementCompras.setLong(14, camionGenerico.getId());

                    System.out.println(statementCompras);
                    statementCompras.execute();
                    resultSet = statementCompras.getResultSet();
                    if (resultSet.next()) {
                        statementPartida.setLong(1, resultSet.getLong(1));
                        statementPartida.setInt(2, 0);
                        statementPartida.setDouble(3, compra.getLitros());
                        statementPartida.setDouble(4, compra.getLitros_15());
                        statementPartida.setString(5, compra.getCod_articulo());

                        System.out.println(statementPartida);
                        statementPartida.addBatch();
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (tipo_movimiento == 3) {
                try {
                    statement.setDate(3, Date.valueOf(compra.getFecha()));
                    statement.setDate(4, Date.valueOf(compra.getFecha()));
                    statement.setDouble(5, compra.getLitros());
                    statement.setDate(6, Date.valueOf(compra.getFecha()));
                    statement.setDouble(7, compra.getLitros_15());
                    statement.setDouble(8, Double.parseDouble("-" + compra.getLitros()));
                    statement.setString(9, compra.getCod_articulo());
                    System.out.println(statement);
                    statement.addBatch();

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (tipo_movimiento == 2) {
                try {
                    statementDirectas.setDate(3, Date.valueOf(compra.getFecha()));
                    statementDirectas.setDate(4, Date.valueOf(compra.getFecha()));
                    statementDirectas.setDate(5, Date.valueOf(compra.getFecha()));
                    statementDirectas.setString(6, compra.getNota_entrega());
                    statementDirectas.setDate(8, Date.valueOf(compra.getFecha()));
                    statementDirectas.setDate(9, Date.valueOf(compra.getFecha()));
                    statementDirectas.setDate(11, Date.valueOf(compra.getFecha()));
                    statementDirectas.setString(13, "0001");

                    System.out.println(statementDirectas);
                    statementDirectas.execute();

                    resultSet = statementDirectas.getResultSet();
                    if (resultSet.next()) {
                        statementPartidaVenta.setLong(1, resultSet.getLong(1));
                        statementPartidaVenta.setInt(2, 0);
                        statementPartidaVenta.setDouble(3, compra.getLitros());
                        statementPartidaVenta.setDouble(4, compra.getLitros_15());
                        statementPartidaVenta.setDate(5, Date.valueOf(compra.getFecha()));
                        statementPartidaVenta.setString(6, compra.getCod_articulo());

                        System.out.println(statementPartidaVenta);
                        statementPartidaVenta.addBatch();
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }

        }

        try {
            statement.executeBatch();
            statementPartida.executeBatch();
            statementPartidaVenta.executeBatch();
//            statementCompras.executeBatch();
            statement.close();
            statementCompras.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    boolean lecturaExisten(Workbook workbook, Database db) {
        Camion camionA = db.consultaCamiones("AV3064G");
        Camion camionF = db.consultaCamiones("GI9102BJ");
        Camion camionC = db.consultaCamiones("9190CKJ");
        Camion camionG = db.consultaCamiones("1827BZY");
        Camion camionGenerico = db.consultaCamiones("");


        String reformatCell, albaran;
        ArrayList<NotaEntrega> notas = new ArrayList<>();
        ArrayList<NotaEntrega> notasPorAlbaran = new ArrayList<>();
        int tipo_movimiento = 0, secuenciaNotaA = 1, secuenciaNotaB = 1, secuenciaNotaC = 1;
        long id_albaranA = 0, id_albaranB = 0, id_albaranC = 0;
        long idVehiculo = 0;
        LocalDate fecha = null;
        VentaRuta ventaRuta = new VentaRuta("");
        boolean crear_venta = false, bonificado;
        double litros;
        double litrosAmbA = 0, litrosAmbB = 0, litrosAmbC = 0, litros15A = 0, litros15B = 0, litros15C = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");


        Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        Array arrayCompartimentos;
        try {
            arrayCompartimentos = db.connection.createArrayOf("INTEGER", array);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


        Sheet sheet = workbook.getSheetAt(0);
        // Dato de la celda
        Object cellValue = null;
        Cell cell;
        for (Row row : sheet) {
            albaran = "";

            boolean shouldBreak = false;
            NotaEntrega notaEntrega = new NotaEntrega();
            if (row.getRowNum() == 0) {
                continue;
            }
            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                try {
                    switch (cn) {
                        case 0 -> {
                            // Fecha
                            fecha = LocalDate.parse(reformatCell, formatter);
                        }
                        // Alb(CAMION) A = 5401-GHK, 0001 | B = 2978-CVX, 0002
                        case 1 -> {
                            albaran = reformatCell.trim().replace(".0", "");
                            if (!albaran.contains("A") && !albaran.contains("C") && !albaran.contains("F") && !albaran.contains("G")) {
                                shouldBreak = true;
                            }
                        }

                        case 2 -> {
                            // Movimiento
                            switch (reformatCell) {
                                case "CARGA" -> {
                                    tipo_movimiento = 1;
                                    if (ventaRuta.getReintro() == null) {
                                        // Misma venta, diferente partida
                                        crear_venta = false;
                                    } else {
                                        // Nueva venta
                                        for (NotaEntrega nota : notasPorAlbaran) {
                                            if (nota.getCod_articulo().equals("00GA")) {
                                                nota.setId_albaran(id_albaranA);
                                            } else if (nota.getCod_articulo().equals("00GB")) {
                                                nota.setId_albaran(id_albaranB);
                                            } else {
                                                nota.setId_albaran(id_albaranC);
                                            }
                                        }
                                        notas.addAll(notasPorAlbaran);
                                        notasPorAlbaran = new ArrayList<>();
                                        crear_venta = true;
                                        secuenciaNotaA = 0;
                                        secuenciaNotaB = 0;
                                        secuenciaNotaC = 0;
                                        ventaRuta = new VentaRuta();
                                        ventaRuta.setFecha(fecha);
                                        ventaRuta.setSecuencia(1);
                                        ventaRuta.setCompartimento(1);
                                        ventaRuta.setNumalbaran(albaran);

                                        if (albaran.contains("A")) {
                                            ventaRuta.setMatricula("AV3064G");
                                            idVehiculo = camionA.getId();
                                        } else if (albaran.contains("C")) {
                                            ventaRuta.setMatricula("9190CKJ");
                                            idVehiculo = camionC.getId();
                                        } else if (albaran.contains("F")) {
                                            ventaRuta.setMatricula("GI9102BJ");
                                            idVehiculo = camionF.getId();
                                        } else if (albaran.contains("G")) {
                                            ventaRuta.setMatricula("1827BZY");
                                            idVehiculo = camionG.getId();
                                        }

                                    }
                                }
                                case "VENTA" -> {
                                    tipo_movimiento = 2;
                                    notaEntrega = new NotaEntrega();
                                    notaEntrega.setCod_albaran(ventaRuta.getNumalbaran());
                                    notaEntrega.setFechaentrega(ventaRuta.getFecha());
                                    crear_venta = false;
                                }
                                case "DESCARGA" -> {
                                    tipo_movimiento = 3;
                                    ventaRuta.setReintro("Reintroducida");
                                    crear_venta = false;
                                }
                                case "COMPRA" -> {
                                    tipo_movimiento = 4;
                                    crear_venta = false;
                                }
                                default -> {
                                    shouldBreak = true;
                                    crear_venta = false;
                                }
                            }
                        }
                        case 3 -> {
                            // Nota Entrega, solo caso movimiento = venta(2)
//                            if (tipo_movimiento == 2) {
                            notaEntrega.setNumalbaran_tesoreria(reformatCell.trim());
                            notaEntrega.setCodigo(reformatCell.trim());

                            if (albaran.contains("A")) {
                                ventaRuta.setMatricula("AV3064G");
                                idVehiculo = camionA.getId();
                            } else if (albaran.contains("C")) {
                                ventaRuta.setMatricula("9190CKJ");
                                idVehiculo = camionC.getId();
                            } else if (albaran.contains("F")) {
                                ventaRuta.setMatricula("GI9102BJ");
                                idVehiculo = camionF.getId();
                            } else if (albaran.contains("G")) {
                                ventaRuta.setMatricula("1827BZY");
                                idVehiculo = camionG.getId();
                            }
//                            }
                        }
                        case 4 -> {
                            // Cli/Prov, solo caso movimiento != carga(1) y descarga(3)
                            if (tipo_movimiento == 4) {
                                notaEntrega.setCod_provedor(String.valueOf(codigo4Digitos(reformatCell, cellValue)));
                            } else {
                                notaEntrega.setCod_cliente(String.valueOf(codigo6Digitos(reformatCell, cellValue)));
                            }
                        }
                        case 6 -> {
                            // Articulo
                            notaEntrega.setCod_articulo(String.valueOf(codigo4Digitos(reformatCell, cellValue)));
                            if (notaEntrega.getCod_articulo().equals("00GB")) {
                                notaEntrega.setSecuenciaB(secuenciaNotaB);
                                secuenciaNotaB++;
                                ventaRuta.setCod_articulo("0002");
                                notaEntrega.setCod_articulo("0002");
                                ventaRuta.setBonificacion(false);
                                notaEntrega.setBonificado(false);
                            } else if (notaEntrega.getCod_articulo().equals("00GA")) {
                                notaEntrega.setSecuenciaA(secuenciaNotaA);
                                secuenciaNotaA++;
                                ventaRuta.setCod_articulo("0001");
                                notaEntrega.setCod_articulo("0001");
                                ventaRuta.setBonificacion(true);
                                notaEntrega.setBonificado(true);
                            }
                        }
                        case 7 -> {
                            // Litros
                            litros = Double.parseDouble(reformatCell.trim().replace(",", ".").replace("-", ""));
                            if (tipo_movimiento == 1) {
                                ventaRuta.setLitros_carga(litros);
                                if (ventaRuta.getCod_articulo().equals("0001")) litrosAmbA = litros;
                                else litrosAmbB = litros;
                            }
                            else if (tipo_movimiento == 2) notaEntrega.setLitros(litros);
                            else if (tipo_movimiento == 3) ventaRuta.setLitros_dev(litros);
                        }
                        case 8 -> {
                            // Litros 15
                            litros = Double.parseDouble(reformatCell.trim().replace(",", ".").replace("-", ""));
                            if (tipo_movimiento == 1) {
                                ventaRuta.setLitros15_carga(litros);
                                if (ventaRuta.getCod_articulo().equals("0001")) litros15A = litros;
                                else litros15B = litros;
                            }
                            else if (tipo_movimiento == 2) notaEntrega.setLitros15(litros);
                            else if (tipo_movimiento == 3) ventaRuta.setLitros15_dev(litros);
                        }
                        case 9 -> {
                            // Documento compra, solo caso movimiento = compra(4)
                            if (!reformatCell.matches("\\s*")) notaEntrega.setDocumento_compra(reformatCell);
                        }
                        case 10 -> {
                            // Cae, solo caso movimiento = compra(4)
                            if (!reformatCell.matches("\\s*")) notaEntrega.setCae(reformatCell);
                        }
                        case 11 -> {
                            // Cre
                            if (!reformatCell.matches("\\s*")) ventaRuta.setCre(reformatCell);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    shouldBreak = true;
                }

                if (shouldBreak) break;


            }

            if (shouldBreak) continue;

            try {
                switch (tipo_movimiento) {
                    case 1 -> {
                        if (crear_venta) {
                            PreparedStatement statement = db.connection.prepareStatement("" +
                                    "insert into gasocentro.venta_ruta (idlocal, codlocal, idempresa, fechaalta, " +
                                    "estado, idmodotransporte, idtipoorganizador, kmsinicio, kmsfinal, totalizainicio, totalizafinal, " +
                                    "expedidornombre, expedidornif, expedidorcae, expedidornifpais, idvehiculo, nombrevehiculo, identificador,  " +
                                    "fechainiciocirculacion, destfuerzasarmadas, fechareintroduccion, numrefalb, expedidordireccion, expedidorcp,  " +
                                    "expedidormunicipio, expedidorprovincia, expedidorca, expedidorpais, origen, bonificado, tipovehiculo, " +
                                    "niftransportista, nifpaistransportista, nombretransportista, compartimentos, presentaciondiferida,  " +
                                    "razonreintroalb, nifcargador, tipoorganizador, finalizadamovilidad, altaasientos, altaasientosreintro, " +
                                    "  identificadorvehiculo, fechamodificacion, horainiciocirculacion, fechaaltavalidacion, horaaltavalidacion,  " +
                                    "nombrecargador, nifpaiscargador) " +
                                    "select ?, loc.codigo, ?, ?, 3, 2, 1, 0, 0, 0, 0, loc.nombre_local, loc.nif, loc.cae, loc.nif_pais, " +
                                    "fve.id, concat(fve.codigo, ' ', fve.marca, ' ', fve.modelo), ?, ?, false, ?, ?, loc.direccion, " +
                                    "loc.cod_postal, loc.municipio, loc.provincia, loc.ccaa, loc.pais, 1, false, fve.tipovehiculo, prov.nif,  " +
                                    "case when prov.nif_pais = 6 then 'ES' end,  " +
                                    "prov.nombrecomercial, ?[0:cis.num_compartimento], false,  " +
                                    "0, loc.nif, 1, false, false, false, ?, ?, '12:00:00', ?, '12:00:00',  " +
                                    "loc.nombre_local, loc.nif_pais " +
                                    "from gasocentro.local loc, gasocentro.proveedor prov,  " +
                                    "gasocentro.flota_vehiculo fve inner join ( " +
                                    "select idvehiculo, max(numcompartimento) as num_compartimento " +
                                    "from gasocentro.flota_vehiculo_cisterna " +
                                    "group by idvehiculo " +
                                    ") as cis on fve.id = cis.idvehiculo " +
                                    "where loc.id = ? and prov.idempresa = ? and prov.transportista = true and prov.codigo = '0037'" +
                                    "and fve.idempresa = ? and fve.matricula = ? " +
                                    "returning id");
                            statement.setLong(1, db.idLocal);
                            statement.setLong(2, db.idEmpresa);
                            statement.setDate(3, Date.valueOf(ventaRuta.getFecha()));
                            statement.setString(4, ventaRuta.getMatricula());
                            statement.setDate(5, Date.valueOf(ventaRuta.getFecha()));
                            statement.setDate(6, Date.valueOf(ventaRuta.getFecha()));
                            statement.setString(7, ventaRuta.getNumalbaran());
                            statement.setArray(8, arrayCompartimentos);
                            statement.setString(9, ventaRuta.getMatricula());
                            statement.setDate(10, Date.valueOf(ventaRuta.getFecha()));
                            statement.setDate(11, Date.valueOf(ventaRuta.getFecha()));
                            statement.setLong(12, db.idLocal);
                            statement.setLong(13, db.idEmpresa);
                            statement.setLong(14, db.idEmpresa);
                            statement.setString(15, ventaRuta.getMatricula());
                            System.out.println(statement);
                            statement.execute();
                            ResultSet resultSet = statement.getResultSet();
                            if (resultSet.next()) {
                                ventaRuta.setId(resultSet.getLong(1));
                            }
                            statement.close();
                            resultSet.close();

                        }
                    }
                    case 2 -> notasPorAlbaran.add(notaEntrega);

                    case 3 -> {
                        CallableStatement statementFunction = db.connection.prepareCall("{ ? = call gasocentro.insert_existen(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
                        statementFunction.registerOutParameter(1, Types.BIGINT);
                        statementFunction.setLong(2, db.idGrupo);
                        statementFunction.setLong(3, db.idEmpresa);
                        statementFunction.setLong(4, db.idLocal);
                        statementFunction.setDate(5, Date.valueOf(ventaRuta.getFecha()));
                        statementFunction.setString(6, ventaRuta.getMatricula());
                        statementFunction.setString(7, ventaRuta.getNumalbaran());
                        statementFunction.setLong(8, ventaRuta.getId());
                        statementFunction.setString(9, ventaRuta.getCod_articulo());
                        statementFunction.setString(10, ventaRuta.getCre());
                        switch (ventaRuta.getCod_articulo()) {
                            case "0001" -> {
                                statementFunction.setDouble(11, litros15A);
                                statementFunction.setDouble(12, litrosAmbA);
                            } case "0002" -> {
                                statementFunction.setDouble(11, litros15B);
                                statementFunction.setDouble(12, litrosAmbB);
                            } case "0003" -> {
                                statementFunction.setDouble(11, litros15C);
                                statementFunction.setDouble(12, litrosAmbC);
                            }
                        }
                        statementFunction.setInt(13, ventaRuta.getSecuencia());
                        statementFunction.setInt(14, ventaRuta.getCompartimento());
                        statementFunction.setDouble(15, ventaRuta.getLitros15_dev());
                        statementFunction.setDouble(16, ventaRuta.getLitros_dev());
                        statementFunction.setString(17, db.codlocal);
                        statementFunction.setLong(18, idVehiculo);
                        statementFunction.setString(19, ventaRuta.getCod_articulo());
                        System.out.println(statementFunction);
                        statementFunction.execute();
                        if (ventaRuta.getCod_articulo().equals("00GA")) {
                            id_albaranA = statementFunction.getLong(1);
                        } else if (ventaRuta.getCod_articulo().equals("00GB")) {
                            id_albaranB = statementFunction.getLong(1);
                        } else {
                            id_albaranC = statementFunction.getLong(1);
                        }
                        ventaRuta.setSecuencia(ventaRuta.getSecuencia() + 1);
                        ventaRuta.setCompartimento(ventaRuta.getCompartimento() + 1);
                        statementFunction.close();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(e.getMessage() + "\n" + e.getNextException());
                return false;
            }


        }

        try {
            for (NotaEntrega nota : notasPorAlbaran) {
                if (nota.getCod_articulo().equals("00GA")) {
                    nota.setId_albaran(id_albaranA);
                } else if (nota.getCod_articulo().equals("00GB")) {
                    nota.setId_albaran(id_albaranB);
                } else {
                    nota.setId_albaran(id_albaranC);
                }
            }
            notas.addAll(notasPorAlbaran);
            for (NotaEntrega nota : notas) {
                PreparedStatement statement = db.connection.prepareStatement("insert into gasocentro.siane_nota_entrega (fechapreparacion, tipomovimiento, expedidornif, expedidorcae, cre, " +
                        "refnotaentregalocal, tipodestinatario, identificadordestinatario, nombredestinatario, epigrafe, canttempamb, " +
                        "canttempfiscal, unidadmedida, desccomercial, marcadores, localidadentrega, cpentrega, calleynumentrega, " +
                        "fechaentrega, idempresa, idalbaran, codlocal, expedidornombre, horavalidacion, fechavalidacion, numnota, estado, " +
                        "idlocal, fechaalta, fechamodificacion, idvehiculo, codnc, iddestinatario, destinatariocp, destinatariomunicipio, " +
                        "destinatarioprovincia, destinatarioca, destinatariopais, destinatariocodigo, codcliente, numalbcomercial, idarticulo, " +
                        "tipoiva, idformapago, horaentrega, tempamb, densidadfiscal, denstempamb, facturado, origen, iee, ieg, " +
                        "bonificado, expedidordireccion, expedidorcp, expedidormunicipio, expedidorprovincia, expedidorca, expedidorpais, compartimento, " +
                        "emisormensaje, nombreemisor, " +
                        "precio, baseimponible, importeiva, importetotal, preciosinimpuestos, descuento, portes, totalbonificacion, totalapagar, numfactura)" +
                        "select ?, 3, loc.nif, loc.cae, ?, ?, psum.tipo, psum.nif, psum.nombre_comercial, " +
                        "case when art.cod_aduanero = 3 then 'B3' " +
                        "when art.cod_aduanero = 4 then 'B4' " +
                        "end, " +
                        "?, ?, 'LTR', art.nomdescrip312, " +
                        "case when art.trazador = '' then 0 " +
                        "when art.trazador <> '' then 1 " +
                        "end, " +
                        "cli.localidad, cli.cod_postal, cli.direccion, ?, ?, ?, loc.codigo, loc.nombre_local, '12:00:00', ?, " +
                        "case when art.codigo = '0001' then ? " +
                        "when art.codigo = '0002' then ? " +
                        "when art.codigo = '0003' then ? " +
                        "end, " +
                        "3, loc.id, ?, ?, ?, " +
                        "case when art.cod_nc = 1 then '27102011' " +
                        "when art.cod_nc = 2 then '27101943' " +
                        "when art.cod_nc = 3 then '27101947' " +
                        "when art.cod_nc = 4 then '27101245' " +
                        "when art.cod_nc = 5 then '27101249' " +
                        "end, " +
                        "psum.id, psum.cod_postal, psum.municipio, psum.provincia, psum.ccaa, psum.pais, psum.codigo, " +
                        "cli.codigo, ?, art.id, art.iva_tipo, cli.formapago, '12:00:00', 15, " +
                        "round ((case when art.densidad15grados_min = 1 then 0.820 " +
                        "when art.densidad15grados_min = 2 then 0.720 end" +
                        " + " +
                        "case when art.densidad15grados_max = 1 then 0.845" +
                        " when art.densidad15grados_max = 2 then 0.880" +
                        " when art.densidad15grados_max = 3 then 0.900" +
                        " when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                        "round ((case when art.densidad15grados_min = 1 then 0.820" +
                        " when art.densidad15grados_min = 2 then 0.720 end" +
                        " + " +
                        "case when art.densidad15grados_max = 1 then 0.845" +
                        " when art.densidad15grados_max = 2 then 0.880" +
                        " when art.densidad15grados_max = 3 then 0.900" +
                        " when art.densidad15grados_max = 4 then 0.775 end) / 2, 2), " +
                        "true, 1, art.impuestoespecial_general, art.impuestoespecial_estatal, ?, loc.direccion, loc.cod_postal, loc.municipio, " +
                        "loc.provincia, loc.ccaa, loc.pais, 1, emi.nif, emi.representante, " +
                        "0, 0, 0, " +
                        "(select sum(importe) from gasocentro.tesoreria_factura_albaran where numalbaran = ? and fecha = ? and idlocal = ? group by numalbaran), " +
                        "0, 0, 0, 0, 0, " +
                        "(select distinct fact.numfactura from gasocentro.tesoreria_factura fact inner join gasocentro.tesoreria_factura_albaran alb on alb.idfactura = fact.id " +
                        "where alb.numalbaran = ? and fact.fechaemision = ? and fact.idlocal = ?) as numfactura " +
                        "from gasocentro.local loc, gasocentro.articulo art, gasocentro.empresa_certificado emi, " +
                        "gasocentro.cliente cli inner join gasocentro.cliente_puntosuministro psum on cli.id = psum.idcliente " +
                        "where loc.id = ? and art.idgrupo = ? and art.codigo = ? and cli.idempresa = ? and cli.codigo = ? " +
                        "and psum.principal = true " +
                        "and emi.idempresa = ? " +
                        "returning id, compartimento");

                statement.setString(1, String.valueOf((nota.getFechaentrega())));
                statement.setString(2, nota.getCre());
                statement.setString(3, nota.getCodigo());
                statement.setDouble(4, nota.getLitros());
                statement.setDouble(5, nota.getLitros15());
                statement.setString(6, String.valueOf((nota.getFechaentrega())));
                statement.setLong(7, db.idEmpresa);
                statement.setLong(8, nota.getId_albaran());
                statement.setDate(9, Date.valueOf(nota.getFechaentrega()));
                statement.setInt(10, nota.getSecuenciaA());
                statement.setInt(11, nota.getSecuenciaB());
                statement.setInt(12, nota.getSecuenciaC());
                statement.setDate(13, Date.valueOf(nota.getFechaentrega()));
                statement.setDate(14, Date.valueOf(nota.getFechaentrega()));
                statement.setLong(15, idVehiculo);
                statement.setString(16, nota.getCod_albaran());
                statement.setBoolean(17, nota.isBonificado());
                statement.setString(18, nota.getNumalbaran_tesoreria());
                statement.setDate(19, Date.valueOf(nota.getFechaentrega()));
                statement.setLong(20, db.idLocal);
                statement.setString(21, nota.getNumalbaran_tesoreria());
                statement.setDate(22, Date.valueOf(nota.getFechaentrega()));
                statement.setLong(23, db.idLocal);
                statement.setLong(24, db.idLocal);
                statement.setLong(25, db.idGrupo);
                statement.setString(26, nota.getCod_articulo());
                statement.setLong(27, db.idEmpresa);
                statement.setString(28, nota.getCod_cliente());
                statement.setLong(29, db.idEmpresa);

                System.out.println(statement);
                statement.execute();

                ResultSet resultSet = statement.getResultSet();

                PreparedStatement updateFirmantes;

                if (resultSet.next()) {
                    statement = db.connection.prepareStatement("INSERT INTO gasocentro.siane_nota_entrega_suministro (compartimento, " +
                            "cantidad, idnotaentrega) values (?, ?, ?)");
                    statement.setInt(1, resultSet.getInt(2));
                    statement.setDouble(2, nota.getLitros());
                    statement.setLong(3, resultSet.getLong(1));
                    statement.execute();
                    System.out.println(statement);

                    statement.close();

                    updateFirmantes = db.connection.prepareStatement("UPDATE gasocentro.siane_nota_entrega sne set niffirmante = subquery.nif, nombrefirmante = subquery.nombre_completo, idfirmante = subquery.id, nifpaisfirmante = 'ES', parentescofirmante = subquery.parentesco, emailfirmante = subquery.email, tlffirmante = subquery.tlf1 " +
                            "from (select nif, nombre_completo, id, parentesco, email, tlf1 " +
                            "from gasocentro.cliente_puntosuministro_contacto where idpuntosuministro in (select id from gasocentro.cliente_puntosuministro where idcliente in (select id from gasocentro.cliente where idempresa = ? and codigo = ?) and principal = true)) as subquery " +
                            "where sne.id = ?");
                    updateFirmantes.setLong(1, db.idEmpresa);
                    updateFirmantes.setString(2, nota.getCod_cliente());
                    updateFirmantes.setLong(3, resultSet.getInt(1));
                    updateFirmantes.execute();

                    updateFirmantes.close();
                }

                resultSet.close();

            }

            PreparedStatement statement = db.connection.prepareStatement("UPDATE gasocentro.siane_albaran sna set numref = (select concat(numref,secuencia) from gasocentro.siane_albaran where id = sna.id) where sna.idlocal = ?;");
            statement.setLong(1, db.idLocal);
            statement.execute();
            statement = db.connection.prepareStatement("UPDATE gasocentro.siane_albaran_reintroduccion sar set refalbaranlocal = (select numref from gasocentro.siane_albaran where id = sar.idalbaran) where sar.idlocal = ?");
            statement.setLong(1, db.idLocal);
            statement.execute();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    boolean procesoIva(Workbook workbook, Database db) {
        String reformatCell;
        String insertIva = "INSERT INTO gasocentro.tesoreria_factura_iva (idfactura, baseimponible, tipoiva, importeiva, idtipoiva) VALUES (?, ?, ?, ?, ?)";
        String queryFact = "SELECT id from gasocentro.tesoreria_factura WHERE numfactura = ? and anocontable = ? and idlocal = ?";
        Sheet sheet = workbook.getSheetAt(0);

        PreparedStatement insert;

        try {
            insert = db.connection.prepareStatement(insertIva);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        Object cellValue = null;
        Cell cell;

        for (Row row : sheet) {
            ResumenIva iva = new ResumenIva();
            if (row.getRowNum() == 0) {
                continue;
            }
            for (int cn = 0; cn < row.getLastCellNum() - 1; cn++) {
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                switch (cn) {
                    case 1 -> {
                        if (cellValue instanceof Double) {
                            reformatCell = reformatCell.replace(".0", "");
                        }
                        iva.setCodigo_factura(reformatCell);
                    }
                    case 2 -> {
                        reformatCell = getFecha(reformatCell, cellValue);
                        cellValue = stringToDate(reformatCell);
                        reformatCell = String.valueOf(((LocalDate) cellValue).getYear());
                        iva.setAnocontable(Integer.parseInt(reformatCell));
                    }
                    case 4 -> iva.setDestinatario(reformatCell);
                    case 5 -> {
                        reformatCell = reformatCell.replace("+", "").replace("-", "").replace(",", ".");
                        if (reformatCell.matches("\\d*.\\d*")) {
                            cellValue = Double.parseDouble(reformatCell);
                            iva.setBase_imponible((Double) cellValue);
                        }
                    }
                    case 6 -> {
                        if (cellValue instanceof Double) {
                            reformatCell = reformatCell.replace(".0", "");
                        }
                        iva.setIva(reformatCell + "%");
                        cellValue = switch (reformatCell) {
                            case "21" -> 1;
                            case "10" -> 2;
                            case "4" -> 3;
                            case "0", "" -> 4;
                            default -> 0;
                        };
                        iva.setIva_tipo((Integer) cellValue);

                    }
                    case 7 -> {
                        reformatCell = reformatCell.replace("+", "").replace("-", "").replace(",", ".");
                        if (reformatCell.matches("\\d*.\\d*")) {
                            cellValue = Double.parseDouble(reformatCell);
                            iva.setCuota((Double) cellValue);
                        }
                    }
                }
            }
            if (iva.getBase_imponible() != null) {
                try {
                    PreparedStatement query = db.connection.prepareStatement(queryFact);
                    query.setString(1, iva.getCodigo_factura());
                    query.setInt(2, iva.getAnocontable());
                    query.setLong(3, db.idLocal);
                    ResultSet resultSet = query.executeQuery();
                    if (resultSet.next()) {
                        insert = db.connection.prepareStatement(insertIva);
                        insert.setLong(1, resultSet.getLong(1));
                        insert.setDouble(2, iva.getBase_imponible());
                        insert.setString(3, iva.getIva());
                        insert.setDouble(4, iva.getCuota());
                        insert.setInt(5, iva.getIva_tipo());
                        insert.addBatch();
                    }
                    query.close();
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        try {
            workbook.close();
            insert.executeBatch();
            insert.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    HashMap<String, HashMap<Integer, ArrayList<Object>>> procesoCuentas(Workbook workbook, Database db) {
        HashMap<Integer, ArrayList<Object>> hashMapArtcompra = new HashMap<>();
        HashMap<Integer, ArrayList<Object>> hashMapArtVenta = new HashMap<>();
        HashMap<Integer, ArrayList<Object>> hashMapProv = new HashMap<>();
        HashMap<String, HashMap<Integer, ArrayList<Object>>> hashMapPrefijo = new HashMap<>();
        int fila = 0, x = 0, tipoCuenta = 0;
        boolean insertar;
        ArrayList<Object> datosCuentasArtCompras, datosCuentasArtVentas, datosCuentasProv;
        String reformatCell, prefijo;

        Sheet sheet = workbook.getSheetAt(x);
        Object cellValue = null;
        Cell cell;

        int digitosCuenta = 0;
        try {
            digitosCuenta = db.consultaMaximoDigitosCuenta();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            datosCuentasArtVentas = new ArrayList<>();
            datosCuentasArtCompras = new ArrayList<>();
            datosCuentasProv = new ArrayList<>();
            prefijo = "";
            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                insertar = true;
                cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                cellValue = getCellValue(cell);
                reformatCell = cellValue.toString();
                if (cn == 2) {
                    if (reformatCell.length() > 3) {
                        prefijo = reformatCell.substring(0, 2);
                        switch (prefijo) {
                            case "40" -> tipoCuenta = 2;
                            case "70" -> tipoCuenta = 3;
                            case "60" -> tipoCuenta = 4;
                            default -> {
                                tipoCuenta = 0;
                                insertar = false;
                            }
                        }
                        cellValue = formatCuenta(reformatCell, cellValue, digitosCuenta, prefijo + "0");
                    } else {
                        insertar = false;
                    }
                }
                if (insertar) {
                    switch (tipoCuenta) {
                        case 2 -> datosCuentasProv.add(cellValue);
                        case 3 -> datosCuentasArtVentas.add(cellValue);
                        case 4 -> datosCuentasArtCompras.add(cellValue);
                    }
                }
            }
            switch (prefijo) {
                case "40" -> hashMapProv.put(fila, datosCuentasProv);
                case "70" -> hashMapArtVenta.put(fila, datosCuentasArtVentas);
                case "60" -> hashMapArtcompra.put(fila, datosCuentasArtCompras);
            }
        }
        hashMapPrefijo.put("40", hashMapProv);
        hashMapPrefijo.put("60", hashMapArtcompra);
        hashMapPrefijo.put("70", hashMapArtVenta);
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMapPrefijo;
    }

    public ArrayList<Object> validarNifConObjeto(Object cellValue) {
        String validNif = cellValue.toString().replace("-", "").replace("ES", "");
        validNif = validNif.toUpperCase().trim();
        ArrayList<Object> datosCliente = new ArrayList<>();
        // Persona juridica
        if (validNif.matches("^[A-Z]\\d{8}") || validNif.matches("^[A-Z]\\d{7}[A-Z]")) {
            datosCliente.add(validNif); // NIF
            datosCliente.add(6); // nif_pais
            datosCliente.add("Sin razón social"); // razonsocial
            datosCliente.add(2); // tipo_persona
        } // Persona fisica nif_pais españa
        else if (validNif.matches("^\\d{8}[A-W]")) {
            validNif = calculaLetra(validNif.replaceAll("\\D", ""), 1);
            datosCliente.add(validNif);
            datosCliente.add(6);
            datosCliente.add(null);
            datosCliente.add(1);
        } // Persona física extranjera
        else if (validNif.matches("^\\d{8}[X-Z]")) {
            datosCliente.add(validNif);
            datosCliente.add(6);
            datosCliente.add(null);
            datosCliente.add(1);
        } // Persona física extranjera
        else if (validNif.matches("^[XT]\\d{7}[A-Z]")) {
            datosCliente.add(validNif);
            datosCliente.add(6);
            datosCliente.add(null);
            datosCliente.add(1);
        }// DNI sin letra
        else if (validNif.matches("^\\d{8}")) {
            validNif = calculaLetra(validNif, 1);
            datosCliente.add(validNif);
            datosCliente.add(6);
            datosCliente.add(null);
            datosCliente.add(1);
        } // Celda vacía o mal formateada
        else {
            datosCliente.add("SinNifAso");
            datosCliente.add(6);
            datosCliente.add(null);
            datosCliente.add(0);
        }
        return datosCliente;
    }

    static String calculaLetra(String cadenaNif, int principio_final) {
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        long numero = Long.parseLong(cadenaNif);
        int posicion;

        //Calculo el resto de la división entre 23 para veriguar la posición
        //dentro de la cadena de letras definida arriba
        posicion = (int) (numero % 23);

        //Devuelvo la letra que hay en la posición "posicion" de la cadena de letras
        if (principio_final == 0) cadenaNif = letras.charAt(posicion) + cadenaNif;
        else if (principio_final == 1) cadenaNif += letras.charAt(posicion);
        return cadenaNif;
    }

    Object getCellValue(Cell cell) {
        Object cellValue = null;
        SimpleDateFormat formatterFecha = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterHora = new SimpleDateFormat("HH:mm:ss");
        switch (cell.getCellType()) {
            case STRING:
                cellValue = cell.getStringCellValue().replace("�", "Ñ").replace("･", "Ñ").replace("¥", "Ñ").replace("§", "º").replace("'", "''").replaceAll("\\s{2,}", " ");
                break;
            case NUMERIC:
                if (DateUtil.isCellInternalDateFormatted(cell)) {
                    cellValue = formatterFecha.format(cell.getDateCellValue());
                } else if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = formatterHora.format(cell.getDateCellValue());
                } else {
                    cellValue = cell.getNumericCellValue();
                }
                break;
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case FORMULA:
                cellValue = cell.getNumericCellValue();
                break;
            case BLANK:
                cellValue = "";
//                        cell.setCellType(CellType.STRING);
        }
        return cellValue;

    }

    public static LocalDate stringToDate(String fecha) {
        return LocalDate.parse(fecha);
    }

    private String getFecha(String reformatCell, Object cellValue) {
        if (cellValue instanceof Double) {
            reformatCell = reformatCell.replace(".0", "");
        } else {
            reformatCell = reformatCell.replace("/", "");
        }
        reformatCell = String.format("%6s", reformatCell).replace(" ", "0");
        reformatCell = "20" + reformatCell.substring(4, 6) + "-" + reformatCell.substring(2, 4) + "-" + reformatCell.substring(0, 2);
        return reformatCell;
    }

    private Object codigo4Digitos(String reformatCell, Object cellValue) {
        reformatCell = reformatCell.trim();
        if (reformatCell.equals("")) {
            cellValue = null;
        } else {
            if (cellValue instanceof Double) {
                reformatCell = reformatCell.replace(".0", "");
            }
            reformatCell = String.format("%4s", reformatCell).replace(" ", "0");
            cellValue = reformatCell;
        }
        return cellValue;
    }

    private Object codigo6Digitos(String reformatCell, Object cellValue) {
        reformatCell = reformatCell.trim();
        if (reformatCell.equals("")) {
            cellValue = null;
        } else {
            if (cellValue instanceof Double) {
                reformatCell = reformatCell.replace(".0", "");
            }
            reformatCell = String.format("%6s", reformatCell).replace(" ", "0");
            cellValue = reformatCell;
        }
        return cellValue;
    }

    private Object formatCuenta(String reformatCell, Object cellValue, int digitosCuenta, String prefijo) {

        if (prefijo == null) {
            prefijo = reformatCell.substring(0, 3);
        }

        reformatCell = reformatCell.replace(".0", "");

        if (reformatCell.length() < digitosCuenta) {
            digitosCuenta -= 3;
            reformatCell = reformatCell.substring(3);
            reformatCell = String.format("%" + digitosCuenta + "s", reformatCell).replace(" ", "0");
            reformatCell = prefijo + reformatCell;
        }/* else {
            for (int i = 3; i < digitosCuenta; i++) {
                caracter = String.valueOf(reformatCell.charAt(i));
                if (reformatCell.charAt(i) == '0') {
                    reformatCell = reformatCell.replace(caracter, "");
                }
                if (reformatCell.length() == digitosCuenta) {
                    break;
                }
            }
        }*/
        cellValue = reformatCell;
        return cellValue;
    }
}
