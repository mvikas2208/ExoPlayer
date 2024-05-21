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
package com.google.android.exoplayer2;

import android.os.SystemClock;
import android.text.TextUtils;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeoutException;

/**
 * Thrown when a non-recoverable playback failure occurs.
 */
public final class ExoPlaybackException extends Exception {

  /**
   * The type of source that produced the error. One of {@link #TYPE_SOURCE}, {@link #TYPE_RENDERER}
   * or {@link #TYPE_UNEXPECTED}.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({TYPE_SOURCE, TYPE_RENDERER, TYPE_UNEXPECTED, TYPE_TIMEOUT})
  public @interface Type {}
  /**
   * The error occurred loading data from a {@link MediaSource}.
   * <p>
   * Call {@link #getSourceException()} to retrieve the underlying cause.
   */
  public static final int TYPE_SOURCE = 0;
  /**
   * The error occurred in a {@link Renderer}.
   * <p>
   * Call {@link #getRendererException()} to retrieve the underlying cause.
   */
  public static final int TYPE_RENDERER = 1;
  /**
   * The error was an unexpected {@link RuntimeException}.
   * <p>
   * Call {@link #getUnexpectedException()} to retrieve the underlying cause.
   */
  public static final int TYPE_UNEXPECTED = 2;

  /** The error was a {@link TimeoutException}. */
  public static final int TYPE_TIMEOUT = 3;

  /** The operation where this error occurred is not defined. */
  public static final int TIMEOUT_OPERATION_UNDEFINED = 0;
  /** The error occurred in {@link ExoPlayer#release}. */
  public static final int TIMEOUT_OPERATION_RELEASE = 1;
  public static final int TIMEOUT_OPERATION_SET_FOREGROUND_MODE = 2;
  /** The error occurred while detaching a surface from the player. */
  public static final int TIMEOUT_OPERATION_DETACH_SURFACE = 3;

  /** If {@link #type} is {@link #TYPE_RENDERER}, this is the name of the renderer. */
  @Nullable public final String rendererName;

  /** If {@link #type} is {@link #TYPE_RENDERER}, this is the index of the renderer. */
  public final int rendererIndex;

  /**
   * If {@link #type} is {@link #TYPE_RENDERER}, this is the {@link Format} the renderer was using
   * at the time of the exception, or null if the renderer wasn't using a {@link Format}.
   */
  @Nullable public final Format rendererFormat;

  /**
   * If {@link #type} is {@link #TYPE_RENDERER}, this is the level of {@link RendererCapabilities.FormatSupport} of the
   * renderer for {@link #rendererFormat}. If {@link #rendererFormat} is null, this is {@link
   * RendererCapabilities#FORMAT_HANDLED}.
   */
  @RendererCapabilities.FormatSupport
  public final int rendererFormatSupport;

  /**
   * If {@link #type} is {@link #TYPE_TIMEOUT}, this is the operation where the timeout happened.
   */
  @TimeoutOperation public final int timeoutOperation;

  /** The value of {@link SystemClock#elapsedRealtime()} when this exception was created. */
  public final long timestampMs;

  /**
   * The {@link MediaSource.MediaPeriodId} of the media associated with this error, or null if
   * undetermined.
   */
  @Nullable public final MediaSource.MediaPeriodId mediaPeriodId;

  /**
   * Whether the error may be recoverable.
   *
   * <p>This is only used internally by ExoPlayer to try to recover from some errors and should not
   * be used by apps.
   *
   * <p>If the {@link #type} is {@link #TYPE_RENDERER}, it may be possible to recover from the error
   * by disabling and re-enabling the renderers.
   */
  /* package */ final boolean isRecoverable;

  @Nullable private final Throwable cause;

  /** The {@link Type} of the playback failure. */
  @Type public final int type;


  /**
   * Creates an instance of type {@link #TYPE_SOURCE}.
   *
   * @param cause The cause of the failure.
   * @return The created instance.
   */
  public static ExoPlaybackException createForSource(IOException cause) {
    return new ExoPlaybackException(TYPE_SOURCE, cause, C.INDEX_UNSET);
  }

  /**
   * The operation which produced the timeout error. One of {@link #TIMEOUT_OPERATION_RELEASE},
   * {@link #TIMEOUT_OPERATION_DETACH_SURFACE} or
   * {@link #TIMEOUT_OPERATION_UNDEFINED}. Note that new operations may be added in the future and
   * error handling should handle unknown operation values.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
      TIMEOUT_OPERATION_UNDEFINED,
      TIMEOUT_OPERATION_RELEASE,
      TIMEOUT_OPERATION_DETACH_SURFACE
  })
  public @interface TimeoutOperation {}

  /**
   * Creates an instance of type {@link #TYPE_RENDERER}.
   *
   * @param cause The cause of the failure.
   * @param rendererIndex The index of the renderer in which the failure occurred.
   * @return The created instance.
   */
  public static ExoPlaybackException createForRenderer(Exception cause, int rendererIndex) {
    return new ExoPlaybackException(TYPE_RENDERER, cause, rendererIndex);
  }

