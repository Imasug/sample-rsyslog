package jp.imanaga.sample.rsyslog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	public static void main(String[] args) {

		Logger logger = LoggerFactory.getLogger(App.class);
		logger.info("test\ttest\r\n\ntest");
	}
}
