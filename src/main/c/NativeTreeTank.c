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
 * $Id$
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

JNIEXPORT jint JNICALL Java_org_treetank_pagelayer_NativeTreeTank_write(JNIEnv *env, jobject o, jint core, jobject jreference, jobject jbuffer)
{
  int       error            = 0;
  u_int8_t *referencePointer = (*env)->GetDirectBufferAddress(env, jreference);
  u_int8_t *bufferPointer    = (*env)->GetDirectBufferAddress(env, jbuffer);
  
  error = syscall(306, core, 1, referencePointer, bufferPointer);

  return error;
}

JNIEXPORT jint JNICALL Java_org_treetank_pagelayer_NativeTreeTank_read(JNIEnv *env, jobject o, jint core, jobject jreference, jobject jbuffer)
{
  int       error            = 0;
  u_int8_t *referencePointer = (*env)->GetDirectBufferAddress(env, jreference);
  u_int8_t *bufferPointer    = (*env)->GetDirectBufferAddress(env, jbuffer);
  
  error = syscall(306, core, 0, referencePointer, bufferPointer);

  return error;
}
