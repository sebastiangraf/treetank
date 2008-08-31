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
  jint address)
{
  jint   error     = 0x0;
  jshort tmp       = length;
  
  error = syscall(306, tank, operation, &tmp, (jint *) address);
  
  if (error != 0x0) {
    return 0x0;
  }
  
  return tmp;
}

/** === IByteBuffer ========================================================= */

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_allocate(
  JNIEnv *env,
  jobject obj)
{
  // Setup.
  jclass   class         = (*env)->GetObjectClass(env, obj);
  jfieldID addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID capacityField = (*env)->GetFieldID(env, class, "mCapacity", "I");
  jint     address       = (*env)->GetIntField(env, obj, addressField);
  jint     capacity      = (*env)->GetIntField(env, obj, capacityField);
  
  // Work.
  address = (jint) malloc(capacity);
  if (address == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Failed to allocate buffer.");
    goto finish;
  }
  (*env)->SetIntField(env, obj, addressField, address);
  
  // Teardown.
finish:
  (*env)->DeleteLocalRef(env, capacityField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_free(
  JNIEnv * env,
  jobject obj)
{
  // Setup.
  jclass   class         = (*env)->GetObjectClass(env, obj);
  jfieldID addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jint     address       = (*env)->GetIntField(env, obj, addressField);
  
  // Work.
  if (address == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Buffer already freed.");
    goto finish;
  }
  free((jint *) address);
  (*env)->SetIntField(env, obj, addressField, 0);
  
  // Teardown.
finish:
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
}

jlong get(
  jbyte *addressPtr,
  jint  *positionPtr)
{
  jbyte singleByte = *(addressPtr + *positionPtr);
  jlong value      = singleByte & 0x3F;
  jbyte sign       = singleByte & 0x40;
  jint  shift      = 0x6;

  *positionPtr += 1;
  while ((singleByte & 0x80) > 0) {
    singleByte = *(addressPtr + *positionPtr);
    value |= (((jlong) (singleByte & 0x7F)) << shift);
    *positionPtr += 1;
    shift += 7;
  }
  if (sign) {
    value *= -1;
  }
  
  return value;
}

JNIEXPORT jlong JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_get__(
  JNIEnv * env,
  jobject obj)
{
  // Setup.
  jclass    class         = (*env)->GetObjectClass(env, obj);
  jfieldID  addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID  positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint      address       = (*env)->GetIntField(env, obj, addressField);
  jint      position      = (*env)->GetIntField(env, obj, positionField);
  jbyte    *addressPtr    = (jbyte *) address;
  jlong     value         = 0x0;
  
  // Work.
  value = get(addressPtr, &position);
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
  
  return value;
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_get___3J(
  JNIEnv *env,
  jobject obj,
  jlongArray values)
{
  // Argument check.
  if (values == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Bad argument.");
    return;
  }
    
  // Setup.
  jclass    class         = (*env)->GetObjectClass(env, obj);
  jfieldID  addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID  positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint      address       = (*env)->GetIntField(env, obj, addressField);
  jint      position      = (*env)->GetIntField(env, obj, positionField);
  jbyte    *addressPtr    = (jbyte *) address;
  jlong    *arrayPtr      = (*env)->GetLongArrayElements(env, values, NULL);
  jint      length        = (*env)->GetArrayLength(env, values);
  jint      i             = 0x0;
  
  // Work.
  for (i = 0; i < length; i++) {
    *arrayPtr = get(addressPtr, &position);
    arrayPtr += 1;
  }
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->ReleaseLongArrayElements(env, values, arrayPtr, 0);
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
  (*env)->DeleteLocalRef(env, values);
}

JNIEXPORT jbyteArray JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_getArray(
  JNIEnv * env,
  jobject obj,
  jint length)
{
  // Argument check.
  if (length < 0) {
    jclass exception = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Bad argument.");
    return NULL;
  }

  // Setup.
  jbyteArray array         = (*env)->NewByteArray(env, length);
  if (array == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Failed to allocate array.");
    return NULL;
  }
  jclass     class         = (*env)->GetObjectClass(env, obj);
  jfieldID   addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID   positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint       address       = (*env)->GetIntField(env, obj, addressField);
  jint       position      = (*env)->GetIntField(env, obj, positionField);
  jbyte     *addressPtr    = (jbyte *) address + position;
  jbyte     *arrayPtr      = (*env)->GetByteArrayElements(env, array, NULL);
    
  // Work.
  (*env)->SetByteArrayRegion(env, array, 0, length, addressPtr);
  position += length;
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->ReleaseByteArrayElements(env, array, arrayPtr, 0);
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
  
  return array;
}

JNIEXPORT jlongArray JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_getAll(
  JNIEnv *env,
  jobject obj,
  jint count)
{
  // Argument check.
  if (count < 0) {
    jclass exception = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Bad argument.");
    return NULL;
  }

  // Setup.
  jlongArray array         = (*env)->NewLongArray(env, count);
  if (array == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Failed to allocate array.");
    return NULL;
  }
  jclass     class         = (*env)->GetObjectClass(env, obj);
  jfieldID   addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID   positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint       address       = (*env)->GetIntField(env, obj, addressField);
  jint       position      = (*env)->GetIntField(env, obj, positionField);
  jbyte     *addressPtr    = (jbyte *) address;
  jlong     *arrayPtr      = (*env)->GetLongArrayElements(env, array, NULL);
  jint       i             = 0x0;
  
  // Work.
  for (i = 0; i < count; i++) {
    *arrayPtr = get(addressPtr, &position);
    arrayPtr += 1;
  }
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->ReleaseLongArrayElements(env, array, arrayPtr, 0);
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
  
  return array;
}

void put(
  jbyte *addressPtr,
  jint  *positionPtr,
  jlong  value)
{
  jbyte singleByte = 0x0;
  
  if (value < 0) {
    value *= -1;
    singleByte = (jbyte) ((value & 0x3F) | 0x40);
  } else {
    singleByte = (jbyte) (value & 0x3F);
  }
  value >>= 6;
  while (value > 0) {
    *(addressPtr + *positionPtr) = singleByte | 0x80;
    singleByte = (jbyte) (value & 0x7F);
    *positionPtr += 1;
    value >>= 7;
  }
  *(addressPtr + *positionPtr) = singleByte;
  *positionPtr += 1;
  
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_put(
  JNIEnv * env,
  jobject obj,
  jlong value)
{
  // Setup.
  jclass    class         = (*env)->GetObjectClass(env, obj);
  jfieldID  addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID  positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint      address       = (*env)->GetIntField(env, obj, addressField);
  jint      position      = (*env)->GetIntField(env, obj, positionField);
  jbyte    *addressPtr    = (jbyte *) address;
  
  // Work.
  put(addressPtr, &position, value);
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_putArray(
  JNIEnv * env,
  jobject obj,
  jbyteArray value)
{
  // Argument check.
  if (value == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Bad argument.");
    return;
  }

  // Setup.
  jclass     class         = (*env)->GetObjectClass(env, obj);
  jfieldID   addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID   positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint       address       = (*env)->GetIntField(env, obj, addressField);
  jint       position      = (*env)->GetIntField(env, obj, positionField);
  jbyte     *addressPtr    = (jbyte *) address + position;
  jbyte     *arrayPtr      = (*env)->GetByteArrayElements(env, value, NULL);
  jint       length        = (*env)->GetArrayLength(env, value);
  
  // Work.
  bcopy(arrayPtr, addressPtr, length);
  position += length;
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->ReleaseByteArrayElements(env, value, arrayPtr, JNI_ABORT);
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
  (*env)->DeleteLocalRef(env, value);
}

JNIEXPORT void JNICALL Java_org_treetank_openbsd_ByteBufferNativeImpl_putAll(
  JNIEnv *env,
  jobject obj,
  jlongArray values)
{
  // Argument check.
  if (values == NULL) {
    jclass exception = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    if (exception != 0)
      (*env)->ThrowNew(env, exception, "Bad argument.");
    return;
  }

  // Setup.
  jclass    class         = (*env)->GetObjectClass(env, obj);
  jfieldID  addressField  = (*env)->GetFieldID(env, class, "mAddress", "I");
  jfieldID  positionField = (*env)->GetFieldID(env, class, "mPosition", "I");
  jint      address       = (*env)->GetIntField(env, obj, addressField);
  jint      position      = (*env)->GetIntField(env, obj, positionField);
  jbyte    *addressPtr    = (jbyte *) address;
  jlong    *arrayPtr      = (*env)->GetLongArrayElements(env, values, NULL);
  jint      length        = (*env)->GetArrayLength(env, values);
  jint      i             = 0x0;
  
  // Work.
  for (i = 0; i < length; i++) {
    put(addressPtr, &position, *arrayPtr);
    arrayPtr += 1;
  }
  (*env)->SetIntField(env, obj, positionField, position);
  
  // Teardown.
  (*env)->ReleaseLongArrayElements(env, values, arrayPtr, JNI_ABORT);
  (*env)->DeleteLocalRef(env, positionField);
  (*env)->DeleteLocalRef(env, addressField);
  (*env)->DeleteLocalRef(env, class);
  (*env)->DeleteLocalRef(env, values);
}
