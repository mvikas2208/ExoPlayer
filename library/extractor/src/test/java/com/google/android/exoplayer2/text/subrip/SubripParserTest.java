/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.text.subrip;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.testutil.TestUtil;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.CuesWithTiming;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link SubripParser}. */
@RunWith(AndroidJUnit4.class)
public final class SubripParserTest {

  private static final String EMPTY_FILE = "media/subrip/empty";
  private static final String TYPICAL_FILE = "media/subrip/typical";
  private static final String TYPICAL_WITH_BYTE_ORDER_MARK =
      "media/subrip/typical_with_byte_order_mark";
  private static final String TYPICAL_EXTRA_BLANK_LINE = "media/subrip/typical_extra_blank_line";
  private static final String TYPICAL_MISSING_TIMECODE = "media/subrip/typical_missing_timecode";
  private static final String TYPICAL_MISSING_SEQUENCE = "media/subrip/typical_missing_sequence";
  private static final String TYPICAL_NEGATIVE_TIMESTAMPS =
      "media/subrip/typical_negative_timestamps";
  private static final String TYPICAL_UNEXPECTED_END = "media/subrip/typical_unexpected_end";
  private static final String TYPICAL_UTF16BE = "media/subrip/typical_utf16be";
  private static final String TYPICAL_UTF16LE = "media/subrip/typical_utf16le";
  private static final String TYPICAL_WITH_TAGS = "media/subrip/typical_with_tags";
  private static final String TYPICAL_NO_HOURS_AND_MILLIS =
      "media/subrip/typical_no_hours_and_millis";

