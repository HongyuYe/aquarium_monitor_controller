package com.shidian.mail;

import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;


//sender
public class MailSender {
//send email by text format

	public boolean sendTextMail(final MailInfo mailInfo) {

		// need authentication or not
		MyAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		if (mailInfo.isValidate()) {
			// if yes, create a password authenticator
			authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		}
		// create a session to send email based on the password authenticator and the attributes of email conversation
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);

//		Session sendMailSession = Session.getInstance(pro, new Authenticator() {
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(mailInfo.getUserName(),mailInfo.getPassword());
//			}
//		});

		try {
			// create an email message based on the session
			Message mailMessage = new MimeMessage(sendMailSession);
			// create the email address of the sender
			Address from = new InternetAddress(mailInfo.getFromAddress());
			// set the email sender
			mailMessage.setFrom(from);
			// create the email address of the receiver and set into the email message
			Address to = new InternetAddress(mailInfo.getToAddress());
			mailMessage.setRecipient(Message.RecipientType.TO, to);
			// set the subject of the email message
			mailMessage.setSubject(mailInfo.getSubject());
			// set the time of  when the email  message sent
			mailMessage.setSentDate(new Date());

			// set the main content of the email message
			String mailContent = mailInfo.getContent();
			mailMessage.setText(mailContent);
			// sent email
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}

//send email by HTML format
	public static boolean sendHtmlMail(MailInfo mailInfo) {
		// need authentication or not
		MyAuthenticator authenticator = null;
		Properties pro = mailInfo.getProperties();
		// if yes, create a password authenticator
		if (mailInfo.isValidate()) {
			authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());
		}
		// create a session to send email based on the password authenticator and the attributes of email conversation
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
		try {
			// create an email message based on the session
			Message mailMessage = new MimeMessage(sendMailSession);
			// create the email address of the sender
			Address from = new InternetAddress(mailInfo.getFromAddress());
			// set the email sender
			mailMessage.setFrom(from);
			// create the email address of the receiver and set into the email message
			Address to = new InternetAddress(mailInfo.getToAddress());
			// Message.RecipientType.TO means the type of the receiver is TO
			mailMessage.setRecipient(Message.RecipientType.TO, to);
			// set the subject of the email message
			mailMessage.setSubject(mailInfo.getSubject());
			// set the time of when the email  message sent
			mailMessage.setSentDate(new Date());
			// MiniMultipart class is a container class, contain the object of MimeBodyPart type
			Multipart mainPart = new MimeMultipart();
			// create a MimeBodyPart which contain HTML content
			BodyPart html = new MimeBodyPart();
			// set the HTML content
			html.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
			mainPart.addBodyPart(html);
			// set MiniMultipart object as the email content
			mailMessage.setContent(mainPart);
			// send email
			Transport.send(mailMessage);
			return true;
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		return false;
	}


//sent email with attachment
	public boolean sendFileMail(MailInfo info, File file){
		Message attachmentMail = createAttachmentMail(info,file);
		try {
			Transport.send(attachmentMail);
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}

	}

//create email with attachments
	private Message createAttachmentMail(final MailInfo info, File file) {
		//create email
		MimeMessage message = null;
		Properties pro = info.getProperties();
		try {

			Session sendMailSession = Session.getInstance(pro, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(info.getUserName(),info.getPassword());
				}
			});

			message = new MimeMessage(sendMailSession);
			// set the basic information of the email
			//create the email address of the sender
			Address from = new InternetAddress(info.getFromAddress());
			//set the email sender
			message.setFrom(from);
			//create the email address of the receiver and set into the email message
			Address to = new InternetAddress(info.getToAddress());
			//set the email receiver, Message.RecipientType.TO means the type of the receiver is TO
			message.setRecipient(Message.RecipientType.TO, to);
			//set the subject of the email message
			message.setSubject(info.getSubject());

			// create the body of the email, use CharSet=UTF-8 to indicate  character encoding to avoid gibberish
			MimeBodyPart text = new MimeBodyPart();
			text.setContent(info.getContent(), "text/html;charset=UTF-8");

			// reate container to describe the data relation
			MimeMultipart mp = new MimeMultipart();
			mp.addBodyPart(text);
				// create email attachment
				MimeBodyPart attach = new MimeBodyPart();

			FileDataSource ds = new FileDataSource(file);
			DataHandler dh = new DataHandler(ds);
				attach.setDataHandler(dh);
				attach.setFileName(MimeUtility.encodeText(dh.getName()));
				mp.addBodyPart(attach);
			mp.setSubType("mixed");
			message.setContent(mp);
			message.saveChanges();

		} catch (Exception e) {
			Log.e("TAG", "failed to create the email with attachment");
			e.printStackTrace();
		}
		// return the email generated
		return message;
	}

}
