package com.frodare.logging;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class RunTest {

  private static final Logger LOG = Logger.getLogger(RunTest.class.getName());

  public static void main(String[] args) {
    loadProps();
    LOG.info("howdy");
  }

  private static void loadProps() {
    try {
      LogManager.getLogManager().readConfiguration(RunTest.class.getResourceAsStream("/logging.properties"));
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

}
