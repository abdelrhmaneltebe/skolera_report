/**
* <h1>Report Service to fill, render and display Jasper report</h1>
* This service is expecting the report name in a request parameter 
* named "$P_REPORT_NAME", also you may send any extra parameters to the report
* by adding this parameter in the format "$P_JASPER_PARAMETER_NAME" the service
* will send only "JASPER_PARAMETER_NAME" and its passed value to Jasper report
* <p>
* <b>Note:</b> in case wrong parameters are passed the service will display a
* message to clarify the source of error or the required missing parameter, etc.
*
* @author  Hany Alshafey
* @version 1.2.5
* @since   2019-11-26
*/
package skolera;

import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import static skolera.EmailSender.sendEmailWithAttachments;

public class report extends HttpServlet {

    /**
     * @return the mailHost
     */
    public String getMailHost() {
        return mailHost;
    }

    /**
     * @param mailHost the mailHost to set
     */
    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    /**
     * @return the mailPort
     */
    public String getMailPort() {
        return mailPort;
    }

    /**
     * @param mailPort the mailPort to set
     */
    public void setMailPort(String mailPort) {
        this.mailPort = mailPort;
    }

    /**
     * @return the mailUser
     */
    public String getMailUser() {
        return mailUser;
    }

    /**
     * @param mailUser the mailUser to set
     */
    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    /**
     * @return the mailPassword
     */
    public String getMailPassword() {
        return mailPassword;
    }

