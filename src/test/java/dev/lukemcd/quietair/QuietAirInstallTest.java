package dev.lukemcd.quietair;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.junit.jupiter.api.Test;

class QuietAirInstallTest {
	static final class Capture extends AbstractAppender {
		final List<String> lines = new ArrayList<>();

		Capture() {
			super("capture", null, null, true, Property.EMPTY_ARRAY);
		}

		@Override
		public void append(LogEvent event) {
			lines.add(event.getMessage().getFormattedMessage());
		}
	}

	static final class TestConfig extends AbstractConfiguration {
		final Capture capture = new Capture();

		TestConfig(LoggerContext ctx) {
			super(ctx, ConfigurationSource.NULL_SOURCE);
		}

		@Override
		protected void doConfigure() {
			capture.start();
			addAppender(capture);
			LoggerConfig root = getRootLogger();
			root.setLevel(Level.INFO);
			root.addAppender(capture, null, null);
		}
	}

	private static void logVanillaLine(Logger logger) {
		logger.info(StandingOnAirFilter.FORMAT, "lukeeIRL");
	}

	@Test
	void filtersBeforeAndAfterReconfigure() {
		LoggerContext ctx = new LoggerContext("quiet-air-install-test");
		try {
			TestConfig first = new TestConfig(ctx);
			ctx.start(first);
			QuietAir.install(ctx);

			Logger vanilla = ctx.getLogger(StandingOnAirFilter.LOGGER_NAME);
			Logger chat = ctx.getLogger("net.minecraft.server.MinecraftServer");

			logVanillaLine(vanilla);
			chat.info("{}", "<lukeeIRL> lol standing on air");
			assertTrue(first.capture.lines.contains("<lukeeIRL> lol standing on air"));
			assertFalse(first.capture.lines.stream().anyMatch(l -> l.startsWith("Player lukeeIRL")));

			TestConfig second = new TestConfig(ctx);
			Configuration previous = ctx.setConfiguration(second);
			assertTrue(previous == first);

			logVanillaLine(vanilla);
			vanilla.info("{} moved too quickly! {},{},{}", "lukeeIRL", 1, 2, 3);
			assertFalse(second.capture.lines.stream().anyMatch(l -> l.startsWith("Player lukeeIRL")));
			assertTrue(second.capture.lines.contains(
					new ParameterizedMessage("{} moved too quickly! {},{},{}", "lukeeIRL", 1, 2, 3).getFormattedMessage()));
		} finally {
			ctx.stop();
		}
	}

	@Test
	void repeatedUpdatesDoNotStackFilters() {
		LoggerContext ctx = new LoggerContext("quiet-air-idempotent-test");
		try {
			TestConfig config = new TestConfig(ctx);
			ctx.start(config);
			QuietAir.install(ctx);
			ctx.updateLoggers();
			ctx.updateLoggers();

			assertTrue(config.getFilter() instanceof StandingOnAirFilter,
					"expected a single StandingOnAirFilter, got " + config.getFilter());
		} finally {
			ctx.stop();
		}
	}
}
