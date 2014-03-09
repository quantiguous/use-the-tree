package com.usethetree;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReturnMsg {

    public static void printReturnMsg (HttpServletRequest request, HttpServletResponse response) {
        
        String errorText = (String) request.getAttribute("errorText");
        String successText = (String) request.getAttribute("successText");
        if (errorText!=null&&!errorText.equals(""))
        	ReturnMsg.appendReturnMsg(response, "red", errorText);
        if (successText!=null&&!successText.equals("")) 
        	ReturnMsg.appendReturnMsg(response, "blue", successText);
    }
       
    public static void appendReturnMsg(HttpServletResponse response, String color, String message) {

        response.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter writer = response.getWriter();
            writer.println( "<font color=\"" + color + "\">" + message + "</font>" );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
