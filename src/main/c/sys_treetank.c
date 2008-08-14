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

/** === TreeTank SysCall ====================================================
  * Used to TT_COMPRESS, encrypt, authenticate, and write buffer to device.
  * 
  * The following steps allow to rebuild the OpenBSD kernel:
  * 1) Add following line to end of /usr/src/sys/kern/syscalls.master:
  * 306	STD		{ int sys_treetank(u_int8_t core, \
  *                u_int8_t operation, \
  *                u_int8_t *bufferPointer, \
  *                u_int32_t *lengthPointer); }
  * 2) Rebuild syscall entries:
  * # cd /usr/src/sys/kern
  * # make init_sysent.c at /usr/src/sys/kern
  * 
  * 3) Add the following line in alphabetic order to /usr/src/sys/conf/files:
  * file kern/sys_treetank.c
  *
  * 4) Rebuild kernel:
  * # cd /usr/src/sys/arch/i386/conf
  * # config GENERIC
  * # cd ../compile/GENERIC
  * # make clean && make depend && make
  * # make install
  * # reboot
  * ======================================================================= */
  
/* --- Constants. --------------------------------------------------------- */

#define TT_COMPRESSION_ALGORITHM CRYPTO_LZS_COMP

#define TT_COMPRESS 1
#define TT_DECOMPRESS 0

#define TT_OK 0
#define TT_ERROR 1

#define TT_NULL_SESSION 0

/* --- Function prototypes. ----------------------------------------------- */

int tt_callback(void *);

/* --- Global variables. -------------------------------------------------- */

static u_int64_t tt_sessionId[] = {
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION };

/* --- Code. -------------------------------------------------------------- */

/*
 * Entry point for sys_treetank system call.
 */
int
sys_treetank(struct proc *p, void *v, register_t *retval)
{

  /* --- Local variables. ------------------------------------------------- */
  
  struct sys_treetank_args *argumentPointer  = v;
  int                       error            = TT_OK;
  struct cryptop           *operationPointer = NULL;
  struct mbuf              *packetPointer    = NULL;
  
  /* --- Initialise session (if required). -------------------------------- */
    
  if (tt_sessionId[SCARG(argumentPointer, core)] == TT_NULL_SESSION)
  {
    struct cryptoini session;  

    bzero(&session, sizeof(session));
    session.cri_alg = TT_COMPRESSION_ALGORITHM;

    if (crypto_newsession(
	  &(tt_sessionId[SCARG(argumentPointer, core)]),
      &session,
      0) != TT_OK)
    {
	  error = TT_ERROR;
      tt_sessionId[SCARG(argumentPointer, core)] = TT_NULL_SESSION;
      printf("ERROR(sys_treetank.c): Could not allocate cryptoini.\n");
	  goto finish;
    }
    
  }
  
  /* --- Initialise buffer. ----------------------------------------------- */
  
  packetPointer = m_gethdr(M_DONTWAIT, MT_DATA);
  if (packetPointer == NULL)
  {
    error = TT_ERROR;
    printf("ERROR(sys_treetank.c): Could not allocate mbuf.\n");
    goto finish;
  }
  
  packetPointer->m_pkthdr.len = 0;
  packetPointer->m_len        = 0;
    
  m_copyback(
    packetPointer,
	0,
	*SCARG(argumentPointer, lengthPointer),
	SCARG(argumentPointer, bufferPointer));

  /* --- Initialise crypto operation. ------------------------------------- */
  
  operationPointer = crypto_getreq(1);
  if (operationPointer == NULL)
  {
    error = TT_ERROR;
    printf("ERROR(sys_treetank.c): Could not allocate crypto.\n");
    goto finish;
  }

  operationPointer->crp_sid               = tt_sessionId[SCARG(argumentPointer, core)];
  operationPointer->crp_ilen              = *SCARG(argumentPointer, lengthPointer);
  operationPointer->crp_flags             = CRYPTO_F_IMBUF;
  operationPointer->crp_buf               = (caddr_t) packetPointer;
  operationPointer->crp_desc->crd_alg     = TT_COMPRESSION_ALGORITHM;
  operationPointer->crp_desc->crd_skip    = 0;
  operationPointer->crp_desc->crd_len     = *SCARG(argumentPointer, lengthPointer);
  operationPointer->crp_desc->crd_inject  = 0;
  if (SCARG(argumentPointer, operation) == TT_COMPRESS)
    operationPointer->crp_desc->crd_flags = CRD_F_COMP;
  else
    operationPointer->crp_desc->crd_flags = 0;
  operationPointer->crp_opaque            = &SCARG(argumentPointer, core);
  operationPointer->crp_callback          = (int (*) (struct cryptop *)) tt_callback;
   
  /* --- Synchronously dispatch crypto operation. ------------------------- */
  
  crypto_dispatch(operationPointer);
  
  while (!(operationPointer->crp_flags & CRYPTO_F_DONE))
  {
    error = tsleep(operationPointer, PSOCK, "sys_treetank", 0);
  }
  
  if (error != TT_OK)
  {
    printf("ERROR(sys_treetank.c): Failed during tsleep.\n");
	goto finish;
  }
  
  if (operationPointer->crp_etype != TT_OK)
  {
    error = operationPointer->crp_etype;
	printf("ERROR(sys_treetank.c): Failed during crypto_dispatch.\n");
	goto finish;
  }
  
  /* --- Collect result from buffer. -------------------------------------- */
  
  *SCARG(argumentPointer, lengthPointer) = operationPointer->crp_olen;
  
  m_copydata(
    operationPointer->crp_buf,
	0,
	*SCARG(argumentPointer, lengthPointer),
	SCARG(argumentPointer, bufferPointer));
  
  /* --- Cleanup for all conditions. -------------------------------------- */
  
finish:
  
  m_freem(operationPointer->crp_buf);
 
  if (operationPointer != NULL)
  {
    crypto_freereq(operationPointer);
  }
  
  return (error);
}

/*
 * Callback to retrieve result of crypto operation.
 */
int
tt_callback(void *op)
{
  
  /* --- Check for migrated session identifier. --------------------------- */
  
  struct cryptop *operationPointer = (struct cryptop *) op;
  
  if (operationPointer->crp_etype == EAGAIN)
  {
    tt_sessionId[* (int *) (operationPointer->crp_opaque)] 
      = operationPointer->crp_sid;
	operationPointer->crp_flags = CRYPTO_F_IMBUF;
	return crypto_dispatch(operationPointer);
  }
  
  /* --- Continue in main syscall to synchronously return to userland. ---- */
  
  wakeup(op);
  return (TT_OK);
  
}
