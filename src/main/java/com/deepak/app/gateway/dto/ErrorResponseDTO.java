package com.deepak.app.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO implements Serializable {

	private static final long serialVersionUID = -643884008217446965L;

	private Date timestamp;

	private Integer status;

	private String errorCode;

	private String errorMessage;

}
