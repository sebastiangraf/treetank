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
 * $Id:sys_treetank.c 4360 2008-08-24 11:17:12Z kramis $
 */

#include "sys_treetank.h"

/** === TreeTank SysCall =======================================================
  * Used to compress, encrypt, authenticate, and write buffer to file.
  * 
  * The following steps allow to rebuild the OpenBSD kernel:
  * 1) Add following line to end of /usr/src/sys/kern/syscalls.master:
  * 306	STD		{ int sys_treetank( \
  *                u_int8_t   tank, \
  *                u_int8_t   operation, \
  *                u_int16_t *lengthPtr, \
  *                u_int8_t  *bufferPtr); }
  *
  * 2) Rebuild syscall entries:
  * # cd /usr/src/sys/kern
  * # make init_sysent.c at /usr/src/sys/kern
  * 
  * 3) Add the following line in alphabetic order to /usr/src/sys/conf/files:
  * file sys_treetank_compression.c
  * file sys_treetank_encryption.c
  * file sys_treetank_authentication.c
  * file sys_treetank_write_fragment.c
  * file sys_treetank_read_fragment.c
  * file sys_treetank.c
  *
  * 4) BUGFIX: Add the following after line 2325 
       in /usr/src/sys/dev/pci/hifn7751.c:
  * if ((crd->crd_flags & CRD_F_IV_PRESENT)
  *    != 0)
  *    continue;
  *
  * 5) BUGFIX: Replace the following in line 2365
  *    in /usr/src/sys/dev/pci/hifn7751.c:
  * if (crp->crp_flags & CRYPTO_F_IMBUF && (crp->crp_mac == NULL))
  *    m_copyback(...);
  * else if (crp->crp_mac != NULL)
  *    bcopy(...);
  *
  * 6) Comment out the following line from /usr/src/sys/arch/i386/conf/GENERIC:
  * glxsb* at pci?
  *
  * 7) create GENERIC configuration:
  * # cd /usr/src/sys/arch/i386/conf
  * # config GENERIC
  *
  * 8) Rebuild kernel:
  * # cd /usr/src/sys/arch/i386/compile/GENERIC
  * # make clean && make depend && make
  * # make install
  * # reboot
  * ========================================================================= */
  
/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_callback(struct cryptop *op);
int sys_treetank_write_fragment(u_int64_t, u_int64_t, u_int64_t, u_int8_t, u_int16_t *, u_int8_t *);
int sys_treetank_read_fragment(u_int64_t, u_int64_t, u_int64_t, u_int8_t, u_int16_t *, u_int8_t *);

/* --- Global variables. ---------------------------------------------------- */

static u_int8_t tt_buffer[TT_CORE_COUNT][TT_BUFFER_LENGTH];
static u_int64_t tt_comp_sessionId[] = {
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL };
static u_int64_t tt_enc_sessionId[] = {
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL };
static u_int64_t tt_auth_sessionId[] = {
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL, TT_NULL,
  TT_NULL, TT_NULL, TT_NULL };
  
static u_int8_t tt_key[32] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

/* --- Code. ---------------------------------------------------------------- */

/*
 * Entry point for sys_treetank system call.
 */
