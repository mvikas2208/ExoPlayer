/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.google.android.exoplayer2.source.dash.e2etest;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.metadata.MetadataDecoderFactory;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.robolectric.PlaybackOutput;
import com.google.android.exoplayer2.robolectric.ShadowMediaCodecConfig;
import com.google.android.exoplayer2.robolectric.TestPlayerRunHelper;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.testutil.CapturingRenderersFactory;
import com.google.android.exoplayer2.testutil.DumpFileAsserts;
import com.google.android.exoplayer2.testutil.FakeClock;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** End-to-end tests using DASH samples. */
@RunWith(AndroidJUnit4.class)
public final class DashPlaybackTest {

  @Rule
  public ShadowMediaCodecConfig mediaCodecConfig =
      ShadowMediaCodecConfig.forAllSupportedMimeTypes();

  @Test
  public void webvttStandaloneFile() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .setMediaSourceFactory(
                new DashMediaSource.Factory(new DefaultDataSource.Factory(applicationContext))
                    .experimentalParseSubtitlesDuringExtraction(true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    // Ensure the subtitle track is selected.
    DefaultTrackSelector trackSelector =
        checkNotNull((DefaultTrackSelector) player.getTrackSelector());
    trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage("en"));
    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/standalone-webvtt/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/standalone-webvtt.dump");
  }

  @Test
  public void ttmlStandaloneXmlFile() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .setMediaSourceFactory(
                new DashMediaSource.Factory(new DefaultDataSource.Factory(applicationContext))
                    .experimentalParseSubtitlesDuringExtraction(true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    // Ensure the subtitle track is selected.
    DefaultTrackSelector trackSelector =
        checkNotNull((DefaultTrackSelector) player.getTrackSelector());
    trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage("en"));
    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/standalone-ttml/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/standalone-ttml.dump");
  }

  // https://github.com/google/ExoPlayer/issues/7985
  @Test
  public void webvttInMp4() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setMediaSourceFactory(
                new DashMediaSource.Factory(new DefaultDataSource.Factory(applicationContext))
                    .experimentalParseSubtitlesDuringExtraction(true))
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    // Ensure the subtitle track is selected.
    DefaultTrackSelector trackSelector =
        checkNotNull((DefaultTrackSelector) player.getTrackSelector());
    trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage("en"));
    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/webvtt-in-mp4/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/webvtt-in-mp4.dump");
  }

  @Test
  public void ttmlInMp4() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setMediaSourceFactory(
                new DashMediaSource.Factory(new DefaultDataSource.Factory(applicationContext))
                    .experimentalParseSubtitlesDuringExtraction(true))
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    // Ensure the subtitle track is selected.
    DefaultTrackSelector trackSelector =
        checkNotNull((DefaultTrackSelector) player.getTrackSelector());
    trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage("en"));
    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/ttml-in-mp4/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/ttml-in-mp4.dump");
  }

  /**
   * This test and {@link #cea608_parseDuringExtraction()} use the same output dump file, to
   * demonstrate the flag has no effect on the resulting subtitles.
   */
  @Test
  public void cea608_parseDuringRendering() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setMediaSourceFactory(
                new DashMediaSource.Factory(new DefaultDataSource.Factory(applicationContext))
                    .experimentalParseSubtitlesDuringExtraction(false))
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    // Ensure the subtitle track is selected.
    DefaultTrackSelector trackSelector =
        checkNotNull((DefaultTrackSelector) player.getTrackSelector());
    trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage("en"));
    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/cea608/manifest.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/cea608.dump");
  }

  /**
   * This test and {@link #cea608_parseDuringRendering()} use the same output dump file, to
   * demonstrate the flag has no effect on the resulting subtitles.
   */
  @Test
  public void cea608_parseDuringExtraction() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setMediaSourceFactory(
                new DashMediaSource.Factory(new DefaultDataSource.Factory(applicationContext))
                    .experimentalParseSubtitlesDuringExtraction(true))
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    // Ensure the subtitle track is selected.
    DefaultTrackSelector trackSelector =
        checkNotNull((DefaultTrackSelector) player.getTrackSelector());
    trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredTextLanguage("en"));
    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/cea608/manifest.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/cea608.dump");
  }

  // https://github.com/google/ExoPlayer/issues/8710
  @Test
  public void emsgNearToPeriodBoundary() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/emsg/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/emsg.dump");
  }

  @Test
  public void renderMetadata_withTimelyOutput() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    RenderersFactory renderersFactory =
        (eventHandler,
            videoRendererEventListener,
            audioRendererEventListener,
            textRendererOutput,
            metadataRendererOutput) ->
            new Renderer[] {new MetadataRenderer(metadataRendererOutput, eventHandler.getLooper())};
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, renderersFactory)
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/emsg/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.playUntilPosition(player, /* mediaItemIndex= */ 0, /* positionMs= */ 500);
    player.release();

    // Ensure output contains metadata up to the playback position.
    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/metadata_from_timely_output.dump");
  }

  @Test
  public void renderMetadata_withEarlyOutput() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    RenderersFactory renderersFactory =
        (eventHandler,
            videoRendererEventListener,
            audioRendererEventListener,
            textRendererOutput,
            metadataRendererOutput) ->
            new Renderer[] {
              new MetadataRenderer(
                  metadataRendererOutput,
                  eventHandler.getLooper(),
                  MetadataDecoderFactory.DEFAULT,
                  /* outputMetadataEarly= */ true)
            };
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, renderersFactory)
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    player.setVideoSurface(new Surface(new SurfaceTexture(/* texName= */ 1)));
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/emsg/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.playUntilPosition(player, /* mediaItemIndex= */ 0, /* positionMs= */ 500);
    player.release();

    // Ensure output contains all metadata irrespective of the playback position.
    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/metadata_from_early_output.dump");
  }

  /**
   * This test might be flaky. The {@link ExoPlayer} instantiated in this test uses a {@link
   * FakeClock} that runs much faster than real time. This might cause the {@link ExoPlayer} to skip
   * and not present some images. That will cause the test to fail.
   */
  @Test
  public void playThumbnailGrid() throws Exception {
    Context applicationContext = ApplicationProvider.getApplicationContext();
    CapturingRenderersFactory capturingRenderersFactory =
        new CapturingRenderersFactory(applicationContext);
    ExoPlayer player =
        new ExoPlayer.Builder(applicationContext, capturingRenderersFactory)
            .setClock(new FakeClock(/* isAutoAdvancing= */ true))
            .build();
    PlaybackOutput playbackOutput = PlaybackOutput.register(player, capturingRenderersFactory);

    player.setMediaItem(MediaItem.fromUri("asset:///media/dash/thumbnails/sample.mpd"));
    player.prepare();
    player.play();
    TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED);
    player.release();

    DumpFileAsserts.assertOutput(
        applicationContext, playbackOutput, "playbackdumps/dash/loadimage.dump");
  }
}
