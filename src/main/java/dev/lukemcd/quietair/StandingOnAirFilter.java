package dev.lukemcd.quietair;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class StandingOnAirFilter extends AbstractFilter {
	static final String LOGGER_NAME = "net.minecraft.server.network.ServerGamePacketListenerImpl";
	static final String FORMAT = "Player {} standing on air - force-sending blocks below";

	private static Result check(String loggerName, String format) {
		return FORMAT.equals(format) && LOGGER_NAME.equals(loggerName) ? Result.DENY : Result.NEUTRAL;
	}

	@SuppressWarnings("deprecation")
	private static String formatOf(Message msg) {
		return msg == null ? null : msg.getFormat();
	}

	@Override
	public Result filter(LogEvent event) {
		return check(event.getLoggerName(), formatOf(event.getMessage()));
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
		return check(logger.getName(), msg);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
		return check(logger.getName(), msg instanceof String s ? s : null);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
		return check(logger.getName(), formatOf(msg));
	}
}