  @Test
  public void parseEmpty() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes = TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), EMPTY_FILE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).isEmpty();
  }

  @Test
  public void parseTypical() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes = TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), TYPICAL_FILE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(6);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue2(allCues.get(2));
    assertTypicalCue3(allCues.get(4));
  }

  @Test
  public void parseTypicalAtOffsetAndRestrictedLength() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes = TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), TYPICAL_FILE);

    ImmutableList<CuesWithTiming> allCues = parser.parse(bytes, 10, bytes.length - 15);

    assertThat(allCues).hasSize(4);
    // Because of the offset, we skip the first line of dialogue
    assertTypicalCue2(allCues.get(0));
    // Because of the length restriction, we only partially parse the third line of dialogue
    Cue thirdCue = allCues.get(2).cues.get(0);
    assertThat(thirdCue.text.toString()).isEqualTo("This is the third subti");
  }

  @Test
  public void parseTypicalWithByteOrderMark() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(
            ApplicationProvider.getApplicationContext(), TYPICAL_WITH_BYTE_ORDER_MARK);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(6);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue2(allCues.get(2));
    assertTypicalCue3(allCues.get(4));
  }

  @Test
  public void parseTypicalExtraBlankLine() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(
            ApplicationProvider.getApplicationContext(), TYPICAL_EXTRA_BLANK_LINE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(6);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue2(allCues.get(2));
    assertTypicalCue3(allCues.get(4));
  }

  @Test
  public void parseTypicalMissingTimecode() throws IOException {
    // Parsing should succeed, parsing the first and third cues only.
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(
            ApplicationProvider.getApplicationContext(), TYPICAL_MISSING_TIMECODE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(4);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue3(allCues.get(2));
  }

  @Test
  public void parseTypicalMissingSequence() throws IOException {
    // Parsing should succeed, parsing the first and third cues only.
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(
            ApplicationProvider.getApplicationContext(), TYPICAL_MISSING_SEQUENCE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(4);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue3(allCues.get(2));
  }

  @Test
  public void parseTypicalNegativeTimestamps() throws IOException {
    // Parsing should succeed, parsing the third cue only.
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(
            ApplicationProvider.getApplicationContext(), TYPICAL_NEGATIVE_TIMESTAMPS);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(2);
    assertTypicalCue3(allCues.get(0));
  }

  @Test
  public void parseTypicalUnexpectedEnd() throws IOException {
    // Parsing should succeed, parsing the first and second cues only.
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), TYPICAL_UNEXPECTED_END);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(4);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue2(allCues.get(2));
  }

  @Test
  public void parseTypicalUtf16LittleEndian() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), TYPICAL_UTF16LE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(6);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue2(allCues.get(2));
    assertTypicalCue3(allCues.get(4));
  }

  @Test
  public void parseTypicalUtf16BigEndian() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), TYPICAL_UTF16BE);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(6);
    assertTypicalCue1(allCues.get(0));
    assertTypicalCue2(allCues.get(2));
    assertTypicalCue3(allCues.get(4));
  }

  @Test
  public void parseCueWithTag() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(ApplicationProvider.getApplicationContext(), TYPICAL_WITH_TAGS);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).isNotNull();
    assertThat(allCues.get(0).cues.get(0).text.toString()).isEqualTo("This is the first subtitle.");
    assertThat(allCues.get(2).cues.get(0).text.toString())
        .isEqualTo("This is the second subtitle.\nSecond subtitle with second line.");
    assertThat(allCues.get(4).cues.get(0).text.toString()).isEqualTo("This is the third subtitle.");
    assertThat(allCues.get(6).cues.get(0).text.toString())
        .isEqualTo("This { \\an2} is not a valid tag due to the space after the opening bracket.");
    assertThat(allCues.get(8).cues.get(0).text.toString())
        .isEqualTo("This is the fifth subtitle with multiple valid tags.");
    assertAlignmentCue(allCues.get(10), Cue.ANCHOR_TYPE_END, Cue.ANCHOR_TYPE_START); // {/an1}
    assertAlignmentCue(allCues.get(12), Cue.ANCHOR_TYPE_END, Cue.ANCHOR_TYPE_MIDDLE); // {/an2}
    assertAlignmentCue(allCues.get(14), Cue.ANCHOR_TYPE_END, Cue.ANCHOR_TYPE_END); // {/an3}
    assertAlignmentCue(allCues.get(16), Cue.ANCHOR_TYPE_MIDDLE, Cue.ANCHOR_TYPE_START); // {/an4}
    assertAlignmentCue(allCues.get(18), Cue.ANCHOR_TYPE_MIDDLE, Cue.ANCHOR_TYPE_MIDDLE); // {/an5}
    assertAlignmentCue(allCues.get(20), Cue.ANCHOR_TYPE_MIDDLE, Cue.ANCHOR_TYPE_END); // {/an6}
    assertAlignmentCue(allCues.get(22), Cue.ANCHOR_TYPE_START, Cue.ANCHOR_TYPE_START); // {/an7}
    assertAlignmentCue(allCues.get(24), Cue.ANCHOR_TYPE_START, Cue.ANCHOR_TYPE_MIDDLE); // {/an8}
    assertAlignmentCue(allCues.get(26), Cue.ANCHOR_TYPE_START, Cue.ANCHOR_TYPE_END); // {/an9}
  }

  @Test
  public void parseTypicalNoHoursAndMillis() throws IOException {
    SubripParser parser = new SubripParser();
    byte[] bytes =
        TestUtil.getByteArray(
            ApplicationProvider.getApplicationContext(), TYPICAL_NO_HOURS_AND_MILLIS);

    List<CuesWithTiming> allCues = parser.parse(bytes);

    assertThat(allCues).hasSize(6);
    assertTypicalCue1(allCues.get(0));
    assertThat(allCues.get(2).startTimeUs).isEqualTo(2_000_000);
    assertThat(allCues.get(3).startTimeUs).isEqualTo(3_000_000);
    assertTypicalCue3(allCues.get(4));
  }

  private static void assertTypicalCue1(CuesWithTiming cuesWithTiming) {
    assertThat(cuesWithTiming.startTimeUs).isEqualTo(0);
    assertThat(cuesWithTiming.cues.get(0).text.toString()).isEqualTo("This is the first subtitle.");
    assertThat(cuesWithTiming.durationUs).isEqualTo(1234000);
  }

  private static void assertTypicalCue2(CuesWithTiming cuesWithTiming) {
    assertThat(cuesWithTiming.startTimeUs).isEqualTo(2345000);
    assertThat(cuesWithTiming.cues.get(0).text.toString())
        .isEqualTo("This is the second subtitle.\nSecond subtitle with second line.");
    assertThat(cuesWithTiming.durationUs).isEqualTo(3456000 - 2345000);
  }

  private static void assertTypicalCue3(CuesWithTiming cuesWithTiming) {
    long expectedStartTimeUs = (((2L * 60L * 60L) + 4L) * 1000L + 567L) * 1000L;
    assertThat(cuesWithTiming.startTimeUs).isEqualTo(expectedStartTimeUs);
    assertThat(cuesWithTiming.cues.get(0).text.toString()).isEqualTo("This is the third subtitle.");
    long expectedEndTimeUs = (((2L * 60L * 60L) + 8L) * 1000L + 901L) * 1000L;
    assertThat(cuesWithTiming.durationUs).isEqualTo(expectedEndTimeUs - expectedStartTimeUs);
  }

  private static void assertAlignmentCue(
      CuesWithTiming cuesWithTiming,
      @Cue.AnchorType int lineAnchor,
      @Cue.AnchorType int positionAnchor) {
    Cue cue = cuesWithTiming.cues.get(0);
    assertThat(cue.lineType).isEqualTo(Cue.LINE_TYPE_FRACTION);
    assertThat(cue.lineAnchor).isEqualTo(lineAnchor);
    assertThat(cue.line).isEqualTo(SubripParser.getFractionalPositionForAnchorType(lineAnchor));
    assertThat(cue.positionAnchor).isEqualTo(positionAnchor);
    assertThat(cue.position)
        .isEqualTo(SubripParser.getFractionalPositionForAnchorType(positionAnchor));
  }
}
