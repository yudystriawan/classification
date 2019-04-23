/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skripsi.classifier;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/**
 *
 * @author Yudystriawan
 */
public class Classification extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        JSONObject jsono = new JSONObject();
        String isi = "mohon di tindak mobil yang parkir di pinggir jalan yang ada di jalanan ayani banjar tek tek karena mengganggu pengguna jalan lain";
        PrintWriter pw = resp.getWriter();
        Preprocessing p = new Preprocessing(isi);
        Model m = new Model();
//        jsono.put("isi", p.result());
        JSONObject jsono = m.process(isi);
        
//        pw.write(m.modelPath.toString());
        pw.write(jsono.toString());
    }

}
