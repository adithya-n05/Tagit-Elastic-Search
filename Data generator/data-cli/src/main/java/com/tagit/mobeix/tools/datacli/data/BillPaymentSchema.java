package com.tagit.mobeix.tools.datacli.data;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class BillPaymentSchema extends FinTxnSchema {

	private String billerCode;
	private String billerCcy;
	private BigDecimal billAmount;
	
}
