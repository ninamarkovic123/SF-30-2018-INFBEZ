package app;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.xml.security.utils.JavaUtils;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import model.keystore.KeyStoreReader;
import model.mailclient.MailBody;
import support.MailHelper;
import support.MailReader;
import util.Base64;
import util.GzipUtil;

public class ReadMailClient extends MailClient {

	public static long PAGE_SIZE = 3;
	public static boolean ONLY_FIRST_PAGE = true;
	
	private static final String KEY_FILE = "./data/session.key";
	private static final String IV1_FILE = "./data/iv1.bin";
	private static final String IV2_FILE = "./data/iv2.bin";
	
	public static void main(String[] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, MessagingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        // Build a new authorized API client service.
        Gmail service = getGmailService();
        ArrayList<MimeMessage> mimeMessages = new ArrayList<MimeMessage>();
        
        String user = "me";
        String query = "is:unread label:INBOX";
        
        List<Message> messages = MailReader.listMessagesMatchingQuery(service, user, query, PAGE_SIZE, ONLY_FIRST_PAGE);
        for(int i=0; i<messages.size(); i++) {
        	Message fullM = MailReader.getMessage(service, user, messages.get(i).getId());
        	
        	MimeMessage mimeMessage;
			try {
				
				mimeMessage = MailReader.getMimeMessage(service, user, fullM.getId());
				
				System.out.println("\n Message number " + i);
				System.out.println("From: " + mimeMessage.getHeader("From", null));
				System.out.println("Subject: " + mimeMessage.getSubject());
				System.out.println("Body: " + MailHelper.getText(mimeMessage));
				System.out.println("\n");
				
				mimeMessages.add(mimeMessage);
	        
			} catch (MessagingException e) {
				e.printStackTrace();
			}	
        }
        
        System.out.println("Select a message to decrypt:");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	        
	    String answerStr = reader.readLine();
	    Integer answer = Integer.parseInt(answerStr);
	    
		MimeMessage chosenMessage = mimeMessages.get(answer);
	    
		
		MailBody mailBody = new MailBody(MailHelper.getText(chosenMessage));
		
		KeyStoreReader keyStoreReader = new KeyStoreReader();
		KeyStore ks = keyStoreReader.readKeyStore("./data/userb.jks", "nina".toCharArray());
		PrivateKey pk = keyStoreReader.getPrivateKeyFromKeyStore(ks, "userb", "nina".toCharArray());
		byte[] tajniKljuc = mailBody.getEncKeyBytes();
		
        //TODO: Decrypt a message and decompress it. The private key is stored in a file.
		Cipher aesCipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKey secretKey = new SecretKeySpec(tajniKljuc, "RSA");
		
		Cipher rsaCipherDec = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		
		rsaCipherDec.init(Cipher.DECRYPT_MODE, pk);
		
		//dekriptovanje
				byte[] receivedTxt = rsaCipherDec.doFinal(secretKey.getEncoded());
				
				SecretKey sky = new SecretKeySpec(receivedTxt, "AES");
				System.out.println(sky);
				
				byte[] iv1 = JavaUtils.getBytesFromFile(IV1_FILE);
				IvParameterSpec ivParameterSpec1 = new IvParameterSpec(iv1);
				aesCipherDec.init(Cipher.DECRYPT_MODE, sky, ivParameterSpec1);
				
				byte[] dm = aesCipherDec.doFinal(mailBody.getEncMessageBytes());
				String newstr = new String(dm);
				
				String dbt = GzipUtil.decompress(Base64.decode(newstr));
				System.out.println("Mail: " + dbt);
				
				byte[] iv2 = JavaUtils.getBytesFromFile(IV2_FILE);
				IvParameterSpec ivParameterSpec2 = new IvParameterSpec(iv2);
				//inicijalizacija za dekriptovanje
				aesCipherDec.init(Cipher.DECRYPT_MODE, sky, ivParameterSpec2);
				
				//dekompresovanje i dekriptovanje subject-a
				String decryptedSubjectTxt = new String(aesCipherDec.doFinal(Base64.decode(chosenMessage.getSubject())));
				String decompressedSubjectTxt = GzipUtil.decompress(Base64.decode(decryptedSubjectTxt));
				System.out.println("Subject text: " + new String(decompressedSubjectTxt));
			}
	}
