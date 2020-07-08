package ib.project.entity;


import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;

import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="Users")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "user_id")
	private Long id;
	
	@Column(name = "username", unique = true, nullable = false)
	private String username;
	
	//@JsonIgnore
	@Column(name = "password", unique = false, nullable = false)
	private String password;
	
	@Column(name = "enabled", unique = false, nullable = false)
	private boolean enabled;
	
	@Column(name = "email", unique = false, nullable = true)
	private String email;
	
	@JsonIgnore
	@Column(name = "certificate", unique = false, nullable = true)
	private String certificate;
	
	@JsonIgnore
	@Column(name = "last_password_reset_date")
    private Timestamp lastPasswordResetDate;
	
	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "authorities_users", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "authority_id"))
	private Set<Authority> authority;


	public User() {
		
	}

	public User(String username, String password, Set<Authority> authority) {
		super();
		this.username = username;
		this.password = password;
		this.authority = authority;
	}

	

	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		Timestamp now = new Timestamp(DateTime.now().getMillis());
        this.setLastPasswordResetDate( now );
        this.password = password;
	}
	

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Set<Authority> getAuthority() {
		return authority;
	}

	public void setAuthority(Set<Authority> authority) {
		this.authority = authority;
	}
	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public Timestamp getLastPasswordResetDate() {
		return lastPasswordResetDate;
	}



	public void setLastPasswordResetDate(Timestamp lastPasswordResetDate) {
		this.lastPasswordResetDate = lastPasswordResetDate;
	}

	
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + "]";
	}
	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authority;
    }
	
	@JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @JsonIgnore
    public String getAuthoritiesAsString() {
    	StringBuilder sb = new StringBuilder();
    	
    	for (Authority authority : this.authority) {
    		sb.append(authority.getName() + " ");
    	}
    	
    	return sb.toString();
    }

}




