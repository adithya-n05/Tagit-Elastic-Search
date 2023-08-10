package com.tagit.mobeix.tools.datacli.data;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FinTxnSchema implements ActivitySchema {

	private String schema;
	private int schemaVersion;
	
	private String debitAccountId;
	private String debitCcy;
	private BigDecimal debitAmount;

	private BigDecimal exchangeRate;
	
	private BigDecimal localEquivAmount;
	private BigDecimal localEquivRate;
	
}
