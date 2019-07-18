package org.akj.batch.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Person {
	@Id
//	@GeneratedValue(generator = "uuid")
//	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(length = 64)
	private String pid;
	private String lastName;
	private String firstName;
	private int gender;
	private int age;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date dateOfBirth;
	private int height;
	private int weight;

}