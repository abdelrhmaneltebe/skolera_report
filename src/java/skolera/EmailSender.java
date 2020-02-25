package skolera;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author  Hany Alshafey
 * @version 1.2.5
 * @since   2019-12-16
 */

public class EmailSender {
 
    public static void sendEmailWithAttachments(String host, String port,
            final String userName, final String password, String toAddress,
            String subject, String message, String logoPath, Object attachment)
            throws AddressException, MessagingException {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);
 
        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
 
        toAddress = toAddress.replaceAll("\"", "");
        
        msg.setFrom(new InternetAddress(userName));
        msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        Multipart multipart = new MimeMultipart("related");
         
        // First part, add body with image placeholder in html for Skolera Logo before Greetings
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        message="<img src=\"cid:image\">"+message;
        messageBodyPart.setContent(message, "text/html");
        multipart.addBodyPart(messageBodyPart);

        // second part (the Logo image)
        messageBodyPart = new MimeBodyPart();
        // DataSource fds = new FileDataSource(logoPath);
        InputStream imageStream = null;
        DataSource fds = null;
        try {
            imageStream = new FileInputStream(logoPath);
            fds = new ByteArrayDataSource(IOUtils.toByteArray(imageStream), "image/png");
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<image>");
            multipart.addBodyPart(messageBodyPart);
        } catch (Exception ex) {
             ex.printStackTrace();
        }

        // Third part the attachments
        String[] attachFiles = null;
        ByteArrayOutputStream bao = null;
        if (attachment instanceof String[]) {
             attachFiles = (String[])attachment;
        }
        if (attachment instanceof ByteArrayOutputStream) {
              bao  = (ByteArrayOutputStream)attachment;
        }
        
        // Adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();
                try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
 
                multipart.addBodyPart(attachPart);
            }
        }
        if (bao != null && bao.size() > 0) {
            byte[] bytearray; 
            bytearray = bao.toByteArray(); 
            MimeBodyPart attachPart = new MimeBodyPart();
            try {
                    BufferedDataSource bds = new BufferedDataSource(bytearray,"Report Card.pdf","application/pdf"); // "application/octet-stream"; 
                    attachPart.setDataHandler(new DataHandler(bds)); 
                    attachPart.setFileName(bds.getName()); 
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                multipart.addBodyPart(attachPart);
        }
        // sets the multi-part as e-mail's content
        msg.setContent(multipart);
 
        // sends the e-mail
        Transport.send(msg);
 
    }
 
}



class BufferedDataSource implements DataSource { 
    private byte[] _data; 
    private java.lang.String _name; 
    private String _contentType;
    
    public BufferedDataSource(byte[] data, String name, String contentType) { 
    _data = data; 
    _name = name;
    _contentType = contentType;
    } 

    public String getContentType() { return _contentType;} 
    public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(_data);} 
    public String getName() { return _name;} 

    /** 
    * Returns an OutputStream from the DataSource 
    * @returns OutputStream Array of bytes converted into an OutputStream 
    */ 
    public OutputStream getOutputStream() throws IOException { 
    OutputStream out = new ByteArrayOutputStream(); 
    out.write(_data); 
    return out;
    }
}
