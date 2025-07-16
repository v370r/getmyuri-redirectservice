package com.getmyuri.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Base67Encoder {

    private static final Logger logger = LoggerFactory.getLogger(Base67Encoder.class);
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~!$";
    private static final int BASE = CHARSET.length();

    public String encode(long number) {

        logger.debug("Encoding number: {}", number);
        StringBuilder sb = new StringBuilder();
        long original = number;
        while (number > 0) {
            sb.append(CHARSET.charAt((int) (number % BASE)));
            number /= BASE;
        }
        while (sb.length() < 7) {
            sb.append(CHARSET.charAt(0));
        }
        String encoded = sb.reverse().toString();
        logger.info("Encoded {} → {}", original, encoded);
        return encoded;
    }
}
