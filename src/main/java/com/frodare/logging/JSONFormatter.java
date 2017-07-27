/*
 * Copyright (C) 2017 Charles Howard
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Some of this code was obtained from minimal-json (Ralf Sternberg):
 * https://github.com/ralfstx/minimal-json/blob/master/com.eclipsesource.json/src/main/java/com/eclipsesource/json/JsonWriter.java
 *
 * JSON formatter was done manually in this class to avoid requiring dependencies.
 */
package com.frodare.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONFormatter extends Formatter {

  private static final Pattern p = Pattern.compile("([a-z0-9]+)\\[([^]]+)]", Pattern.CASE_INSENSITIVE);

  private static final int CONTROL_CHARACTERS_END = 0x001f;

  private static final char[] QUOT_CHARS = {'\\', '"'};
  private static final char[] BS_CHARS = {'\\', '\\'};
  private static final char[] LF_CHARS = {'\\', 'n'};
  private static final char[] CR_CHARS = {'\\', 'r'};
  private static final char[] TAB_CHARS = {'\\', 't'};

  /*
   * In JavaScript, U+2028 and U+2029 characters count as line endings and must be encoded.
   * http://stackoverflow.com/questions/2965293/javascript-parse-error-on-u2028-unicode-character
   */
  private static final char[] UNICODE_2028_CHARS = {'\\', 'u', '2', '0', '2', '8'};
  private static final char[] UNICODE_2029_CHARS = {'\\', 'u', '2', '0', '2', '9'};

  private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'a', 'b', 'c', 'd', 'e', 'f'};

  private static final String NEW_LINE = System.getProperty("line.separator");

  private Boolean parseMessageFields = null;

  @Override
  public String format(LogRecord record) {
    determineParseMessageFields();
    StringBuilder buf = new StringBuilder();
    buf.append("{");
    writeStringEntry(buf, "level", s(record.getLevel()));
    buf.append(",");
    writeStringEntry(buf, "message", record.getMessage());
    buf.append(",");
    writeLongEntry(buf, "millis", record.getMillis());
    buf.append(",");
    writeLongEntry(buf, "seqNum", record.getSequenceNumber());
    buf.append(",");
    writeSource(buf, "source", record);
    buf.append(",");
    writeThrown(buf, "thrown", record);
    if (parseMessageFields) {
      buf.append(",");
      writeMessageFields(buf, "fields", record.getMessage());
    }
    buf.append("}");
    buf.append(NEW_LINE);
    return buf.toString();
  }

  private static void writeMessageFields(StringBuilder buf, String key, String message) {
    writeQuotedString(buf, key);
    buf.append(":");
    parseFields(buf, message);
  }

  private static void parseFields(StringBuilder buf, String message) {
    if (message == null) {
      buf.append("null");
      return;
    }
    Matcher m = p.matcher(message);
    boolean first = true;
    boolean found = false;
    buf.append('{');
    while (m.find()) {
      if (m.groupCount() == 0) {
        continue;
      }
      if (m.groupCount() == 2) {
        found = true;
        if (first) {
          first = false;
        } else {
          buf.append(",");
        }
        writeMessageField(buf, m.group(1), m.group(2));
      }
    }
    if (!found) {
      buf.delete(buf.length() - 1, buf.length());
      buf.append("null");
    } else {
      buf.append('}');
    }
  }

  private static void writeMessageField(StringBuilder buf, String key, String value) {
    buf.append("\"").append(key).append("\":\"").append(value).append("\"");
  }

  private void determineParseMessageFields() {
    if (parseMessageFields == null) {
      parseMessageFields = bool(LogManager.getLogManager().getProperty("com.frodare.logging.JSONFormatter.parseMessageFields"));
    }
  }

  private void writeThrown(StringBuilder buf, String key, LogRecord record) {
    writeQuotedString(buf, key);
    buf.append(":");
    if (record.getThrown() == null) {
      buf.append("null");
      return;
    }
    buf.append('"');
    jsonEncodeString(buf, getStackTrace(record.getThrown()));
    buf.append('"');
  }

  private String getStackTrace(Throwable t) {
    if (t != null) {
      StringBuilder buf = new StringBuilder();
      writeStackTrace(buf, t);
      return buf.toString();
    }
    return "null";
  }

  private void writeStackTrace(StringBuilder buf, Throwable t) {
    if (t == null) {
      return;
    }
    buf.append("Exception in ");
    buf.append(t.getClass());
    buf.append(": ");
    buf.append(t.getMessage()).append(NEW_LINE);
    for (StackTraceElement e : t.getStackTrace()) {
      writeStackTraceElement(buf, e);
    }
    writeStackTrace(buf, t.getCause());
  }

  private static void writeStackTraceElement(StringBuilder buf, StackTraceElement e) {
    buf.append("    at ");
    buf.append(e.getClassName()).append(".").append(e.getMethodName());
    buf.append("(").append(e.getFileName()).append(":").append(e.getLineNumber()).append(")");
    buf.append(NEW_LINE);
  }

  private static String s(Level level) {
    if (level == null) {
      return "null";
    }
    return level.toString();
  }

  private static void writeSource(StringBuilder buf, String key, LogRecord record) {
    String clazz = record.getSourceClassName();
    String method = record.getSourceMethodName();

    writeQuotedString(buf, key);
    buf.append(":");

    if (clazz == null) {
      buf.append("null");
      return;
    }

    buf.append('"');
    jsonEncodeString(buf, record.getSourceClassName());

    if (method == null) {
      buf.append('"');
      return;
    }

    buf.append(".");
    jsonEncodeString(buf, record.getSourceMethodName());
    buf.append("()");
    buf.append('"');
  }

  private static void writeStringEntry(StringBuilder buf, String key, String value) {
    writeQuotedString(buf, key);
    buf.append(":");
    writeQuotedString(buf, value);
  }

  private static void writeLongEntry(StringBuilder buf, String key, long value) {
    writeQuotedString(buf, key);
    buf.append(":");
    buf.append(value);
  }

  private static void writeQuotedString(StringBuilder buf, String s) {
    if (s == null) {
      jsonEncodeString(buf, s);
      return;
    }
    buf.append('"');
    jsonEncodeString(buf, s);
    buf.append('"');
  }

  private static void jsonEncodeString(StringBuilder buf, String s) {
    if (s == null) {
      buf.append("null");
      return;
    }
    char[] chars = s.toCharArray();
    char[] replacement;
    for (char c : chars) {
      replacement = getReplacementChars(c);
      if (replacement == null) {
        buf.append(c);
      } else {
        buf.append(replacement);
      }
    }
  }

  private static char[] getReplacementChars(char ch) {
    if (ch > '\\') {
      if (ch < '\u2028' || ch > '\u2029') {
        // The lower range contains 'a' .. 'z'. Only 2 checks required.
        return null;
      }
      return ch == '\u2028' ? UNICODE_2028_CHARS : UNICODE_2029_CHARS;
    }
    if (ch == '\\') {
      return BS_CHARS;
    }
    if (ch > '"') {
      // This range contains '0' .. '9' and 'A' .. 'Z'. Need 3 checks to get here.
      return null;
    }
    if (ch == '"') {
      return QUOT_CHARS;
    }
    if (ch > CONTROL_CHARACTERS_END) {
      return null;
    }
    if (ch == '\n') {
      return LF_CHARS;
    }
    if (ch == '\r') {
      return CR_CHARS;
    }
    if (ch == '\t') {
      return TAB_CHARS;
    }
    return new char[]{'\\', 'u', '0', '0', HEX_DIGITS[ch >> 4 & 0x000f], HEX_DIGITS[ch & 0x000f]};
  }

  private static boolean bool(String s) {
    if (s == null) {
      return false;
    }
    return s.trim().equalsIgnoreCase("true");
  }

}
