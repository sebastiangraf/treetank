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
 * $Id:sys_treetank.h 4360 2008-08-24 11:17:12Z kramis $
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

/* --- Global constants. ---------------------------------------------------- */

#define TT_SYSCALL_SUCCESS 0x0
#define TT_SYSCALL_FAILURE 0x1

#define TT_NULL 0x0

#define TT_CORE_COUNT 15
#define TT_BUFFER_LENGTH 32767

#define TT_CREATE_TANK    0x1
#define TT_UPDATE_TANK    0x2
#define TT_ERASE_TANK     0x3
#define TT_OPEN_TANK      0x4
#define TT_CLOSE_TANK     0x5
#define TT_WRITE_REVISION 0x6
#define TT_READ_REVISION  0x7
#define TT_WRITE_FRAGMENT 0x8
#define TT_READ_FRAGMENT  0x9

#define TT_REFERENCE_LENGTH 24

#define TT_COMPRESSION_ALGORITHM CRYPTO_LZS_COMP
#define TT_ENCRYPTION_ALGORITHM CRYPTO_AES_CBC
#define TT_AUTHENTICATION_ALGORITHM CRYPTO_SHA1_HMAC

#define TT_KEY_LENGTH 256
#define TT_BLOCK_LENGTH 16
#define TT_ROUNDS 14

#define TT_START_LENGTH 8
#define TT_LENGTH_LENGTH 4
#define TT_HMAC_LENGTH 12

#define TT_START_OFFSET 0
#define TT_LENGTH_OFFSET 8
#define TT_HMAC_OFFSET 12