    /**
     * @param mailPassword the mailPassword to set
     */
    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }
    /**
     * @return the dbName
     */
    private String mailHost = "";
    /**
     * @return the dbName
     */
    private String mailPort = "";
    /**
     * @return the dbName
     */
    private String mailUser = "";
    /**
     * @return the dbName
     */
    private String mailPassword = "";
    
    
    public String getDbName() {
        return dbName;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return the responseOut
     */
    public HttpServletResponse getResponseOut() {
        return responseOut;
    }

    /**
     * @param responseOut the responseOut to set
     */
    public void setResponseOut(HttpServletResponse responseOut) {
        this.responseOut = responseOut;
    }

    private HttpServletResponse responseOut = null;
    private String dbName = "";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        setResponseOut(response);
        request.setCharacterEncoding("UTF-8");
        Connection conn = null;
        String dbName = request.getParameter("$P_DB_NAME");
        String showErros = request.getParameter("$P_SHOW_ERRORS");
        String token = request.getParameter("$P_TOKEN");
        if (dbName == null || dbName.trim().equals("")|| dbName.trim().equals("----")) {
            message("Missing inputs","Expecting DB name in $P_DB_NAME");
            return;
        }
        String reportName = request.getParameter("$P_REPORT_NAME");
        if (reportName == null || reportName.trim().equals("")|| reportName.trim().equals("----")) {
            message("Missing inputs","Expecting report name in $P_REPORT_NAME");
            return;
        }
        String sendByEmail = request.getParameter("$P_SEND_BY_EMAIL");
        String eMailList = request.getParameter("$P_EMAIL_LIST");
        if (sendByEmail != null && sendByEmail.toUpperCase().equals("YES") && (eMailList == null || eMailList.trim().equals(""))) {
            message("E-Mail issue","You select to send by Email but you didn't specify an $P_EMAIL_LIST");
            return;
        }
        
        if (sendByEmail != null && sendByEmail.toUpperCase().equals("YES") ) {
            String error = "";
            setMailHost(this.getInitParameter("EMAIL_HOST"));
            setMailPort(this.getInitParameter("EMAIL_PORT"));
            setMailUser(this.getInitParameter("EMAIL_USER"));
            setMailPassword(this.getInitParameter("EMAIL_PASSWORD"));
            if (getMailHost().equals(""))        error += "<BR/>Missing email SMTP MAIL_HOST in Config.";
            if (getMailPort().equals(""))        error += "<BR/>Missing email SMTP MAIL_PORT in Config.";
            if (getMailUser().equals(""))        error += "<BR/>Missing email SMTP MAIL_USER in Config.";
            if (getMailPassword().equals(""))    error += "<BR/>Missing SMTP MAIL_PASSWORD in Config.";
           
            if (!error.equals(""))
            {
                message("E-Mail Configuration issue",error);
                return;
            }
        }
        
        setDbName(dbName);
        
        ServletContext context = (ServletContext) getServletConfig().getServletContext();
        String reportFile = "";
        String reportPath = "" ;
        String configName = "REPORT";
        String configSection = "LOCATION";
        reportPath = getKeyFromBundle(configName,configSection,false).trim();
        String tokenRequired =  getKeyFromBundle("REPORT","TOKEN_REQUIRED",false).trim();
        reportFile =  reportName + ".jasper";
         
        String relativeLogoPath = "/WEB-INF/DEFAULT/SKOLERA_HEADER.png";
        String absoluteLogoDiskPath = getServletContext().getRealPath(relativeLogoPath);

        String relativeWebPath = "/WEB-INF/"+getDbName().toUpperCase()+"/"+reportFile;
        String absoluteFolderPath = getServletContext().getRealPath("/WEB-INF/"+getDbName().toUpperCase()+"/");
        String absoluteDiskPath = getServletContext().getRealPath(relativeWebPath);

        File initialFile = new File(absoluteDiskPath);
        InputStream fs = null;
        try {
             fs = new FileInputStream(initialFile);
        } catch(FileNotFoundException ex) {
            message("Invalid file","Please select a valid report name for $P_REPORT_NAME parameter ");
            return;
        }
        
        // Token checking commented temporarily till being implemented by UI     
        //   if (!tokenRequired.equals("FALSE") && !checkValidAuthorization(token)) {
        //     message("Invalid Credentials","Access Forbidden!");
        //       return;
        //  }
       
        Map parm = new HashMap();
        Locale locale = new Locale("en", "US");
        parm.put(JRParameter.REPORT_LOCALE, locale);
        parm.put("ABSOLUTE_PATH",absoluteFolderPath);
        Enumeration en = request.getParameterNames();
        parm.put("REPORT_PATH", reportPath.equals("") ? absoluteFolderPath : reportPath);
        ArrayList parameters = new ArrayList();
        parameters = getKeyFromBundleMany(reportName);
        if (parameters.size() > 0) {
            for (int i = 0; i <parameters.size(); i++) {
                HashMap rec = (HashMap) parameters.get(i);
                parm.put(rec.get("SECTION"), rec.get("VALUE"));
            }
        }
           
        while (en.hasMoreElements()) {
            String requestPram = (String) en.nextElement();
            if (requestPram.startsWith("$P_")) {
                String value = request.getParameter(requestPram);
                String actualParam = null;
                actualParam = requestPram.split("\\$P_")[1];
                if (value != null && !value.trim().equals(""))
                   parm.put(actualParam, value);
            }
        }
       if (reportName != null) {
            try {
                    Context ctx = new InitialContext(); 
                    DataSource ds;
                    ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/"+getDbName());
                    conn = ds.getConnection();
                    JasperReport template = (JasperReport) JRLoader.loadObject(fs);
                    template.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
                    JasperPrint print = JasperFillManager.fillReport(template, parm, conn);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    JasperExportManager.exportReportToPdfStream(print, baos);
                    if (sendByEmail != null && sendByEmail.toUpperCase().equals("YES")) {
                        if (sendToEmails(eMailList,absoluteLogoDiskPath, baos))
                              response.setStatus(response.SC_OK);
                        else {
                              response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
                         }
                    }
                    else
                    {  
                        response.setContentType("application/pdf");
                        ServletOutputStream out = response.getOutputStream();
                        out.write(baos.toByteArray());
                        out.flush();
                        out.close();
                    }
                    conn.close();
                 } catch (JRException ex) {
                      String msg = getTrace(ex);
                      message("Report Error",showErros != null && showErros.toUpperCase().equals("YES") ? ex.getMessage()+"<BR/>"+msg : "");
                       ex.printStackTrace();
                } catch (Exception ex) {
                      String msg = getTrace(ex);
                      message("Exception Error",showErros != null && showErros.toUpperCase().equals("YES") ? ex.getMessage()+"<BR/>"+msg : "");
                       ex.printStackTrace();
                        try {
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
            }

        }
    }
    
    private Boolean sendToEmails(String mailTo,String logoPath, ByteArrayOutputStream bao) {
        String subject = "Report card from Skolera®";
        String  message = "<br/><b>Greetings,</b><br/><br/>You're receiving this email because Skolera® admin has sent you a Report Card(s),"
                        + "<br/><br/>Thanks & Regards," 
                        + "<br/><b>Powered by Skolera®</b>"
                        + "<p>"
                        + "<font size='1.7' color='#778899'>"
                        + "<BR><BR>This email and any files transmitted with it are confidential and intended solely for the use of the individual or entity to whom they are addressed. If you have received this email in error please notify the system manager. This message contains confidential information and is intended only for the individual named. If you are not the named addressee you should not disseminate, distribute or copy this e-mail. Please notify the sender immediately by e-mail if you have received this e-mail by mistake and delete this e-mail from your system. If you are not the intended recipient you are notified that disclosing, copying, distributing or taking any action in reliance on the contents of this information is strictly prohibited."
                        + "</font>"
                        + "</p>";
        try {
               sendEmailWithAttachments(getMailHost(), getMailPort(), getMailUser(), getMailPassword(), mailTo,
                subject, message, logoPath, bao);
                 return true;
        } catch (Exception ex) {
                ex.printStackTrace();
                return false;
        }

       
    }
    private String getTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
      //processRequest(request, response);
      setResponseOut(response);
      message("Skolera Reports","Powered by Skolera®");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Skolera® Report Service";
    }
    public String getKeyFromBundle(String name,String section, boolean required ) {
    Connection conn = null;
    String bundleValue = "";
    try {
        String query = "select VALUE from system_config WHERE name = '"+name+"' and section = '"+section+"'";
        Context ctx = new InitialContext();
        DataSource ds;
        ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/"+getDbName());
        conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next() == false)
        {   if (required == true)
                 message("Invalid Configuration","Please check invalid configuration data");
        }
        else
         {
             do { bundleValue = rs.getString("VALUE"); 
             } while (rs.next()); 
              conn.close();
         }
      }
    catch (SQLException ex) {
        if (required == true)
                     message("Invalid Configuration","Please check invalid configuration data");
                       try {
                           if (conn != null)
                                conn.close();
                       } catch (SQLException e) {
                           // Exception must not be spooled as the config file is now optional
                           //e.printStackTrace();
                       }
                       // Exception must not be spooled as the config file is now optiona
                       //ex.printStackTrace();
                   }
    catch (NamingException ex) {
        message("Invalid Database","Please check $P_DB_NAME for valid DB Connection");
        return "";
    }
    return bundleValue;
}
    
    public Boolean checkValidAuthorization(String token) {
    Connection conn = null;
    Integer countToken = 0;
    try {
        if (token == null) {
             //message("Invalid Configuration","Please check invalid configuration data");
             return false;
        }
        String query = "select count(*) as COUNT_IT from users WHERE LENGTH('"+token+"') >= 20 and tokens like '%"+token+"%'";
        Context ctx = new InitialContext();
        DataSource ds;
        ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/"+getDbName());
        conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next() == false)  ;
            // Exception must not be spooled as the config file is now optional
            //message("Invalid Configuration","Please check invalid configuration data");
        else
         {
             do { countToken = rs.getInt("COUNT_IT"); 
             } while (rs.next()); 
              conn.close();
         }
      }
    catch (SQLException ex) {
                message("Invalid Configuration","Please check invalid configuration data");
                       try {
                           if (conn != null)
                                conn.close();
                       } catch (SQLException e) {
                           // Exception must not be spooled as the config file is now optional
                           //e.printStackTrace();
                       }
                       // Exception must not be spooled as the config file is now optional
                       //ex.printStackTrace();
                   }
    catch (NamingException ex) {
        message("Invalid Database","Please check $P_DB_NAME for valid DB Connection");
        return false;
    }
    if (countToken == 0) return false;
    return true;
}
 
    public ArrayList getKeyFromBundleMany(String name) {
    Connection conn = null;
    String bundleValue = "";
    String sectionValue = "";
    ArrayList  sectionsOut = new ArrayList();
    try {
        String query = "select SECTION,VALUE from system_config WHERE name = '"+name+"'";
        Context ctx = new InitialContext();
        DataSource ds;
        ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/"+getDbName());
        conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        if (rs.next() == false)
            return sectionsOut;
           
        else
         {
             do { 
                 sectionValue = rs.getString("SECTION"); 
                 bundleValue = rs.getString("VALUE");
                 sectionsOut.add(getHashMap("SECTION",sectionValue,"VALUE",bundleValue));
             } while (rs.next()); 
              conn.close();
         }
       
        
     }
    catch (SQLException ex) {
               
                       try {
                           if (conn != null)
                                conn.close();
                       } catch (SQLException e) {
                           // Exception must not be spooled as the config file is now optional
                           //e.printStackTrace();
                       }
                       // Exception must not be spooled as the config file is now optional
                       //ex.printStackTrace();
                   }
    catch (NamingException ex) {
        message("Invalid Database","Please check $P_DB_NAME for valid DB Connection");
        return sectionsOut;
    }
    return sectionsOut;
}
   
    private void message(String ...msg) {
         
      try { 
          String title = "",message = ""; 
          switch (msg.length) {
              case 0:
                  title = "No message...";
                  break;
              case 2:
                  title = msg[0];
                  message = msg[1];
                  break;
              default:
                  message = msg[0];
                  break;
          }

            PrintWriter out = this.getResponseOut().getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Skolera® report</title>");            
            out.println("</head>");
            out.println("<body>");
            if (!title.equals("")) out.println("<h2>"+title+ "</h2>");
            else out.println("<h2> Skolera® Reports </h2>");
            if (!message.equals("")) out.println("<p>"+message+ "</p>");
            out.println("</body>");
            out.println("</html>");
             out.flush();
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }


    public HashMap<String,Object> getHashMap(Object...data)
     {
      HashMap result = new HashMap<String,Object>();
      try{
         for (int i = 0; i < data.length;)
         { result.put(data[i].toString(),data[i+1]);
           i += 2;}
      }catch (Exception ex)
      { ex.printStackTrace(); }
      return result;
     }

}

