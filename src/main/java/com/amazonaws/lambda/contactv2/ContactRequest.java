package com.amazonaws.lambda.contactv2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Contact")
public class ContactRequest {
		private String contact_id;
		private String site;
		private String name;
		private String email; 
		private String message; 
		private String create;
		private String reCapVal;

		public ContactRequest() {
			this.contact_id = UUID.randomUUID().toString();
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Canada/Pacific"));
			this.create = date.toString();
		}
		public String getContact_id() {
			return contact_id;
		}
		public void setContact_id(String contact_id) {
			this.contact_id = contact_id;
		}
		public String getSite() {
			return site;
		}
		public void setSite(String site) { 
			this.site = site;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getCreate() {
			return create;
		}
		public void setCreate(String create) {
			this.create = create;
		}
		public String getReCapVal() {
			return reCapVal;
		}
		public void setReCapVal(String reCapVal) {
			this.reCapVal = reCapVal;
		}
		@Override
		public String toString() {
			return "ContactRequest [contact_id=" + contact_id + ", site=" + site + ", name=" + name + ", email=" + email
					+ ", message=" + message + ", create=" + create + ", reCapVal=" + reCapVal + "]";
		}
	}