package dev.lukemcd.quietair;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuietAir implements ModInitializer {
	public static final String MOD_ID = "quiet-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if (!(LogManager.getContext(false) instanceof LoggerContext ctx)) {
			LOGGER.warn("Quiet Air: logging backend is not Log4j Core, nothing will be filtered");
			return;
		}

		install(ctx);
		LOGGER.info("Quiet Air loaded - hiding 'force-sending blocks below' spam");
	}

	static void install(LoggerContext ctx) {
		attach(ctx, ctx.getConfiguration());

		ctx.addPropertyChangeListener(evt -> {
			if (LoggerContext.PROPERTY_CONFIG.equals(evt.getPropertyName())
					&& evt.getNewValue() instanceof Configuration config) {
				attach(ctx, config);
			}
		});
	}

	private static void attach(LoggerContext ctx, Configuration config) {
		if (hasFilter(config)) {
			return;
		}

		StandingOnAirFilter filter = new StandingOnAirFilter();
		filter.start();
		config.addFilter(filter);
		ctx.updateLoggers();
	}

	private static boolean hasFilter(Configuration config) {
		Filter existing = config.getFilter();
		if (existing instanceof CompositeFilter composite) {
			for (Filter f : composite.getFiltersArray()) {
				if (f instanceof StandingOnAirFilter) {
					return true;
				}
			}
			return false;
		}
		return existing instanceof StandingOnAirFilter;
	}
}
