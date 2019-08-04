package jp.imanaga.sample.rsyslog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.net.SyslogOutputStream;

public class MultiLineSyslogAppender extends SyslogAppender {

	Logger alertLogger = LoggerFactory.getLogger("ALERT");

	// https://tools.ietf.org/html/rfc3164
	final Pattern pattern = Pattern.compile("^(<.*>.{15} [^\\s]* )([\\s\\S]*)$");

	int maxMessageSize = 65000;

	@Override
	protected void append(ILoggingEvent eventObject) {

		if (!isStarted()) {
			return;
		}

		String message = getLayout().doLayout(eventObject);
		if (message == null) {
			return;
		}

		Matcher matcher = pattern.matcher(message);
		if (matcher.find()) {

			String prefix = matcher.group(1);
			String suffix = matcher.group(2).replace("\t", "    ");

			try (SyslogOutputStream sos = createOutputStream();) {
				for (String partSuffix : suffix.split("\r\n|\r|\n", -1)) {
					for (String msg : Splitter.fixedLength(maxMessageSize).split(partSuffix)) {
						sos.write(new StringBuilder(prefix).append(msg).toString().getBytes(getCharset()));
						sos.flush();
					}
				}
				postProcess(eventObject, sos);
			} catch (Exception e) {
				alertLogger.error("Syslog stream error.", e);
			}

		} else {
			alertLogger.error("The message doesn't match the pattern. pattern={}, message={}", pattern.pattern(),
					message);
		}
	}

	/**
	 *
	 * @return
	 */
	public int getMaxMessageSize() {
		return maxMessageSize;
	}

	/**
	 * Maximum size for the syslog message (in characters); messages
	 * longer than this are truncated. The default value is 65400 (which
	 * is near the maximum for syslog-over-UDP). Note that the value is
	 * characters; the number of bytes may vary if non-ASCII characters
	 * are present.
	 */
	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = maxMessageSize;
	}
}
