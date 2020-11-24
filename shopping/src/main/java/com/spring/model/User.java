package com.spring.model;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class User implements Serializable {

	private static final long serialVersionUID = -8850740904859933967L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userid")
	private int userId;
	private String email;
	@Column(unique = true)
	private String username;
	private String password;
	@Column(name = "usertype")
	private String userType;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userid) {
		this.userId = userid;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String usertype) {
		this.userType = usertype;
	}

	@Override
	public String toString() {
		return "User [userid=" + userId + ", email=" + email + ", username=" + username + ", password=" + password
				+ ", age=" + age + ", address=" + address + "]";
	}

	public User() {
		super();
	}

	public User(int userId, String email, String username, String password, int age, Address address) {
		super();
		this.userId = userId;
		this.email = email;
		this.username = username;
		this.password = password;
		this.age = age;
		this.address = address;
	}

	private int age;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
	private Address address;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
		this.password = password;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

}