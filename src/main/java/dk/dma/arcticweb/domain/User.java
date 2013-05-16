package dk.dma.arcticweb.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import org.apache.commons.codec.digest.DigestUtils;

@Entity
@NamedQueries( {
	@NamedQuery(name = "User:getByUsername", query = "SELECT u FROM User u WHERE u.username=:username")
})
public class User extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	private static final String PASSWORD_SALT = "fa26frADu8";	
	
	private String username;
	private String passwordHash;
	private String email;
	private Date lastLogin;
	private Stakeholder stakeholder;	
	
	public User() {
		super();
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	@Override
	public Integer getId() {
		return id;
	}
	
	@Column(unique = true, nullable = false, length = 32)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(nullable = false, length = 256)
	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passowordHash) {
		this.passwordHash = passowordHash;
	}

	@Column(nullable = false, length = 128)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Column(nullable = true)
	public Date getLastLogin() {
		return lastLogin;
	}
	
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true)
	public Stakeholder getStakeholder() {
		return stakeholder;
	}
	
	public void setStakeholder(Stakeholder stakeholder) {
		this.stakeholder = stakeholder;
	}
		
	@Transient
	public void setPassword(String password) {
		setPasswordHash(hashPassword(password));
	}
	
	@Transient
	public boolean passwordMatch(String password) {
		if (password == null) return false;
		return hashPassword(password).equals(getPasswordHash());
	}
	
	@Transient
	public static String hashPassword(String password) {
		return DigestUtils.sha256Hex(DigestUtils.sha256Hex(password) + PASSWORD_SALT);
	}
	
	public static void main(String[] args) {
		String password = "qwerty";
		if (args.length > 0) {
			password = args[0];
		}
		String hash = hashPassword(password);
		System.out.println("password     : " + password);
		System.out.println("password hash: " + hash);
	}
	
}
