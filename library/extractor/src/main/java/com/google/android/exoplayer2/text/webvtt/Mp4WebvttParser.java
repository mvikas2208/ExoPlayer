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
package com.google.android.exoplayer2.text.webvtt;

import static com.google.android.exoplayer2.util.Assertions.checkArgument;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.CuesWithTiming;
import com.google.android.exoplayer2.text.SubtitleParser;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.Util;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link SubtitleParser} for Webvtt embedded in a Mp4 container file.
 *
 * @deprecated com.google.android.exoplayer2 is deprecated. Please migrate to androidx.media3 (which
 *     contains the same ExoPlayer code). See <a
 *     href="https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide">the
 *     migration guide</a> for more details, including a script to help with the migration.
 */
@SuppressWarnings("ConstantField")
@Deprecated
public final class Mp4WebvttParser implements SubtitleParser {

  private static final int BOX_HEADER_SIZE = 8;

  @SuppressWarnings("ConstantCaseForConstants")
  private static final int TYPE_payl = 0x7061796c;

  @SuppressWarnings("ConstantCaseForConstants")
  private static final int TYPE_sttg = 0x73747467;

  @SuppressWarnings("ConstantCaseForConstants")
  private static final int TYPE_vttc = 0x76747463;

  private final ParsableByteArray parsableByteArray;

  public Mp4WebvttParser() {
    parsableByteArray = new ParsableByteArray();
  }

  @Override
  public ImmutableList<CuesWithTiming> parse(byte[] data, int offset, int length) {
    parsableByteArray.reset(data, /* limit= */ offset + length);
    parsableByteArray.setPosition(offset);
    List<Cue> cues = new ArrayList<>();
    while (parsableByteArray.bytesLeft() > 0) {
      // Webvtt in Mp4 samples have boxes inside of them, so we have to do a traditional box
      // parsing: first 4 bytes size and then 4 bytes type.
      checkArgument(
          parsableByteArray.bytesLeft() >= BOX_HEADER_SIZE,
          "Incomplete Mp4Webvtt Top Level box header found.");
      int boxSize = parsableByteArray.readInt();
      int boxType = parsableByteArray.readInt();
      if (boxType == TYPE_vttc) {
        cues.add(parseVttCueBox(parsableByteArray, boxSize - BOX_HEADER_SIZE));
      } else {
        // Peers of the VTTCueBox are still not supported and are skipped.
        parsableByteArray.skipBytes(boxSize - BOX_HEADER_SIZE);
      }
    }
    return ImmutableList.of(
        new CuesWithTiming(cues, /* startTimeUs= */ C.TIME_UNSET, /* durationUs= */ C.TIME_UNSET));
  }

  @Override
  public void reset() {}

  private static Cue parseVttCueBox(ParsableByteArray sampleData, int remainingCueBoxBytes) {
    @Nullable Cue.Builder cueBuilder = null;
    @Nullable CharSequence cueText = null;
    while (remainingCueBoxBytes > 0) {
      checkArgument(
          remainingCueBoxBytes >= BOX_HEADER_SIZE, "Incomplete vtt cue box header found.");
      int boxSize = sampleData.readInt();
      int boxType = sampleData.readInt();
      remainingCueBoxBytes -= BOX_HEADER_SIZE;
      int payloadLength = boxSize - BOX_HEADER_SIZE;
      String boxPayload =
          Util.fromUtf8Bytes(sampleData.getData(), sampleData.getPosition(), payloadLength);
      sampleData.skipBytes(payloadLength);
      remainingCueBoxBytes -= payloadLength;
      if (boxType == TYPE_sttg) {
        cueBuilder = WebvttCueParser.parseCueSettingsList(boxPayload);
      } else if (boxType == TYPE_payl) {
        cueText =
            WebvttCueParser.parseCueText(
                /* id= */ null, boxPayload.trim(), /* styles= */ Collections.emptyList());
      } else {
        // Other VTTCueBox children are still not supported and are ignored.
      }
    }
    if (cueText == null) {
      cueText = "";
    }
    return cueBuilder != null
        ? cueBuilder.setText(cueText).build()
        : WebvttCueParser.newCueForText(cueText);
  }
}
