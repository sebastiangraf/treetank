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

#include "sys_treetank.h"

/** === TreeTank SysCall =======================================================
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
  * file kern/sys_treetank_compression.c
  * file kern/sys_treetank.c
  *
  * 4) Rebuild kernel:
  * # cd /usr/src/sys/arch/i386/conf
  * # config GENERIC
  * # cd ../compile/GENERIC
  * # make clean && make depend && make
  * # make install
  * # reboot
  * ========================================================================= */
  
/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_compression(u_int8_t, u_int8_t, u_int8_t *, u_int32_t *);

/* --- Global variables. ---------------------------------------------------- */

u_int8_t tt_buffer[8][32000];

/* --- Code. ---------------------------------------------------------------- */

/*
 * Entry point for sys_treetank system call.
 */
int
sys_treetank(struct proc *p, void *v, register_t *retval)
{

  /* --- Local variables. --------------------------------------------------- */
  
  int                       error            = TT_OK;
  struct sys_treetank_args *argumentPointer  = v;
  
  /* --- Copy buffer from user space. --------------------------------------- */
  
  copyin(
    SCARG(argumentPointer, bufferPointer),
    tt_buffer[SCARG(argumentPointer, core)],
    *SCARG(argumentPointer, lengthPointer));

  /* --- Perform compression. ----------------------------------------------- */
  
  if (sys_treetank_compression(
    SCARG(argumentPointer, core),
    SCARG(argumentPointer, operation),
    tt_buffer[SCARG(argumentPointer, core)],
    SCARG(argumentPointer, lengthPointer)) != TT_OK)
  {
    error = TT_ERROR;
    printf("ERROR(sys_treetank.c): Could not perform compression.\n");
    goto finish;
  }
  
  /* --- Copy buffer to user space. ----------------------------------------- */
	
  copyout(
    tt_buffer[SCARG(argumentPointer, core)],
    SCARG(argumentPointer, bufferPointer),
    *SCARG(argumentPointer, lengthPointer));
  
  /* --- Cleanup for all conditions. ---------------------------------------- */
  
finish:
  
  return (error);
  
}
