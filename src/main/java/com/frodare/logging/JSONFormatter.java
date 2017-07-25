package com.frodare.logging;


import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JSONFormatter extends Formatter {

  @Override
  public String format(LogRecord record) {
    return record.getMessage() + System.getProperty("line.separator");
  }

}
