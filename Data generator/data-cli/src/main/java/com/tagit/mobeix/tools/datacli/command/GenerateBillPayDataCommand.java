package com.tagit.mobeix.tools.datacli.command;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import com.github.rvesse.airline.annotations.restrictions.NotEmpty;
import com.github.rvesse.airline.annotations.restrictions.Once;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.tagit.mobeix.tools.datacli.DataCliApplication;
import com.tagit.mobeix.tools.datacli.data.ActivitySchema;
import com.tagit.mobeix.tools.datacli.data.BillPaymentSchema;
import com.tagit.mobeix.tools.datacli.data.Format;
import com.tagit.mobeix.tools.datacli.data.Geolocation;
import com.tagit.mobeix.tools.datacli.data.UserActivity;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ToString
@Command(name = "generate-billpay-data", description = "Generate recurring Bill Payment data from start date till today")
public class GenerateBillPayDataCommand implements Runnable {

   private final static int MAXIMUM_ITERATIONS = 1000;

   @Autowired
   private ObjectMapper objectMapper;
   
   public GenerateBillPayDataCommand() {
      super();
      DataCliApplication.context.getAutowireCapableBeanFactory().autowireBean(this);
      log.info("[Constructor] {}", this);
      
   }

   @Option(name = { "-u", "--user" }, description = "The user customer identifier", title = "User CIF")
   @Required
   @NotEmpty
   @NotBlank
   @Once
   public String userId;
   
   @Option(name = { "-b", "--biller" }, description = "The biller code", title = "Biller Code")
   @Required
   @NotEmpty
   @NotBlank
   @Once
   public String billerCode;

   @Option(name = { "-s", "--startdate" }, description = "Start Date in ISO format, ex. 1996-09-05", title = "Start Date")
   @Once
   public String startDateStr;

   @Option(name = { "-f", "--freq" }, description = "Period frequency in ISO format, ex. P1M, P4W, P30D", title = "Period Frequency")
   @Once
   public String periodStr = "P1M";
   
   @Option(name = { "-a", "--amount" }, description = "The amount", title = "Bill Amount")
   @Once
   public BigDecimal amount; 

   @Option(name = { "-o", "--output" }, description = "Output format in JSON or CSV", title = "Output format")
   @Once
   public Format format = Format.JSON; 
   
