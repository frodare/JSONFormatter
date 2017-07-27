package com.frodare.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class JSONFormatterTest {

  private static final Logger LOG = Logger.getLogger(JSONFormatter.class.getName());

  static {
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      handler.setFormatter(new JSONFormatter());
    }
  }

  private final JSONFormatter formatter = new JSONFormatter();

  @Test
  public void format() {
    LogRecord r = new LogRecord(Level.INFO, "howdy!");
    r.setSourceMethodName("meth");
    r.setSourceClassName("com.frodare.Test");
    r.setMillis(1001);
    r.setSequenceNumber(15);
    String actual = Deencapsulation.invoke(formatter, "format", r);
    String expected = "{\"level\":\"INFO\",\"message\":\"howdy!\",\"millis\":1001,\"seqNum\":15,\"source\":\"com.frodare.Test.meth()\",\"thrown\":null}\n";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void formatWithFields() {
    Deencapsulation.setField(formatter, "parseMessageFields", true);
    LogRecord r = new LogRecord(Level.INFO, "howdy FOO[Bar] and Foo[BAZ]!");
    r.setSourceMethodName("meth");
    r.setSourceClassName("com.frodare.Test");
    r.setMillis(1001);
    r.setSequenceNumber(15);
    String actual = Deencapsulation.invoke(formatter, "format", r);
    String expectedFields = "{\"FOO\":\"Bar\",\"Foo\":\"BAZ\"}";
    String expected =
        "{\"level\":\"INFO\",\"message\":\"howdy FOO[Bar] and Foo[BAZ]!\",\"millis\":1001,\"seqNum\":15,\"source\":\"com.frodare.Test.meth()\",\"thrown\":null,\"fields\":"
            + expectedFields + "}\n";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void parseFields() {
    StringBuilder buf = new StringBuilder();
    String message = "test test TEST[foo] bar[baz]test";
    Deencapsulation.invoke(JSONFormatter.class, "parseFields", buf, message);
    Assert.assertEquals("{\"TEST\":\"foo\",\"bar\":\"baz\"}", buf.toString());
  }

  @Test
  public void parseFieldsNone() {
    StringBuilder buf = new StringBuilder();
    String message = "test test TESTfoo barbaztest";
    Deencapsulation.invoke(JSONFormatter.class, "parseFields", buf, message);
    Assert.assertEquals("null", buf.toString());
  }

  @Test
  public void parseFieldsEmpty() {
    StringBuilder buf = new StringBuilder();
    String message = "";
    Deencapsulation.invoke(JSONFormatter.class, "parseFields", buf, message);
    Assert.assertEquals("null", buf.toString());
  }

  @Test
  public void parseFieldsNull() {
    StringBuilder buf = new StringBuilder();
    String message = null;
    Deencapsulation.invoke(JSONFormatter.class, "parseFields", new Class<?>[]{StringBuilder.class, String.class}, buf, message);
    Assert.assertEquals("null", buf.toString());
  }

  @Test
  public void writeMessageField() {
    StringBuilder buf = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "writeMessageField", buf, "Foo", "Bar");
    Assert.assertEquals("\"Foo\":\"Bar\"", buf.toString());
  }

  @Test
  public void writeStackTraceElement() {
    StackTraceElement e = new StackTraceElement("com.frodare.Foo", "meth", "Test.class", 420);
    writeStackTraceElement(e, "    at com.frodare.Foo.meth(Test.class:420)\n");
  }

  private void writeStackTraceElement(StackTraceElement in, String out) {
    StringBuilder s = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "writeStackTraceElement", new Class<?>[]{StringBuilder.class, StackTraceElement.class}, s, in);
    Assert.assertEquals(out, s.toString());
  }

  @Test
  public void writeSource() {
    LogRecord r = new LogRecord(Level.INFO, "foo");
    r.setSourceClassName("Test.class");
    r.setSourceMethodName("invokeMe");
    writeSource(r, "\"key\":\"Test.class.invokeMe()\"");
  }

  @Test
  public void writeSourceNulls() {
    LogRecord r = new LogRecord(Level.INFO, "foo");
    writeSource(r, "\"key\":null");
  }

  @Test
  public void writeSourceNullMethod() {
    LogRecord r = new LogRecord(Level.INFO, "foo");
    r.setSourceClassName("Test.class");
    writeSource(r, "\"key\":\"Test.class\"");
  }

  private void writeSource(LogRecord in, String out) {
    StringBuilder s = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "writeSource", new Class<?>[]{StringBuilder.class, String.class, LogRecord.class}, s, "key", in);
    Assert.assertEquals(out, s.toString());
  }

  @Test
  public void writeStringEntry() {
    writeStringEntry("test", "\"key\":\"test\"");
    writeStringEntry("foo\tbar", "\"key\":\"foo\\tbar\"");
    writeStringEntry(null, "\"key\":null");
  }

  private void writeStringEntry(String in, String out) {
    StringBuilder s = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "writeStringEntry", new Class<?>[]{StringBuilder.class, String.class, String.class}, s, "key", in);
    Assert.assertEquals(out, s.toString());
  }

  @Test
  public void writeLongEntry() {
    writeLongEntry(1, "\"key\":1");
    writeLongEntry(0, "\"key\":0");
  }

  private void writeLongEntry(long in, String out) {
    StringBuilder s = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "writeLongEntry", s, "key", in);
    Assert.assertEquals(out, s.toString());
  }

  @Test
  public void writeQuotedString() {
    writeQuotedString("test\n", "\"test\\n\"");
    writeQuotedString("foo", "\"foo\"");
    writeQuotedString("", "\"\"");
    writeQuotedString("null", "\"null\"");
    writeQuotedString(null, "null");
  }

  private void writeQuotedString(String in, String out) {
    StringBuilder s = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "writeQuotedString", new Class<?>[]{StringBuilder.class, String.class}, s, in);
    Assert.assertEquals(out, s.toString());
  }

  @Test
  public void jsonEncodeString() {
    jsonEncodeString("test\n", "test\\n");
    jsonEncodeString("foo", "foo");
    jsonEncodeString("", "");
    jsonEncodeString(null, "null");
  }

  private void jsonEncodeString(String in, String out) {
    StringBuilder s = new StringBuilder();
    Deencapsulation.invoke(JSONFormatter.class, "jsonEncodeString", new Class<?>[]{StringBuilder.class, String.class}, s, in);
    Assert.assertEquals(out, s.toString());
  }

  @Test
  public void getReplacementChars() {
    getReplacementChars('\n', "\\n");
    getReplacementChars('\t', "\\t");
    getReplacementChars('s', null);
  }

  private void getReplacementChars(Character in, String out) {
    char[] ca = Deencapsulation.invoke(JSONFormatter.class, "getReplacementChars", new Class<?>[]{Character.class}, in);
    Assert.assertEquals(out, s(ca));
  }

  private String s(char[] c) {
    if (c == null) {
      return null;
    }
    return new String(c);
  }

  @Ignore
  @Test
  public void testActualLog() throws Exception {
    LOG.log(Level.INFO, "TEST MESSAGE", new RuntimeException("Level 1", new IllegalArgumentException("level 2")));
  }

}