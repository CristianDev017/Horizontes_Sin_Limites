
package com.horizontes.servlet;

import com.horizontes.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        JsonUtil.sendJson(response, 200, Map.of("mensaje", "Sesion cerrada"));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(200);
    }
}