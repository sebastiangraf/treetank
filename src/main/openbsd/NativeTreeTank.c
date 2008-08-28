/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id:NativeTreeTank.c 4360 2008-08-24 11:17:12Z kramis $
 */

#include "NativeTreeTank.h"
#include <jni.h>

#include <sys/syscall.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <err.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>

JNIEXPORT jshort JNICALL Java_org_treetank_pagelayer_CryptoNativeImpl_syscall(
  JNIEnv *env,
  jobject o,
  jbyte tank,
  jbyte operation,
  jshort length,
  jobject buffer)
{
  jint   error     = 0x0;
  jshort tmp       = length;
  jbyte *bufferPtr = (*env)->GetDirectBufferAddress(env, buffer);
  
  error = syscall(306, tank, operation, &tmp, bufferPtr);
  
  if (error != 0x0) {
    return 0x0;
  } else {
    return tmp;
  }
}

JNIEXPORT jlong JNICALL Java_org_treetank_utils_FastByteBuffer_allocate(
  JNIEnv *env,
  jobject o,
  jint capacity)
{
  return (caddr_t) malloc(capacity);
}

JNIEXPORT void JNICALL Java_org_treetank_utils_FastByteBuffer_free(
  JNIEnv * env,
  jobject o,
  jlong address)
{
  free((caddr_t) address);
}

JNIEXPORT jbyte JNICALL Java_org_treetank_utils_FastByteBuffer_get(
  JNIEnv * env,
  jobject o,
  jlong address,
  jint position)
{
  return (jbyte) *((caddr_t) address + position);
}

JNIEXPORT void JNICALL Java_org_treetank_utils_FastByteBuffer_put(
  JNIEnv * env,
  jobject o,
  jlong address,
  jint position,
  jbyte value)
{
  (jbyte) *((caddr_t) address + position) = value;
}
