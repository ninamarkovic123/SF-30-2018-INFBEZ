package ib.dto;

public class UserDTO {

	private Long id;

	private String email;

	private String password;

	private String username;
	
	private String firstname;

	private String lastname;
	
	private String path;
	
	public UserDTO(String email, String password, String username, String firstname, String lastname) {
		this.email=email;
		this.password=password;
		this.username=username;
		this.firstname=firstname;
		this.lastname=lastname;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


}