  /**
   * Creates an instance of type {@link #TYPE_UNEXPECTED}.
   *
   * @param cause The cause of the failure.
   * @return The created instance.
   */
  /* package */ static ExoPlaybackException createForUnexpected(RuntimeException cause) {
    return new ExoPlaybackException(TYPE_UNEXPECTED, cause, C.INDEX_UNSET);
  }

  private ExoPlaybackException(@Type int type, Throwable cause, int rendererIndex) {
    this(
        type,
        cause,
        /* customMessage= */ null,
        /* rendererName= */ null,
        rendererIndex,
        /* rendererFormat= */ null,
        /* rendererFormatSupport= */ RendererCapabilities.FORMAT_HANDLED,
        /* timeoutOperation= */ TIMEOUT_OPERATION_UNDEFINED,
        /* isRecoverable= */ false);
  }

  /**
   * Retrieves the underlying error when {@link #type} is {@link #TYPE_SOURCE}.
   *
   * @throws IllegalStateException If {@link #type} is not {@link #TYPE_SOURCE}.
   */
  public IOException getSourceException() {
    Assertions.checkState(type == TYPE_SOURCE);
    return (IOException) cause;
  }

  /**
   * Retrieves the underlying error when {@link #type} is {@link #TYPE_RENDERER}.
   *
   * @throws IllegalStateException If {@link #type} is not {@link #TYPE_RENDERER}.
   */
  public Exception getRendererException() {
    Assertions.checkState(type == TYPE_RENDERER);
    return (Exception) cause;
  }

  /**
   * Retrieves the underlying error when {@link #type} is {@link #TYPE_UNEXPECTED}.
   *
   * @throws IllegalStateException If {@link #type} is not {@link #TYPE_UNEXPECTED}.
   */
  public RuntimeException getUnexpectedException() {
    Assertions.checkState(type == TYPE_UNEXPECTED);
    return (RuntimeException) cause;
  }

  /**
   * Creates an instance of type {@link #TYPE_TIMEOUT}.
   *
   * @param cause The cause of the failure.
   * @param timeoutOperation The operation that caused this timeout.
   * @return The created instance.
   */
  public static ExoPlaybackException createForTimeout(
      TimeoutException cause, @TimeoutOperation int timeoutOperation) {
    return new ExoPlaybackException(
        TYPE_TIMEOUT,
        cause,
        /* customMessage= */ null,
        /* rendererName= */ null,
        /* rendererIndex= */ C.INDEX_UNSET,
        /* rendererFormat= */ null,
        /* rendererFormatSupport= */ RendererCapabilities.FORMAT_HANDLED,
        timeoutOperation,
        /* isRecoverable= */ false);
  }

  private ExoPlaybackException(
      @Nullable String message,
      @Nullable Throwable cause,
      @Type int type,
      @Nullable String rendererName,
      int rendererIndex,
      @Nullable Format rendererFormat,
      @RendererCapabilities.FormatSupport int rendererFormatSupport,
      @Nullable MediaSource.MediaPeriodId mediaPeriodId,
      @TimeoutOperation int timeoutOperation,
      long timestampMs,
      boolean isRecoverable) {
    super(message, cause);
    this.type = type;
    this.cause = cause;
    this.rendererName = rendererName;
    this.rendererIndex = rendererIndex;
    this.rendererFormat = rendererFormat;
    this.rendererFormatSupport = rendererFormatSupport;
    this.mediaPeriodId = mediaPeriodId;
    this.timeoutOperation = timeoutOperation;
    this.timestampMs = timestampMs;
    this.isRecoverable = isRecoverable;
  }

  private ExoPlaybackException(
      @Type int type,
      @Nullable Throwable cause,
      @Nullable String customMessage,
      @Nullable String rendererName,
      int rendererIndex,
      @Nullable Format rendererFormat,
      @RendererCapabilities.FormatSupport int rendererFormatSupport,
      @TimeoutOperation int timeoutOperation,
      boolean isRecoverable) {
    this(
        deriveMessage(
            type,
            customMessage,
            rendererName,
            rendererIndex,
            rendererFormat,
            rendererFormatSupport),
        cause,
        type,
        rendererName,
        rendererIndex,
        rendererFormat,
        rendererFormatSupport,
        /* mediaPeriodId= */ null,
        timeoutOperation,
        /* timestampMs= */ SystemClock.elapsedRealtime(),
        isRecoverable);
  }

  @Nullable
  private static String deriveMessage(
      @Type int type,
      @Nullable String customMessage,
      @Nullable String rendererName,
      int rendererIndex,
      @Nullable Format rendererFormat,
      @RendererCapabilities.FormatSupport int rendererFormatSupport) {
    @Nullable String message;
    switch (type) {
      case TYPE_SOURCE:
        message = "Source error";
        break;
      case TYPE_RENDERER:
        message =
            rendererName
                + " error"
                + ", index="
                + rendererIndex
                + ", format="
                + rendererFormat
                + ", format_supported="
                + RendererCapabilities.getFormatSupportString(rendererFormatSupport);
        break;
      case TYPE_TIMEOUT:
        message = "Timeout error";
        break;
      case TYPE_UNEXPECTED:
      default:
        message = "Unexpected runtime error";
        break;
    }
    if (!TextUtils.isEmpty(customMessage)) {
      message += ": " + customMessage;
    }
    return message;
  }

}
