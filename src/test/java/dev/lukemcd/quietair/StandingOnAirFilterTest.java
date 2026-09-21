package dev.lukemcd.quietair;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StandingOnAirFilterTest {
	private static final StandingOnAirFilter FILTER = new StandingOnAirFilter();
	private static LoggerContext ctx;

	@BeforeAll
	static void setUp() {
		ctx = new LoggerContext("quiet-air-test");
	}

	@AfterAll
	static void tearDown() {
		ctx.stop();
	}

	private static Logger logger(String name) {
		return ctx.getLogger(name);
	}

	private static LogEvent event(String loggerName, org.apache.logging.log4j.message.Message msg) {
		return Log4jLogEvent.newBuilder().setLoggerName(loggerName).setLevel(Level.INFO).setMessage(msg).build();
	}

	@Test
	void deniesVanillaLineBeforeEventIsBuilt() {
		assertEquals(Result.DENY, FILTER.filter(logger(StandingOnAirFilter.LOGGER_NAME), Level.INFO, null,
				StandingOnAirFilter.FORMAT, (Object) "lukeeIRL"));
	}

	@Test
	void deniesVanillaLineAsEvent() {
		assertEquals(Result.DENY, FILTER.filter(event(StandingOnAirFilter.LOGGER_NAME,
				new ParameterizedMessage(StandingOnAirFilter.FORMAT, "lukeeIRL"))));
	}

	@Test
	void ignoresSamePatternFromOtherLogger() {
		assertEquals(Result.NEUTRAL, FILTER.filter(logger("some.other.Mod"), Level.INFO, null,
				StandingOnAirFilter.FORMAT, (Object) "lukeeIRL"));
		assertEquals(Result.NEUTRAL, FILTER.filter(event("some.other.Mod",
				new ParameterizedMessage(StandingOnAirFilter.FORMAT, "lukeeIRL"))));
	}

	@Test
	void ignoresChatContainingThePhrase() {
		// Chat is logged with the player's text as an argument or a plain message, never as this pattern.
		assertEquals(Result.NEUTRAL, FILTER.filter(event("net.minecraft.server.MinecraftServer",
				new ParameterizedMessage("{}", "<lukeeIRL> Player {} standing on air - force-sending blocks below"))));
		assertEquals(Result.NEUTRAL, FILTER.filter(event("net.minecraft.server.MinecraftServer",
				new SimpleMessage("<lukeeIRL> lol I'm standing on air"))));
	}

	@Test
	void ignoresOtherLinesFromSameLogger() {
		assertEquals(Result.NEUTRAL, FILTER.filter(logger(StandingOnAirFilter.LOGGER_NAME), Level.WARN, null,
				"{} moved too quickly! {},{},{}", "lukeeIRL", 1, 2, 3));
	}
}
