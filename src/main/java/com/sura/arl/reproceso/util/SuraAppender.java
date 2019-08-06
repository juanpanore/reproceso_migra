package com.sura.arl.reproceso.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import suramericana.swb.trace.api.ISuraLogger;
import suramericana.swb.trace.logging.SuraLogger;

public class SuraAppender extends AppenderBase<ILoggingEvent> {

    ISuraLogger loggerApp = SuraLogger.create(AppLoggingUtil.NOMBRE_APP, AppLoggingUtil.NOMBRE_MOD_PD);

    @Override
    protected void append(ILoggingEvent eventObject) {

        switch (eventObject.getLevel().levelStr) {
        case "INFO":
            loggerApp.info(eventObject.getFormattedMessage());
            break;
        case "DEBUG":
            loggerApp.debug(eventObject.getFormattedMessage());
            break;
        case "ERROR":
            loggerApp.error(eventObject.getFormattedMessage());
            break;
        case "TRACE":
            loggerApp.info(eventObject.getFormattedMessage());
            break;
        default:
            break;
        }

    }

}
