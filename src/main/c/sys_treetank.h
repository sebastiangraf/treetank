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
 
/* --- Includes. ------------------------------------------------------------ */

#include <sys/types.h>
#include <sys/param.h>
#include <sys/systm.h>
#include <sys/kernel.h>
#include <sys/proc.h>
#include <sys/mount.h>
#include <sys/syscallargs.h>
#include <sys/ioctl.h>
#include <sys/mbuf.h>
#include <crypto/cryptodev.h>

/* --- Constants. ----------------------------------------------------------- */

#define TT_COMPRESSION_ALGORITHM CRYPTO_LZS_COMP
#define TT_ENCRYPTION_ALGORITHM CRYPTO_AES_CBC
#define TT_AUTHENTICATION_ALGORITHM CRYPTO_SHA1_HMAC

#define TT_KEY_LENGTH 256
#define TT_BLOCK_LENGTH 16
#define TT_ROUNDS 14

#define TT_WRITE 1
#define TT_READ 0

#define TT_OK 0
#define TT_ERROR 1

#define TT_NULL_SESSION 0
