package com.tagit.mobeix.tools.datacli;

import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Parser;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import com.tagit.mobeix.tools.datacli.command.GenerateBillPayDataCommand;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@PropertySource(value = { "classpath:data-cli.properties" }, ignoreResourceNotFound = true)
@Log4j2
@Cli(name = "mdb-cli", description = "Mobeix Digital Banking Command Line Inteface", defaultCommand = Help.class, commands = {
		GenerateBillPayDataCommand.class }, parserConfiguration = @Parser(errorHandler = CollectAll.class))
public class DataCliApplication implements CommandLineRunner, ApplicationContextAware {

	/**
	 * The application context that will be used to bootstrap the commands with
	 * Spring.
	 */
	public static ApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(DataCliApplication.class, args);
	}

	@Override
	public void run(String... args) {

		log.info("[run] Running with arguments: {}", (Object[]) args);
		com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(DataCliApplication.class);
		try {
			// Parse with a result to allow us to inspect the results of parsing
			ParseResult<Runnable> result = cli.parseWithResult(args);
			if (result.wasSuccessful()) {
				// Parsed successfully, so just run the command and exit
				result.getCommand().run();
				System.exit(0);
			} else {
				log.error("[run] Error Results: {}", result);
				// Parsing failed
				// Display errors and then the help information
				System.err.println(String.format("%d errors encountered:", result.getErrors().size()));
				int i = 1;
				for (ParseException e : result.getErrors()) {
					log.error("[run] {}", String.format("Error %d: %s", i, e.getMessage()));
					System.err.println(String.format("Error %d: %s", i, e.getMessage()));
					i++;
				}
				System.err.println();
				com.github.rvesse.airline.help.Help.<Runnable>help(cli.getMetadata(), Arrays.asList(args), System.err);
			}
		} catch (Exception e) {
			log.error("[run] Exception: {}", e);
			// Errors should be being collected so if anything is thrown it is unexpected
			System.err.println(String.format("Unexpected error: %s", e.getMessage()));
			// e.printStackTrace(System.err);
		}

		// If we got here we are exiting abnormally
		System.exit(1);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		log.info("[setApplicationContext] Capturing application context {}", applicationContext);
		DataCliApplication.context = applicationContext;
	}


    @Bean
    ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.enable(SerializationFeature.INDENT_OUTPUT);
		return objectMapper;

	}
	
}
