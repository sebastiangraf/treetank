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

#include "sys_treetank.h"

/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_read_fragment(u_int8_t, u_int16_t *, u_int8_t *);

int sys_treetank_compression(u_int8_t, u_int8_t, u_int16_t *, u_int8_t *);
int sys_treetank_encryption(u_int8_t, u_int8_t, u_int16_t *, u_int8_t *);
int sys_treetank_authentication(u_int8_t, u_int8_t, u_int16_t *, u_int8_t *);

/* --- Global variables. ---------------------------------------------------- */

/* --- Functions. ----------------------------------------------------------- */
int
sys_treetank_read_fragment(
  u_int8_t   core,
  u_int16_t *lengthPtr,
  u_int8_t  *bufferPtr)
{

  /* --- Local variables. --------------------------------------------------- */
  
  int syscall = TT_SYSCALL_SUCCESS;

  /* --- Call operations. --------------------------------------------------- */

  if (*lengthPtr < TT_REFERENCE_LENGTH) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_read_fragment.c): Invalid lengthPtr argument.\n");
    goto finish;
  }
  
  if (sys_treetank_authentication(
      core,
      TT_READ_FRAGMENT,
      lengthPtr,
      bufferPtr) != TT_SYSCALL_SUCCESS) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_read_fragment.c): Could not perform authentication.\n");
    goto finish;
  }
  
  if (sys_treetank_encryption(
      core,
      TT_READ_FRAGMENT,
      lengthPtr,
      bufferPtr) != TT_SYSCALL_SUCCESS) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_read_frament.c): Could not perform encryption.\n");
    goto finish;
  }
  
  if (sys_treetank_compression(
      core,
      TT_READ_FRAGMENT,
      lengthPtr,
      bufferPtr) != TT_SYSCALL_SUCCESS) {
    syscall = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_read_fragment.c): Could not perform compression.\n");
    goto finish;
  }
  
  /* --- Cleanup. ----------------------------------------------------------- */
  
finish:
  
  return (syscall);
  
}
