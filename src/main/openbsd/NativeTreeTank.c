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

/** === ICrypto ============================================================= */

JNIEXPORT jshort JNICALL Java_org_treetank_openbsd_CryptoNativeImpl_syscall(
  JNIEnv *env,
  jobject obj,
  jbyte tank,
  jbyte operation,
  jshort length,
  jlong address)
{
  jint   error     = 0x0;
  jshort tmp       = length;
  
  error = syscall(306, tank, operation, &tmp, address);
  
  if (error != 0x0) {
    return 0x0;
  } else {
    return tmp;
  }
}

/** === IByteBuffer ========================================================= */

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_allocate(
  JNIEnv *env,
  jobject obj)
{
  // Setup.
  jclass   class         = (*env)->GetObjectClass(env, obj);
  jfieldID addressField  = (*env)->GetFieldID(env, class, "mAddress", "J");
  jfieldID capacityField = (*env)->GetFieldID(env, class, "mCapacity", "I");
  jlong    address       = (*env)->GetIntField(env, obj, addressField);
  jint     capacity      = (*env)->GetIntField(env, obj, capacityField);
  
  // Work.
  address = malloc(capacity);
  
  // Teardown.
  (*env)->SetLongField(env, obj, addressField, address);
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_free(
  JNIEnv * env,
  jobject obj)
{
  // Setup.
  jclass   class         = (*env)->GetObjectClass(env, obj);
  jfieldID addressField  = (*env)->GetFieldID(env, class, "mAddress", "J");
  jlong    address       = (*env)->GetIntField(env, obj, addressField);
  
  // Work.
  free(address);
  
  // Teardown.
  (*env)->SetLongField(env, obj, addressField, 0);
}

JNIEXPORT jlong JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_get__(
  JNIEnv * env,
  jobject obj)
{
  // Setup.
  jclass    class         = (*env)->GetObjectClass(env, obj);
  jfieldID  addressField  = (*env)->GetFieldID(env, class, "mAddress", "J");
  jfieldID  positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jlong     address       = (*env)->GetIntField(env, obj, addressField);
  jint      position      = (*env)->GetIntField(env, obj, positionField);
  jbyte    *addressPtr    = (jbyte *) address;
  jbyte     singleByte    = *(addressPtr + (position++));
  jlong     value         = singleByte & 0x7F;
  jint      shift         = 7;
  
  // Work.
  while ((singleByte & 0x80) > 0) {
    singleByte = *(addressPtr + (position++));
    value |= (((jlong) (singleByte & 0x7F)) << shift);
    shift += 7;
  }
  
  // Teardown.
  (*env)->SetIntField(env, obj, positionField, position);
  return value;
}

JNIEXPORT jbyteArray JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_get__I(
  JNIEnv * env,
  jobject obj,
  jint length)
{
  // Setup.
  jclass     class         = (*env)->GetObjectClass(env, obj);
  jfieldID   addressField  = (*env)->GetFieldID(env, class, "mAddress", "J");
  jfieldID   positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jlong      address       = (*env)->GetIntField(env, obj, addressField);
  jint       position      = (*env)->GetIntField(env, obj, positionField);
  jbyte     *addressPtr    = (jbyte *) address;
  jbyteArray array         = (*env)->NewByteArray(env, length);
  
  // Work.
  (*env)->SetByteArrayRegion(env, array, 0, length, addressPtr);
  position += length;
  
  // Teardown.
  (*env)->SetIntField(env, obj, positionField, position);
  return array;
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_put__J(
  JNIEnv * env,
  jobject obj,
  jlong value)
{
  // Setup.
  jclass    class         = (*env)->GetObjectClass(env, obj);
  jfieldID  addressField  = (*env)->GetFieldID(env, class, "mAddress", "J");
  jfieldID  positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jlong     address       = (*env)->GetIntField(env, obj, addressField);
  jint      position      = (*env)->GetIntField(env, obj, positionField);
  jbyte    *addressPtr    = (jbyte *) address;
  jbyte     singleByte    = (jbyte) (value & 0x7F);
  
  // Work.
  value >>= 7;
  while (value > 0) {
    *(addressPtr + (position++)) = singleByte | 0x80;
    singleByte = (jbyte) (value & 0x7F);
    value >>= 7;
  }
  *(addressPtr + (position++)) = singleByte;
  
  // Teardown.
  (*env)->SetIntField(env, obj, positionField, position);
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_put___3B(
  JNIEnv * env,
  jobject obj,
  jbyteArray value)
{
  // Setup.
  jclass     class         = (*env)->GetObjectClass(env, obj);
  jfieldID   addressField  = (*env)->GetFieldID(env, class, "mAddress", "J");
  jfieldID   positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jlong      address       = (*env)->GetIntField(env, obj, addressField);
  jint       position      = (*env)->GetIntField(env, obj, positionField);
  jbyte     *addressPtr    = (jbyte *) address;
  jbyte     *arrayPtr      = (*env)->GetByteArrayElements(env, value, NULL);
  jint       length        = (*env)->GetArrayLength(env, value);
  
  // Work.
  bcopy(arrayPtr, addressPtr, length);
  position += length;
  
  // Teardown.
  (*env)->SetIntField(env, obj, positionField, position);
  (*env)->ReleaseByteArrayElements(env, value, arrayPtr, 0);
}
