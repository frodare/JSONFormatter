package com.frodare.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

public class JSONFormatterTest {

  private static final Logger LOG = Logger.getLogger(JSONFormatter.class.getName());

  static {
    for(Handler handler : Logger.getLogger("").getHandlers()){
      handler.setFormatter(new JSONFormatter());
    }
  }

  private final JSONFormatter formatter = new JSONFormatter();

  @Test
  public void format() throws Exception {
    LogRecord r = new LogRecord(Level.INFO, "Test");
   // Assert.assertEquals("Test", formatter.format(r));
    LOG.log(Level.INFO, "HOWDY!");

    LOG.log(Level.INFO, "HOWDY!");

    LOG.log(Level.INFO, "HOWDY!");
  }

}