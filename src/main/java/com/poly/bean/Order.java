package com.poly.bean;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuppressWarnings("serial")
@Table(name="orders")
@NamedQuery(name="Order.findAll", query="SELECT o FROM Order o")
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String address;

	@Temporal(TemporalType.DATE)
    @Column(name = "createdate")
    Date createdate=new Date();

	private String email;

	private String fullname;

	private String phone;

	private boolean status;

	//bi-directional many-to-one association to Orderdetail
	@JsonIgnore
	@OneToMany(mappedBy="order")
	private List<Orderdetail> orderdetails;

	//bi-directional many-to-one association to Account
	@ManyToOne
	@JoinColumn(name="username")
	private Account account;

	

}