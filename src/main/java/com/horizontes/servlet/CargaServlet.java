
package com.horizontes.servlet;

import com.horizontes.dao.*;
import com.horizontes.model.*;
import com.horizontes.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

@WebServlet("/api/carga")
@MultipartConfig
public class CargaServlet extends HttpServlet {

    private final UsuarioDAO     usuarioDAO     = new UsuarioDAO();
    private final DestinoDAO     destinoDAO     = new DestinoDAO();
    private final ProveedorDAO   proveedorDAO   = new ProveedorDAO();
    private final PaqueteDAO     paqueteDAO     = new PaqueteDAO();
    private final ClienteDAO     clienteDAO     = new ClienteDAO();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final PagoDAO        pagoDAO        = new PagoDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!esAdmin(req)) { JsonUtil.sendError(res, 403, "Acceso denegado"); return; }

        Part filePart = req.getPart("archivo");
        if (filePart == null) { JsonUtil.sendError(res, 400, "No se envio archivo"); return; }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(filePart.getInputStream(), "UTF-8"));

        int procesados = 0, errores = 0;
        List<String> listaErrores = new ArrayList<>();

        String linea;
        int numLinea = 0;

        while ((linea = reader.readLine()) != null) {
            numLinea++;
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            try {
                if      (linea.startsWith("USUARIO"))          procesarUsuario(linea);
                else if (linea.startsWith("DESTINO"))          procesarDestino(linea);
                else if (linea.startsWith("PROVEEDOR"))        procesarProveedor(linea);
                else if (linea.startsWith("SERVICIO_PAQUETE")) procesarServicioPaquete(linea);
                else if (linea.startsWith("PAQUETE"))          procesarPaquete(linea);
                else if (linea.startsWith("CLIENTE"))          procesarCliente(linea);
                else if (linea.startsWith("RESERVACION"))      procesarReservacion(linea);
                else if (linea.startsWith("PAGO"))             procesarPago(linea);
                else {
                    listaErrores.add("Linea " + numLinea + " [FORMATO]: Instruccion desconocida");
                    errores++; continue;
                }
                procesados++;
            } catch (Exception e) {
                listaErrores.add("Linea " + numLinea + " [ERROR]: " + e.getMessage());
                errores++;
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("procesados", procesados);
        resultado.put("errores",    errores);
        resultado.put("detalle",    listaErrores);
        JsonUtil.sendJson(res, 200, resultado);
    }

    private String[] extraerArgs(String linea) {
        Pattern p = Pattern.compile("\"([^\"]*)\"|([^,()\"\\s][^,()\"]*[^,()\"\\s]?|\\d+\\.?\\d*)");
        Matcher m = p.matcher(linea.substring(linea.indexOf('(') + 1, linea.lastIndexOf(')')));
        List<String> args = new ArrayList<>();
        while (m.find()) args.add(m.group(1) != null ? m.group(1) : m.group(2).trim());
        return args.toArray(new String[0]);
    }

    private void procesarUsuario(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 3) throw new Exception("USUARIO requiere 3 argumentos");
        if (a[1].length() < 6) throw new Exception("Password debe tener minimo 6 caracteres");
        if (usuarioDAO.buscarPorNombre(a[0]) != null) throw new Exception("Usuario '" + a[0] + "' ya existe");
        Usuario u = new Usuario();
        u.setNombre(a[0]); u.setPassword(a[1]); u.setTipo(Integer.parseInt(a[2]));
        if (!usuarioDAO.insertar(u)) throw new Exception("Error al insertar usuario");
    }

    private void procesarDestino(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 3) throw new Exception("DESTINO requiere 3 argumentos");
        Destino d = new Destino();
        d.setNombre(a[0]); d.setPais(a[1]); d.setDescripcion(a[2]);
        if (!destinoDAO.insertar(d)) throw new Exception("Error al insertar destino");
    }

    private void procesarProveedor(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 3) throw new Exception("PROVEEDOR requiere 3 argumentos");
        Proveedor p = new Proveedor();
        p.setNombre(a[0]); p.setTipo(Integer.parseInt(a[1])); p.setPais(a[2]);
        if (!proveedorDAO.insertar(p)) throw new Exception("Error al insertar proveedor");
    }

    private void procesarPaquete(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 5) throw new Exception("PAQUETE requiere 5 argumentos");
        // Buscar destino por nombre
        List<Destino> destinos = destinoDAO.listar();
        Destino dest = destinos.stream()
                .filter(d -> d.getNombre().equalsIgnoreCase(a[1]))
                .findFirst().orElse(null);
        if (dest == null) throw new Exception("Destino '" + a[1] + "' no existe");
        Paquete p = new Paquete();
        p.setNombre(a[0]); p.setDestinoId(dest.getId());
        p.setDuracionDias(Integer.parseInt(a[2]));
        p.setPrecioVenta(new BigDecimal(a[3]));
        p.setCapacidad(Integer.parseInt(a[4]));
        if (!paqueteDAO.insertar(p)) throw new Exception("Error al insertar paquete");
    }

    private void procesarServicioPaquete(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 4) throw new Exception("SERVICIO_PAQUETE requiere 4 argumentos");
        // Buscar paquete por nombre
        List<Paquete> paquetes = paqueteDAO.listar();
        Paquete paq = paquetes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(a[0]))
                .findFirst().orElse(null);
        if (paq == null) throw new Exception("Paquete '" + a[0] + "' no existe");
        // Buscar proveedor por nombre
        List<Proveedor> proveedores = proveedorDAO.listar();
        Proveedor prov = proveedores.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(a[1]))
                .findFirst().orElse(null);
        if (prov == null) throw new Exception("Proveedor '" + a[1] + "' no existe");
        ServicioPaquete s = new ServicioPaquete();
        s.setPaqueteId(paq.getId()); s.setProveedorId(prov.getId());
        s.setDescripcion(a[2]); s.setCosto(new BigDecimal(a[3]));
        if (!paqueteDAO.insertarServicio(s)) throw new Exception("Error al insertar servicio");
    }

    private void procesarCliente(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 6) throw new Exception("CLIENTE requiere 6 argumentos");
        if (clienteDAO.buscarPorDpi(a[0]) != null) throw new Exception("Cliente '" + a[0] + "' ya existe");
        Cliente c = new Cliente();
        c.setDpi(a[0]); c.setNombre(a[1]);
        c.setFechaNac(new SimpleDateFormat("dd/MM/yyyy").parse(a[2]));
        c.setTelefono(a[3]); c.setEmail(a[4]); c.setNacionalidad(a[5]);
        if (!clienteDAO.insertar(c)) throw new Exception("Error al insertar cliente");
    }

    private void procesarReservacion(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 4) throw new Exception("RESERVACION requiere 4 argumentos");
        List<Paquete> paquetes = paqueteDAO.listar();
        Paquete paq = paquetes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(a[0]))
                .findFirst().orElse(null);
        if (paq == null) throw new Exception("Paquete '" + a[0] + "' no existe");
        Usuario agente = usuarioDAO.buscarPorNombre(a[1]);
        if (agente == null) throw new Exception("Usuario '" + a[1] + "' no existe");
        Reservacion r = new Reservacion();
        r.setPaqueteId(paq.getId());
        r.setAgenteId(agente.getId());
        java.util.Date parsed = new SimpleDateFormat("dd/MM/yyyy").parse(a[2]);
        r.setFechaViaje(new java.sql.Date(parsed.getTime()));
        r.setCostoTotal(paq.getPrecioVenta());
        List<Cliente> pasajeros = new ArrayList<>();
        for (String dpi : a[3].split("\\|")) {
            Cliente c = clienteDAO.buscarPorDpi(dpi.trim());
            if (c == null) throw new Exception("Cliente con DPI '" + dpi + "' no existe");
            pasajeros.add(c);
        }
        r.setPasajeros(pasajeros);
        if (reservacionDAO.insertar(r) == null) throw new Exception("Error al insertar reservacion");
    }

    private void procesarPago(String linea) throws Exception {
        String[] a = extraerArgs(linea);
        if (a.length < 4) throw new Exception("PAGO requiere 4 argumentos");
        Reservacion res = reservacionDAO.buscarPorNumero(a[0]);
        if (res == null) throw new Exception("Reservacion '" + a[0] + "' no existe");
        Pago p = new Pago();
        p.setReservacionId(res.getId());
        p.setMonto(new BigDecimal(a[1]));
        p.setMetodo(Integer.parseInt(a[2]));
        p.setFecha(new SimpleDateFormat("dd/MM/yyyy").parse(a[3]));
        if (!pagoDAO.insertar(p)) throw new Exception("Error al insertar pago");
        // Verificar si se confirma
        BigDecimal totalPagado = pagoDAO.totalPagado(res.getId());
        if (totalPagado.compareTo(res.getCostoTotal()) >= 0) {
            reservacionDAO.cambiarEstado(res.getId(), "CONFIRMADA");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) { res.setStatus(200); }

    private boolean esAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && u.getTipo() == 3;
    }
}