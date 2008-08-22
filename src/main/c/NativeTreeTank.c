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

#define TT_WRITE_INT(PTR, VAL) { \
            (PTR)[0] = (u_int8_t) (VAL >> 24); \
            (PTR)[1] = (u_int8_t) (VAL >> 16); \
            (PTR)[2] = (u_int8_t) (VAL >>  8); \
            (PTR)[3] = (u_int8_t)  VAL; }
      
#define TT_READ_INT(PTR) \
           ((((PTR)[0] & 0xFF) << 24) \
          | (((PTR)[1] & 0xFF) << 16) \
          | (((PTR)[2] & 0xFF) <<  8) \
          |  ((PTR)[3] & 0xFF))

/*
 * gcc -shared -Wall -I/treetank/jre/include/ -o /treetank/service/libTreeTank.so NativeTreeTank.c
 */

JNIEXPORT jint JNICALL Java_org_treetank_pagelayer_NativeTreeTank_write(JNIEnv *env, jobject o, jint core, jbyteArray jreference, jbyteArray jbuffer)
{

  int   error     = 0;
  u_int8_t   ref[24];
  u_int8_t   buf[32768];
  u_int8_t *reference = (u_int8_t*)((*env)->GetByteArrayElements(env, jreference, 0));
  u_int8_t *buffer    = (u_int8_t*)((*env)->GetByteArrayElements(env, jbuffer, 0));
  
  bzero(ref, 24);
  TT_WRITE_INT(ref + 8, TT_READ_INT(reference + 8));
  memcpy(&buf, buffer, TT_READ_INT(ref + 8));
  
  error = syscall(306, core, 1, &ref, &buf);
  
  memcpy(reference, &ref, 24);
  memcpy(buffer, &buf, TT_READ_INT(ref + 8));
    
  (*env)->ReleaseByteArrayElements (env, jreference, reference, 0);
  (*env)->ReleaseByteArrayElements (env, jbuffer, buffer, 0);

  return error;
}

JNIEXPORT jbyteArray JNICALL Java_org_treetank_pagelayer_NativeTreeTank_read(JNIEnv *env, jobject o, jint core, jbyteArray jin, jint len)
{

  u_int8_t   reference[24];
  u_int8_t   buffer[32768];
  u_int8_t  *in     = (u_int8_t*)((*env)->GetByteArrayElements(env, jin, 0));
  
  bzero(reference, 24);
  TT_WRITE_INT(reference + 8, len);
  memcpy(&buffer, in, TT_READ_INT(reference + 8));

  syscall(306, core, 0, &reference, &buffer);
  
  jbyteArray jout = (*env)->NewByteArray(env, TT_READ_INT(reference + 8));
  u_int8_t  *out  = (u_int8_t *)((*env)->GetByteArrayElements(env, jout, 0));

  memcpy(out, &buffer, TT_READ_INT(reference + 8));

  return jout;
}
