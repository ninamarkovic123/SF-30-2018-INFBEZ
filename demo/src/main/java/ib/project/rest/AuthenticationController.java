package ib.project.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ib.project.security.authentication.JwtAuthenticationRequest;
import ib.exception.ResourceConflictException;
import ib.security.TokenUtils;
import ib.project.certificate.CreateUserCertificate;
import ib.dto.UserDTO;
import ib.project.entity.User;
import ib.project.entity.UserTokenState;
import ib.project.service.impl.CustomUserDetailsService;
import ib.service.UserService;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

	@Autowired
	private TokenUtils tokenUtils;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	@Autowired
	private UserService userService;

	@PostMapping("/login")
	public ResponseEntity<UserTokenState> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest,
			HttpServletResponse response) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
						authenticationRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		User user = (User) authentication.getPrincipal();
		String jwt = tokenUtils.generateToken(user.getEmail(), user.getAuthoritiesAsString());
		int expiresIn = tokenUtils.getExpiredIn();

		return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
	}

	//registracija korisnika
	@RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> register(@RequestBody UserDTO userDTO) throws ParseException {
		 User existUser = this.userService.findByEmail(userDTO.getEmail());
			if (existUser != null) {
				throw new ResourceConflictException(userDTO.getId(), "User already exists");
			}
			userDTO.setPath(CreateUserCertificate.createCertificate(userDTO));
			User user = this.userService.save(userDTO);
			return new ResponseEntity<>(user, HttpStatus.CREATED);
	  }
	
	// U slucaju isteka vazenja JWT tokena, endpoint koji se poziva da se token osvezi
	@PostMapping(value = "/refresh")
	public ResponseEntity<UserTokenState> refreshAuthenticationToken(HttpServletRequest request) {

		String token = tokenUtils.getToken(request);
		String username = this.tokenUtils.getUsernameFromToken(token);
		User user = (User) this.userDetailsService.loadUserByUsername(username);

		if (this.tokenUtils.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
			String refreshedToken = tokenUtils.refreshToken(token);
			int expiresIn = tokenUtils.getExpiredIn();

			return ResponseEntity.ok(new UserTokenState(refreshedToken, expiresIn));
		} else {
			UserTokenState userTokenState = new UserTokenState();
			return ResponseEntity.badRequest().body(userTokenState);
		}
	}
	@RequestMapping(value = "/change-password", method = RequestMethod.POST)
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> changePassword(@RequestBody PasswordChanger passwordChanger) {
		userDetailsService.changePassword(passwordChanger.oldPassword, passwordChanger.newPassword);

		Map<String, String> result = new HashMap<>();
		result.put("result", "success");
		return ResponseEntity.accepted().body(result);
	}

	static class PasswordChanger {
		public String oldPassword;
		public String newPassword;
	}
	
	@RequestMapping(value = "/download/{username}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> download(@PathVariable("username")String username) {
		String fileName = username + ".jks";
		Path path = Paths.get("C:\\Users\\Lenovo\\git\\SF-30-2018-INFBEZ\\demo\\jks" +fileName);
		System.out.println(fileName);
		File file = null;
		try {
			file = new File(path.toString());
		}
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} 
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("filename", fileName);
		byte[] bFile = readBytesFromFile(file.toString());
		return ResponseEntity.ok().headers(headers).body(bFile);
	}
	
	
	@RequestMapping(value = "/certificate/{username}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadCertificate(@PathVariable("username")String username) {
		String fileName = username + ".cer";
		Path path = Paths.get("C:\\Users\\Lenovo\\git\\SF-30-2018-INFBEZ\\demo\\jks" +fileName);
		File file = null;
		try {
			file = new File(path.toString());
		}
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} 
		HttpHeaders headers = new HttpHeaders();
		headers.add("filename", fileName);
		byte[] bFile = readBytesFromFile(file.toString());
		return ResponseEntity.ok().headers(headers).body(bFile);
	}	
	
	@RequestMapping(value = "/search/{email}", method = RequestMethod.GET)
	public ResponseEntity<User> searchUser(@PathVariable("email")String email) {
		User user = userService.findByEmail(email);
		return ResponseEntity.ok().body(user);
	}
	
	
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.findByActive(false);
		return ResponseEntity.accepted().body(users);
	}
	
	
	@RequestMapping(value = "/activate/{email}", method = RequestMethod.GET)
	public ResponseEntity<User> activateUser(@PathVariable("email")String email) {
		User user = userService.activateUser(email);
		return ResponseEntity.accepted().body(user);
	}
	
	private static byte[] readBytesFromFile(String filePath) {

		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;
		try {

			File file = new File(filePath);
			bytesArray = new byte[(int) file.length()];
			// read file into bytes[]
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bytesArray);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return bytesArray;
	}

	public File getResourceFilePath(String path) {
		
		URL url = this.getClass().getClassLoader().getResource(path);
		File file = null;

		try {
			
			file = new File(url.toURI());
		} catch (Exception e) {
			file = new File(url.getPath());
		}

		return file;
	}
	
	
}