int
sys_treetank(struct proc *p, void *v, register_t *retval)
{

  /* --- Local variables. --------------------------------------------------- */
  
  int                       syscall          = TT_SYSCALL_SUCCESS;
  struct sys_treetank_args *argumentPointer  = v;
  u_int8_t                  tank             = SCARG(argumentPointer, tank);
  u_int8_t                  operation        = SCARG(argumentPointer, operation);
  u_int16_t                *lengthPtr        = SCARG(argumentPointer, lengthPtr);
  u_int8_t                 *bufferPtr        = SCARG(argumentPointer, bufferPtr);
  u_int8_t                  command          = operation & 0xF;
  u_int8_t                  core             = operation >> 0x4;
  struct cryptoini          session;  
  
  /* --- Check arguments. --------------------------------------------------- */
  
  if (tank == TT_NULL) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank.c): Invalid tank argument.\n");
    goto finish;
  }
  
  if ((command == TT_NULL) || (core == TT_NULL)) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank.c): Invalid operation argument.\n");
    goto finish;
  }
  
  if (
      (lengthPtr == NULL)
      || (*lengthPtr == TT_NULL)
      || (*lengthPtr > TT_BUFFER_LENGTH)) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank.c): Invalid lengthPtr argument.\n");
    goto finish;
  }
  
  if (bufferPtr == NULL) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank.c): Invalid bufferPtr argument.\n");
    goto finish;
  }
  
  /* --- Initialise sessions. ----------------------------------------------- */
  
  if (tt_comp_sessionId[core] == TT_NULL) {
    bzero(&session, sizeof(session));
    session.cri_alg = TT_COMPRESSION_ALGORITHM;

    if (crypto_newsession(
          &(tt_comp_sessionId[core]),
          &session,
          0x0) != TT_SYSCALL_SUCCESS) {
      syscall = TT_SYSCALL_FAILURE;
      tt_comp_sessionId[core] = TT_NULL;
      printf("ERROR(sys_treetank.c): Could not allocate cryptoini for compression.\n");
      goto finish;
    }
  }
  
  if (tt_enc_sessionId[core] == TT_NULL)
  {
    bzero(&session, sizeof(session));
    session.cri_alg  = TT_ENCRYPTION_ALGORITHM;
    session.cri_klen = TT_KEY_LENGTH;
    session.cri_rnd  = TT_ROUNDS;
    session.cri_key  = (caddr_t) &tt_key;
    
    if (crypto_newsession(
          &(tt_enc_sessionId[core]),
          &session,
          0x0) != TT_SYSCALL_SUCCESS) {
      syscall = TT_SYSCALL_FAILURE;
      tt_enc_sessionId[core] = TT_NULL;
      printf("ERROR(sys_treetank.c): Could not allocate cryptoini for encryption.\n");
      goto finish;
    }
    
  }
  
  if (tt_auth_sessionId[core] == TT_NULL) {
    bzero(&session, sizeof(session));
    session.cri_alg  = TT_AUTHENTICATION_ALGORITHM;
    session.cri_klen = TT_KEY_LENGTH;
    session.cri_key  = (caddr_t) &tt_key;

    if (crypto_newsession(
          &(tt_auth_sessionId[core]),
          &session,
          0x0) != TT_SYSCALL_SUCCESS) {
      syscall = TT_SYSCALL_FAILURE;
      tt_auth_sessionId[core] = TT_NULL;
      printf("ERROR(sys_treetank.c): Could not allocate cryptoini for authentication.\n");
      goto finish;
    }
  }
  
  /* --- Copy buffer from user to kernel space. ----------------------------- */
  
  copyin(
    bufferPtr,
    tt_buffer[core],
    *lengthPtr);
    
  /* --- Select command. ---------------------------------------------------- */
  
  switch (command) {
    case TT_WRITE_FRAGMENT:
        syscall = sys_treetank_write_fragment(
            tt_comp_sessionId[core],
            tt_enc_sessionId[core],
            tt_auth_sessionId[core],
            core,
            lengthPtr,
            tt_buffer[core]);
        break;
    case TT_READ_FRAGMENT:
        syscall = sys_treetank_read_fragment(
            tt_comp_sessionId[core],
            tt_enc_sessionId[core],
            tt_auth_sessionId[core],
            core,
            lengthPtr,
            tt_buffer[core]);
        break;
    default:
        syscall = TT_SYSCALL_FAILURE;
  }
  
  if (syscall == TT_SYSCALL_FAILURE) {
    printf("ERROR(sys_treetank.c): Error during command execution.\n");
    goto finish;
  }
  
  /* --- Copy buffer from kernel to user space. ----------------------------- */
  
  if (
      (lengthPtr == NULL)
      || (*lengthPtr == TT_NULL)
      || (*lengthPtr > TT_BUFFER_LENGTH)) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank.c): Invalid lengthPtr result.\n");
    goto finish;
  }
  
  copyout(
    tt_buffer[core],
    bufferPtr,
    *lengthPtr);
  
  /* --- Cleanup. ----------------------------------------------------------- */
  
finish:
  
  return (syscall);
  
}

/*
 * Callback to retrieve result of crypto operation.
 */
int
sys_treetank_callback(struct cryptop *op)
{
  
  if (op->crp_etype == EAGAIN) {
    op->crp_flags = CRYPTO_F_IMBUF;
    return crypto_dispatch(op);
  }
  
  wakeup(op);
  return (TT_SYSCALL_SUCCESS);
  
}

// Example for UIO:
// tt_uioBufferVector.iov_base = &tt_uioBufferArray;
// tt_uioBufferVector.iov_len  = sizeof(tt_uioBufferArray);
// tt_uioBuffer.uio_iov        = &tt_uioBufferVector;
// tt_uioBuffer.uio_iovcnt     = 1;
// tt_uioBuffer.uio_offset     = 0;
// tt_uioBuffer.uio_resid      = sizeof(tt_uioBufferArray);
// tt_uioBuffer.uio_segflg     = UIO_SYSSPACE;
// tt_uioBuffer.uio_procp      = p;
