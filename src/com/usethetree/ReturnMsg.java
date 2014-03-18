/*
 * Copyright 2014 NH Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
