/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * Permission to use, copy, modify, and/or distribute this software for non-
 * commercial use with or without fee is hereby granted, provided that the 
 * above copyright notice, the patent notice, and this permission notice
 * appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

#include "NativeComp.h"
#include <jni.h>

#include <sys/syscall.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <err.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>

JNIEXPORT jbyteArray JNICALL Java_org_treetank_pagelayer_NativeCompression_compress(JNIEnv *env, jobject o, jbyteArray jin)
{

  char           buffer[64000];

  unsigned char *in     = (unsigned char*)((*env)->GetByteArrayElements(env, jin, 0));
  int            length = (*env)->GetArrayLength(env, jin);
  
  //memcpy(void * destination, void * source, size_t bytes);
  memcpy(&buffer, in, length);

  syscall(306, 1, &buffer, &length);
  
  jbyteArray jout = (*env)->NewByteArray(env, length);
  unsigned char *out = (unsigned char*)((*env)->GetByteArrayElements(env, jout, 0));
  memcpy(out, &buffer, length);

  return jout;
}

JNIEXPORT jbyteArray JNICALL Java_org_treetank_pagelayer_NativeCompression_decompress(JNIEnv *env, jobject o, jbyteArray jin)
{

  char           buffer[64000];

  unsigned char *in     = (unsigned char*)((*env)->GetByteArrayElements(env, jin, 0));
  int            length = (*env)->GetArrayLength(env, jin);
  
  //memcpy(void * destination, void * source, size_t bytes);
  memcpy(&buffer, in, length);

  syscall(306, 0, &buffer, &length);
  
  jbyteArray jout = (*env)->NewByteArray(env, length);
  unsigned char *out = (unsigned char*)((*env)->GetByteArrayElements(env, jout, 0));
  memcpy(out, &buffer, length);

  return jout;
}