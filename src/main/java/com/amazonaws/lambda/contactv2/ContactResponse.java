package com.amazonaws.lambda.contactv2;

public class ContactResponse {
	private String contact_id;
	private String message;
	private String create;
	
	public String getContact_id() {
		return contact_id;
	}
	public void setContact_id(String contact_id) {
		this.contact_id = contact_id;
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
	
	@Override
	public String toString() {
		return "ContactResponse [contact_id=" + contact_id + ", message=" + message + ", create=" + create + "]";
	} 
}
