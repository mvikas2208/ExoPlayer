/*
 * Copyright 2022 The Android Open Source Project
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
package com.google.android.exoplayer2.effect;

import android.graphics.Gainmap;
import com.google.android.exoplayer2.util.GlUtil.GlException;

/**
 * Interface for a {@link GlShaderProgram} that samples from a gainmap.
 *
 * @deprecated com.google.android.exoplayer2 is deprecated. Please migrate to androidx.media3 (which
 *     contains the same ExoPlayer code). See <a
 *     href="https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide">the
 *     migration guide</a> for more details, including a script to help with the migration.
 */
@Deprecated
/* package */ interface GainmapShaderProgram extends GlShaderProgram {

  /**
   * Sets the {@link Gainmap} that is applied to the output frame.
   *
   * @param gainmap The {@link Gainmap}.
   */
  void setGainmap(Gainmap gainmap) throws GlException;
}