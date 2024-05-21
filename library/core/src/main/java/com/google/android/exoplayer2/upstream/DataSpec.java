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
package com.google.android.exoplayer2.upstream;

import android.net.Uri;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.util.Assertions;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * Defines a region of data.
 */
public final class DataSpec {

  /**
   * The flags that apply to any request for data. Possible flag values are {@link #FLAG_ALLOW_GZIP}
   * and {@link #FLAG_ALLOW_CACHING_UNKNOWN_LENGTH}.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef(
      flag = true,
      value = {FLAG_ALLOW_GZIP, FLAG_ALLOW_CACHING_UNKNOWN_LENGTH})
  public @interface Flags {}
  /**
   * Permits an underlying network stack to request that the server use gzip compression.
   * <p>
   * Should not typically be set if the data being requested is already compressed (e.g. most audio
   * and video requests). May be set when requesting other data.
   * <p>
   * When a {@link DataSource} is used to request data with this flag set, and if the
   * {@link DataSource} does make a network request, then the value returned from
   * {@link DataSource#open(DataSpec)} will typically be {@link C#LENGTH_UNSET}. The data read from
   * {@link DataSource#read(byte[], int, int)} will be the decompressed data.
   */
  public static final int FLAG_ALLOW_GZIP = 1;

  /**
   * Permits content to be cached even if its length can not be resolved. Typically this's the case
   * for progressive live streams and when {@link #FLAG_ALLOW_GZIP} is used.
   */
  public static final int FLAG_ALLOW_CACHING_UNKNOWN_LENGTH = 1 << 1; // 2

  /**
   * The set of HTTP methods that are supported by ExoPlayer {@link HttpDataSource}s. One of {@link
   * #HTTP_METHOD_GET}, {@link #HTTP_METHOD_POST} or {@link #HTTP_METHOD_HEAD}.
   */
  @Documented
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({HTTP_METHOD_GET, HTTP_METHOD_POST, HTTP_METHOD_HEAD})
  public @interface HttpMethod {}

  public static final int HTTP_METHOD_GET = 1;
  public static final int HTTP_METHOD_POST = 2;
  public static final int HTTP_METHOD_HEAD = 3;

  /**
   * The source from which data should be read.
   */
  public final Uri uri;

  /**
   * The HTTP method, which will be used by {@link HttpDataSource} when requesting this DataSpec.
   * This value will be ignored by non-http {@link DataSource}s.
   */
  public final @HttpMethod int httpMethod;

  /**
   * The HTTP body, null otherwise. If the body is non-null, then httpBody.length will be non-zero.
   */
  public final @Nullable byte[] httpBody;

  /** @deprecated Use {@link #httpBody} instead. */
  @Deprecated public final @Nullable byte[] postBody;

  /**
   * The absolute position of the data in the full stream.
   */
  public final long absoluteStreamPosition;
  /**
   * The position of the data when read from {@link #uri}.
   * <p>
   * Always equal to {@link #absoluteStreamPosition} unless the {@link #uri} defines the location
   * of a subset of the underlying data.
   */
  public final long position;
  /**
   * The length of the data, or {@link C#LENGTH_UNSET}.
   */
  public final long length;
  /**
   * A key that uniquely identifies the original stream. Used for cache indexing. May be null if the
   * {@link DataSpec} is not intended to be used in conjunction with a cache.
   */
  public final @Nullable String key;
  /**
   * Request flags. Currently {@link #FLAG_ALLOW_GZIP} and
   * {@link #FLAG_ALLOW_CACHING_UNKNOWN_LENGTH} are the only supported flags.
   */
  public final @Flags int flags;

  /**
   * Construct a {@link DataSpec} for the given uri and with {@link #key} set to null.
   *
   * @param uri {@link #uri}.
   */
  public DataSpec(Uri uri) {
    this(uri, 0);
  }

  /**
   * Construct a {@link DataSpec} for the given uri and with {@link #key} set to null.
   *
   * @param uri {@link #uri}.
   * @param flags {@link #flags}.
   */
  public DataSpec(Uri uri, @Flags int flags) {
    this(uri, 0, C.LENGTH_UNSET, null, flags);
  }

  /**
   * Construct a {@link DataSpec} where {@link #position} equals {@link #absoluteStreamPosition}.
   *
   * @param uri {@link #uri}.
   * @param absoluteStreamPosition {@link #absoluteStreamPosition}, equal to {@link #position}.
   * @param length {@link #length}.
   * @param key {@link #key}.
   */
  public DataSpec(Uri uri, long absoluteStreamPosition, long length, @Nullable String key) {
    this(uri, absoluteStreamPosition, absoluteStreamPosition, length, key, 0);
  }

  /**
   * Construct a {@link DataSpec} where {@link #position} equals {@link #absoluteStreamPosition}.
   *
   * @param uri {@link #uri}.
   * @param absoluteStreamPosition {@link #absoluteStreamPosition}, equal to {@link #position}.
   * @param length {@link #length}.
   * @param key {@link #key}.
   * @param flags {@link #flags}.
   */
  public DataSpec(
      Uri uri, long absoluteStreamPosition, long length, @Nullable String key, @Flags int flags) {
    this(uri, absoluteStreamPosition, absoluteStreamPosition, length, key, flags);
  }