   @Override
   public void run() {
      log.info("[run] {}", this);
      
      Period period = Period.parse(periodStr);
      LocalDate startDate = LocalDate.parse(startDateStr);
      
      LocalDate date = startDate;
      List<UserActivity> activities = new ArrayList<>(); 
      int count = 0;
      do {

          UserActivity activity = new UserActivity();
          activity.setUser(userId);
          activity.setActivity("BILLPAY");
          activity.setStatus("S");
          
          BillPaymentSchema billpay = new BillPaymentSchema();
          billpay.setSchema("BILLPAY");     
          billpay.setSchemaVersion(1);
          
          
          BigDecimal actualAmount = amount;
          double seed = RandomUtils.nextDouble(0, 100);
          if (seed > 90) {
        	  actualAmount = amount.add(new BigDecimal(amount.doubleValue() * RandomUtils.nextDouble(0, 0.20))).setScale(2, RoundingMode.HALF_EVEN);
          } else if (seed > 80) {
        	  actualAmount = amount.subtract(new BigDecimal(amount.doubleValue() * RandomUtils.nextDouble(0, 0.20))).setScale(2, RoundingMode.HALF_EVEN);
          }
          
          billpay.setBillerCode(billerCode);
          billpay.setDebitAccountId("10000000001");
          billpay.setDebitCcy("SGD");
          billpay.setDebitAmount(actualAmount);
          billpay.setBillerCcy("SGD");
          billpay.setBillAmount(actualAmount);
          billpay.setExchangeRate(BigDecimal.ONE);
          billpay.setLocalEquivAmount(actualAmount);
          billpay.setLocalEquivRate(BigDecimal.ONE);
          activity.setData(billpay);
          
          ZonedDateTime activityDate = generateActivityDate(date);
    	  activity.setDate(activityDate);
    	  activity.setReference("BP-" + activityDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssnnnnnn")) + "-" + RandomStringUtils.randomNumeric(3));
          activity.setGeolocation(new Geolocation(RandomUtils.nextDouble(0,90), RandomUtils.nextDouble(0, 180), RandomUtils.nextDouble(0, 1)));
          activities.add(activity);
          
          date = computeNextActivityDate(date, period);
          count++;
      } while (date.isBefore(LocalDate.now()) && count < MAXIMUM_ITERATIONS);
      
      if (count >= MAXIMUM_ITERATIONS)
    	  log.warn("[run] Maximum iterations {} reached. Terminating iteration.", MAXIMUM_ITERATIONS);
      
      export(activities);
   }

   
   /**
    * 
    * @param activities
    */
	private void export(List<UserActivity> activities) {

		switch (format) {
		case CSV:

			// CsvWriter (and all other file writers) work with an instance of
			// java.io.Writer
			Writer outputWriter = new OutputStreamWriter(System.out);
			CsvWriter writer = new CsvWriter(outputWriter, new CsvWriterSettings());
			writer.writeHeaders("User", "Date", "Activity", "Status", "Reference", "Schema", "Schema Version", "Debit Account", "Debit Currency", "Debit Amount", "Biller Code", "Biller Currency", "Bill Amount", "FX Rate", "Local Equivalent", "Local Equivalent FX Rate");

			activities.forEach(activity -> {
				
				BillPaymentSchema data = BillPaymentSchema.class.cast(activity.getData());
				
				List<Object> columns = new ArrayList<>();
				columns.add(activity.getUser());
				columns.add(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(activity.getDate()));
				columns.add(activity.getActivity());
				columns.add(activity.getStatus());
				columns.add(activity.getReference());
				columns.add(data.getSchema());
				columns.add(data.getSchemaVersion());
				columns.add(data.getDebitAccountId());
				columns.add(data.getDebitCcy());
				columns.add(data.getDebitAmount());
				columns.add(data.getBillerCode());
				columns.add(data.getBillerCcy());
				columns.add(data.getBillAmount());
				columns.add(data.getExchangeRate());
				columns.add(data.getLocalEquivAmount());				
				columns.add(data.getLocalEquivRate());
				// writes the row
				writer.writeRow(columns);
			});
			writer.close();

			break;
		default:
		case JSON:
			// System.out.println(gson.toJson(activities));
			try {
				System.out.println(objectMapper.writeValueAsString(activities));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}

	}


	/**
     * @param date current date  
     * @param period period
     * @return return next working date with a little bit of randomization
     */
	private static LocalDate computeNextActivityDate(LocalDate date, Period period) {
		LocalDate next = date.plus(period);
		int adjustDays = RandomUtils.nextBoolean() ? RandomUtils.nextInt(0, 2) : -1 * RandomUtils.nextInt(0, 2);
		log.debug("[computeNextActivityDate] date = {}, period = {}, next = {}, adjustDays = {}", date, period, next, adjustDays);
		next = next.plusDays(adjustDays);
		while (DayOfWeek.of(next.get(ChronoField.DAY_OF_WEEK)) == DayOfWeek.SATURDAY || DayOfWeek.of(next.get(ChronoField.DAY_OF_WEEK)) == DayOfWeek.SUNDAY) {
			log.debug("[computeNextActivityDate] next = {}, daysofWeek = {}", next, DayOfWeek.of(next.get(ChronoField.DAY_OF_WEEK)));
			next = next.minusDays(1);
		}
		return next;
	}

	/**
	 * @param date Local Date
	 * @return ZonedDateTime with randomized time
	 */
	private static ZonedDateTime generateActivityDate(LocalDate date) {
		LocalTime randomTime = LocalTime.ofNanoOfDay(RandomUtils.nextLong(0, 86400000000000L));
		ZonedDateTime zdt = ZonedDateTime.of(date, randomTime, ZoneId.systemDefault());
		log.debug("[generateActivityDate] zdt = {}", zdt);
		return zdt;
	}
	
}