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
 * $Id:NativeTreeTank.c 4340 2008-08-21 06:25:17Z kramis $
 */

#include "NativeTreeTank.h"
#include <jni.h>

#include <sys/syscall.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>
#include <err.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>

/*
 * gcc -shared -Wall -I/treetank/jre/include/ -o /treetank/service/libTreeTank.so NativeTreeTank.c
 */

JNIEXPORT jbyteArray JNICALL Java_org_treetank_pagelayer_NativeTreeTank_write(JNIEnv *env, jobject o, jint core, jbyteArray jin, jint len)
{

  u_int8_t   buffer[64000];
  u_int8_t  *in     = (u_int8_t*)((*env)->GetByteArrayElements(env, jin, 0));
  u_int32_t  length = len;
  
  memcpy(&buffer, in, length);
  
  syscall(306, core, 1, &buffer, &length);
    
  jbyteArray jout = (*env)->NewByteArray(env, length);
  u_int8_t  *out  = (u_int8_t *)((*env)->GetByteArrayElements(env, jout, 0));
  
  memcpy(out, &buffer, length);

  return jout;
}

JNIEXPORT jbyteArray JNICALL Java_org_treetank_pagelayer_NativeTreeTank_read(JNIEnv *env, jobject o, jint core, jbyteArray jin, jint len)
{

  u_int8_t   buffer[64000];
  u_int8_t  *in     = (u_int8_t*)((*env)->GetByteArrayElements(env, jin, 0));
  u_int32_t  length = len;
    
  memcpy(&buffer, in, length);

  syscall(306, core, 0, &buffer, &length);
  
  jbyteArray jout = (*env)->NewByteArray(env, length);
  u_int8_t  *out  = (u_int8_t *)((*env)->GetByteArrayElements(env, jout, 0));

  memcpy(out, &buffer, length);

  return jout;
}
