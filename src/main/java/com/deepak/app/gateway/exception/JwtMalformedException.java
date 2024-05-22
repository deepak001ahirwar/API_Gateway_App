package com.deepak.app.gateway.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtMalformedException extends RuntimeException {

	private static final long serialVersionUID = 6726443746300207815L;
	
	private String message;
	
	public JwtMalformedException(String message) {
		super(message);
		this.message = message;
	}

}