  /**
   * Construct a {@link DataSpec} where {@link #position} may differ from
   * {@link #absoluteStreamPosition}.
   *
   * @param uri {@link #uri}.
   * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
   * @param position {@link #position}.
   * @param length {@link #length}.
   * @param key {@link #key}.
   * @param flags {@link #flags}.
   */
  public DataSpec(
      Uri uri,
      long absoluteStreamPosition,
      long position,
      long length,
      @Nullable String key,
      @Flags int flags) {
    this(uri, null, absoluteStreamPosition, position, length, key, flags);
  }

  /**
   * Construct a {@link DataSpec} by inferring the {@link #httpMethod} based on the {@code postBody}
   * parameter. If postBody is non-null, then httpMethod is set to {@link #HTTP_METHOD_POST}. If
   * postBody is null, then httpMethod is set to {@link #HTTP_METHOD_GET}.
   *
   * @param uri {@link #uri}.
   * @param postBody {@link #httpBody} The body of the HTTP request, which is also used to infer the
   *     {@link #httpMethod}.
   * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
   * @param position {@link #position}.
   * @param length {@link #length}.
   * @param key {@link #key}.
   * @param flags {@link #flags}.
   */
  public DataSpec(
      Uri uri,
      @Nullable byte[] postBody,
      long absoluteStreamPosition,
      long position,
      long length,
      @Nullable String key,
      @Flags int flags) {
    this(
        uri,
        /* httpMethod= */ postBody != null ? HTTP_METHOD_POST : HTTP_METHOD_GET,
        /* httpBody= */ postBody,
        absoluteStreamPosition,
        position,
        length,
        key,
        flags);
  }

  /**
   * Construct a {@link DataSpec} where {@link #position} may differ from {@link
   * #absoluteStreamPosition}.
   *
   * @param uri {@link #uri}.
   * @param httpMethod {@link #httpMethod}.
   * @param httpBody {@link #httpBody}.
   * @param absoluteStreamPosition {@link #absoluteStreamPosition}.
   * @param position {@link #position}.
   * @param length {@link #length}.
   * @param key {@link #key}.
   * @param flags {@link #flags}.
   */
  @SuppressWarnings("deprecation")
  public DataSpec(
      Uri uri,
      @HttpMethod int httpMethod,
      @Nullable byte[] httpBody,
      long absoluteStreamPosition,
      long position,
      long length,
      @Nullable String key,
      @Flags int flags) {
    Assertions.checkArgument(absoluteStreamPosition >= 0);
    Assertions.checkArgument(position >= 0);
    Assertions.checkArgument(length > 0 || length == C.LENGTH_UNSET);
    this.uri = uri;
    this.httpMethod = httpMethod;
    this.httpBody = (httpBody != null && httpBody.length != 0) ? httpBody : null;
    this.postBody = this.httpBody;
    this.absoluteStreamPosition = absoluteStreamPosition;
    this.position = position;
    this.length = length;
    this.key = key;
    this.flags = flags;
  }

  /**
   * Returns whether the given flag is set.
   *
   * @param flag Flag to be checked if it is set.
   */
  public boolean isFlagSet(@Flags int flag) {
    return (this.flags & flag) == flag;
  }

  @Override
  public String toString() {
    return "DataSpec["
        + getHttpMethodString()
        + " "
        + uri
        + ", "
        + Arrays.toString(httpBody)
        + ", "
        + absoluteStreamPosition
        + ", "
        + position
        + ", "
        + length
        + ", "
        + key
        + ", "
        + flags
        + "]";
  }

  /**
   * Returns an uppercase HTTP method name (e.g., "GET", "POST", "HEAD") corresponding to the {@link
   * #httpMethod}.
   */
  public final String getHttpMethodString() {
    return getStringForHttpMethod(httpMethod);
  }

  /**
   * Returns an uppercase HTTP method name (e.g., "GET", "POST", "HEAD") corresponding to the {@code
   * httpMethod}.
   */
  public static String getStringForHttpMethod(@HttpMethod int httpMethod) {
    switch (httpMethod) {
      case HTTP_METHOD_GET:
        return "GET";
      case HTTP_METHOD_POST:
        return "POST";
      case HTTP_METHOD_HEAD:
        return "HEAD";
      default:
        throw new AssertionError(httpMethod);
    }
  }

  /**
   * Returns a {@link DataSpec} that represents a subrange of the data defined by this DataSpec. The
   * subrange includes data from the offset up to the end of this DataSpec.
   *
   * @param offset The offset of the subrange.
   * @return A {@link DataSpec} that represents a subrange of the data defined by this DataSpec.
   */
  public DataSpec subrange(long offset) {
    return subrange(offset, length == C.LENGTH_UNSET ? C.LENGTH_UNSET : length - offset);
  }

  /**
   * Returns a {@link DataSpec} that represents a subrange of the data defined by this DataSpec.
   *
   * @param offset The offset of the subrange.
   * @param length The length of the subrange.
   * @return A {@link DataSpec} that represents a subrange of the data defined by this DataSpec.
   */
  public DataSpec subrange(long offset, long length) {
    if (offset == 0 && this.length == length) {
      return this;
    } else {
      return new DataSpec(
          uri,
          httpMethod,
          httpBody,
          absoluteStreamPosition + offset,
          position + offset,
          length,
          key,
          flags);
    }
  }

  /**
   * Returns a copy of this {@link DataSpec} with the specified Uri.
   *
   * @param uri The new source {@link Uri}.
   * @return The copied {@link DataSpec} with the specified Uri.
   */
  public DataSpec withUri(Uri uri) {
    return new DataSpec(
        uri, httpMethod, httpBody, absoluteStreamPosition, position, length, key, flags);
  }
}