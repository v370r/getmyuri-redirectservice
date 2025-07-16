package com.getmyuri.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateCalculations {

    private static final Logger logger = LoggerFactory.getLogger(DateCalculations.class);

    public static Date calculateExpiryFrom(Date baseTime, String ttlStr) {

        logger.info("Calculating expiry from baseTime={} with TTL string='{}'", baseTime, ttlStr);

        ZonedDateTime zdt = ZonedDateTime.ofInstant(baseTime.toInstant(), ZoneOffset.UTC);

        Pattern pattern = Pattern.compile("(\\d+)([Mdhm])");
        Matcher matcher = pattern.matcher(ttlStr);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "M":
                    zdt = zdt.plusMonths(value);
                    logger.debug("Added {} month(s)", value);
                    break;
                case "d":
                    zdt = zdt.plusDays(value);
                    logger.debug("Added {} day(s)", value);
                    break;
                case "h":
                    zdt = zdt.plusHours(value);
                    logger.debug("Added {} hour(s)", value);
                    break;
                case "m":
                    zdt = zdt.plusMinutes(value);
                    logger.debug("Added {} minute(s)", value);
                    break;
            }
        }
        Date expiryDate = Date.from(zdt.toInstant());
        logger.info("Final calculated expiry date: {}", expiryDate);
        return expiryDate;
    }
}
