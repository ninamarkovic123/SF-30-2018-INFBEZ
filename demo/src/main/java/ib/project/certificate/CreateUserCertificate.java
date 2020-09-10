package ib.project.certificate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bouncycastle.cert.X509CRLHolder;

import ib.dto.UserDTO;
import ib.jks.IssuerData;
import ib.jks.KeyStoreReader;
import ib.jks.KeyStoreWriter;
import ib.jks.SubjectData;
import ib.project.entity.User;

public class CreateUserCertificate {

	private static CertificateGenerator certificateGenerator = new CertificateGenerator();
	private static KeyStoreWriter keyStoreWriter = new KeyStoreWriter();
	private static SignedCertificateGenerator signedCertificateGenerator = new SignedCertificateGenerator();
	private static CRLManager CRLManager = new CRLManager();
	private static KeyStoreReader keyStoreReader = new KeyStoreReader();
	private static final String KEY_STORE_FILE = "./data/root.jks";
	private static final String KEY_STORE_PASS = "root";
	private static final String KEY_STORE_ALIAS = "root";

	public static String createCertificate(UserDTO userDTO) {
	
			SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = new Date(), endDate = new Date();
			KeyStore keyStoreCA = keyStoreReader.readKeyStore(KEY_STORE_FILE, KEY_STORE_PASS.toCharArray());
			Certificate certificateCA = keyStoreReader.getCertificateFromKeyStore(keyStoreCA, KEY_STORE_ALIAS);
			X509Certificate c = (X509Certificate) certificateCA;
			PrivateKey privateKeyCA = keyStoreReader.getPrivateKeyFromKeyStore(keyStoreCA, KEY_STORE_ALIAS, "root".toCharArray());
			System.out.println("Procitan privatni kljuc: " + privateKeyCA);
			PublicKey publicKeyCA = keyStoreReader.getPublicKeyFromCertificate(certificateCA);
			System.out.println("\nProcitan javni kljuc iz sertifikata: " + publicKeyCA);
			IssuerData issuerDataCA = keyStoreReader.getIssuerFromCertificate(certificateCA, privateKeyCA);
			System.out.println("\nProcitani podaci o izdavacu CA sertifikata: " + issuerDataCA);
	
			try {
				startDate = iso8601Formater.parse("2020-04-01");
				endDate = iso8601Formater.parse("2025-04-01");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			KeyPair keyPairSubject = certificateGenerator.generateKeyPair();
			String name = userDTO.getFirstname() + " " + userDTO.getLastname();
			SubjectData subjectData = new SubjectData(keyPairSubject.getPublic(), 
					"FTN", name, "Katedra za informatiku", "RS", userDTO.getEmail(), "123454321", "101", startDate, endDate);
			X509Certificate certificate = signedCertificateGenerator.generateSignedCertificate(issuerDataCA, subjectData);
			printCertificate(certificate);
			X509CRLHolder crlHolder = CRLManager.createCRL(c, privateKeyCA);
			System.out.println("Da li je CRL validan? (provera sa CA sertifikatom): " + CRLManager.isCRLValid(crlHolder, c)); // jeste
			System.out.println("Da li je CRL validan? (provera sa sertifikatom koji nije CA sertifikat): " + CRLManager.isCRLValid(crlHolder, certificate)); // nije
			System.out.println("Da li je sertifikat povucen: " + CRLManager.isCertificateRevoked(crlHolder, certificate.getSerialNumber()));
			KeyStore keystore = keyStoreWriter.loadKeyStore(null, userDTO.getPassword().toCharArray());
			keyStoreWriter.addToKeyStore(keystore, userDTO.getEmail(), keyPairSubject.getPrivate(), userDTO.getPassword().toCharArray(), certificate);
			keyStoreWriter.saveKeyStore(keystore, "C:\\Users\\Aleksandra\\git\\IB-Projekat\\demo\\jks\\"+userDTO.getEmail()+".jks", userDTO.getPassword().toCharArray());
			userDTO.setPath("C:\\Users\\Aleksandra\\git\\IB-Projekat\\demo\\jks\\"+userDTO.getEmail()+".jks");
			try {	
		        FileOutputStream out = new FileOutputStream("C:\\Users\\Aleksandra\\git\\IB-Projekat\\demo\\jks\\"+ userDTO.getEmail() + ".cer");
		        out.write(certificate.getEncoded());
		        out.close();
			} catch (CertificateEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return userDTO.getPath();
		}
	
	
	private static void printCertificate(X509Certificate certificate) {
		System.out.println("ISSUER: " + certificate.getIssuerX500Principal().getName());
		System.out.println("SUBJECT: " + certificate.getSubjectX500Principal().getName());
		System.out.println("Sertifikat:");
		System.out.println("--------------------------------------------------------------------------------------------------------------");
		System.out.println(certificate);
		System.out.println("--------------------------------------------------------------------------------------------------------------");
		System.out.println("--------------------------------------------------------------------------------------------------------------");
	}
	
	
	public static void createRootCertificate() {
		//pokrenuta metoda samo jednom, cuva se u data folderu root.jks koji sluzi za generisanje ostalih sertifikata
		SimpleDateFormat iso8601Formater = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = new Date(), endDate = new Date();
		try {
			startDate = iso8601Formater.parse("2020-04-01");
			endDate = iso8601Formater.parse("2030-04-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		KeyPair keyPairCA = certificateGenerator.generateKeyPair();
		IssuerData issuerDataCA = new IssuerData("IB", "Fakultet tehnickih nauka", "Katedra za informatiku", "RS", 
				"ftnmail_ib@uns.ac.rs", "123445", keyPairCA.getPrivate());
		String serialNumber = "1";
		SubjectData subjectDataCA = new SubjectData(keyPairCA.getPublic(), issuerDataCA.getX500name(), serialNumber, startDate, endDate);
		X509Certificate certificateCA = certificateGenerator.generateCertificate(issuerDataCA, subjectDataCA);
		KeyStore keystore = keyStoreWriter.loadKeyStore(null, "root".toCharArray());
		keyStoreWriter.addToKeyStore(keystore, "root", keyPairCA.getPrivate(), "root".toCharArray(), certificateCA);
		keyStoreWriter.saveKeyStore(keystore, "C:\\Users\\Aleksandra\\git\\IB-Projekat\\demo\\data\\"+"root.jks", "root".toCharArray());

	}
	
	
}
